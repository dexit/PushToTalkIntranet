package com.sinobpo.godtalk;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.sinobpo.R;
import com.sinobpo.daseHelper.ChartContentDaoImpl;
import com.sinobpo.service.MainService;
import com.sinobpo.util.ChatContent;
import com.sinobpo.util.Constant;
import com.sinobpo.util.FileName;
import com.sinobpo.util.FileState;
import com.sinobpo.util.Person;
import com.sinobpo.util.ShakeListener;
import com.sinobpo.util.ShakeListener.OnShakeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupExpandListener;

/**
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ĶԽ��� �汾�� ���ڣ� �����������棨��ϵ�˽��棩 ��������� �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class MainActivity extends Activity implements View.OnClickListener {
	/***/
	private ExpandableListView ev = null;

	private String[] groupIndicatorLabeles = null;
	private SettingDialog settingDialog = null;
	private CallingDialog callingDialog = null;
	private CallingDialog receiveAudioDialog=null;
	private MyBroadcastRecv broadcastRecv = null;
	private IntentFilter bFilter = null;
	private ArrayList<Map<Integer, Person>> children = null;
	private ArrayList<Integer> personKeys = null;
	private MainService mService = null;
	private Intent mMainServiceIntent = null;
	private ExListAdapter adapter = null;
	private Person me = null;
	private Person person = null;
	private AlertDialog dialog = null;
	private boolean isPaused = false;// �жϱ����ǲ��ǿɼ�
	private boolean isRemoteUserClosed = false; // �Ƿ�Զ���û��Ѿ��ر���ͨ����
	private boolean isFinish = false;// �ж��Ƿ��˳�Ӧ�ó���
	private ArrayList<FileState> receivedFileNames = null;// ���յ��ĶԷ����������ļ���
	private ArrayList<FileState> beSendFileNames = null;// ���͵��Է����ļ�����Ϣ
	private boolean isRecording = false;
	/** ˢ�°�ť */
	private ImageButton btnFriendRefresh;
	File recordingFile;
	/** �ײ� ��ʾ��ǰ���ӵ�wifi */
	private TextView txtShowWifi;
	/**wifi������*/
	private WifiManager wifiManager = null;
	
	/**�����ļ�Dialog*/
	private AlertDialog recDialog; 
	
	//�����ļ���dialog��OK��ť
	private Button btn_receive;
	private String filePath="//sdcard/shenliao-sinobpo/";
	
	/**�ҵ�Ip*/
	private String localIp="";
	private ChartContentDaoImpl chartDaoImpl;
	private String clickPersonIp="";

	/**
	 * MainService�����뵱ǰActivity�İ�������
	 */
	private ServiceConnection sConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((MainService.ServiceBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	};

	private Adapter_ReceiveSendFileList receiveFileListAdapter = new Adapter_ReceiveSendFileList(
			this,1);
	private Adapter_ReceiveSendFileList sendFileListAdapter = new Adapter_ReceiveSendFileList(
			this,0);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.godtalk_activity);
		
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		//��õ�ǰ�����û�
		groupIndicatorLabeles = getResources().getStringArray(
				R.array.groupIndicatorLabeles);

		// ��ǰActivity���̨MainService���а�
		mMainServiceIntent = new Intent(this, MainService.class);
		bindService(mMainServiceIntent, sConnection, BIND_AUTO_CREATE);
		startService(mMainServiceIntent);

		btnFriendRefresh = (ImageButton) findViewById(R.id.btnFriendRefresh);
		btnFriendRefresh.setOnClickListener(this);
		txtShowWifi = (TextView) findViewById(R.id.txtShowWifi);

		ev = (ExpandableListView) findViewById(R.id.expListUser);
		/**�������ַ��ٷ����ϵ�View,ʹ���޷����*/
		TextView tv_overlay=(TextView)findViewById(R.id.tv_overlay);
		tv_overlay.getBackground().setAlpha(0);
		
		chartDaoImpl=new ChartContentDaoImpl(this);
		
		regBroadcastRecv();

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File path = new File(// ����һ����ʱ�ļ���
					Environment.getExternalStorageDirectory().getAbsolutePath()
							+ "/Android/data/com.apress.proandroidmedia.ch07.altaudiorecorder/files/");
			if (path.exists()) {
				// ɾ����ʱ�ļ�
				File[] temFiles = path.listFiles();
				File tempFile;
				for (int i = 0; i < temFiles.length; i++) {
					tempFile = temFiles[i];
					tempFile.delete();
				}
			} else {
				path.mkdirs();
			}
			try {
				recordingFile = File.createTempFile("recording", ".pcm", path);// �ļ������Լ�����
			} catch (IOException e) {
				throw new RuntimeException("Couldn't create file on SD card", e);
			}
		} else {
			Toast.makeText(this, "SD���Ѱγ�������������ʱ�����ã�", Toast.LENGTH_SHORT)
					.show();
		}
		
		// ҡһҡʵ���б�ˢ��
		ShakeListener shakeListener = new ShakeListener(MainActivity.this); // ����һ������
		shakeListener.setOnShakeListener(new OnShakeListener() { // ����setOnShakeListener�������м���
					public void onShake() {
						mService.refreshFriend();
					}
				});
		// �ս������������˾��ʾ
		Toast.makeText(MainActivity.this, "ҡ�ο�ˢ���б�", Toast.LENGTH_SHORT)
				.show();
		
	}



	/**
	 * ��ȡ����WIFI��Ϣ
	 * 
	 * @return
	 * @throws IOException
	 * @throws SocketException
	 */
	public String getWifiInfo() {
		String wifiInfo = null;
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							if (inetAddress.isReachable(1000)) {
								localIp = inetAddress.getHostAddress()
										.toString();
								wifiInfo = wifiManager.getConnectionInfo()
										.getSSID()
										+ "(" + localIp + ")";
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		return wifiInfo;
	}

	// ==============================ExpandableListView����������===================================
	private class ExListAdapter extends BaseExpandableListAdapter {
		private Context context = null;

		public ExListAdapter(Context context) {
			this.context = context;
		}

		// ���ĳ���û�����
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return children.get(groupPosition).get(
					personKeys.get(childPosition));
		}

		// ����û����û��б��е����
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return personKeys.get(childPosition);
		}

		// �����û�����View
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parentView) {
			View view = null;
			if (groupPosition < children.size()) {// ���groupPosition������ܴ�children�б��л��һ��children����
				Person person = children.get(groupPosition).get(
						personKeys.get(childPosition));// ��õ�ǰ�û�ʵ��
				view = getLayoutInflater().inflate(R.layout.godtalk_child_item,
						null);// ����List�û���Ŀ���ֶ���
				view.setOnClickListener(MainActivity.this);
				view.setTag(person);// ���һ��tag����Ա��ڳ����¼��͵���¼��и��ݸñ�ǽ�����ش���
				view.setPadding(10, 0, 0, 0);// ����������հ׾���
				ImageView headIconView = (ImageView) view
						.findViewById(R.id.txtPerHeadIcon);// ͷ��
				TextView nickeNameView = (TextView) view
						.findViewById(R.id.txtPerNickName);// �ǳ�
				TextView loginTimeView = (TextView) view
						.findViewById(R.id.txtPerLoginTime);// ��¼ʱ��
				TextView msgCountView = (TextView) view
						.findViewById(R.id.txtPerMsgCount);// δ����Ϣ����
				Button imgStartCalling = (Button) view
						.findViewById(R.id.imgStartCalling);// ��ʼ����
				// TextView
				// audioMsgCount=(TextView)view.findViewById(R.id.txtAudioMsgCount);//δ����������
				TextView ipaddressView = (TextView) view
						.findViewById(R.id.txtPerIpAddre);// IP��ַ
				imgStartCalling.setOnClickListener(new CallingOnClickListener(
						person.ipAddress, imgStartCalling));
				headIconView.setImageResource(person.personHeadIconId);
				nickeNameView.setText(person.personNickeName);
				loginTimeView.setText(person.loginTime);
				// String msgCountStr =
				// getString(R.string.init_msg_count);��ʽ���ַ���
				// �����û�id��service���ø��û�����Ϣ����
				int count = mService.getMessagesCountById(person.personId)
						+ mService.getAudioMsgCountById(person.personId);
				msgCountView.setText(String.valueOf(count));
				if (count > 0) {
					msgCountView.setVisibility(View.VISIBLE);
				} else {
					msgCountView.setVisibility(View.GONE);
				}
				// audioMsgCount.setText("������Ϣ����"+mService.getAudioMsgCountById(person.personId));

				ipaddressView.setText(person.ipAddress);
			}
			return view;
		}

		// ���ĳ���û����е��û���
		@Override
		public int getChildrenCount(int groupPosition) {
			int childrenCount = 0;
			if (groupPosition < children.size())
				childrenCount = children.get(groupPosition).size();
			return childrenCount;
		}

		// ���ĳ���û������
		@Override
		public Object getGroup(int groupPosition) {
			return children.get(groupPosition);
		}

		// ����û�������,�ô����û����������ص��������Ƶ�����
		@Override
		public int getGroupCount() {
			return groupIndicatorLabeles.length;
		}

		// ����û������
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		// �����û��鲼��View
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			int childrenCount = 0;
			if (groupPosition < children.size()) {// ���groupPosition����ܴ�children�б��л��children�������ø�children�����е��û�����
				childrenCount = children.get(groupPosition).size();
			}
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.godtalk_parent_item, null);
			}
			TextView title = (TextView) view
					.findViewById(R.id.txtFriendNickName);
			TextView txtMsgCount = (TextView) view
					.findViewById(R.id.txtMsgCount);
			title.setText(groupIndicatorLabeles[groupPosition]);
			txtMsgCount.setText("(" + childrenCount + ")");

			// if(isExpanded)
			// image.setBackgroundResource(R.drawable.narrow_select);
			// else
			// image.setBackgroundResource(R.drawable.narrow);
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

	/**
	 * ��ϵ�����ֺ���ġ����С���ť
	 * 
	 * @author Buddy
	 * 
	 */
	public class CallingOnClickListener implements
			android.view.View.OnClickListener {
		private String ipAddress;
		private TextView btnStartCalling;

		public CallingOnClickListener(String ipAddress, TextView btnStartCalling) {
			this.btnStartCalling = btnStartCalling;
			this.ipAddress = ipAddress;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.imgStartCalling:
				isRecording = true;
				// mService.startCalling(ipAddress);
				if (null == callingDialog)
					callingDialog = new CallingDialog(MainActivity.this,
							mService, ipAddress);
				callingDialog.show();
				break;
			}
		}
	}

	// =================================ExpandableListView��������������===================================================

	// ������ѵ������Ϣ
	private void getMyInfomation() {
		SharedPreferences pre = PreferenceManager
				.getDefaultSharedPreferences(this);
		int iconId = pre.getInt("headIconId", R.drawable.applause2);
		String nickeName = pre.getString("nickeName", "��������ǳ�");
		ImageView myHeadIcon = (ImageView) findViewById(R.id.imvMyHead);
		myHeadIcon.setImageResource(iconId);
		TextView myNickeName = (TextView) findViewById(R.id.txtMyNickName);
		myNickeName.setText(nickeName);
		me = new Person();
		me.personHeadIconId = iconId;
		me.personNickeName = nickeName;
		me.ipAddress=localIp;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.linLayMyInfo:// ����ϵͳ���ô���
			showSettingDialog();
			break;
		case R.id.linLayPersonItem:// ת������Ϣҳ��
			person = (Person) view.getTag();// �û��б��childView�����ʱ
			openChartPage(person);
			break;
		case R.id.long_send_msg:// �����б��childViewʱ�ڵ����Ĵ����е��"������Ϣ"��ťʱ
			person = (Person) view.getTag();
			openChartPage(person);
			if (null != dialog)
				dialog.dismiss();
			break;
		case R.id.btnFriendRefresh:// ˢ�º���
			mService.refreshFriend();
			if (getWifiInfo() != null) {
				txtShowWifi.setText(getWifiInfo());// ��ʾwifi��Ϣ
			} else {
				txtShowWifi.setText("δ��������");
				txtShowWifi.setTextColor(Color.RED);
			}
			break;
		case R.id.long_send_file:
			Intent intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			startActivityForResult(intent, 0);
			dialog.dismiss();
			break;
		case R.id.long_click_call:
			person = (Person) view.getTag();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(me.personNickeName);
			String title = String.format(getString(R.string.talk_with),
					person.personNickeName);
			builder.setMessage(title);
			builder.setIcon(me.personHeadIconId);
			builder.setNegativeButton(getString(R.string.close),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface cdialog, int which) {
							cdialog.dismiss();
						}
					});
			final AlertDialog callDialog = builder.show();
			callDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							mService.stopTalk(person.personId);
						}
					});
			mService.startTalk(person.personId);
			break;
		case R.id.long_click_cancel:
			dialog.dismiss();
			break;
		}
	}

	boolean finishedSendFile = false;// ��¼��ǰ��Щ�ļ��ǲ��Ǳ����Ѿ����չ���

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (null != data) {
				int selectType = data.getExtras().getInt("selectType");
				if (selectType == Constant.SELECT_FILE_PATH) {// ����յ������ļ���ѡ��ģʽ��˵��������Ҫ����Է����������ļ�����ѵ�ǰѡ����ļ���·�����ط����
					String fileSavePath = data.getExtras().getString(
							"fileSavePath");
					if (null != fileSavePath) {
						mService.receiveFiles(fileSavePath);
						finishedSendFile = true;// �ѱ��ν���״̬��Ϊtrue
						filePath =data.getExtras().getString(
						"fileSavePath");
						System.out.println("over save file ...");
					} else {//�ļ��в���д���޷������ļ���
						Toast.makeText(this,
								getString(R.string.folder_can_not_write),
								Toast.LENGTH_SHORT).show();
					}
				} else if (selectType == Constant.SELECT_FILES) {// ����յ������ļ�ѡ��ģʽ��˵��������Ҫ�����ļ�����ѵ�ǰѡ��������ļ����ظ�����㡣
					@SuppressWarnings("unchecked")
					final ArrayList<FileName> files = (ArrayList<FileName>) data
							.getExtras().get("files");
					mService.sendFiles(person.personId, files);// �ѵ�ǰѡ��������ļ����ظ������

					// ��ʾ�ļ������б�
					beSendFileNames = mService.getBeSendFileNames();// �ӷ������������Ҫ���յ��ļ����ļ���
					if (beSendFileNames.size() <= 0)
						return;
					sendFileListAdapter.setResources(beSendFileNames);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(me.personNickeName);
					builder.setMessage(R.string.start_to_send_file);
					builder.setIcon(me.personHeadIconId);
					View vi = getLayoutInflater().inflate(
							R.layout.request_file_popupwindow_layout, null);
					builder.setView(vi);
					final AlertDialog fileListDialog = builder.show();
					fileListDialog
							.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface arg0) {
									beSendFileNames.clear();
									files.clear();
								}
							});
					ListView lv = (ListView) vi
							.findViewById(R.id.receive_file_list);// ��Ҫ���յ��ļ��嵥
					lv.setAdapter(sendFileListAdapter);
					Button btn_ok = (Button) vi
							.findViewById(R.id.receive_file_okbtn);
					btn_ok.setVisibility(View.GONE);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);
					// ����ð�ť���������ļ�ѡ�����������ó��ļ���ѡ��ģʽ��ѡ��һ���������նԷ��ļ����ļ���
					btn_ok.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!finishedSendFile) {// ��������ļ��Ѿ����չ������ٴ��ļ���ѡ����
								Intent intent = new Intent(
										MainActivity.this,
										FileManagerActivity.class);
								intent.putExtra("selectType",
										Constant.SELECT_FILE_PATH);
								startActivityForResult(intent, 0);
							}
						}
					});
					// ����ð�ť������������㷢���û��ܾ������ļ��Ĺ㲥
					btn_cancle.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							fileListDialog.dismiss();
						}
					});

				}
			}
		}
	}

	// ��ʾ��Ϣ���öԻ���
	private void showSettingDialog() {
		if (null == settingDialog)
			settingDialog = new SettingDialog(this, R.style.SettingDialog);
		settingDialog.show();
	}

	// ������ʾ�û��յ���Ϣ
	public void msgRemind() {
		int playSound = R.raw.callremin;
		SharedPreferences sp = getSharedPreferences("bell_list_preference",
				Context.MODE_PRIVATE);
		int callremin = sp.getInt("callremin", 1);
		MediaPlayer mMediaPlayer = null;
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if (callremin == 1) {
			playSound = R.raw.callremin;
		} else if (callremin == 2) {
			playSound = R.raw.callremin2;
		} else if (callremin == 3) {
			playSound = R.raw.callremin3;
		} else if (callremin == 4) {
			playSound = R.raw.callremin4;
		} else {
			return;
		}
		mMediaPlayer = MediaPlayer.create(this, playSound);
		try {
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// =========================�㲥������==========================================================
	private class MyBroadcastRecv extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.updateMyInformationAction)) {
				getMyInfomation();
			} else if (intent.getAction().equals(
					Constant.dataReceiveErrorAction)
					|| intent.getAction().equals(Constant.dataSendErrorAction)) {
				Toast
						.makeText(MainActivity.this,
								intent.getExtras().getString("msg"),
								Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(
					Constant.fileReceiveStateUpdateAction)) {// �յ����Է������ļ�����״̬֪ͨ
				if (!isPaused) {
					receivedFileNames = mService.getReceivedFileNames();// ��õ�ǰ�����ļ�����״̬
					receiveFileListAdapter.setResources(receivedFileNames);
					receiveFileListAdapter.notifyDataSetChanged();// �����ļ������б�
				}
			} else if (intent.getAction().equals(
					Constant.fileSendStateUpdateAction)) {// �յ����Է������ļ�����״̬֪ͨ
				if (!isPaused) {
					beSendFileNames = mService.getBeSendFileNames();// ��õ�ǰ�����ļ�����״̬
					sendFileListAdapter.setResources(beSendFileNames);
					sendFileListAdapter.notifyDataSetChanged();// �����ļ������б�
				}
			} else if (intent.getAction().equals(
					Constant.receivedTalkRequestAction)) {
				if (!isFinish) {
					msgRemind();
					adapter.notifyDataSetChanged();
				}
			} else if (intent.getAction().equals(Constant.recorderFailAction)) {
				Toast.makeText(MainActivity.this, "¼����������",
						Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(
					Constant.remoteUserClosedTalkAction)) {
				isRemoteUserClosed = true;// ������յ�Զ���û��ر�ͨ��ָ����Ѹñ����Ϊtrue
			} else if (intent.getAction().equals(//�Է��ܾ������ļ�
					Constant.remoteUserRefuseReceiveFileAction)) {
				Toast.makeText(MainActivity.this,
						getString(R.string.refuse_receive_file),
						Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(
					Constant.personHasChangedAction)) {//��ȡ�û���Ϣ
				children = mService.getChildren();
				personKeys = mService.getPersonKeys();
				if (null == adapter) {
					adapter = new ExListAdapter(MainActivity.this);
					ev.setAdapter(adapter);
					ev.expandGroup(0);
					ev.setGroupIndicator(null);
					ev.setDivider(null);
				}
				adapter.notifyDataSetChanged();
			} else if (intent.getAction().equals(Constant.hasMsgUpdatedAction)) {
				msgRemind();
				adapter.notifyDataSetChanged();
			} else if (intent.getAction().equals(Constant.remoteCallingAction)) {//�յ�����(����ֻ����ʾdialog,�����ؽ���������ʧ�ڶԷ�����¼������ʱ��ִ��)
				String ipAddress = intent.getExtras().getString("ipAddress");
				receiveAudioDialog = new CallingDialog(
						MainActivity.this, ipAddress);
				if (!ipAddress.equals("")) {
					receiveAudioDialog.show();
				}
				handler_SendFile.sendEmptyMessage(2);
			} else if (intent.getAction().equals(
					Constant.receivedSendFileRequestAction)) {// ���յ��ļ�����������������ļ�
				if (!isPaused) {// ��������ڿɼ�״̬����Ӧ�㲥,����һ����ʾ���Ƿ�Ҫ���շ��������ļ�
					receivedFileNames = mService.getReceivedFileNames();// �ӷ������������Ҫ���յ��ļ����ļ���
					if (receivedFileNames.size() <= 0)
						return;
					receiveFileListAdapter.setResources(receivedFileNames);
					Person psn = (Person) intent.getExtras().get("person");
					AlertDialog.Builder builder = new AlertDialog.Builder(
							context);
					builder.setTitle(psn.personNickeName);
					builder.setMessage(R.string.sending_file_to_you);
					builder.setIcon(psn.personHeadIconId);
					View vi = getLayoutInflater().inflate(
							R.layout.request_file_popupwindow_layout, null);
					builder.setView(vi);
					recDialog = builder.show();
					recDialog
							.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface arg0) {
									receivedFileNames.clear();
									if (!finishedSendFile) {// ��������ļ���δ���վ͹رս��մ��ڣ�˵���������ν��գ�ͬʱ��Զ�̷���һ���ܾ����յ�ָ�
										Intent intent = new Intent();
										intent
												.setAction(Constant.refuseReceiveFileAction);
										sendBroadcast(intent);
									}
									finishedSendFile = false;// �ر��ļ����նԻ��򣬱���ʾ�����ļ�������ɣ��ѱ����ļ�����״̬��Ϊfalse
								}
							});
					ListView lv = (ListView) vi
							.findViewById(R.id.receive_file_list);// ��Ҫ���յ��ļ��嵥
					lv.setAdapter(receiveFileListAdapter);
					btn_receive = (Button) vi
							.findViewById(R.id.receive_file_okbtn);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);
					// ����ð�ť���������ļ�ѡ�����������ó��ļ���ѡ��ģʽ��ѡ��һ���������նԷ��ļ����ļ���
					btn_receive.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							btn_receive.setEnabled(false);
							if (!finishedSendFile) {// ��������ļ��Ѿ����չ������ٴ��ļ���ѡ����
								Intent intent = new Intent(
										MainActivity.this,
										FileManagerActivity.class);
								intent.putExtra("selectType",
										Constant.SELECT_FILE_PATH);
								startActivityForResult(intent, 0);
							}
							// dialog.dismiss();
						}
					});
					// ����ð�ť������������㷢���û��ܾ������ļ��Ĺ㲥
					btn_cancle.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							recDialog.dismiss();
						}
					});
					
					Constant.sendFileIsOver=false;
					/**����handler�����ļ��Ƿ������*/
					if(Constant.sendFileIsOver==false){
						handler_SendFile.sendEmptyMessage(1);
					}else{
						if(receivedFileNames.size()==1){
							btn_receive.setText("��");
							btn_receive.setEnabled(true);
							btn_receive.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									openFile(filePath+"/"+receivedFileNames.get(0).fileName);
								}
							});
						}else{
							recDialog.dismiss();
							Toast.makeText(MainActivity.this, "����ͨ�������á���ġ������ļ������в鿴", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		}
	}
	
	private class SaveData extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			int personID=Integer.parseInt(params[0]);
			
			List<com.sinobpo.util.Message> list_msgs = mService.getMessagesById(personID);
			
			int i = 0;
			if (null != list_msgs) {
				while(list_msgs.size()>0){
					com.sinobpo.util.Message msg = list_msgs.remove(0);
					
					ChatContent chatContent = new ChatContent(params[1],
							me.ipAddress, msg.receivedTime,
							msg.msg, "", person.personHeadIconId,
							person.personNickeName, "1", "");
					chartDaoImpl.saveToCallLog(chatContent);
					i++;
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Intent intent = new Intent(MainActivity.this, ChartMsgActivity.class);
			intent.putExtra("person", person);
			intent.putExtra("me", me);
			startActivity(intent);
		}
		
	}
	
	private Handler handler_SendFile=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case 1:
				if(Constant.sendFileIsOver==false){
					sendEmptyMessageDelayed(1, 300);
				}else{
					if(receivedFileNames.size()==1){
						btn_receive.setText("��");
						btn_receive.setEnabled(true);
						btn_receive.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								System.out.println("ooooooooooooooooooo9");
								//����浽��Ĭ���ļ��о�����д������
								openFile(filePath+"/"+receivedFileNames.get(0).fileName);
							}
						});
					}else{
						recDialog.dismiss();
						Toast.makeText(MainActivity.this, "����ͨ�������á���ġ������ļ������в鿴", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 2:
				if(Constant.receiveAudio==false){
					sendEmptyMessageDelayed(2, 300);
				}else{
					receiveAudioDialog.dismiss();
				}
				break;
			}
			
		}
		
	};
	
	/** ���ļ� **/
		private void openFile(String path) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);

			File f = new File(path);
			String type =Constant.getMIMEType(f.getName());
			intent.setDataAndType(Uri.fromFile(f), type);
			startActivity(intent);
		}

	// =========================�㲥����������==========================================================

	// �㲥������ע��
	private void regBroadcastRecv() {
		broadcastRecv = new MyBroadcastRecv();
		bFilter = new IntentFilter();
		bFilter.addAction(Constant.updateMyInformationAction);
		bFilter.addAction(Constant.personHasChangedAction);
		bFilter.addAction(Constant.hasMsgUpdatedAction);
		bFilter.addAction(Constant.receivedSendFileRequestAction);
		bFilter.addAction(Constant.remoteUserRefuseReceiveFileAction);
		bFilter.addAction(Constant.dataReceiveErrorAction);
		bFilter.addAction(Constant.dataSendErrorAction);
		bFilter.addAction(Constant.fileReceiveStateUpdateAction);
		bFilter.addAction(Constant.fileSendStateUpdateAction);
		bFilter.addAction(Constant.receivedTalkRequestAction);
		bFilter.addAction(Constant.remoteUserClosedTalkAction);
		bFilter.addAction(Constant.sendAudioAction);
		bFilter.addAction(Constant.recorderFailAction);
		bFilter.addAction(Constant.remoteCallingAction);
		bFilter.addAction(Constant.remoteFinishCallingAction);
		registerReceiver(broadcastRecv, bFilter);
		
	}

	// ��ת������ҳ��
	private void openChartPage(Person person) {
		String personId=String.valueOf(person.personId);
		clickPersonIp=person.ipAddress;
		
		new SaveData().execute(personId,clickPersonIp);
	}
	
	/**
	 * �˳��Ի���
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		// final Bundle bu=bundle;
		Dialog dialog = null;
		switch (id) {
		case 0:
			dialog = new AlertDialog.Builder(MainActivity.this).setIcon(
					android.R.drawable.ic_dialog_info).setTitle("ȷ���˳���")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									unregisterReceiver(broadcastRecv);
									stopService(mMainServiceIntent);
									unbindService(sConnection);
									
									ChartContentDaoImpl daoImpl=new ChartContentDaoImpl(MainActivity.this);
									daoImpl.deleteAllDate();
									
									android.os.Process.killProcess(android.os.Process.myPid()) ;
									isFinish = true;
								}
							}).setNeutralButton("��̨����",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									moveTaskToBack(true);// ��̨����
								}
							}).setNegativeButton("ȡ��", null).create();
			break;
		}
		return dialog;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getOrder()) {
		case 1:
			showSettingDialog();
			break;
		case 2:
			startActivity(new Intent(MainActivity.this,
					SettingsActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// ���ؼ������¼�
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			showDialog(0);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		if (getWifiInfo() != null) {
			txtShowWifi.setText(getWifiInfo());// ��ʾwifi��Ϣ
		} else {
			txtShowWifi.setText("δ��������");
			txtShowWifi.setTextColor(Color.RED);
		}
		isPaused = false;
		getMyInfomation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastRecv);
		stopService(mMainServiceIntent);
		unbindService(sConnection);
		isFinish = true;
	}

}