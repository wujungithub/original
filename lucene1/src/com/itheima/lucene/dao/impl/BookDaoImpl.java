package com.itheima.lucene.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.itheima.lucene.dao.BookDao;
import com.itheima.lucene.pojo.Book;

public class BookDaoImpl implements BookDao {

	@Override
	public List<Book> queryBookList() {
		//连接数据库
		Connection connection=null;
		
		PreparedStatement preparedStatement=null;
		
		ResultSet resultSet=null;
		
		//创建集合封装图书列表
		List<Book> list=new ArrayList<>();
		
		//获取链接
		try {
			Class.forName("com.mysql.jdbc.Driver");
			//获得链接
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lucene", "root", "root");
			//创建sql语句
			String sql="select * from book ";
			//创建预编译对象防止注入
			 preparedStatement = connection.prepareStatement(sql);
			//执行查询方法,获取结果集
			 resultSet = preparedStatement.executeQuery();
			//遍历结果集
			 while(resultSet.next()){
				 Book book =new Book();
				 book.setId(resultSet.getInt("id"));
				 book.setName(resultSet.getString("name"));
				 book.setPrice(resultSet.getFloat("price"));
				 book.setPic(resultSet.getString("pic"));
				 book.setDesc(resultSet.getString("description"));
				 list.add(book);
			 }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}

}
