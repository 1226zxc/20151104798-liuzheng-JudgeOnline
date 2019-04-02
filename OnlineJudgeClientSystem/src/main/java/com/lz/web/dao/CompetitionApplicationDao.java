package com.lz.web.dao;

import java.util.List;

import com.lz.web.dao.base.BaseDao;
import com.lz.web.dao.base.MyBatisRepository;
import com.lz.web.po.CompetitionApplication;

@MyBatisRepository
public interface CompetitionApplicationDao extends
		BaseDao<CompetitionApplication, CompetitionApplication> {
	List<Integer> findAllCompetionId();
}
