package com.lz.web.controller.front;

import com.lz.web.bean.ResponseMap;
import com.lz.web.controller.PageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lz.web.po.Announcement;
import com.lz.web.service.front.AnnouncementService;
import com.lz.web.service.page.PageService;
import com.lz.web.vo.response.AnnouncementResponse;

@Controller
@RequestMapping("/AnnouncementController")
public class AnnouncementController extends PageController<Announcement, Announcement, AnnouncementResponse> {
    @Autowired
    private AnnouncementService announcementService;

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap find(@RequestParam("id") Integer id) {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("announcement", announcementService.getAnnouncementById(id));
        return responseMap;
    }

    @Override
    public PageService<Announcement, Announcement> getPageService() {
        return announcementService;
    }

    @Override
    public Class<AnnouncementResponse> returnVoClass() {
        return AnnouncementResponse.class;
    }
}
