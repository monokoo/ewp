/**
 * @author tanguozhi
 */
package com.gz.tool.servlet.dbmanager;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gz.tool.db.DBConnection;
import com.gz.tool.db.DBStructManager;
import com.gz.tool.db.DBStructManagerModel;


public class DBManagerServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	private static HashMap<String, String> mTableHashMap = new HashMap<String, String>();
	
	public static String SUBMIT_FLAG = "submitFlag";
	public static String REQUEST_URI = "";
	
	//用户管理
	public final static String USER_SELECT = "USER000000";					//用户操作
	public final static String USER_CREATE = "USER000001";					//创建用户
	public final static String USER_DROP = "USER000002";					//删除用户
	
	//数据库
	public final static String DATABASES_LOGIN = "DB000000";				//登录数据库
	public final static String DATABASES_SELECT = "DB000001";				//选择数据库
	public final static String DATABASES_CREATE = "DB000002";				//创建数据库
	public final static String DATABASES_DROP = "DB000003";					//删除数据库
	
	//表格
	public final static String TABLE_CREATE = "TB000000";					//创建表格
	public final static String TABLE_DROP = "TB000001";						//删除表格
	public final static String TABLE_SELECT = "TB000002";					//选择表格
	public final static String TABLE_BACK = "TB000003";						//返回到数据库选择界面
	
	//表列
	public final static String COLUMN_CREATE = "COL000000";					//创建表列
	public final static String COLUMN_DROP = "COL000001";					//删除表列
	public final static String COLUMN_BACK = "COL000002";					//返回到表选择界面
	
	//表数据
	public final static String TABLE_DATA_SELECT = "DATA000001";			//表数据选择
	public final static String TABLE_DATA_CREATE_OR_UPDATE = "DATA000002";	//表数据新增或更新
	public final static String TABLE_DATA_DELETE = "DATA000003";			//表数据删除
	public final static String TABLE_DATA_BACK= "DATA000004";				//返回到表选择界面
	
	/*
	 * 表现层进入
	 * BXC00001		
	 */
	 @Override  
	 public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		 REQUEST_URI = request.getRequestURI().split("\\?")[0];
		 
		 String submitFlag = request.getParameter(SUBMIT_FLAG);
		 
		 String pageStr = "";
		  if(submitFlag==null || "".equals(submitFlag)) {	//跳转到登录界面
			  removeInfoByKey(request.getSession().getId());
			  DBConnection.removeInfoByKey(request.getSession().getId());
			 pageStr = DBManagerPage.getDBLoginPage();
		 } else if(USER_SELECT.equals(submitFlag)) {		//用户操作
			 pageStr = handleUserSelect(request);
		 } else if(USER_CREATE.equals(submitFlag)) {		//创建用户
			 pageStr = handleUserCreate(request);
		 } else if(USER_DROP.equals(submitFlag)) {			//删除用户
			 pageStr = handleUserDrop(request);
		 } else if(DATABASES_LOGIN.equals(submitFlag)) {	//数据库登录操作
			 pageStr = handleDatabasesLogin(request);
		 } else if(DATABASES_SELECT.equals(submitFlag)) {	//数据库选择
			 pageStr = handleDatabasesSelect(request);
		 } else if(DATABASES_CREATE.equals(submitFlag)) {	//数据库选择
			 pageStr = handleDatabasesCreate(request);
		 } else if(DATABASES_DROP.equals(submitFlag)) {		//删除数据库
			 pageStr = handleDatabasesDrop(request);
		 } else if (TABLE_CREATE.equals(submitFlag)) {		//创建表格
			 pageStr = handleTableCreate(request);
		 } else if (TABLE_DROP.equals(submitFlag)) {		//删除表格
			 pageStr = handleTableDrop(request);
		 } else if (TABLE_SELECT.equals(submitFlag)) {		//选择表格
			 pageStr = handleTableSelect(request);
		 } else if (TABLE_BACK.equals(submitFlag)) {		//返回到数据库选择界面
			 pageStr = handleTableBack(request);
		 } else if (COLUMN_CREATE.equals(submitFlag)) {		//创建表列
			 pageStr = handleColumnCreate(request);
		 } else if (COLUMN_DROP.equals(submitFlag)) {		//删除表列
			 pageStr = handleColumnDrop(request);
		 } else if (COLUMN_BACK.equals(submitFlag)) {		//返回到表选择界面
			 pageStr = handleColumnBack(request);
		 } else if (TABLE_DATA_SELECT.equals(submitFlag)) {	//表格数据选择界面
			 pageStr = handleTableDataSelect(request);
		 } else if (TABLE_DATA_CREATE_OR_UPDATE.equals(submitFlag)) {	//表格数据选择界面
			 pageStr = handleTableDataCreateOrUpdate(request);
		 } else if (TABLE_DATA_DELETE.equals(submitFlag)) {	//表格数据删除
			 pageStr = handleTableDataDelete(request);
		 }   else if (TABLE_DATA_BACK.equals(submitFlag)) {		//返回到表选择界面
			 pageStr = handleTableDataBack(request);
		 }
		 response.setContentType("text/html;charset=UTF-8"); 
		 response.getWriter().write(pageStr);
	 }
	 
	 private String handleUserCreate(HttpServletRequest request) {
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 String userName = request.getParameter("userName");
			 String userPassword = request.getParameter("userPassword");
			 boolean isExit = DBStructManager.userIsExit(conn, userName);
			 if(!isExit) {
				 String errorInfo = DBStructManager.createUser(conn, userName, userPassword);
				 if(errorInfo==null || "".equals(errorInfo)) {
					 ArrayList<String> userList = DBStructManager.getAllUser(conn);
					 return DBManagerPage.getDBUserSelectPage(userList);
				 } else {
					 return DBManagerPage.getDBErrorPage(errorInfo);
				 }
			 }
			 return DBManagerPage.getDBErrorPage("用户创建失败，用户已存在！");
		 }
		 
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 private String handleUserDrop(HttpServletRequest request) {
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 String userName = request.getParameter("userName");
			 String errorInfo = DBStructManager.dropUser(conn, userName);
			 if(errorInfo==null || "".equals(errorInfo)) {
				 ArrayList<String> userList = DBStructManager.getAllUser(conn);
				 return DBManagerPage.getDBUserSelectPage(userList);
			 } else {
				 return DBManagerPage.getDBErrorPage(errorInfo);
			 }
		 }
		 
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 private String handleUserSelect(HttpServletRequest request) {
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn==null) {
			 handleDatabasesLogin(request);
		 }
		 
		 conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 ArrayList<String> userList = DBStructManager.getAllUser(conn);
			 return DBManagerPage.getDBUserSelectPage(userList);
		 }
		 
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 /**
	  * 从表数据界面返回到表格列表界面
	  * @param request	客户端请求对象
	  * @return	返回表格列表界面
	  */
	 private String handleTableDataBack(HttpServletRequest request) {
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 ArrayList<String> tableList = DBStructManager.getAllTable(conn);
			 return DBManagerPage.getDBTablesSelectPage(tableList);
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 /**
	  * 删除表数据
	  * @param request	客户端请求对象
	  * @return 返回表数据列表界面
	  */
	 private String handleTableDataDelete(HttpServletRequest request) { 
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 String tableName = mTableHashMap.get(request.getSession().getId());
			 String columnName = request.getParameter("columnName");
			 String columnData = request.getParameter("columnData");
			 String errorInfo = DBStructManager.removeTableData(conn, tableName, columnName, columnData);
			 if(errorInfo!=null || !"".equals(errorInfo)) {
				 return handleTableDataSelect(request);
			 } else {
				 return DBManagerPage.getDBErrorPage(errorInfo);
			 }
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 /**
	  * 新增或更新表数据（根据主键进行操作）
	  * @param request	客户端请求对象
	  * @return 返回表数据列表界面
	  */
	 private String handleTableDataCreateOrUpdate(HttpServletRequest request) { 
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 String tableName = mTableHashMap.get(request.getSession().getId());
			 //处理数据
			 String columnAndData = request.getParameter("columnAndData");
			 if(tableName!=null && columnAndData!=null && !"".equals(columnAndData)) {
				 String[] columnAndDatas = columnAndData.split("###;###");
				 Map<String, String> columnAndDataMap = new HashMap<String, String>();
				 for (int i=0; i<columnAndDatas.length; i++) {
					 if("".equals(columnAndDatas[i])) {
						 continue;
					 }
					 String[] cd = columnAndDatas[i].split("###=###");
					 String column = cd[0];
					 String data = "";
					 if(cd.length>1)
						 data = cd[1];
					 columnAndDataMap.put(column, data);
				 }
				 
				 //获取主键
				 String privateKeyColumn = DBStructManager.getKeyColumn(conn, tableName);
				 if(privateKeyColumn!=null && !"".equals(privateKeyColumn)) {
					 //根据主键检测数据是否已经存在
					 Map<String, String> dataList = DBStructManager.getTableData(conn, tableName, privateKeyColumn, columnAndDataMap.get(privateKeyColumn));
					 if(dataList.size()>0) {	//更新
						 String errorInfo = DBStructManager.updateTableData(conn, tableName, columnAndDataMap, privateKeyColumn, columnAndDataMap.get(privateKeyColumn));
						 if(errorInfo!=null || !"".equals(errorInfo)) {
							 return handleTableDataSelect(request);
						 } else {
							 return DBManagerPage.getDBErrorPage(errorInfo);
						 }
					 } else {	//新增
						 String errorInfo = DBStructManager.insertTableData(conn, tableName, columnAndDataMap);
						 if(errorInfo!=null || !"".equals(errorInfo)) {
							 return handleTableDataSelect(request);
						 } else {
							 return DBManagerPage.getDBErrorPage(errorInfo);
						 }
					 }
				 } else {
					 return DBManagerPage.getDBErrorPage(tableName+"表因没有主键所以不支持更新或删除操作！");
				 }
			 }
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 /**
	  * 表格数据操作
	  * @param request	客户端请求对象
	  * @return 返回表格数据列表界面
	  */
	 private String handleTableDataSelect(HttpServletRequest request) { 
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 String tableName = request.getParameter("tableName");
			 if(tableName==null || "".equals(tableName)) {
				 tableName = mTableHashMap.get(request.getSession().getId());
			 }
			 
			 if(tableName!=null && !"".equals(tableName)) {
				 mTableHashMap.remove(request.getSession().getId());
				 //缓存当前的进入的表格
				 mTableHashMap.put(request.getSession().getId(), tableName);
				 //获取主键
				 String privateKeyColumn = DBStructManager.getKeyColumn(conn, tableName);
				 
				 List<Map<String, String>> columnAndDataList = DBStructManager.getTableAllColumnAndData(conn, tableName);
				 return DBManagerPage.getDBColumnAdnDataSelectPage(columnAndDataList, privateKeyColumn);
			 }
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 
	 /**
	  * 从表格选择列表返回到数据库选择列表
	  * @param request	客户端请求对象
	  * @return	返回数据库选择列表界面
	  */
	 private String handleTableBack(HttpServletRequest request) {
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 ArrayList<String> databaseList = DBStructManager.getAllDatabase(conn);
			 return DBManagerPage.getDBDabasesSelectPage(databaseList);
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 /**
	  * 从表列列表界面返回到表格列表界面
	  * @param request	客户端请求对象
	  * @return	返回表格列表界面
	  */
	 private String handleColumnBack(HttpServletRequest request) {
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 ArrayList<String> tableList = DBStructManager.getAllTable(conn);
			 return DBManagerPage.getDBTablesSelectPage(tableList);
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 /**
	  * 删除表列
	  * @param request	客户端请求对象
	  * @return	返回表列操作界面
	  */
	 private String handleColumnDrop(HttpServletRequest request) {
		 String tableName = mTableHashMap.get(request.getSession().getId());
		 if (tableName!=null) {
			 String columnName = request.getParameter("columnName");
			 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
			 if(conn!=null && columnName!=null) {
				 String errorInfo = DBStructManager.dropColumn(conn, tableName, columnName);
				 if(errorInfo==null || "".equals(errorInfo)) {
					 ArrayList<DBStructManagerModel> columnList = DBStructManager.getAllColumn(conn, tableName);
					 return DBManagerPage.getDBColumnSelectPage(columnList);
				 } else {
					 return DBManagerPage.getDBErrorPage(errorInfo);
				 }
			 }
		 }
		 return DBManagerPage.getDBErrorPage("表列删除失败！");
	 }
	 
	 /**
	  * 创建表列
	  * @param request	客户端请求对象
	  * @return	返回表列操作界面
	  */
	 private String handleColumnCreate(HttpServletRequest request) {
		 //从全局缓存中取出相应的表格
		 String tableName = mTableHashMap.get(request.getSession().getId());
		 if (tableName!=null) {
			 DBStructManagerModel dbsmm = new DBStructManagerModel();
			 String columnName = request.getParameter("columnName");
			 String columnType = request.getParameter("columnType");
			 String columnDefault = request.getParameter("columnDefault");
			 String columnIsNull = request.getParameter("columnIsNull");
			 String columnLength = request.getParameter("columnLength");
			 String columnPrimaryKey = request.getParameter("columnPrimaryKey");
			 
			 dbsmm.setTableName(tableName);
			 dbsmm.setColumnName(columnName);
			 dbsmm.setColumnType(columnType);
			 dbsmm.setColumnDefault(columnDefault);
			 dbsmm.setColumnIsNull(columnIsNull);
			 dbsmm.setColumnLength(columnLength);
			 dbsmm.setColumnPrimaryKey(columnPrimaryKey);
			 
			 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
			 if(conn!=null) {
				 boolean isExit = DBStructManager.columnIsExit(conn, tableName, columnName);
				 String errorInfo = "";
				 if (isExit) {
					 return DBManagerPage.getDBErrorPage("表列创建失败，\""+columnName+"\"表列已经存在！");
				 } else {
					 errorInfo = DBStructManager.insertColumn(conn, dbsmm);
				 }
				 if(errorInfo==null || "".equals(errorInfo)) {
					 ArrayList<DBStructManagerModel> columnList = DBStructManager.getAllColumn(conn, tableName);
					 return DBManagerPage.getDBColumnSelectPage(columnList);
				 } else {
					 return DBManagerPage.getDBErrorPage(errorInfo);
				 }
			 }
		 }
		 return DBManagerPage.getDBErrorPage("表列创建失败！");
	 }
	 
	 /**
	  * 处理表格选择操作
	  * @param request	客户端请求对象
	  * @return	返回表格列表操作界面
	  */
	 private String handleTableSelect(HttpServletRequest request) {
		 String tableName = request.getParameter("tableName");
		 
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 mTableHashMap.remove(request.getSession().getId());
			 //缓存当前的进入的表格
			 mTableHashMap.put(request.getSession().getId(), tableName);
			 
			 ArrayList<DBStructManagerModel> columnList = DBStructManager.getAllColumn(conn, tableName);
			 return DBManagerPage.getDBColumnSelectPage(columnList);
		 }
		 return DBManagerPage.getDBErrorPage("表格进入失败！");
	 }
	 
	 /**
	  * 处理表格删除操作
	  * @param request	客户端请求对象
	  * @return	返回表格列表操作界面
	  */
	 private String handleTableDrop(HttpServletRequest request) {
		 String tableName = request.getParameter("tableName");
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 boolean bFlag = DBStructManager.dropTable(conn, tableName);
			 if(bFlag) {
				 ArrayList<String> tableList = DBStructManager.getAllTable(conn);
				 return DBManagerPage.getDBTablesSelectPage(tableList);
			 }
		 }
		 return DBManagerPage.getDBErrorPage("表格删除失败！");
	 }
	 
	 /**
	  * 处理表格创建操作
	  * @param request	客户端请求对象
	  * @return	返回表格列表操作界面
	  */
	 private String handleTableCreate(HttpServletRequest request) {
		 String tableName = request.getParameter("tableName");
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 boolean isExit = DBStructManager.tableIsExit(conn, tableName);
			 if(!isExit) {
				 String errorInfo = DBStructManager.createTable(conn, tableName);
				 if(errorInfo==null || "".equals(errorInfo)) {
					 ArrayList<String> tableList = DBStructManager.getAllTable(conn);
					 return DBManagerPage.getDBTablesSelectPage(tableList);
				 } else {
					 return DBManagerPage.getDBErrorPage(errorInfo);
				 }
			 } else {
				 return DBManagerPage.getDBErrorPage("表格已经存在！");
			 }
			 
		 }
		 return DBManagerPage.getDBErrorPage("表格创建失败！");
	 }
	 
	 /**
	  * 处理数据库删除操作	
	  * @param request	客户端请求对象
	  * @return 返回数据库列表操作界面
	  */
	 private String handleDatabasesDrop(HttpServletRequest request) {
		 String databaseName = request.getParameter("databaseName");
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 boolean isExit = DBStructManager.databaseIsExit(conn, databaseName);
			 if(isExit) {
				 String errorInfo = DBStructManager.dropDatabase(conn, databaseName);
				 if (errorInfo==null || "".equals(errorInfo)) {
					 ArrayList<String> databaseList = DBStructManager.getAllDatabase(conn);
					 return DBManagerPage.getDBDabasesSelectPage(databaseList);
				 } else {
					 return DBManagerPage.getDBErrorPage(errorInfo);
				 }
			 }
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 /**
	  * 处理数据库创建操作
	  * @param request	客户端请求对象
	  * @return 返回数据库列表操作界面
	  */
	 private String handleDatabasesCreate(HttpServletRequest request) {
		 String databaseName = request.getParameter("databaseName");
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 String errorInfo = DBStructManager.createDatabase(conn, databaseName);
			 if (errorInfo==null || "".equals(errorInfo)) {
				 ArrayList<String> databaseList = DBStructManager.getAllDatabase(conn);
				 return DBManagerPage.getDBDabasesSelectPage(databaseList);
			 } else {
				 return DBManagerPage.getDBErrorPage(errorInfo);
			 }
			 
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 
	 /**
	  * 处理数据库选择操作
	  * @param request	客户端请求对象
	  * @return	返回数据库列表操作界面
	  */
	 private String handleDatabasesSelect(HttpServletRequest request) {
		 String databaseName = request.getParameter("databaseName");
		 Connection conn = DBConnection.getConnectionByKey(request.getSession().getId());
		 if(conn!=null) {
			 ArrayList<String> tableList = DBStructManager.getAllTable(conn, databaseName);
			 return DBManagerPage.getDBTablesSelectPage(tableList);
		 }
		 return DBManagerPage.getDBErrorPage("请先登录！");
	 }
	 /**
	  * 处理数据库登录操作
	  * @param request		客户端请求对象
	  * @return 返回登录成功或登录失败的界面
	  */
	 private String handleDatabasesLogin(HttpServletRequest request) {
		 //处理请求参数
		 String userName = request.getParameter("userName");		
		 String userPassword = request.getParameter("userPassword");
		 String dbTypeFull = request.getParameter("dbType");
		 String dbIP = request.getParameter("dbIP");
		 String dbPort = request.getParameter("dbPort");
		 String dbDirver = dbTypeFull.split(";")[0];
		 String dbUrl = dbTypeFull.split(";")[1]+dbIP+":"+dbPort+"/";
		 
		 //获取数据库连接通道
		 Connection conn = DBConnection.openConnection(request.getSession().getId(), 
				 										dbDirver, 
				 										dbUrl, 
				 										userName, 
				 										userPassword);
		 if(conn!=null) {
			 ArrayList<String> databaseList = DBStructManager.getAllDatabase(conn);
			 return DBManagerPage.getDBDabasesSelectPage(databaseList);
		 } else {
			 String message = DBConnection.getStackTraceByKey(request.getSession().getId());
			 if(message!=null && !"".equals(message)) {
				 return DBManagerPage.getDBErrorPage(message);
			 }
		 }
		 
		 return DBManagerPage.getDBErrorPage("请核对你的登录信息！");
	 }
	 
	 public static String getTimeoutPage() {
		 return DBManagerPage.getDBTimeoutPage();
	 }
	 
	 /**
	  * 清空全局缓存数据
	  * @param key	关键字
	  */
	 public static void removeInfoByKey(String key) {
		 //清空缓存的表格记录
		 mTableHashMap.remove(key);
	 }
}

