package com.sinobpo.godtalk;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sinobpo.R;
import com.sinobpo.util.FileName;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ڵ���
 * �汾��
 * ���ڣ�
 * �����������ļ�
 * �������޸�
 * �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class ReceiveFileBrowserActivity extends ListActivity implements OnClickListener{
	private List<FileName> filePaths = new ArrayList<FileName>();//���浱ǰĿ¼�µ������ļ����ļ�·��
	private String defaultPath="//sdcard/shenliao-sinobpo";
	private ReceiveFileBrowserAdapter adapter = null;
	private Button btnBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receive_file_browser_activity);
		btnBack=(Button)findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);
		
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){//�ж�SD���Ƿ����
			File mkFile=new File("//sdcard/shenliao");
			if(!mkFile.exists()){
				mkFile.mkdir(); //�����ļ���
			}
		}
		
		getFileDir(defaultPath);
	}
	
//	�������¼�
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnBack://����
			ReceiveFileBrowserActivity.this.finish();
			break;
		}
	}
	
	/**
	 * ���б��е���Ŀ�����ʱ�ᴥ�����¼�
	 */
	@Override
	protected void onListItemClick(ListView listView, View itemView, int position, long id) {
		File file = new File(filePaths.get(position).fileName);//�����List�б���������item����Ӧ���ļ�
		if (file.isDirectory()) {//������ļ�ΪĿ¼�ļ���򿪸�Ŀ¼
			return;
		} else {
			openFile(filePaths.get(position).fileName);
		}
	}
	
	/**
	 * @param filePath ��Ҫ�򿪵�Ŀ¼·��
	 * �򿪸�Ŀ¼���������������ļ���Ϣ������Ŀ¼���ļ�
	 * ���������ļ��������fileNames�б��У��������ļ�·�������filePaths�б���
	 */
	private void getFileDir(String filePath) {
		if(null==filePath)return;//����ǲ��ǳ����˸�Ŀ¼
		File dirFile = new File(filePath);
		File[] files = dirFile.listFiles();//��ȡ��ǰĿ¼�µ������ļ�
		if(null!=files){
			filePaths.clear();
			for (File file : files) {
				if(file.isDirectory()){
					FileName fPath = new FileName(1,file.getPath());
					filePaths.add(fPath);
				}else{
					FileName fPath = new FileName(2,file.getPath(),file.length(),false,null,file.getPath());
					filePaths.add(fPath);
				}
			}
			Collections.sort(filePaths);//�������򣬰��ļ�������ǰ�棬�ļ����ں���
			if(null==adapter){
				adapter = new ReceiveFileBrowserAdapter(this,filePaths);
			}else{
				adapter.setDatasource(filePaths);
			}
			setListAdapter(adapter);//�ѻ�õ��ļ���Ϣ����List���������������������б���Ŀ
		}
	}
	
	/** ���ļ� **/
	private void openFile(String path) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		File f = new File(path);
		String type =getMIMEType(f.getName());
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}
	
	/** ��ȡMIME���� **/
	public static String getMIMEType(String name) {
		String type = "";
		String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
		if (end.equals("apk")) {
			return "application/vnd.android.package-archive";
		} else if (end.equals("mp4") || end.equals("avi") || end.equals("3gp")
				|| end.equals("rmvb")) {
			type = "video";
		} else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf")
				|| end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("txt") || end.equals("log")) {
			type = "text";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}
}
