package com.lz.web.service.front;

import com.lz.util.BeanMapperUtil;
import com.lz.util.Log4JUtil;
import com.lz.util.UUIDUtil;
import com.lz.web.dao.CompetitionApplicationDao;
import com.lz.web.dto.CompetitionApplyDTO;
import com.lz.web.exception.ServiceLogicException;
import com.lz.web.po.CompetitionApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompetitionApplicationService {

    @Autowired
    private CompetitionApplicationDao competitionApplicationDao;

    public void addApplication(CompetitionApplyDTO dto) throws ServiceLogicException {

        CompetitionApplication competitionApplication = BeanMapperUtil.map(dto, CompetitionApplication.class);
        // 不是自增长的主键，所以需要生成一个唯一的ID号
        competitionApplication.setCompetitionApplicationId(UUIDUtil.getUUID());
        competitionApplication.setIsHaveSendEmail(false);
        competitionApplication.setIsHaveHandle(false);
        try {
            competitionApplicationDao.add(competitionApplication);
        } catch (Exception e) {
            Log4JUtil.logError(e);
            throw new ServiceLogicException("申请失败，你的邮箱已经被申请过了，请不要再申请");
        }

    }
}
