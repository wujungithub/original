package com.itheima.lucene.junit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.itheima.lucene.dao.BookDao;
import com.itheima.lucene.dao.impl.BookDaoImpl;
import com.itheima.lucene.pojo.Book;

public class CreatIndexTest {
	
	@Test
	public void creatIndex() throws IOException{
		//1.首先是获取数据
		BookDao bookDao=new BookDaoImpl();
		List<Book> books = bookDao.queryBookList();
		//2.创建document文本对象,遍历集合数据,根据数据创建域对象,将域对象添加到文本对象中,
		//一条数据一个文本对象,创建集合来封装文本对象
		List<Document> documents=new ArrayList<>();
		for(Book book:books){
			Document document=new Document();
			//向文档对象中添加域对象,数据存储到文档域中
			document.add(new StoredField("id",book.getId()));
			document.add(new TextField("name",book.getName(),Store.YES));
			document.add(new FloatField("price",book.getPrice(),Store.YES));
			document.add(new StoredField("pic",book.getPic()));
			document.add(new TextField("desc",book.getDesc(),Store.NO));
			/*//设置加权值Boost
			TextField textField = new TextField("desc",book.getDesc(),Store.NO);
			if(4==book.getId()){
				textField.setBoost(3.0f);
			}
			document.add(textField);*/
			//把document放到集合中
			documents.add(document);
		}
		//3.创建分词器  标准分词器
		Analyzer analyzer= new IKAnalyzer();
		//4.创建索引库目录对象Directory
		Directory directory=FSDirectory.open(new File("F:/practice/luceneItemp"));
		//5.创建IndexWriteConfig对象，写入索引需要的配置
		IndexWriterConfig config=new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		//6.创建IndexWriter输出流对象
		IndexWriter indexWriter=new IndexWriter(directory, config);
		//7.将文档对象输出到索引库
		for(Document document:documents){
			//将文档对象添加到索引库
			indexWriter.addDocument(document);
		}
		//8.关闭资源
		indexWriter.close();
	}
	/**
	 * 搜索索引
	 * @throws Exception 
	 */
	@Test
	public void queryIndex() throws Exception{
		//1.创建query查询对象
		//创建分词对象
		Analyzer analyzer=new IKAnalyzer();
		//创建查询解析器
		QueryParser queryParser=new QueryParser("name", analyzer);
		//创建查询对象
		Query query= queryParser.parse("name:java");
		//2.创建输入流对象,声明索引库位置
		Directory directory=FSDirectory.open(new File("F:/practice/luceneItemp"));
		IndexReader indexReader=DirectoryReader.open(directory);
		//3.创建搜索索引对象
		IndexSearcher indexSearcher=new IndexSearcher(indexReader);
		//4.执行搜索
		TopDocs topDocs = indexSearcher.search(query, 10);
		//通过查询到的头文件来获得文件总数
		System.out.println("总文件数:"+topDocs.totalHits);
		//通过头文件对象获得文件结果集
		ScoreDoc[] docs = topDocs.scoreDocs;
		//遍历获得文件对象
		for (ScoreDoc scoreDoc : docs) {
			//获得文件ID
			int docID = scoreDoc.doc;
			//通过id获得文件对象
			Document doc = indexSearcher.doc(docID);
			
			System.out.println("=============================");
			System.out.println("docID:" + docID);
			System.out.println("bookId:" + doc.get("id"));
			System.out.println("name:" + doc.get("name"));
			System.out.println("price:" + doc.get("price"));
			System.out.println("pic:" + doc.get("pic"));
		}
		//关闭资源
		indexReader.close();
	}
	
	/*
	 * 索引的维护:增删改 查
	 */
	//首先是提供获得索引输出流对象的方法:进行删除和修改
	public IndexWriter getIndexWriter() throws Exception{
		Analyzer analyzer= new IKAnalyzer();
		Directory directory=FSDirectory.open(new File("F:/practice/luceneItemp"));
		IndexWriterConfig config=new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		IndexWriter indexWriter=new IndexWriter(directory, config);
		return indexWriter;
	}
	//执行删除操作:删除全部和按条件删除
	
	public void delete() throws Exception{
		IndexWriter indexWriter = getIndexWriter();
		//indexWriter.deleteAll();//此方法是删除全部索引,文档也删除,慎用
		indexWriter.deleteDocuments(new Term("name","java"));
		indexWriter.close();
	}
	
	//修改:执行修改时注意的是,没有修改api,修改api就是添加操作,更新操作时先删除再添加,
	//当更新索引的目标文档存在的时候会执行先删除后添加操作,如果不存在就执行添加操作;
	
	public void update() throws Exception{
		IndexWriter indexWriter = getIndexWriter();
		Document document=new Document();
		document.add(new StoredField("id","1000"));
		document.add(new TextField("name", "lucene测试", Store.YES));
		indexWriter.updateDocument(new Term("name","lucene"), document);
		
		indexWriter.close();
	}
	
