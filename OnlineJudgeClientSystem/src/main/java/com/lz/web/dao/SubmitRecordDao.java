package com.lz.web.dao;

import java.util.List;

import com.lz.web.dao.base.MyBatisRepository;
import com.lz.web.po.SubmitRecord;

@MyBatisRepository
public interface SubmitRecordDao {
	void add(SubmitRecord submitRecord);

	void update(SubmitRecord submitRecord);

	void deleteWithCondition(SubmitRecord condition);

	List<SubmitRecord> findWithCondition(SubmitRecord condition);

	long querySubmitRecordotalCountWithCondition(SubmitRecord condition);
}
