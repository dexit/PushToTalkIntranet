package com.sinobpo.adapter;

import java.util.ArrayList;

import com.sinobpo.R;
import com.sinobpo.util.ChatContent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Copyright (C) 2012 移动应用研发组-神聊对加机
 * 版本：
 * 日期：
 * 描述：测试 查看聊天记录
 * 操作：添加
 * 操作人：移动应用研发组-gxc
 */
public class Adapter_LV_ChatLog extends BaseAdapter{

	public ArrayList<ChatContent> list_data;
	public Context con;
	private LayoutInflater inflater;
	
	public Adapter_LV_ChatLog(Context con,ArrayList<ChatContent> list_data){
		this.con=con;
		this.list_data=list_data;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list_data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View myView=null;
		if(convertView==null){
			myView=inflater.from(con).inflate(R.layout.item_chat_log, null);
		}else{
			myView=convertView;
		}
		
		TextView tv_toIP=(TextView)myView.findViewById(R.id.tv_item_chat_log_toIP);
		TextView tv_fromIP=(TextView)myView.findViewById(R.id.tv_item_chat_log_fromIP);
		TextView tv_time=(TextView)myView.findViewById(R.id.tv_item_chat_log_time);
		TextView tv_chatID=(TextView)myView.findViewById(R.id.tv_item_chat_log_chatID);
		TextView tv_body=(TextView)myView.findViewById(R.id.tv_item_chat_log_body);
		TextView tv_filePath=(TextView)myView.findViewById(R.id.tv_item_chat_log_filePath);
		
		tv_toIP.setText(list_data.get(position).getToIP());
		tv_fromIP.setText(list_data.get(position).getFromIP());
		tv_time.setText(list_data.get(position).getTime().toString());
		tv_chatID.setText(list_data.get(position).getChatID().toString());
		tv_body.setText(list_data.get(position).getBody().toString());
		tv_filePath.setText(list_data.get(position).getFilePath().toString());
		return myView;
	}

}
