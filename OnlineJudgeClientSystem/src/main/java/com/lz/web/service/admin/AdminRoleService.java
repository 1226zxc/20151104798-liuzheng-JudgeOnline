package com.lz.web.service.admin;

import java.util.List;

import com.lz.web.dao.RoleDao;
import com.lz.web.dao.base.BaseDao;
import com.lz.web.exception.ServiceLogicException;
import com.lz.web.po.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lz.web.service.page.PageService;

@Service
public class AdminRoleService extends PageService<Role, Role> {
	@Autowired
	private RoleDao roleDao;

	public void add(String roleName, String permissions) {
		Role role = new Role();
		role.setRoleName(roleName);

		// 判断格式是否正确
		if (permissions.matches("([a-zA-Z]*,)*")) {
			throw new ServiceLogicException("权限格式不对");
		}

		role.setPermissions(permissions);

		roleDao.add(role);
	}

	public void deleteById(Integer id) {
		roleDao.deleteById(id);
	}

	public Role findById(Integer id) {
		return roleDao.findById(id);
	}

	public List<Role> findAll() {
		return roleDao.find();
	}

	public void update(Integer id, String roleName, String permissions) {
		Role role = new Role();
		role.setRoleId(id);
		role.setRoleName(roleName);
		// 判断格式是否正确
		if (permissions.matches("([a-zA-Z]*,)*")) {
			throw new ServiceLogicException("权限格式不对");
		}

		role.setPermissions(permissions);
		roleDao.update(role);
	}

	@Override
	public BaseDao<Role, Role> getUseDao() {
		return roleDao;
	}

}
