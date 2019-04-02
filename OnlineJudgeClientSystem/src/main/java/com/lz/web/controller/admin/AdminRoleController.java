package com.lz.web.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.lz.web.bean.ResponseMap;
import com.lz.web.controller.PageController;
import com.lz.web.controller.annotation.AdminPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lz.web.permission.Permissions;
import com.lz.web.po.Role;
import com.lz.web.service.admin.AdminRoleService;
import com.lz.web.service.page.PageResult;
import com.lz.web.service.page.PageService;
import com.lz.web.vo.request.AddRoleVO;
import com.lz.web.vo.request.UpdateRoleVO;

@Controller
@RequestMapping("/AdminRoleController")
public class AdminRoleController extends PageController<Role, Role, Role> {
    @Autowired
    private AdminRoleService adminRoleService;

    @AdminPermission(value = Permissions.RoleAdd)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap add(@Valid AddRoleVO vo) {
        adminRoleService.add(vo.getRoleName(), vo.getPermissions());
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @AdminPermission(value = Permissions.RoleUpdate)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap update(@Valid UpdateRoleVO vo) {
        adminRoleService.update(vo.getRoleId(), vo.getRoleName(), vo.getPermissions());
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @AdminPermission(value = Permissions.RoleFind)
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap find(@RequestParam("id") Integer id) {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("announcement", adminRoleService.findById(id));
        return responseMap;
    }

    @AdminPermission(value = Permissions.RoleFind)
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap findAll() {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("allRole", adminRoleService.findAll());
        return responseMap;
    }

    @AdminPermission(value = Permissions.RoleFind)
    @Override
    public PageResult<Role> list(int pageShowCount, int wantPageNumber) {
        return super.list(pageShowCount, wantPageNumber);
    }

    @AdminPermission(value = Permissions.RoleFind)
    @Override
    public PageResult<Role> listWithCondition(int pageShowCount, int wantPageNumber, Role condition) {
        return super.listWithCondition(pageShowCount, wantPageNumber, condition);
    }

    @RequestMapping(value = "/allPermission", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap allPermission() {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();

        Permissions[] permissions = Permissions.values();
        List<Map<String, String>> pList = new ArrayList<Map<String, String>>();
        Map<String, String> tempMap = null;
        for (Permissions p : permissions) {
            tempMap = new HashMap<String, String>();
            tempMap.put("name", p.getName());
            tempMap.put("explanation", p.getExplanation());
            pList.add(tempMap);
        }

        responseMap.append("allPermission", pList);
        return responseMap;
    }

    @AdminPermission(value = Permissions.RoleDelete)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap delete(@RequestParam("id") Integer id) {
        adminRoleService.deleteById(id);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        return responseMap;
    }

    @Override
    public PageService<Role, Role> getPageService() {
        return adminRoleService;
    }

    @Override
    public Class<Role> returnVoClass() {
        return Role.class;
    }
}
