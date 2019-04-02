package com.lz.web.service.admin;

import java.util.Date;

import com.lz.util.BeanMapperUtil;
import com.lz.web.dao.AnnouncementDao;
import com.lz.web.dao.base.BaseDao;
import com.lz.web.dto.AddAnnouncementDTO;
import com.lz.web.dto.UpdateAnnouncementDTO;
import com.lz.web.po.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lz.web.service.page.PageService;

@Service
public class AdminAnnouncementService extends
		PageService<Announcement, Announcement> {
	@Autowired
	private AnnouncementDao announcementDao;

	public void add(AddAnnouncementDTO dto) {
		Announcement announcement = BeanMapperUtil.map(dto, Announcement.class);
		if (dto.getIsPublish()) {
			announcement.setAnnouncementPublishTime(new Date());
		}
		announcement.setAnnouncementCreateTime(new Date());
		announcementDao.add(announcement);
	}

	public void deleteById(Integer id) {
		announcementDao.deleteById(id);
	}

	public Announcement findById(Integer id) {
		return announcementDao.findById(id);
	}

	public void update(UpdateAnnouncementDTO dto) {
		Announcement announcement = BeanMapperUtil.map(dto, Announcement.class);
		if (dto.getIsPublish()) {
			announcement.setAnnouncementPublishTime(new Date());
		}
		announcementDao.update(announcement);
	}

	public void publish(Integer id) {
		Announcement announcement = new Announcement();
		announcement.setAnnouncementId(id);
		announcement.setAnnouncementPublishTime(new Date());
		announcement.setIsPublish(true);
		announcementDao.update(announcement);
	}

	@Override
	public BaseDao<Announcement, Announcement> getUseDao() {
		return announcementDao;
	}
}
