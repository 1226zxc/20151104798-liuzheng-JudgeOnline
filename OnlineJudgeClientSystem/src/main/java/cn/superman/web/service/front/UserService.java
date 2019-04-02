package cn.superman.web.service.front;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;

import cn.superman.constant.ConstantParameter;
import cn.superman.util.BeanMapperUtil;
import cn.superman.util.EncryptUtility;
import cn.superman.util.Log4JUtil;
import cn.superman.web.dao.UserDao;
import cn.superman.web.dao.base.BaseDao;
import cn.superman.web.dto.UpdateUserPasswordDTO;
import cn.superman.web.dto.UserLeaderboardDTO;
import cn.superman.web.dto.UserLoginDTO;
import cn.superman.web.dto.UserRegisterDTO;
import cn.superman.web.dto.UserUpdateDTO;
import cn.superman.web.exception.ServiceLogicException;
import cn.superman.web.po.User;
import cn.superman.web.service.EmailService;
import cn.superman.web.service.EmailService.EmailRunnable;
import cn.superman.web.service.page.PageService;

/**
 * 用户模块业务逻辑处理类
 *
 * @author 刘铮
 */
@SuppressWarnings("Duplicates")
@Service
public class UserService extends PageService<User, User> implements InitializingBean {
    @Autowired
    private UserDao userDao;
    @Autowired
    private EmailService emailService;
    private static Map<Integer, String> updateCodeCache = new HashMap<Integer, String>();
    private static Map<String, String> forgetPasswordCodeCache = new HashMap<String, String>();
    private List<UserLeaderboardDTO> haveDoneProblemTop50Cache = null;
    private List<UserLeaderboardDTO> rightProblemTop50Cache = null;
    private List<UserLeaderboardDTO> sloveProblemTotalValueTop50Cache = null;

    /**
     * 用户注册业务处理
     * @param dto 用户数据库注册对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO dto) {
        if (!dto.getConfirmPassword().equals(dto.getPassword())) {
            throw new ServiceLogicException("两次密码不一致");
        }
        // 检查账号唯一性
        checkAccountIsUnique(dto.getAccount());

        User user = BeanMapperUtil.map(dto, User.class);
        // 准备用户存储准备，比如进行密码md5加密
        user.setPassword(EncryptUtility.Md5Encoding(user.getPassword()));
        // 如果用户没有填写用户名，自动生成一个
        if (StringUtils.isBlank(user.getNickname())) {
            user.setNickname("用户" + System.currentTimeMillis());
        }

        // 将用户进行数据库存储
        try {
            userDao.add(user);
        } catch (Exception e) {
            Log4JUtil.logError(e);
            throw new ServiceLogicException("注册失败，若有需要请联系管理员");
        }

        // 注册成功后，准备用户必要的初始化信息
        updateUserInitData(user);
    }

    /**
     * 为新注册的用户分配资源，包括文件资源和数据库资源。
     * 为新注册用户创建一个空文件夹用于保存以后提交的代码源文件，
     * 这个空文件夹的名字就是这个用户的ID号，文件夹位置由{@code ConstantParameter.USER_SUBMIT_CODE_ROOT_PATH}
     * 决定。除此之外，为新注册用户决定以后提交的代码记录保存哪一张记录表中。
     * {@code String tableName = "submit_record" + index;}将决定此用户日后提交代码将保存到此表中。
     * submit_recordX表：每{@code ConstantParameter.SUBMIT_RECORD_TABLE_CREATE_GAP}个人共用此张表，如果超过此
     * 值将会新建一张提交表。例如id为21的新注册用户，20/10 = 2 ，代表此用户代码将提交到submit_record2，同时余数为0
     * 则新建submit_record2。具体创建规则请看代码
     * @param user 新注册用户的信息
     */
    private void updateUserInitData(User user) {
        // 用户创建出来后，用它的ID编号，创建它源码的保存文件，用户保存提交记录的表等内容
        String id = user.getUserId() + "";
        File userJavaFileRoot = new File(ConstantParameter.USER_SUBMIT_CODE_ROOT_PATH + File.separator + id);
        if (userJavaFileRoot.mkdirs()) {
            user.setHaveDoneProblem(0);
            user.setRightProblemCount(0);
            user.setTotalSolveValue(0);
            user.setSourceFileRootPath(userJavaFileRoot.getAbsolutePath());
        }

        // 决定新注册的用户以后提交的代码记录提交到哪个表里
        int index = user.getUserId() / ConstantParameter.SUBMIT_RECORD_TABLE_CREATE_GAP;
        String tableName = "submit_record" + index;
        // 设置新注册的用户提交的代码的表号
        user.setSubmitRecordTableName(tableName);
        // 创建出，用于保存该用户的提交记录表
        // 每XX个人一张表,当求余的结果不是1时，表明这张表已经创建出来了，不需要再创建
        if (user.getUserId() % ConstantParameter.SUBMIT_RECORD_TABLE_CREATE_GAP == 0) {
            userDao.createNewSubmitRecordTable(tableName);
        }

        userDao.update(user);
    }

