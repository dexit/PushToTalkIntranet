package com.sinobpo.daseHelper;

import com.sinobpo.util.Constant;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.provider.SyncStateContract.Constants;

/**
 * Copyright (C) 2012 �ƶ�Ӧ���з���-��ҵͨѶ¼
 * �汾��
 * ���ڣ�
 * ���������ݿ⣨�ҵ��ղأ�ͨ����¼��
 * ���������
 * �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class MyOpenHelper extends SQLiteOpenHelper{
	
	
	final static String CREATE_TABLE_SQL_COLLECT = "create table if not exists "
		+ Constant.TABLE_NAME_OF_CHARTCONTENT
		+ "  (_id integer primary key,fromIP,toIP,time,body,chatID,imgHead,name,Tag,filePath)";
	
	
	public MyOpenHelper(Context context, String name, CursorFactory factory,
			int version){
		super(context, name, null, version);
	}
	
	public MyOpenHelper(Context context){
		super(context, Constant.DATA_COLLECT, null, Constant.DATEBASEVERSION);
		System.out.println("+++++++++++");
	}
	
	/**
	 * ���ݿⱻ����ʱ�����ã�ֻ����һ��
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TABLE_SQL_COLLECT);
		System.out.println("create guo.db ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
