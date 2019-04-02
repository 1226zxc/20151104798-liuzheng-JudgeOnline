package com.lz.web.service.front;

import java.util.List;

import com.lz.web.dao.ProblemTypeDao;
import com.lz.web.dao.base.BaseDao;
import com.lz.web.po.ProblemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lz.web.service.page.PageService;

@Service
public class ProblemTypeService extends PageService<ProblemType, ProblemType> {
	@Autowired
	private ProblemTypeDao problemTypeDao;

	public ProblemType getProblemTypeById(Integer problemTypeId) {
		return problemTypeDao.findById(problemTypeId);
	}

	public List<ProblemType> findAll() {
		return problemTypeDao.find();
	}

	@Override
	public BaseDao<ProblemType, ProblemType> getUseDao() {
		return problemTypeDao;
	}
}
