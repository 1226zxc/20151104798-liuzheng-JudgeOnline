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
import com.lz.web.dto.AddManagerDTO;
import com.lz.web.dto.UpdateManagerDTO;
import com.lz.web.permission.Permissions;
import com.lz.web.po.Manager;
import com.lz.web.service.admin.AdminManagerService;
import com.lz.web.service.page.PageResult;
import com.lz.web.service.page.PageService;
import com.lz.web.vo.request.AddManagerVO;
import com.lz.web.vo.request.UpdateManagerVO;

@Controller
@RequestMapping("/AdminManagerController")
public class AdminManagerController extends PageController<Manager, Manager, Manager> {
    @Autowired
    private AdminManagerService adminManagerService;

    @Override
    public PageService<Manager, Manager> getPageService() {
        return adminManagerService;
    }

    @Override
    public Class<Manager> returnVoClass() {
        return Manager.class;
    }

    @AdminPermission(value = Permissions.ManagerFind)
    @Override
    public PageResult<Manager> list(int pageShowCount, int wantPageNumber) {
        return super.list(pageShowCount, wantPageNumber);
    }

    @AdminPermission(value = Permissions.ManagerFind)
    @Override
    public PageResult<Manager> listWithCondition(int pageShowCount, int wantPageNumber, Manager condition) {
        return super.listWithCondition(pageShowCount, wantPageNumber, condition);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap login(@RequestParam("account") String account, @RequestParam("password") String password, HttpSession session) {
        Manager manager = adminManagerService.login(account, password);
        session.setAttribute(WebConstant.MANAGER_SESSION_ATTRIBUTE_NAME, manager);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @RequestMapping(value = "/getNickname", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap getNickname(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(WebConstant.MANAGER_SESSION_ATTRIBUTE_NAME);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("nickname", manager.getNickname());
        return responseMap;
    }

    @AdminPermission(value = Permissions.ManagerFind)
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap find(@RequestParam("id") Integer id) {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("manager", adminManagerService.findById(id));
        return responseMap;
    }

    /**
     * 处理管理员删除请求的方法，请求此方法需要有{@code Permissions.ManagerDelete}权限
     * @param id 请求删除的id
     * @return 返回删除结果
     */
    @AdminPermission(value = Permissions.ManagerDelete)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap delete(@RequestParam("id") Integer id) {
        adminManagerService.deleteById(id);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @AdminPermission(value = Permissions.ManagerAdd)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap add(@Valid AddManagerVO vo, HttpSession session) {
        AddManagerDTO dto = BeanMapperUtil.map(vo, AddManagerDTO.class);
        adminManagerService.add(dto);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @AdminPermission(value = Permissions.ManagerUpdate)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap update(@Valid UpdateManagerVO vo, HttpSession session) {
        UpdateManagerDTO dto = BeanMapperUtil.map(vo, UpdateManagerDTO.class);
        adminManagerService.update(dto);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }
}
