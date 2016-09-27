package com.sinobpo.godtalk;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sinobpo.R;
import com.sinobpo.util.Constant;
import com.sinobpo.util.FileName;
import com.sinobpo.util.ShakeListener;
import com.sinobpo.util.ShakeListener.OnShakeListener;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * Copyright (C) 2012 移动应用研发组-神聊对讲机 版本： 日期： 描述：选择文件发送界面（Dialog样式） 操作：添加/整理/修改
 * 操作人：移动应用研发组-gxc
 */
public class FileManagerActivity extends ListActivity implements
		OnClickListener {
	/** 保存当前目录下的所有文件的文件路径 */
	private List<FileName> filePaths = new ArrayList<FileName>();
	/** 根目录路径 */
	private String rootPath = "/";
	/** 初始化上级目录路径 */
	private String parentPath = "/";
	private String defaultPath = "//sdcard/shenliao-sinobpo";
	/** 目录选择 “上一级” */
	private Button btnReturnRoot = null;
	/** 目录选择 “下一级” */
	private Button btnReturnParent = null;
	/** 分类布局 */
	private RelativeLayout linLaySort = null;
	private RadioGroup radio_group;
	/** 按钮“摇一摇” */
	private Button btnShake = null;
	/** 按钮“确定”（当当前是接受文件时显示） */
	private Button btnConfirm = null;
	/** 保存被选择的所有文件路径 */
	private ArrayList<FileName> selectedFilePath = new ArrayList<FileName>();
	/** 用来显示当前目录路径(接受文件时显示) */
	private TextView mPath;
	/** 当前路径 */
	private String currentPath = null;
	private int selectType = 0;
	/** 接受传递过来的类型值 图片、视频等 */
	private String type;
	private MyFileAdapter adapter = null;
	ContentResolver resolver;
	File dirFile = new File(Environment.getExternalStorageDirectory()
			.getAbsolutePath());

	/** 进度条 */
	private ProgressBar pb;
	/***/
	private Button btn_overlay;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// 判断"//sdcard/shenliao-sinobpo"目录是否存在
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File mkFile = new File("//sdcard/shenliao-sinobpo");
			if (!mkFile.exists()) {
				mkFile.mkdir(); // 创建文件夹
			}
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.file_manager_activity);
		
		linLaySort=(RelativeLayout)findViewById(R.id.linLaySort);
		radio_group = (RadioGroup) findViewById(R.id.radio_group_FMAct);
		pb = (ProgressBar) findViewById(R.id.pb_FMAct);
		btn_overlay=(Button)findViewById(R.id.btn_overlay_FMAct);
		btn_overlay.getBackground().setAlpha(0);
		prepareListener();

		resolver = getContentResolver();

		if (null == adapter) {
			adapter = new MyFileAdapter(FileManagerActivity.this, filePaths);
		} else {
			adapter.setDatasource(filePaths);
		}
		setListAdapter(adapter);// 把获得的文件信息传给List适配器，让适配器更新列表条目

		Intent intent = getIntent();
		selectType = intent.getExtras().getInt("selectType");
		type = intent.getExtras().getString("type");

		btnShake = (Button) findViewById(R.id.btnShake);
		btnConfirm = (Button) findViewById(R.id.btnConfirm);
		// 摇晃手机发送文件
		ShakeListener shakeListener = new ShakeListener(
				FileManagerActivity.this); // 创建一个对象
		shakeListener.setOnShakeListener(new OnShakeListener() { // 调用setOnShakeListener方法进行监听
					public void onShake() {
						Intent intent = new Intent();
						// Bundle bundle=new Bundle();
						if (selectType == Constant.SELECT_FILES) {// 如果当前为选择文件模式则返回当前选择的所有文件
							intent
									.putExtra("selectType",
											Constant.SELECT_FILES);
							intent.putExtra("files", selectedFilePath);
						}
						setResult(RESULT_OK, intent);
						finish();
					}
				});

		// 确定选择的文件路径
		btnConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				if (selectType == Constant.SELECT_FILE_PATH) {// 如果当前为文件夹选择模式则返回当前选择的文件夹路径
					File file = new File(currentPath);
					intent.putExtra("selectType", Constant.SELECT_FILE_PATH);
					if (file.canWrite()) {
						intent.putExtra("fileSavePath", currentPath);
					}
				}
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		btnReturnRoot = (Button) findViewById(R.id.return_root_path);
		btnReturnRoot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View returnRootBtn) {
				getFileDir(rootPath);
			}
		});
		btnReturnParent = (Button) findViewById(R.id.return_parent_path);
		btnReturnParent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View returnParentBtn) {
				getFileDir(parentPath);
			}
		});

		mPath = (TextView) findViewById(R.id.mPath);
		TextView title = (TextView) findViewById(R.id.file_select_title);
		if (selectType == Constant.SELECT_FILE_PATH) {//选择用来接收文件的文件夹
			title.setText(getString(R.string.select_path_for_save));
			getFileDir(defaultPath);
			linLaySort.setVisibility(View.GONE);
			btnReturnRoot.setVisibility(View.VISIBLE);
			btnReturnParent.setVisibility(View.VISIBLE);
			btnConfirm.setVisibility(View.VISIBLE);
			btnShake.setVisibility(View.GONE);
		} else {//选择要发送的文件
			title.setText(getString(R.string.select_file_for_send));
			btnReturnRoot.setVisibility(View.GONE);
			btnReturnParent.setVisibility(View.GONE);
			btnConfirm.setVisibility(View.GONE);
			btnShake.setVisibility(View.VISIBLE);
			linLaySort.setVisibility(View.VISIBLE);
			btnShake.setOnClickListener(this);
			new initData().execute(type);
			
			//初始化按钮背景
			if(type.equals("image")){
				radio_group.check(radio_group.getChildAt(0).getId());
			}else if(type.equals("audio")){
				radio_group.check(radio_group.getChildAt(1).getId());
			}else if(type.equals("apk")){
				radio_group.check(radio_group.getChildAt(3).getId());
			}else if(type.equals("media")){
				radio_group.check(radio_group.getChildAt(2).getId());
			}else{
				radio_group.check(radio_group.getChildAt(4).getId());
			}
		}
	}

	// 更新列表
	Handler handlerSearch = new Handler() {
		public void handleMessage(Message msg) {

		};
	};

	/**
	 * 分类按钮监听事件
	 */
	public void prepareListener() {

		radio_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.btnSendImage:
					 filePaths.clear();
					 selectedFilePath.clear();
					 Constant.fileSelectedState.clear();
					 new initData().execute("image");
					break;
				case R.id.btnSendAudio:
					 filePaths.clear();
					 selectedFilePath.clear();
					 Constant.fileSelectedState.clear();
					 new initData().execute("audio");
					break;
				case R.id.btnSendMedia:
					 filePaths.clear();
					 selectedFilePath.clear();
					 Constant.fileSelectedState.clear();
					 new initData().execute("media");
					break;
				case R.id.btnSendApk:
					 filePaths.clear();
					 selectedFilePath.clear();
					 Constant.fileSelectedState.clear();
					 new initData().execute("apk");
					break;
				case R.id.radiobtn_received:
					 filePaths.clear();
					 selectedFilePath.clear();
					 Constant.fileSelectedState.clear();
					 new initData().execute("received");
					break;
				}
			}

		});
	}

	/**
	 * @param filePath
	 *            需要打开的目录路径 打开该目录并获得里面的所有文件信息，包括目录和文件
	 *            并把所有文件名存放在fileNames列表中，把所有文件路径存放在filePaths列表中
	 */
	private void getFileDir(String filePath) {
		if (null == filePath)
			return;// 检测是不是超出了根目录
		File dirFile = new File(filePath);
		parentPath = dirFile.getParent();// 获得当前目录的父目录
		File[] files = dirFile.listFiles();// 提取当前目录下的所有文件
		if (null != files) {
			filePaths.clear();
			selectedFilePath.clear();
			currentPath = filePath;
			Constant.fileSelectedState.clear();
			mPath.setText(getString(R.string.current_path_label) + filePath);
			for (File file : files) {
				if (selectType == Constant.SELECT_FILE_PATH) {// 如果选择模式为文件夹模式则只获得文件夹
					if (file.isDirectory()) {
						FileName fPath = new FileName(1, file.getPath());
						filePaths.add(fPath);
					}
				} else {// 如果选择模式为文件模式则获得所有文件夹与文件
					if (file.isDirectory()) {
						FileName fPath = new FileName(1, file.getPath());
						filePaths.add(fPath);
					} else {
						FileName fPath = new FileName(2, file.getPath(), file
								.length(), false, null, file.getPath());
						filePaths.add(fPath);
					}
				}
			}
			Collections.sort(filePaths);// 进行排序，把文件夹排在前面，文件排在后面
			if (null == adapter) {
				adapter = new MyFileAdapter(this, filePaths);
			} else {
				adapter.setDatasource(filePaths);
			}
			setListAdapter(adapter);// 把获得的文件信息传给List适配器，让适配器更新列表条目
		}
	}

	/**
	 * 获得图片方法，dirFile为文件夹路径
	 * 
	 * @param dirFile
	 */
	public void getImageFile(File dirFile) {
		Cursor cursor = resolver.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Images.Media.DEFAULT_SORT_ORDER);
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		while (cursor.moveToNext()) {
			String url = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
			Long length = cursor.getLong(cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
			Long imageId = cursor.getLong(cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
			bitmap = MediaStore.Images.Thumbnails.getThumbnail(resolver,
					imageId, Images.Thumbnails.MICRO_KIND, options);
			FileName fPath = new FileName(2, url, length, false, bitmap, url);
			filePaths.add(fPath);
		}
	}

	// 获得音乐方法
	public void getAudioFile(File dirFile) {
		Cursor cursor = resolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		while (cursor.moveToNext()) {
			String url = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			Long length = cursor.getLong(cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
			FileName fPath = new FileName(2, url, length, false, null, url);
			filePaths.add(fPath);
		}
	}

	// 获得视频方法
	public void getMediaFile(File dirFile) {
		Cursor cursor = resolver.query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Video.Media.DEFAULT_SORT_ORDER);
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		while (cursor.moveToNext()) {
			String url = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
			Long length = cursor.getLong(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
			Long videoId = cursor.getLong(cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
			bitmap = MediaStore.Video.Thumbnails.getThumbnail(resolver,
					videoId, Images.Thumbnails.MICRO_KIND, options);
			FileName fPath = new FileName(2, url, length, false, bitmap, url);
			filePaths.add(fPath);
		}
	}

	// 获得apk方法
	public void getApkFile(File dirFile) {
		List<PackageInfo> packages = getPackageManager()
				.getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				String appName = packageInfo.applicationInfo.loadLabel(
						getPackageManager()).toString();
				Drawable drawable = packageInfo.applicationInfo
						.loadIcon(getPackageManager());
				BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				String appDir = packageInfo.applicationInfo.publicSourceDir;
				File file = new File(appDir);
				FileName fPath = new FileName(2, appDir, file.length(), false,
						bitmap, appName);
				filePaths.add(fPath);
			}
		}
	}
	
	/**获得已收文件*/
	private void getReceiveFile(String filePath){
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
		}
	}

	/**
	 * 当列表中的条目被点击时会触发该事件
	 */
	@Override
	protected void onListItemClick(ListView listView, View itemView,
			int position, long id) {
		File file = new File(filePaths.get(position).fileName);// 获得在List中被点击的这个item所对应的文件
		if (file.isDirectory()) {// 如果该文件为目录文件则打开该目录
			getFileDir(filePaths.get(position).fileName);
		} else {// 如果该文件是一个普通文件则修改该条中选择框的状态，即选中该文件或取消选中
			CheckBox cb = (CheckBox) itemView.findViewById(R.id.file_selected);
			cb.setChecked(!cb.isChecked());// 选择该文件或取消选择
			onCheck(cb);// 传给onCheck方法继续处理
		}
	}

	// 检查检测框架的状态，根据该状态来保存或删除文件信息
	public void onCheck(View fileSelectedCheckBox) {
		CheckBox cb = (CheckBox) fileSelectedCheckBox;
		int fileIndex = (Integer) cb.getTag();// 获得该检测框在文件列表中对应的序号，该序号与列表中该条目的序号一致
		Constant.fileSelectedState.put(fileIndex, cb.isChecked());
		if (cb.isChecked()) {// 如果是被选中则保存该序号对应的文件信息
			FileName fName = filePaths.get(fileIndex);
			if (!selectedFilePath.contains(fName)) {
				filePaths.get(fileIndex).bitmap = null;
				selectedFilePath.add(filePaths.get(fileIndex));
			}
		} else {// 如果取消选中则从保存的文件信息中删除该序号对应的文件信息
			selectedFilePath.remove(filePaths.get(fileIndex));
		}
	}

	/**
	 * 日期： 描述：获取对应类型的数据 ‘ 操作人：gxc
	 * 
	 * @throws IOException
	 * @throws SQLException
	 *             3个参数分别是Params，Progress和Result。
	 */
	private class initData extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
			btn_overlay.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String type = params[0];
			if (type.equals("image")) {
				filePaths.clear();
				selectedFilePath.clear();
				Constant.fileSelectedState.clear();
				getImageFile(dirFile);
			} else if (type.equals("audio")) {
				filePaths.clear();
				selectedFilePath.clear();
				Constant.fileSelectedState.clear();
				getAudioFile(dirFile);
			} else if (type.equals("media")) {
				filePaths.clear();
				selectedFilePath.clear();
				Constant.fileSelectedState.clear();
				getMediaFile(dirFile);
			} else if (type.equals("apk")) {
				filePaths.clear();
				selectedFilePath.clear();
				Constant.fileSelectedState.clear();
				getApkFile(dirFile);
			}else{
				filePaths.clear();
				selectedFilePath.clear();
				Constant.fileSelectedState.clear();
				getReceiveFile(defaultPath);
			}
			return type;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (null == adapter) {
				adapter = new MyFileAdapter(FileManagerActivity.this, filePaths);
			} else {
				adapter.setDatasource(filePaths);
			}
			FileManagerActivity.this.setListAdapter(adapter);// 把获得的文件信息传给List适配器，让适配器更新列表条目

			pb.setVisibility(View.GONE);
			btn_overlay.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnShake:
			Intent intent = new Intent();
			if (selectType == Constant.SELECT_FILES) {// 如果当前为选择文件模式则返回当前选择的所有文件
				intent.putExtra("selectType", Constant.SELECT_FILES);
				intent.putExtra("files", selectedFilePath);
				setResult(RESULT_OK, intent);
				finish();
			}
			break;
		}
	}
}