package com.itheima.lucene.dao;

import java.util.List;

import com.itheima.lucene.pojo.Book;

public interface BookDao {
	/**
	 * 链接数据库,进行数据的获取
	 */
	public List<Book> queryBookList();
	
}
