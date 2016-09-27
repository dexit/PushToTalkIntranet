package com.sinobpo.godtalk;

import com.sinobpo.R;
import com.sinobpo.service.MainService;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

/**
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ڵ���
 * �汾��
 * ���ڣ�
 * �������Զ���dialog(����������ֺ���ĺ��а�ť������dialog)
 * �������޸�
 * �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class CallingDialog extends Dialog implements OnClickListener{
	private Button btnCallingOk = null;
	private Context context = null;
	private MainService service=null;
	String ipAddress=null;
	
	public CallingDialog(Context context) {
		super(context);
		this.context=context;
	}
	
	public CallingDialog(Context context,String ipAddress){
		super(context);
		this.context=context;
		this.ipAddress=ipAddress;
	}
	
	public CallingDialog(Context context,MainService service,String ipAddress){
		super(context);
		this.context=context;
		this.service=service;
		this.ipAddress=ipAddress;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.godtalk_calling_dialog_item);
		this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		btnCallingOk=(Button)findViewById(R.id.btnCallingOk);
		if(service==null){//˵���ǽ�������
			btnCallingOk.setText("����IPΪ"+ipAddress+"�ĺ���");
			btnCallingOk.setOnClickListener(this);
		}else{//����ִ��¼��
			btnCallingOk.setOnTouchListener(callingKeyListener);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnCallingOk://����˵���������þͿ���ʹdialog��ʧ
//			service.stopCalling();
			break;
		}
		dismiss();
	}
	
	private OnTouchListener callingKeyListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				service.startCalling(ipAddress);
				break;
			case MotionEvent.ACTION_UP:
				service.stopCalling();
				dismiss();
				break;
			}
			return false;
		}
	};
}
