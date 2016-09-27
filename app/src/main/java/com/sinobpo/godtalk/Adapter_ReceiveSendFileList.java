package com.sinobpo.godtalk;

import java.util.ArrayList;

import com.sinobpo.R;
import com.sinobpo.util.Constant;
import com.sinobpo.util.FileState;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Copyright (C) 2012 移动应用研发组-神聊对讲机
 * 版本：
 * 日期：
 * 描述：发送文件Dialog指定的布局
 * 操作：修改
 * 操作人：移动应用研发组-gxc
 */
public class Adapter_ReceiveSendFileList extends BaseAdapter{
	private ArrayList<FileState> receivedFileNames = null;//接收到的对方传过来的文件名 
	private Context context = null;
	/**0:发送文件 1：接收文件*/
	private int  status=-1;
	
	public Adapter_ReceiveSendFileList(Context context,int status){
		this.context = context;
		this.status=status;
	}

	@Override
	public int getCount() {
		return receivedFileNames.size();
	}

	@Override
	public Object getItem(int position) {
		return receivedFileNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View vi = inflater.inflate(R.layout.send_receive_file_layout, null);
		ImageView fileIcon = (ImageView)vi.findViewById(R.id.file_icon);
		TextView fileName = (TextView)vi.findViewById(R.id.file_name);
		TextView fileSize = (TextView)vi.findViewById(R.id.file_size);
		ProgressBar bar = (ProgressBar)vi.findViewById(R.id.file_progress);
		TextView filePercent = (TextView)vi.findViewById(R.id.file_percent);
		
		FileState fs = receivedFileNames.get(position);//获得一个文件状态信息
		String ext = fs.fileName.substring(fs.fileName.lastIndexOf(".")+1);
		Integer srcId = Constant.exts.get(ext);
		if(null == srcId)srcId = R.drawable.gdoc;
		fileIcon.setImageResource(srcId);
		
		fileName.setText(fs.fileName);
		fileSize.setText(Constant.formatFileSize(fs.fileSize));
		bar.setMax(100);
		bar.setProgress(fs.percent);
		filePercent.setText(Constant.formatFileSize(fs.currentSize)+"/"+Constant.formatFileSize(fs.fileSize));
		if(status==0){
			if(fs.percent==100){
				filePercent.setTextColor(Color.GREEN);
				Toast.makeText(context, "文件发送受完毕！", Toast.LENGTH_SHORT).show();
				Constant.sendFileIsOver=true; 
			}
		}else if(status==1){
			if(fs.percent==100){
				filePercent.setTextColor(Color.GREEN);
				Toast.makeText(context, "文件已接受完毕！", Toast.LENGTH_SHORT).show();
				Constant.sendFileIsOver=true; 
			}
		
		}
		
		return vi;
	}
	
	public void setResources(ArrayList<FileState> receivedFileNames){
		this.receivedFileNames = receivedFileNames;
	}
}

