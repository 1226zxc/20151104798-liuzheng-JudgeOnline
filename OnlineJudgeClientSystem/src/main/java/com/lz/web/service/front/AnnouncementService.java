package com.lz.web.service.front;

import com.lz.web.dao.AnnouncementDao;
import com.lz.web.dao.base.BaseDao;
import com.lz.web.po.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lz.web.service.page.PageService;

@Service
public class AnnouncementService extends PageService<Announcement, Announcement> {

	@Autowired
	private AnnouncementDao announcementDao;
	private static Announcement defaultCondition = null;

	static {
		defaultCondition = new Announcement();
		defaultCondition.setIsPublish(true);
	}

	@Override
	public BaseDao<Announcement, Announcement> getUseDao() {
		return announcementDao;
	}

	@Override
	public Announcement getDefaultCondition() {
		return defaultCondition;
	}

	public Announcement getAnnouncementById(Integer announcementId) {
		return announcementDao.findById(announcementId);
	}

}
