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
 * Copyright (C) 2012 移动应用研发组-室内导览
 * 版本：
 * 日期：
 * 描述：自定义dialog(点击好友名字后面的呼叫按钮弹出的dialog)
 * 操作：修改
 * 操作人：移动应用研发组-gxc
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
		if(service==null){//说明是接收语音
			btnCallingOk.setText("来自IP为"+ipAddress+"的呼叫");
			btnCallingOk.setOnClickListener(this);
		}else{//否则执行录音
			btnCallingOk.setOnTouchListener(callingKeyListener);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnCallingOk://这里说明都不调用就可以使dialog消失
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
