package com.lz.web.controller.front;

import com.lz.web.bean.ResponseMap;
import com.lz.web.controller.PageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lz.web.po.ProblemType;
import com.lz.web.service.front.ProblemTypeService;
import com.lz.web.service.page.PageService;

@Controller
@RequestMapping("/ProblemTypeController")
public class ProblemTypeController extends PageController<ProblemType, ProblemType, ProblemType> {
    @Autowired
    private ProblemTypeService problemTypeService;

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap find(@RequestParam("id") Integer id) {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("problemType", problemTypeService.getProblemTypeById(id));
        return responseMap;
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap findAll() {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("allProblemType", problemTypeService.findAll());
        return responseMap;
    }

    @Override
    public PageService<ProblemType, ProblemType> getPageService() {
        return problemTypeService;
    }

    @Override
    public Class<ProblemType> returnVoClass() {
        return ProblemType.class;
    }
}
