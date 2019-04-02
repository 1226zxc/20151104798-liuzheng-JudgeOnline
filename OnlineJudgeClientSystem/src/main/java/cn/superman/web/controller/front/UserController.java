package cn.superman.web.controller.front;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.superman.util.BeanMapperUtil;
import cn.superman.web.bean.ResponseMap;
import cn.superman.web.bean.VerificationCode;
import cn.superman.web.constant.WebConstant;
import cn.superman.web.dto.UpdateUserPasswordDTO;
import cn.superman.web.dto.UserLeaderboardDTO;
import cn.superman.web.dto.UserLoginDTO;
import cn.superman.web.dto.UserRegisterDTO;
import cn.superman.web.dto.UserUpdateDTO;
import cn.superman.web.dto.VerificationCodeDTO;
import cn.superman.web.exception.ServiceLogicException;
import cn.superman.web.po.User;
import cn.superman.web.service.front.UserService;
import cn.superman.web.vo.request.UpdateUserPasswordVO;
import cn.superman.web.vo.request.UserLoginVO;
import cn.superman.web.vo.request.UserRegisterVO;
import cn.superman.web.vo.request.UserUpdateVO;
import cn.superman.web.vo.response.UserLoginResponse;

/**
 * 用户请求转发控制器
 * @author 刘铮
 */
@Controller
@RequestMapping("/UserController")
@SuppressWarnings("Duplicates")
public class UserController {
    private static final String VERIFICATION_CODE_ATTR_NAME = "verificationCode";

    @Autowired
    private UserService userService;

    /**
     * 注册用户
     * @param userRegisterVO 用户输入的表单数据
     * @param session session会话这里指用于获取生成的验证码
     * @return 返回注册结果：成功或失败
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap registerUser(@Valid UserRegisterVO userRegisterVO, HttpSession session) {
        // 获取生成的验证码
        String code = (String) session.getAttribute(VERIFICATION_CODE_ATTR_NAME);
        if (StringUtils.isBlank(code)) {
            throw new ServiceLogicException("验证码失效，请刷新一次验证码");
        }

        if (!code.equals(userRegisterVO.getVerificationCode())) {
            throw new ServiceLogicException("验证码输入错误");
        }

        UserRegisterDTO dto = BeanMapperUtil.map(userRegisterVO, UserRegisterDTO.class);
        userService.register(dto);
        return new ResponseMap().buildSucessResponse();
    }

    @RequestMapping(value = "/checkAccountIsUnique", method = RequestMethod.GET)
    public ResponseMap checkAccountIsUnique(@RequestParam(name = "account") String account) {
        userService.checkAccountIsUnique(account);

        ResponseMap responseMap = new ResponseMap().buildSucessResponse();

        return responseMap;
    }

    /**
     * 用户登陆处理
     * @param userLoginVO 用户登陆信息
     * @param request HttpServletRequest实例对象
     * @return 返回登陆结果
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("Duplicates")
    public ResponseMap loginUser(@Valid UserLoginVO userLoginVO, HttpServletRequest request) {
        UserLoginDTO dto = BeanMapperUtil.map(userLoginVO, UserLoginDTO.class);
        User user = userService.login(dto);
        UserLoginResponse response = BeanMapperUtil.map(user, UserLoginResponse.class);
        request.getSession().setAttribute(WebConstant.USER_SESSION_ATTRIBUTE_NAME, user);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("user", response);
        return responseMap;
    }

    /**
     * 注销登陆
     * 这里只完成删除的当前登录的用户信息
     * @param request HttpServletRequest request请求
     * @return 注销结果
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap logout(HttpServletRequest request) {
        request.getSession().removeAttribute(WebConstant.USER_SESSION_ATTRIBUTE_NAME);
        return new ResponseMap().buildSucessResponse();
    }

    @RequestMapping(value = "/sendUpdateCodeEmail", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap sendUpdateCodeEmail(HttpServletRequest request) {
        // 获取当前系统登录的用户对象
        User user = getLoginUser(request);
        userService.sendUpdateCodeEmail(user);
        return new ResponseMap().buildSucessResponse();
    }

    /**
     * 用户忘记密码请求处理
     * @param vo 用户提交的表单数据
     * @param request HttpServletRequest request实例对象
     * @return 处理结果
     */
    @RequestMapping(value = "/updateSubmit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap updateSubmit(@Valid UserUpdateVO vo, HttpServletRequest request) {
        UserUpdateDTO dto = BeanMapperUtil.map(vo, UserUpdateDTO.class);
        dto.setUserId(getLoginUser(request).getUserId());

        // 更新用户信息
        userService.update(dto);

        User user = userService.find(dto.getUserId());
        UserLoginResponse response = BeanMapperUtil.map(user, UserLoginResponse.class);
        request.getSession().setAttribute(WebConstant.USER_SESSION_ATTRIBUTE_NAME, user);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("user", response);

        return responseMap;
    }

    @RequestMapping(value = "/sendForgetPasswordEmail/{account}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap sendForgetPasswordEmail(@PathVariable("account") String account) {
        userService.sendForgetPasswordEmail(account);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();

        return responseMap;
    }

    /**
     * 忘记密码功能请求处理
     * @param vo 用户提交表单
     * @param request HttpServletRequest 实例对象
     * @return 忘记密码处理结果sueccess = true 或 false
     */
    @RequestMapping(value = "/updatePasswordSubmit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap updatePasswordSubmit(@Valid UpdateUserPasswordVO vo, HttpServletRequest request) {
        UpdateUserPasswordDTO dto = BeanMapperUtil.map(vo, UpdateUserPasswordDTO.class);
        userService.updateUserPassword(dto);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @RequestMapping(value = "/leaderboard", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap Leaderboard() {
        List<UserLeaderboardDTO> haveDoneProblemTop50 = userService.getHaveDoneProblemTop50();
        List<UserLeaderboardDTO> rightProblemTop50 = userService.getRightProblemTop50();
        List<UserLeaderboardDTO> sloveProblemTotalValueTop50 = userService.getSloveProblemTotalValueTop50();

        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("haveDoneProblemTop50", haveDoneProblemTop50);
        responseMap.append("rightProblemTop50", rightProblemTop50);
        responseMap.append("sloveProblemTotalValueTop50", sloveProblemTotalValueTop50);

        return responseMap;
    }

    @RequestMapping(value = "/changeVerificationCode", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap changeVerificationCode(HttpServletRequest request) {
        VerificationCodeDTO dto = getNewVerificationCode();
        request.getSession().setAttribute(VERIFICATION_CODE_ATTR_NAME, dto.getTextContent());

        ResponseMap responseMap = new ResponseMap().buildSucessResponse();

        responseMap.put("verificationCode", "data:image/jpg;base64," + dto.getImageData());
        return responseMap;
    }

    private VerificationCodeDTO getNewVerificationCode() {
        VerificationCodeDTO dto = new VerificationCodeDTO();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        VerificationCode verificationCode = VerificationCode.getDefaultNewinstance();
        try {
            verificationCode.writeImgae(byteArrayOutputStream);
            dto.setTextContent(verificationCode.getText());
            dto.setImageData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            return dto;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    /**
     * 获取当前登录中的用户对象
     * @param request HttpServletRequest request请求对象
     * @return 当前系统登录的用户
     */
    private User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(WebConstant.USER_SESSION_ATTRIBUTE_NAME);
    }

}
