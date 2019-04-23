package com.lz.web.service.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.lz.util.BeanMapperUtil;
import com.lz.web.dao.UserDao;
import com.lz.web.dao.base.BaseDao;
import com.lz.web.dto.UserBanDTO;
import com.lz.web.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lz.web.service.page.PageService;

@Service
public class AdminUserService extends PageService<User, User> {
    @Autowired
    private UserDao userDao;

    public User findById(Integer id) {
        return userDao.findById(id);
    }

    public void banUser(UserBanDTO dto) {
        User user = BeanMapperUtil.map(dto, User.class);
        userDao.update(user);
    }

    /**
     * 统计用户信息，比如统计一共做了多少道题目，一共解决了多少道题目，
     * 一共解决的题目价值是多少,统计从执行该方法的时间开始，25小时以内有提交过代码的用户
     */
    public void countUserData() {
        Calendar calendar = Calendar.getInstance();
        Date endTime = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, -25);
        Date beginTime = calendar.getTime();

        List<User> users = userDao.findWithLastSubmitTimeGap(beginTime, endTime);
        List<UserDao.BatchUpdateData> datas = BeanMapperUtil.mapList(users, UserDao.BatchUpdateData.class);

        userDao.countHaveDoneProblem(datas);
        userDao.countRightProblem(datas);
        userDao.countTotalSolveValue(datas);
    }

    @Override
    public BaseDao<User, User> getUseDao() {
        return userDao;
    }
}
