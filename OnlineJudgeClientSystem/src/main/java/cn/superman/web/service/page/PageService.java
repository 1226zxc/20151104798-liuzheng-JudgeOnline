package cn.superman.web.service.page;

import java.util.List;

import cn.superman.web.dao.base.BaseDao;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@SuppressWarnings("Duplicates")
public abstract class PageService<T, E> {
	public abstract BaseDao<T, E> getUseDao();

	public E getDefaultCondition() {
		return null;
	}

	/**
	 * 获得指定页数据
	 * @param pageShowCount 当前页展示的数据量
	 * @param wantPageNumber 想要请求的页
	 * @return 返回指定的页数据的请求结果
	 */
	public PageResult<T> getPage(int pageShowCount, int wantPageNumber) {
		wantPageNumber = wantPageNumber < 1 ? 1 : wantPageNumber;
		PageQuery<E> pageQuery = new PageQuery<E>();
		pageQuery.setWantPageNumber(wantPageNumber);
		pageQuery.setWantPageShowCount(pageShowCount);
		pageQuery.setConditionEntity(getDefaultCondition());
		return getPageResult(pageQuery);
	}

	/**
	 * 获取指定页码的数据
	 * @param pageShowCount 展示的数据量
	 * @param wantPageNumber 请求的页号
	 * @param condition 条件请求
	 * @return 返回指定页号的数据
	 */
	public PageResult<T> getPage(int pageShowCount, int wantPageNumber, E condition) {
		wantPageNumber = wantPageNumber < 1 ? 1 : wantPageNumber;
		PageQuery<E> pageQuery = new PageQuery<E>();
		pageQuery.setWantPageNumber(wantPageNumber);
		pageQuery.setWantPageShowCount(pageShowCount);
		pageQuery.setConditionEntity(condition);
		return getPageResult(pageQuery);
	}

	/**
	 * 获取首页的数据
	 * @param pageShowCount 展示的数据量
	 * @return 返回首页的数据
	 */
	public PageResult<T> firstPage(int pageShowCount) {
		PageQuery<E> pageQuery = new PageQuery<E>();
		pageQuery.setWantPageNumber(1);
		pageQuery.setWantPageShowCount(pageShowCount);
		pageQuery.setConditionEntity(getDefaultCondition());
		return getPageResult(pageQuery);
	}

	public PageResult<T> firstPage(PageQuery<E> pageQuery) {
		pageQuery.setWantPageNumber(1);
		return getPageResult(pageQuery);
	}

	/**
	 * 获取下一页的数据
	 * @param pageShowCount 展示的数据量
	 * @param currentPage 当前页号
	 * @return 返回下一页的数据
	 */
	public PageResult<T> nextPage(int pageShowCount, int currentPage) {
		PageQuery<E> pageQuery = new PageQuery<E>();
		pageQuery.setWantPageNumber(currentPage + 1);
		pageQuery.setWantPageShowCount(pageShowCount);
		pageQuery.setConditionEntity(getDefaultCondition());
		return getPageResult(pageQuery);
	}

	public PageResult<T> nextPage(PageQuery<E> pageQuery) {
		return getPageResult(pageQuery);
	}

	public PageResult<T> prePage(int pageShowCount, int currentPage) {
		PageQuery<E> pageQuery = new PageQuery<E>();
		pageQuery.setWantPageNumber(currentPage - 1);
		pageQuery.setWantPageShowCount(pageShowCount);
		pageQuery.setConditionEntity(getDefaultCondition());
		return getPageResult(pageQuery);
	}

	public PageResult<T> prePage(PageQuery<E> pageQuery) {
		return getPageResult(pageQuery);
	}

	/**
	 * 请求数据库，获取指定页数据
	 * @param pageQuery 封装了分页查询的信息
	 * @return 返回查询数据库的结果
	 */
	private PageResult<T> getPageResult(PageQuery<E> pageQuery) {
		PageResult<T> pageResult = new PageResult<T>();

		if (pageQuery.getOrderByAttributeName() == null) {
			PageHelper.startPage(pageQuery.getWantPageNumber(), pageQuery.getWantPageShowCount());
		} else {
			PageHelper.startPage(pageQuery.getWantPageNumber(), pageQuery.getWantPageShowCount(),
					pageQuery.getOrderByAttributeName() + " " + pageQuery.getOrderByType().name());
		}

		List<T> list = null;
		if (pageQuery.getConditionEntity() == null) {
			list = getUseDao().find();
		} else {
			list = getUseDao().findWithCondition(pageQuery.getConditionEntity());
		}

		PageInfo<T> info = new PageInfo<T>(list);
		pageResult.setResult(list);
		pageResult.setTotalCount(info.getTotal());
		pageResult.setCurrentPage(info.getPageNum());
		pageResult.setTotalPage(info.getPages());
		return pageResult;
	}

}
