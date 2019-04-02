package com.lz.web.dao;

import java.util.List;

import com.lz.web.dao.base.BaseDao;
import com.lz.web.dao.base.MyBatisRepository;
import com.lz.web.po.ProblemType;

@MyBatisRepository
public interface ProblemTypeDao extends BaseDao<ProblemType, ProblemType> {
    List<ProblemType> findWithSomeIds(List<Integer> ids);
}
