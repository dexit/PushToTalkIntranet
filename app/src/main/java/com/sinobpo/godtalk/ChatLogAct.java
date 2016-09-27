package com.sinobpo.godtalk;

import java.util.ArrayList;

import com.sinobpo.R;
import com.sinobpo.adapter.Adapter_LV_ChatLog;
import com.sinobpo.daseHelper.ChartContentDaoImpl;
import com.sinobpo.util.ChatContent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;

/**
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ڵ���
 * �汾��
 * ���ڣ�
 * �����������¼
 * ���������/����/�޸�
 * �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class ChatLogAct extends Activity{

	private ListView lv_chat_log;
	private String chatID;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_chat_log);
		
		Intent intent=getIntent();
		chatID=intent.getStringExtra("chatID");
		
		init();
	}
	
	private void init(){
		lv_chat_log=(ListView)findViewById(R.id.chat_log_list_CLAct);
		
		Intent intent=getIntent();
		
		String toIP=intent.getStringExtra("meIP");
		String fromIP=intent.getStringExtra("personIP");
		ChartContentDaoImpl daoImpl=new ChartContentDaoImpl(this);
		
		ArrayList<ChatContent> list_data=daoImpl.getData(toIP, fromIP);
		
		System.out.println("list_data.size="+list_data.size());
		
		Adapter_LV_ChatLog adapter=new Adapter_LV_ChatLog(this, list_data);
		lv_chat_log.setAdapter(adapter);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// ���µ������BACK��ͬʱû���ظ�
			// DO SOMETHING
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