	//查询:子查询,和通过查询解析器进行查询
	  //抽执行搜索的方法
	public void doSearcher(Query query) throws Exception{
		//2.创建输入流对象,声明索引库位置
		Directory directory=FSDirectory.open(new File("F:/practice/luceneItemp"));
		IndexReader indexReader=DirectoryReader.open(directory);
		//3.创建搜索索引对象
		IndexSearcher indexSearcher=new IndexSearcher(indexReader);
		//4.执行搜索
		TopDocs topDocs = indexSearcher.search(query, 10);
		//通过查询到的头文件来获得文件总数
		System.out.println("总文件数:"+topDocs.totalHits);
		//通过头文件对象获得文件结果集
		ScoreDoc[] docs = topDocs.scoreDocs;
		//遍历获得文件对象
		for (ScoreDoc scoreDoc : docs) {
			//获得文件ID
			int docID = scoreDoc.doc;
			//通过id获得文件对象
			Document doc = indexSearcher.doc(docID);
			
			System.out.println("=============================");
			System.out.println("docID:" + docID);
			System.out.println("bookId:" + doc.get("id"));
			System.out.println("name:" + doc.get("name"));
			System.out.println("price:" + doc.get("price"));
			System.out.println("pic:" + doc.get("pic"));
		}
		//关闭资源
		indexReader.close();
	}
	//标准查询
	@Test
	public void queryByStandardTerm() throws Exception{
		Query query =new TermQuery(new Term("name", "lucene"));
		doSearcher(query);
	}
	//指定数字范围查询NumerickRangeQquery
	@Test
	public void queryByNumericRangeQuery() throws Exception{
		Query query =NumericRangeQuery.newFloatRange("price", 70f, 80f, true, false);
		doSearcher(query);
	}
	
	
	//布尔查询,实现组合条件查询BooleanQuery
	@Test
	public void queryByBooleanQuery(){
		//创建不同的查询对象,然后通过布尔查询对象组合进行查询
		Query query1=new TermQuery(new Term("name","lucene"));
		
		Query query2 =NumericRangeQuery.newFloatRange("price", 70f, 80f, true, false);
		
		BooleanQuery booleanQuery=new BooleanQuery();
		
		booleanQuery.add(query1, Occur.MUST);
		booleanQuery.add(query2, Occur.SHOULD);
	}
	
	//QueryParser 可以创建查询对象,通过.parse(查询语法);
	@Test
	public void queryByQueryParser() throws Exception{
		//创建分词器
		Analyzer analyzer =new IKAnalyzer();
		//创建差下一步解析器
		QueryParser parser=new QueryParser("desc", analyzer);
		//获取查询语句对象
		Query query = parser.parse("desc:java AND lucene");
		//输出查询语句对象
		System.out.println(query);
		//执行搜索
		doSearcher(query);
		
	}
	
	
	//通过MultiFieldQueryParser对多个域查询。
	
	@Test
	public void queryByMultiFieldQueryParse() throws Exception{
		//创建分词器
		Analyzer analyzer =new IKAnalyzer();
		
		String[] fields= {"name","desc"};
		MultiFieldQueryParser  mfqp = new MultiFieldQueryParser(fields, analyzer);
		
		Query query = mfqp.parse("java");
		doSearcher(query);
	}
	
	/**
	 * 练习: 1.创建索引
	 * 		2.搜索索引
	 * @throws Exception 
	 */
	public void practice() throws Exception{
		//练习创建搜索索引
		//1.从数据库/本地磁盘/web网页中获取数据
		//2.遍历数据,创建文本对象Document,创建域对象Field
		BookDao bookDao =new BookDaoImpl();
		List<Book> books = bookDao.queryBookList();
		
		//创建集合对象
		List<Document> documents=new ArrayList<>();
		
		for(Book book:books){
			Document document=new Document();
		
			document.add(new TextField("id",String.valueOf(book.getId()),Store.YES));
			document.add(new TextField("name", book.getName(),Store.YES));
			document.add(new TextField("price", String.valueOf(book.getPrice()),Store.YES));
			document.add(new TextField("pic", book.getPic(),Store.YES));
			document.add(new TextField("desc", book.getDesc(),Store.YES));
			
			//将文件对象添加到集合中
			documents.add(document);
		}
		//3.创建分词对象Analyzer
		Analyzer analyzer =new IKAnalyzer();
		//4.创建Directory对象,指出索引库位置
		Directory directory=FSDirectory.open(new File("f:/practice/wj001/luceneitemp"));
		//5.创建输出流对象,将问价读取到索引库
		Version version= Version.LUCENE_4_10_3;
		//创建索引输出流配置对象		
		IndexWriterConfig config=new IndexWriterConfig(version, analyzer);
		IndexWriter indexWriter=new IndexWriter(directory, config);
		//6读取文件对象
		for (Document document : documents) {
			//将文件添加到索引库
			indexWriter.addDocument(document);
		}
		//7.关闭资源
		indexWriter.close();
	}	
	
	//2.搜索索引
	public void searchIndex() throws Exception{
		//1.首先是回去条件接口(网页输入栏中用户输出)
		//2.创建查询对象Query
		  //首先创建分词对象
		Analyzer analyzer =new IKAnalyzer();
		  //根据分词对象创建查询解析器
		QueryParser parser=new QueryParser("name", analyzer);
		Query query= parser.parse("name:lucene");
		//3.创建输入流对象指明索引库位置
		Directory directory =FSDirectory.open(new File("f:/practice/wj001/luceneItemp"));
		//获取索引输入流对象
		IndexReader indexReader=DirectoryReader.open(directory);
		
		//4.创建索引搜索对象
		IndexSearcher indexSearcher=new IndexSearcher(indexReader);
		//5.执行搜索获取头文件
		TopDocs topDocs = indexSearcher.search(query, 5);
		System.out.println("总数:"+topDocs.totalHits);
		//6.获取结果集
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		//7.遍历结果集
		for (ScoreDoc scoreDoc : scoreDocs) {
			//获得文件ID
			int docID = scoreDoc.doc;
			//根据ID查询文件
			Document doc = indexSearcher.doc(docID);
			
			System.out.println("docID:"+docID);
			System.out.println("price:"+doc.get("price"));
			System.out.println("pic:"+doc.get("pic"));
			System.out.println("name"+doc.get("name"));
			System.out.println("desc"+doc.get("desc"));
		}
		//8.关闭资源
		indexReader.close();
	}
	
}