    /**
     * 用户登陆
     * 流程：
     * - 根据账号和密码取出记录
     * - 判断是否已被封禁
     * @param dto 用户登陆信息
     * @return 登陆的用户信息
     * @throws ServiceLogicException
     */
    public User login(UserLoginDTO dto) throws ServiceLogicException {
        User user = new User();
        user.setAccount(dto.getAccount());
        user.setPassword(EncryptUtility.Md5Encoding(dto.getPassword()));
        List<User> users = userDao.findWithCondition(user);
        if (users.size() < 1) {
            throw new ServiceLogicException("账号或者密码错误");
        }

        user = users.get(0);

        if (user.getIsBan() != null && user.getIsBan()) {
            throw new ServiceLogicException("你的账号已经被封，请联系管理员解封");
        }

        return user;
    }

    /**
     * 忘记密码，发送邮箱验证信息
     * @param account 需要验证的账号
     */
    @SuppressWarnings("Duplicates")
    public void sendForgetPasswordEmail(String account) {
        User condition = new User();
        condition.setAccount(account);
        User user = null;
        try {
            user = userDao.findWithCondition(condition).get(0);
        } catch (Exception e) {
            if (!(e instanceof ArrayIndexOutOfBoundsException)) {
                Log4JUtil.logError(e);
            }
            throw new ServiceLogicException("系统不存在该账号");
        }

        // 生成一个4位随机验证码
        String code = RandomStringUtils.randomNumeric(4);
        forgetPasswordCodeCache.put(account, code);

        String emailSubject = ConstantParameter.SYSTEM_NAME + "重新设置密码验证码";
        String emailContent = "验证码为：" + code;
        String emailReceiver = user.getEmail();
        emailService.sendEmail(new EmailRunnable(emailSubject, emailContent, emailReceiver));
    }

    public void updateUserPassword(UpdateUserPasswordDTO dto) throws ServiceLogicException {

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new ServiceLogicException("新密码和确认密码不对应");
        }

        if (!dto.getEmailVerificationCode().equals(forgetPasswordCodeCache.get(dto.getAccount()))) {
            throw new ServiceLogicException("邮件验证码不正确");
        }

        // 移除验证码，避免二次使用
        forgetPasswordCodeCache.remove(dto.getAccount());

        User user = null;
        User condition = new User();
        condition.setAccount(dto.getAccount());
        try {
            user = userDao.findWithCondition(condition).get(0);
        } catch (Exception e) {
            if (!(e instanceof ArrayIndexOutOfBoundsException)) {
                Log4JUtil.logError(e);
            }
            throw new ServiceLogicException("系统不存在该账号");
        }
        user.setUserId(user.getUserId());
        user.setPassword(EncryptUtility.Md5Encoding(dto.getNewPassword()));
        userDao.update(user);
    }

    /**
     * 检查账号是否唯一
     * @param account 待检查的账户
     */
    public void checkAccountIsUnique(String account) {
        User user = new User();
        user.setAccount(account);
        if (userDao.queryTotalCountWithCondition(user) > 0) {
            throw new ServiceLogicException("该账号已经被注册了，请更换账号");
        }
    }

    public List<UserLeaderboardDTO> getRightProblemTop50() {
        return rightProblemTop50Cache;
    }

    public List<UserLeaderboardDTO> getHaveDoneProblemTop50() {
        return haveDoneProblemTop50Cache;
    }

    public List<UserLeaderboardDTO> getSloveProblemTotalValueTop50() {
        return sloveProblemTotalValueTop50Cache;
    }

    public void updateUserLeaderboardCache() {
        PageHelper.startPage(1, 50, "right_problem_count desc");
        List<User> users = userDao.find();
        rightProblemTop50Cache = BeanMapperUtil.mapList(users, UserLeaderboardDTO.class);

        PageHelper.startPage(1, 50, "have_done_problem desc");
        users = userDao.find();
        haveDoneProblemTop50Cache = BeanMapperUtil.mapList(users, UserLeaderboardDTO.class);

        PageHelper.startPage(1, 50, "total_solve_value desc");
        users = userDao.find();
        sloveProblemTotalValueTop50Cache = BeanMapperUtil.mapList(users, UserLeaderboardDTO.class);
    }

    /**
     * 发送用户信息修改验证邮件
     * @param user 当前操作修改个人信息用户的实例
     */
    public void sendUpdateCodeEmail(User user) {
        // 生成一个4位随机验证码
        String code = RandomStringUtils.randomNumeric(4);
        updateCodeCache.put(user.getUserId(), code);

        // 邮件主题
        String emailSubject = ConstantParameter.SYSTEM_NAME + "用户信息修改验证码";
        // 邮件内容
        String emailContent = "验证码为：" + code;
        // 邮件收件人
        String emailReceiver = user.getEmail();
        // 执行邮件发送
        emailService.sendEmail(new EmailRunnable(emailSubject, emailContent, emailReceiver));
    }

    /**
     * 更新用户信息
     * @param dto 用户的新信息
     */
    public void update(UserUpdateDTO dto) {
        if (!dto.getEmailVerificationCode().equals(updateCodeCache.get(dto.getUserId()))) {
            throw new ServiceLogicException("邮件验证码不正确,或者验证码已经失效");
        }

        updateCodeCache.remove(dto.getUserId());

        User user = BeanMapperUtil.map(dto, User.class);
        if (user.getPassword() != null) {
            user.setPassword(EncryptUtility.Md5Encoding(user.getPassword()));
        }
        userDao.update(user);
    }

    public User find(Integer userId) {
        return userDao.findById(userId);
    }

    @Override
    public BaseDao<User, User> getUseDao() {
        return userDao;
    }

    /**
     * 当程序加载起来时，就加载一次前50名的内容
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        updateUserLeaderboardCache();
    }

}
