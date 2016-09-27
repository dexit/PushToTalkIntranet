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
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ĶԽ��� �汾�� ���ڣ� ������ѡ���ļ����ͽ��棨Dialog��ʽ�� ���������/����/�޸�
 * �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class FileManagerActivity extends ListActivity implements
		OnClickListener {
	/** ���浱ǰĿ¼�µ������ļ����ļ�·�� */
	private List<FileName> filePaths = new ArrayList<FileName>();
	/** ��Ŀ¼·�� */
	private String rootPath = "/";
	/** ��ʼ���ϼ�Ŀ¼·�� */
	private String parentPath = "/";
	private String defaultPath = "//sdcard/shenliao-sinobpo";
	/** Ŀ¼ѡ�� ����һ���� */
	private Button btnReturnRoot = null;
	/** Ŀ¼ѡ�� ����һ���� */
	private Button btnReturnParent = null;
	/** ���಼�� */
	private RelativeLayout linLaySort = null;
	private RadioGroup radio_group;
	/** ��ť��ҡһҡ�� */
	private Button btnShake = null;
	/** ��ť��ȷ����������ǰ�ǽ����ļ�ʱ��ʾ�� */
	private Button btnConfirm = null;
	/** ���汻ѡ��������ļ�·�� */
	private ArrayList<FileName> selectedFilePath = new ArrayList<FileName>();
	/** ������ʾ��ǰĿ¼·��(�����ļ�ʱ��ʾ) */
	private TextView mPath;
	/** ��ǰ·�� */
	private String currentPath = null;
	private int selectType = 0;
	/** ���ܴ��ݹ���������ֵ ͼƬ����Ƶ�� */
	private String type;
	private MyFileAdapter adapter = null;
	ContentResolver resolver;
	File dirFile = new File(Environment.getExternalStorageDirectory()
			.getAbsolutePath());

	/** ������ */
	private ProgressBar pb;
	/***/
	private Button btn_overlay;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// �ж�"//sdcard/shenliao-sinobpo"Ŀ¼�Ƿ����
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File mkFile = new File("//sdcard/shenliao-sinobpo");
			if (!mkFile.exists()) {
				mkFile.mkdir(); // �����ļ���
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
		setListAdapter(adapter);// �ѻ�õ��ļ���Ϣ����List���������������������б���Ŀ

		Intent intent = getIntent();
		selectType = intent.getExtras().getInt("selectType");
		type = intent.getExtras().getString("type");

		btnShake = (Button) findViewById(R.id.btnShake);
		btnConfirm = (Button) findViewById(R.id.btnConfirm);
		// ҡ���ֻ������ļ�
		ShakeListener shakeListener = new ShakeListener(
				FileManagerActivity.this); // ����һ������
		shakeListener.setOnShakeListener(new OnShakeListener() { // ����setOnShakeListener�������м���
					public void onShake() {
						Intent intent = new Intent();
						// Bundle bundle=new Bundle();
						if (selectType == Constant.SELECT_FILES) {// �����ǰΪѡ���ļ�ģʽ�򷵻ص�ǰѡ��������ļ�
							intent
									.putExtra("selectType",
											Constant.SELECT_FILES);
							intent.putExtra("files", selectedFilePath);
						}
						setResult(RESULT_OK, intent);
						finish();
					}
				});

		// ȷ��ѡ����ļ�·��
		btnConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				if (selectType == Constant.SELECT_FILE_PATH) {// �����ǰΪ�ļ���ѡ��ģʽ�򷵻ص�ǰѡ����ļ���·��
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
		if (selectType == Constant.SELECT_FILE_PATH) {//ѡ�����������ļ����ļ���
			title.setText(getString(R.string.select_path_for_save));
			getFileDir(defaultPath);
			linLaySort.setVisibility(View.GONE);
			btnReturnRoot.setVisibility(View.VISIBLE);
			btnReturnParent.setVisibility(View.VISIBLE);
			btnConfirm.setVisibility(View.VISIBLE);
			btnShake.setVisibility(View.GONE);
		} else {//ѡ��Ҫ���͵��ļ�
			title.setText(getString(R.string.select_file_for_send));
			btnReturnRoot.setVisibility(View.GONE);
			btnReturnParent.setVisibility(View.GONE);
			btnConfirm.setVisibility(View.GONE);
			btnShake.setVisibility(View.VISIBLE);
			linLaySort.setVisibility(View.VISIBLE);
			btnShake.setOnClickListener(this);
			new initData().execute(type);
			
			//��ʼ����ť����
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

	// �����б�
	Handler handlerSearch = new Handler() {
		public void handleMessage(Message msg) {

		};
	};

	/**
	 * ���ఴť�����¼�
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
	 *            ��Ҫ�򿪵�Ŀ¼·�� �򿪸�Ŀ¼���������������ļ���Ϣ������Ŀ¼���ļ�
	 *            ���������ļ��������fileNames�б��У��������ļ�·�������filePaths�б���
	 */
	private void getFileDir(String filePath) {
		if (null == filePath)
			return;// ����ǲ��ǳ����˸�Ŀ¼
		File dirFile = new File(filePath);
		parentPath = dirFile.getParent();// ��õ�ǰĿ¼�ĸ�Ŀ¼
		File[] files = dirFile.listFiles();// ��ȡ��ǰĿ¼�µ������ļ�
		if (null != files) {
			filePaths.clear();
			selectedFilePath.clear();
			currentPath = filePath;
			Constant.fileSelectedState.clear();
			mPath.setText(getString(R.string.current_path_label) + filePath);
			for (File file : files) {
				if (selectType == Constant.SELECT_FILE_PATH) {// ���ѡ��ģʽΪ�ļ���ģʽ��ֻ����ļ���
					if (file.isDirectory()) {
						FileName fPath = new FileName(1, file.getPath());
						filePaths.add(fPath);
					}
				} else {// ���ѡ��ģʽΪ�ļ�ģʽ���������ļ������ļ�
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
			Collections.sort(filePaths);// �������򣬰��ļ�������ǰ�棬�ļ����ں���
			if (null == adapter) {
				adapter = new MyFileAdapter(this, filePaths);
			} else {
				adapter.setDatasource(filePaths);
			}
			setListAdapter(adapter);// �ѻ�õ��ļ���Ϣ����List���������������������б���Ŀ
		}
	}

	/**
	 * ���ͼƬ������dirFileΪ�ļ���·��
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

	// ������ַ���
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

	// �����Ƶ����
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

	// ���apk����
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
	
	/**��������ļ�*/
	private void getReceiveFile(String filePath){
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
		}
	}

	/**
	 * ���б��е���Ŀ�����ʱ�ᴥ�����¼�
	 */
	@Override
	protected void onListItemClick(ListView listView, View itemView,
			int position, long id) {
		File file = new File(filePaths.get(position).fileName);// �����List�б���������item����Ӧ���ļ�
		if (file.isDirectory()) {// ������ļ�ΪĿ¼�ļ���򿪸�Ŀ¼
			getFileDir(filePaths.get(position).fileName);
		} else {// ������ļ���һ����ͨ�ļ����޸ĸ�����ѡ����״̬����ѡ�и��ļ���ȡ��ѡ��
			CheckBox cb = (CheckBox) itemView.findViewById(R.id.file_selected);
			cb.setChecked(!cb.isChecked());// ѡ����ļ���ȡ��ѡ��
			onCheck(cb);// ����onCheck������������
		}
	}

	// ������ܵ�״̬�����ݸ�״̬�������ɾ���ļ���Ϣ
	public void onCheck(View fileSelectedCheckBox) {
		CheckBox cb = (CheckBox) fileSelectedCheckBox;
		int fileIndex = (Integer) cb.getTag();// ��øü������ļ��б��ж�Ӧ����ţ���������б��и���Ŀ�����һ��
		Constant.fileSelectedState.put(fileIndex, cb.isChecked());
		if (cb.isChecked()) {// ����Ǳ�ѡ���򱣴����Ŷ�Ӧ���ļ���Ϣ
			FileName fName = filePaths.get(fileIndex);
			if (!selectedFilePath.contains(fName)) {
				filePaths.get(fileIndex).bitmap = null;
				selectedFilePath.add(filePaths.get(fileIndex));
			}
		} else {// ���ȡ��ѡ����ӱ�����ļ���Ϣ��ɾ������Ŷ�Ӧ���ļ���Ϣ
			selectedFilePath.remove(filePaths.get(fileIndex));
		}
	}

	/**
	 * ���ڣ� ��������ȡ��Ӧ���͵����� �� �����ˣ�gxc
	 * 
	 * @throws IOException
	 * @throws SQLException
	 *             3�������ֱ���Params��Progress��Result��
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
			FileManagerActivity.this.setListAdapter(adapter);// �ѻ�õ��ļ���Ϣ����List���������������������б���Ŀ

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
			if (selectType == Constant.SELECT_FILES) {// �����ǰΪѡ���ļ�ģʽ�򷵻ص�ǰѡ��������ļ�
				intent.putExtra("selectType", Constant.SELECT_FILES);
				intent.putExtra("files", selectedFilePath);
				setResult(RESULT_OK, intent);
				finish();
			}
			break;
		}
	}
}