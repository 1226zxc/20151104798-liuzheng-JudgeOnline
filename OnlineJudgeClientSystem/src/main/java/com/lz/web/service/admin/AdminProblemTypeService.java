package com.lz.web.service.admin;

import java.util.List;

import com.lz.util.BeanMapperUtil;
import com.lz.web.dao.ProblemTypeDao;
import com.lz.web.dao.base.BaseDao;
import com.lz.web.dto.AddProblemTypeDTO;
import com.lz.web.dto.UpdateProblemTypeDTO;
import com.lz.web.po.ProblemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lz.web.service.page.PageService;

@Service
public class AdminProblemTypeService extends PageService<ProblemType, ProblemType> {
    @Autowired
    private ProblemTypeDao problemTypeDao;

    public List<ProblemType> findAll() {
        return problemTypeDao.find();
    }

    public ProblemType findById(Integer id) {
        return problemTypeDao.findById(id);
    }

    public void add(AddProblemTypeDTO dto) {
        ProblemType problemType = BeanMapperUtil.map(dto, ProblemType.class);
        problemTypeDao.add(problemType);
    }

    public void update(UpdateProblemTypeDTO dto) {
        ProblemType problemType = BeanMapperUtil.map(dto, ProblemType.class);
        problemTypeDao.update(problemType);
    }

    public void deleteById(Integer id) {
        problemTypeDao.deleteById(id);
    }

    @Override
    public BaseDao<ProblemType, ProblemType> getUseDao() {
        return problemTypeDao;
    }
}
