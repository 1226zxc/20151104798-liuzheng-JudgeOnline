package com.lz.web.controller.admin;

import java.util.List;

import javax.validation.Valid;

import com.lz.util.BeanMapperUtil;
import com.lz.web.bean.ResponseMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lz.web.controller.annotation.AdminPermission;
import com.lz.web.dto.UpdateCompetitionAccountDTO;
import com.lz.web.permission.Permissions;
import com.lz.web.po.CompetitionAccount;
import com.lz.web.service.admin.AdminCompetitionAccountService;
import com.lz.web.vo.request.UpdateCompetitionAccountVO;

@Controller
@RequestMapping("/AdminCompetitionAccountController")
public class AdminCompetitionAccountController {
    @Autowired
    private AdminCompetitionAccountService adminCompetitionAccountService;

    @AdminPermission(value = Permissions.CompetitionAccountUpdate)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap update(@Valid UpdateCompetitionAccountVO vo) {
        UpdateCompetitionAccountDTO dto = BeanMapperUtil.map(vo, UpdateCompetitionAccountDTO.class);
        adminCompetitionAccountService.update(dto);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @AdminPermission(value = Permissions.CompetitionAccountFind)
    @RequestMapping(value = "/getCompetitionAccount/{competitionId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap getCompetitionAccount(@PathVariable("competitionId") Integer competitionId) {
        List<CompetitionAccount> allAccounts = adminCompetitionAccountService.findCompetitionAccount(competitionId);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("allAccounts", allAccounts);
        return responseMap;
    }

    @AdminPermission(value = Permissions.CompetitionAccountFind)
    @RequestMapping(value = "/getAllCompetitionIds", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap getAllCompetitionIds() {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("allCompetitionIds", adminCompetitionAccountService.getAllCompetitionIds());
        return responseMap;
    }

}
