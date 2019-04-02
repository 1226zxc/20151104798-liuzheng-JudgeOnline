package com.lz.web.controller.admin;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.lz.util.BeanMapperUtil;
import com.lz.web.bean.ResponseMap;
import com.lz.web.constant.WebConstant;
import com.lz.web.controller.PageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lz.web.controller.annotation.AdminPermission;
import com.lz.web.dto.AddAnnouncementDTO;
import com.lz.web.dto.UpdateAnnouncementDTO;
import com.lz.web.permission.Permissions;
import com.lz.web.po.Announcement;
import com.lz.web.po.Manager;
import com.lz.web.service.admin.AdminAnnouncementService;
import com.lz.web.service.page.PageResult;
import com.lz.web.service.page.PageService;
import com.lz.web.vo.request.AddAnnouncementVO;
import com.lz.web.vo.request.UpdateAnnouncementVO;

@Controller
@RequestMapping("/AdminAnnouncementController")
public class AdminAnnouncementController extends PageController<Announcement, Announcement, Announcement> {
    @Autowired
    private AdminAnnouncementService adminAnnouncementService;

    @AdminPermission(value = Permissions.AnnouncementFind)
    @Override
    public PageResult<Announcement> list(int pageShowCount, int wantPageNumber) {
        return super.list(pageShowCount, wantPageNumber);
    }

    @AdminPermission(value = Permissions.AnnouncementFind)
    @Override
    public PageResult<Announcement> listWithCondition(int pageShowCount, int wantPageNumber, Announcement condition) {
        return super.listWithCondition(pageShowCount, wantPageNumber, condition);
    }

    @AdminPermission(value = Permissions.AnnouncementFind)
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap find(@RequestParam("id") Integer id) {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("announcement", adminAnnouncementService.findById(id));
        return responseMap;
    }

    @AdminPermission(value = Permissions.AnnouncementAdd)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap add(@Valid AddAnnouncementVO vo, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(WebConstant.MANAGER_SESSION_ATTRIBUTE_NAME);
        AddAnnouncementDTO dto = BeanMapperUtil.map(vo, AddAnnouncementDTO.class);
        dto.setAnnouncementCreateManagerId(manager.getManagerId());
        adminAnnouncementService.add(dto);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @AdminPermission(value = Permissions.AnnouncementUpdate)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap update(@Valid UpdateAnnouncementVO vo, HttpSession session) {
        Manager manager = (Manager) session.getAttribute(WebConstant.MANAGER_SESSION_ATTRIBUTE_NAME);
        UpdateAnnouncementDTO dto = BeanMapperUtil.map(vo, UpdateAnnouncementDTO.class);
        dto.setAnnouncementCreateManagerId(manager.getManagerId());
        adminAnnouncementService.update(dto);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @AdminPermission(value = Permissions.AnnouncementDelete)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap delete(@RequestParam("id") Integer id) {
        adminAnnouncementService.deleteById(id);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @AdminPermission(value = Permissions.AnnouncementPublish)
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap publish(@RequestParam("id") Integer id) {
        adminAnnouncementService.publish(id);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @Override
    public PageService<Announcement, Announcement> getPageService() {
        return adminAnnouncementService;
    }

    @Override
    public Class<Announcement> returnVoClass() {
        return Announcement.class;
    }
}
