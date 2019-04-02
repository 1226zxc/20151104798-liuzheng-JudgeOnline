package com.lz.web.dao;

import com.lz.web.dao.base.BaseDao;
import com.lz.web.dao.base.MyBatisRepository;
import com.lz.web.po.Announcement;

@MyBatisRepository
public interface AnnouncementDao extends BaseDao<Announcement, Announcement> {

}
