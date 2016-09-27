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
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ĶԽ���
 * �汾��
 * ���ڣ�
 * �����������ļ�Dialogָ���Ĳ���
 * �������޸�
 * �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class Adapter_ReceiveSendFileList extends BaseAdapter{
	private ArrayList<FileState> receivedFileNames = null;//���յ��ĶԷ����������ļ��� 
	private Context context = null;
	/**0:�����ļ� 1�������ļ�*/
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
		
		FileState fs = receivedFileNames.get(position);//���һ���ļ�״̬��Ϣ
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
				Toast.makeText(context, "�ļ���������ϣ�", Toast.LENGTH_SHORT).show();
				Constant.sendFileIsOver=true; 
			}
		}else if(status==1){
			if(fs.percent==100){
				filePercent.setTextColor(Color.GREEN);
				Toast.makeText(context, "�ļ��ѽ�����ϣ�", Toast.LENGTH_SHORT).show();
				Constant.sendFileIsOver=true; 
			}
		
		}
		
		return vi;
	}
	
	public void setResources(ArrayList<FileState> receivedFileNames){
		this.receivedFileNames = receivedFileNames;
	}
}

