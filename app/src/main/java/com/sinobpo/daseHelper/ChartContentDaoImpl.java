package com.sinobpo.daseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.sinobpo.util.ChatContent;
import com.sinobpo.util.Constant;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Copyright (C) 2012 移动应用研发组-企业通讯录 版本： 日期： 描述：通话记录 数据库表操作类 操作：添加
 * 操作人：移动应用研发组-gxc
 */
public class ChartContentDaoImpl {
	private MyOpenHelper mDatabaseHelper;

	public ChartContentDaoImpl(Context con) {
		this.mDatabaseHelper = new MyOpenHelper(con);
	}
	
	/**
	 * 判断是否已经跟该好友聊过天了
	 * @param toIP 发送方
	 * @param fromIP 接收方
	 * @return 
	 */
	public String getChatID(String toIP,String fromIP){
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Cursor cursor=db.rawQuery("select chatID from "+ Constant.TABLE_NAME_OF_CHARTCONTENT+" where (toIP = '"+ toIP+"' and fromip = '"+fromIP+"')or (toip = '"+fromIP+"' and fromip = '"+toIP+"')" , null);
		String chatId="";
		if(cursor.moveToFirst()){
			chatId=cursor.getString(0);
		}
		cursor.close();
		db.close();
		return chatId;
	}
	
	/**
	 * 获取chatID的最大值
	 * @return
	 */
	public String getMaxChatID(){
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Cursor cursor=db.rawQuery("select count(chatID)+1 from "+Constant.TABLE_NAME_OF_CHARTCONTENT, null);		
		String maxChatID="1";		
		if(cursor.moveToFirst()){
			maxChatID=cursor.getString(0);
		}
		cursor.close();
		db.close();
		
		return maxChatID;
	}
	
	/**
	 * 向聊天记录表添加数据；
	 * @param person
	 */
	public void saveToCallLog(ChatContent chatContent){
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Date date = new Date(System.currentTimeMillis());
		String time=new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").format(date);
		String sql="insert into "+Constant.TABLE_NAME_OF_CHARTCONTENT+ " (fromIP,toIP,time,body,chatID,imgHead,name,Tag,filePath) values (?,?,?,?,?,?,?,?,?) ";
		
		db.execSQL(sql,new Object[]{chatContent.getFromIP(),chatContent.getToIP(),chatContent.getBody(),chatContent.getTime(),time
				,chatContent.getImgHead(),chatContent.getName(),chatContent.getTag(),chatContent.filePath});
		db.close();
	}
	

	
	/**
	 * 取出聊天记录中所有数据
	 * @return 
	 */
	public ArrayList<ChatContent> findChartLog(){
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		ArrayList<ChatContent> arraylist=new ArrayList<ChatContent>();
		String sql="select * from "+Constant.TABLE_NAME_OF_CHARTCONTENT +" order by chatID";
		Cursor cursor=db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			ChatContent chartContent=new ChatContent();
			chartContent.setFromIP(cursor.getString(cursor.getColumnIndex("fromIP")));
			chartContent.setToIP(cursor.getString(cursor.getColumnIndex("toIP")));
			chartContent.setBody(cursor.getString(cursor.getColumnIndex("time")));
			chartContent.setTime(cursor.getString(cursor.getColumnIndex("body")));
			chartContent.setChatID(cursor.getString(cursor.getColumnIndex("chatID")));
			chartContent.setImgHead(cursor.getInt(cursor.getColumnIndex("imgHead")));
			chartContent.setName(cursor.getString(cursor.getColumnIndex("name")));
			chartContent.setTag(cursor.getString(cursor.getColumnIndex("Tag")));
			chartContent.setFilePath(cursor.getString(cursor.getColumnIndex("filePath")));
			arraylist.add(chartContent);
		}
		cursor.close();
		db.close();
		return arraylist;
	}
	
	/**
	 * 取出聊天记录中所有数据
	 * @return 
	 */
	public ArrayList<ChatContent> getDataByChatId(String chatId){
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		String sql="select * from "+Constant.TABLE_NAME_OF_CHARTCONTENT +" where chatID='"+chatId+"' order by chatID Limit 15";
		Cursor cursor=db.rawQuery(sql, null);
		ArrayList<ChatContent> arraylist=new ArrayList<ChatContent>();
		while(cursor.moveToNext()){
			ChatContent chartContent=new ChatContent();
			chartContent.setFromIP(cursor.getString(cursor.getColumnIndex("fromIP")));
			chartContent.setToIP(cursor.getString(cursor.getColumnIndex("toIP")));
			chartContent.setBody(cursor.getString(cursor.getColumnIndex("time")));
			chartContent.setTime(cursor.getString(cursor.getColumnIndex("body")));
			chartContent.setChatID(cursor.getString(cursor.getColumnIndex("chatID")));
			chartContent.setImgHead(cursor.getInt(cursor.getColumnIndex("imgHead")));
			chartContent.setName(cursor.getString(cursor.getColumnIndex("name")));
			chartContent.setTag(cursor.getString(cursor.getColumnIndex("Tag")));
			chartContent.setFilePath(cursor.getString(cursor.getColumnIndex("filePath")));
			arraylist.add(chartContent);
		}
		cursor.close();
		db.close();
		return arraylist;
	}
	
	/**
	 * 取出聊天记录中所有数据
	 * @return 
	 */
	public ArrayList<ChatContent> getData(String toIP,String fromIP){
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		String sql="select * from "+Constant.TABLE_NAME_OF_CHARTCONTENT +" where (toIP = '"+ toIP+"' and fromip = '"+fromIP+"')or (toip = '"+fromIP+"' and fromip = '"+toIP+"')"  +" order by chatID  asc Limit 15";
		Cursor cursor=db.rawQuery(sql, null);
		ArrayList<ChatContent> arraylist=new ArrayList<ChatContent>();
		while(cursor.moveToNext()){
			ChatContent chartContent=new ChatContent();
			chartContent.setFromIP(cursor.getString(cursor.getColumnIndex("fromIP")));
			chartContent.setToIP(cursor.getString(cursor.getColumnIndex("toIP")));
			chartContent.setBody(cursor.getString(cursor.getColumnIndex("time")));
			chartContent.setTime(cursor.getString(cursor.getColumnIndex("body")));
			chartContent.setChatID(cursor.getString(cursor.getColumnIndex("chatID")));
			chartContent.setImgHead(cursor.getInt(cursor.getColumnIndex("imgHead")));
			chartContent.setName(cursor.getString(cursor.getColumnIndex("name")));
			chartContent.setTag(cursor.getString(cursor.getColumnIndex("Tag")));
			chartContent.setFilePath(cursor.getString(cursor.getColumnIndex("filePath")));
			arraylist.add(chartContent);
		}
		cursor.close();
		db.close();
		return arraylist;
	}
	
	
	/**
	 * 清空表
	 */
	public void deleteAllDate(){
		System.out.println("000000000");
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		String sql="delete from "+Constant.TABLE_NAME_OF_CHARTCONTENT;
		db.execSQL(sql);
		db.close();
	}
}
