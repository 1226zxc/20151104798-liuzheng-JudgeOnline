package com.lz.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.lz.web.dao.base.BaseDao;
import com.lz.web.dao.base.MyBatisRepository;
import com.lz.web.po.Problem;

@MyBatisRepository
public interface ProblemDao extends BaseDao<Problem, Problem> {
    public void userSloveProblem(@Param("userIdData") String userIdData, @Param("problemId") Integer problemId);

    public void increaseSubmitProblemCount(Integer problemId);

    public List<Problem> findPulishProblemByLikeName(String problemName);
}
