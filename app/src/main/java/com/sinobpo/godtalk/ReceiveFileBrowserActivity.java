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
 * Copyright (C) 2012 移动应用研发组-室内导览
 * 版本：
 * 日期：
 * 描述：已收文件
 * 操作：修改
 * 操作人：移动应用研发组-gxc
 */
public class ReceiveFileBrowserActivity extends ListActivity implements OnClickListener{
	private List<FileName> filePaths = new ArrayList<FileName>();//保存当前目录下的所有文件的文件路径
	private String defaultPath="//sdcard/shenliao-sinobpo";
	private ReceiveFileBrowserAdapter adapter = null;
	private Button btnBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receive_file_browser_activity);
		btnBack=(Button)findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);
		
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){//判断SD卡是否存在
			File mkFile=new File("//sdcard/shenliao");
			if(!mkFile.exists()){
				mkFile.mkdir(); //创建文件夹
			}
		}
		
		getFileDir(defaultPath);
	}
	
//	组件点击事件
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnBack://返回
			ReceiveFileBrowserActivity.this.finish();
			break;
		}
	}
	
	/**
	 * 当列表中的条目被点击时会触发该事件
	 */
	@Override
	protected void onListItemClick(ListView listView, View itemView, int position, long id) {
		File file = new File(filePaths.get(position).fileName);//获得在List中被点击的这个item所对应的文件
		if (file.isDirectory()) {//如果该文件为目录文件则打开该目录
			return;
		} else {
			openFile(filePaths.get(position).fileName);
		}
	}
	
	/**
	 * @param filePath 需要打开的目录路径
	 * 打开该目录并获得里面的所有文件信息，包括目录和文件
	 * 并把所有文件名存放在fileNames列表中，把所有文件路径存放在filePaths列表中
	 */
	private void getFileDir(String filePath) {
		if(null==filePath)return;//检测是不是超出了根目录
		File dirFile = new File(filePath);
		File[] files = dirFile.listFiles();//提取当前目录下的所有文件
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
			Collections.sort(filePaths);//进行排序，把文件夹排在前面，文件排在后面
			if(null==adapter){
				adapter = new ReceiveFileBrowserAdapter(this,filePaths);
			}else{
				adapter.setDatasource(filePaths);
			}
			setListAdapter(adapter);//把获得的文件信息传给List适配器，让适配器更新列表条目
		}
	}
	
	/** 打开文件 **/
	private void openFile(String path) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		File f = new File(path);
		String type =getMIMEType(f.getName());
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}
	
	/** 获取MIME类型 **/
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
