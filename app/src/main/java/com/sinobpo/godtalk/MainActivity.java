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
 * Copyright (C) 2012 移动应用研发组-神聊对讲机 版本： 日期： 描述：主界面（联系人界面） 操作：添加 操作人：移动应用研发组-gxc
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
	private boolean isPaused = false;// 判断本身是不是可见
	private boolean isRemoteUserClosed = false; // 是否远程用户已经关闭了通话。
	private boolean isFinish = false;// 判断是否退出应用程序
	private ArrayList<FileState> receivedFileNames = null;// 接收到的对方传过来的文件名
	private ArrayList<FileState> beSendFileNames = null;// 发送到对方的文件名信息
	private boolean isRecording = false;
	/** 刷新按钮 */
	private ImageButton btnFriendRefresh;
	File recordingFile;
	/** 底部 显示当前连接的wifi */
	private TextView txtShowWifi;
	/**wifi管理器*/
	private WifiManager wifiManager = null;
	
	/**接收文件Dialog*/
	private AlertDialog recDialog; 
	
	//接受文件的dialog的OK按钮
	private Button btn_receive;
	private String filePath="//sdcard/shenliao-sinobpo/";
	
	/**我的Ip*/
	private String localIp="";
	private ChartContentDaoImpl chartDaoImpl;
	private String clickPersonIp="";

	/**
	 * MainService服务与当前Activity的绑定连接器
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
		
		//获得当前在线用户
		groupIndicatorLabeles = getResources().getStringArray(
				R.array.groupIndicatorLabeles);

		// 当前Activity与后台MainService进行绑定
		mMainServiceIntent = new Intent(this, MainService.class);
		bindService(mMainServiceIntent, sConnection, BIND_AUTO_CREATE);
		startService(mMainServiceIntent);

		btnFriendRefresh = (ImageButton) findViewById(R.id.btnFriendRefresh);
		btnFriendRefresh.setOnClickListener(this);
		txtShowWifi = (TextView) findViewById(R.id.txtShowWifi);

		ev = (ExpandableListView) findViewById(R.id.expListUser);
		/**覆盖在手风琴分组上的View,使其无法点击*/
		TextView tv_overlay=(TextView)findViewById(R.id.tv_overlay);
		tv_overlay.getBackground().setAlpha(0);
		
		chartDaoImpl=new ChartContentDaoImpl(this);
		
		regBroadcastRecv();

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File path = new File(// 创建一个临时文件夹
					Environment.getExternalStorageDirectory().getAbsolutePath()
							+ "/Android/data/com.apress.proandroidmedia.ch07.altaudiorecorder/files/");
			if (path.exists()) {
				// 删除临时文件
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
				recordingFile = File.createTempFile("recording", ".pcm", path);// 文件名称以及类型
			} catch (IOException e) {
				throw new RuntimeException("Couldn't create file on SD card", e);
			}
		} else {
			Toast.makeText(this, "SD卡已拔出，语音功能暂时不可用！", Toast.LENGTH_SHORT)
					.show();
		}
		
		// 摇一摇实现列表刷新
		ShakeListener shakeListener = new ShakeListener(MainActivity.this); // 创建一个对象
		shakeListener.setOnShakeListener(new OnShakeListener() { // 调用setOnShakeListener方法进行监听
					public void onShake() {
						mService.refreshFriend();
					}
				});
		// 刚进入这个界面土司提示
		Toast.makeText(MainActivity.this, "摇晃可刷新列表", Toast.LENGTH_SHORT)
				.show();
		
	}



	/**
	 * 获取连接WIFI信息
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

	// ==============================ExpandableListView数据适配器===================================
	private class ExListAdapter extends BaseExpandableListAdapter {
		private Context context = null;

		public ExListAdapter(Context context) {
			this.context = context;
		}

		// 获得某个用户对象
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return children.get(groupPosition).get(
					personKeys.get(childPosition));
		}

		// 获得用户在用户列表中的序号
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return personKeys.get(childPosition);
		}

		// 生成用户布局View
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parentView) {
			View view = null;
			if (groupPosition < children.size()) {// 如果groupPosition的序号能从children列表中获得一个children对象
				Person person = children.get(groupPosition).get(
						personKeys.get(childPosition));// 获得当前用户实例
				view = getLayoutInflater().inflate(R.layout.godtalk_child_item,
						null);// 生成List用户条目布局对象
				view.setOnClickListener(MainActivity.this);
				view.setTag(person);// 添加一个tag标记以便在长按事件和点击事件中根据该标记进行相关处理
				view.setPadding(10, 0, 0, 0);// 设置左边填充空白距离
				ImageView headIconView = (ImageView) view
						.findViewById(R.id.txtPerHeadIcon);// 头像
				TextView nickeNameView = (TextView) view
						.findViewById(R.id.txtPerNickName);// 昵称
				TextView loginTimeView = (TextView) view
						.findViewById(R.id.txtPerLoginTime);// 登录时间
				TextView msgCountView = (TextView) view
						.findViewById(R.id.txtPerMsgCount);// 未读信息计数
				Button imgStartCalling = (Button) view
						.findViewById(R.id.imgStartCalling);// 开始呼叫
				// TextView
				// audioMsgCount=(TextView)view.findViewById(R.id.txtAudioMsgCount);//未读语音计数
				TextView ipaddressView = (TextView) view
						.findViewById(R.id.txtPerIpAddre);// IP地址
				imgStartCalling.setOnClickListener(new CallingOnClickListener(
						person.ipAddress, imgStartCalling));
				headIconView.setImageResource(person.personHeadIconId);
				nickeNameView.setText(person.personNickeName);
				loginTimeView.setText(person.loginTime);
				// String msgCountStr =
				// getString(R.string.init_msg_count);格式化字符串
				// 根据用户id从service层获得该用户的消息数量
				int count = mService.getMessagesCountById(person.personId)
						+ mService.getAudioMsgCountById(person.personId);
				msgCountView.setText(String.valueOf(count));
				if (count > 0) {
					msgCountView.setVisibility(View.VISIBLE);
				} else {
					msgCountView.setVisibility(View.GONE);
				}
				// audioMsgCount.setText("语音消息数："+mService.getAudioMsgCountById(person.personId));

				ipaddressView.setText(person.ipAddress);
			}
			return view;
		}

		// 获得某个用户组中的用户数
		@Override
		public int getChildrenCount(int groupPosition) {
			int childrenCount = 0;
			if (groupPosition < children.size())
				childrenCount = children.get(groupPosition).size();
			return childrenCount;
		}

		// 获得某个用户组对象
		@Override
		public Object getGroup(int groupPosition) {
			return children.get(groupPosition);
		}

		// 获得用户组数量,该处的用户组数量返回的是组名称的数量
		@Override
		public int getGroupCount() {
			return groupIndicatorLabeles.length;
		}

		// 获得用户组序号
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		// 生成用户组布局View
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			int childrenCount = 0;
			if (groupPosition < children.size()) {// 如果groupPosition序号能从children列表中获得children对象，则获得该children对象中的用户数量
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
	 * 联系人名字后面的“呼叫”按钮
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

	// =================================ExpandableListView数据适配器结束===================================================

	// 获得自已的相关信息
	private void getMyInfomation() {
		SharedPreferences pre = PreferenceManager
				.getDefaultSharedPreferences(this);
		int iconId = pre.getInt("headIconId", R.drawable.applause2);
		String nickeName = pre.getString("nickeName", "输入你的昵称");
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
		case R.id.linLayMyInfo:// 弹出系统设置窗口
			showSettingDialog();
			break;
		case R.id.linLayPersonItem:// 转到发信息页面
			person = (Person) view.getTag();// 用户列表的childView被点击时
			openChartPage(person);
			break;
		case R.id.long_send_msg:// 长按列表的childView时在弹出的窗口中点击"发送信息"按钮时
			person = (Person) view.getTag();
			openChartPage(person);
			if (null != dialog)
				dialog.dismiss();
			break;
		case R.id.btnFriendRefresh:// 刷新好友
			mService.refreshFriend();
			if (getWifiInfo() != null) {
				txtShowWifi.setText(getWifiInfo());// 显示wifi信息
			} else {
				txtShowWifi.setText("未连接网络");
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

	boolean finishedSendFile = false;// 记录当前这些文件是不是本次已经接收过了

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (null != data) {
				int selectType = data.getExtras().getInt("selectType");
				if (selectType == Constant.SELECT_FILE_PATH) {// 如果收到的是文件夹选择模式，说明现在是要保存对方传过来的文件，则把当前选择的文件夹路径返回服务层
					String fileSavePath = data.getExtras().getString(
							"fileSavePath");
					if (null != fileSavePath) {
						mService.receiveFiles(fileSavePath);
						finishedSendFile = true;// 把本次接收状态置为true
						filePath =data.getExtras().getString(
						"fileSavePath");
						System.out.println("over save file ...");
					} else {//文件夹不可写，无法保存文件！
						Toast.makeText(this,
								getString(R.string.folder_can_not_write),
								Toast.LENGTH_SHORT).show();
					}
				} else if (selectType == Constant.SELECT_FILES) {// 如果收到的是文件选择模式，说明现在是要发送文件，则把当前选择的所有文件返回给服务层。
					@SuppressWarnings("unchecked")
					final ArrayList<FileName> files = (ArrayList<FileName>) data
							.getExtras().get("files");
					mService.sendFiles(person.personId, files);// 把当前选择的所有文件返回给服务层

					// 显示文件发送列表
					beSendFileNames = mService.getBeSendFileNames();// 从服务层获得所有需要接收的文件的文件名
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
							.findViewById(R.id.receive_file_list);// 需要接收的文件清单
					lv.setAdapter(sendFileListAdapter);
					Button btn_ok = (Button) vi
							.findViewById(R.id.receive_file_okbtn);
					btn_ok.setVisibility(View.GONE);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);
					// 如果该按钮被点击则打开文件选择器，并设置成文件夹选择模式，选择一个用来接收对方文件的文件夹
					btn_ok.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!finishedSendFile) {// 如果本次文件已经接收过了则不再打开文件夹选择器
								Intent intent = new Intent(
										MainActivity.this,
										FileManagerActivity.class);
								intent.putExtra("selectType",
										Constant.SELECT_FILE_PATH);
								startActivityForResult(intent, 0);
							}
						}
					});
					// 如果该按钮被点击则向服务层发送用户拒绝接收文件的广播
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

	// 显示信息设置对话框
	private void showSettingDialog() {
		if (null == settingDialog)
			settingDialog = new SettingDialog(this, R.style.SettingDialog);
		settingDialog.show();
	}

	// 铃声提示用户收到消息
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

	// =========================广播接收器==========================================================
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
					Constant.fileReceiveStateUpdateAction)) {// 收到来自服务层的文件接收状态通知
				if (!isPaused) {
					receivedFileNames = mService.getReceivedFileNames();// 获得当前所有文件接收状态
					receiveFileListAdapter.setResources(receivedFileNames);
					receiveFileListAdapter.notifyDataSetChanged();// 更新文件接收列表
				}
			} else if (intent.getAction().equals(
					Constant.fileSendStateUpdateAction)) {// 收到来自服务层的文件接收状态通知
				if (!isPaused) {
					beSendFileNames = mService.getBeSendFileNames();// 获得当前所有文件接收状态
					sendFileListAdapter.setResources(beSendFileNames);
					sendFileListAdapter.notifyDataSetChanged();// 更新文件接收列表
				}
			} else if (intent.getAction().equals(
					Constant.receivedTalkRequestAction)) {
				if (!isFinish) {
					msgRemind();
					adapter.notifyDataSetChanged();
				}
			} else if (intent.getAction().equals(Constant.recorderFailAction)) {
				Toast.makeText(MainActivity.this, "录音发生错误！",
						Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(
					Constant.remoteUserClosedTalkAction)) {
				isRemoteUserClosed = true;// 如果接收到远程用户关闭通话指令则把该标记置为true
			} else if (intent.getAction().equals(//对方拒绝接受文件
					Constant.remoteUserRefuseReceiveFileAction)) {
				Toast.makeText(MainActivity.this,
						getString(R.string.refuse_receive_file),
						Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(
					Constant.personHasChangedAction)) {//获取用户信息
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
			} else if (intent.getAction().equals(Constant.remoteCallingAction)) {//收到语音(这里只是显示dialog,真正地接受语音消失在对方按下录音键的时候执行)
				String ipAddress = intent.getExtras().getString("ipAddress");
				receiveAudioDialog = new CallingDialog(
						MainActivity.this, ipAddress);
				if (!ipAddress.equals("")) {
					receiveAudioDialog.show();
				}
				handler_SendFile.sendEmptyMessage(2);
			} else if (intent.getAction().equals(
					Constant.receivedSendFileRequestAction)) {// 接收到文件发送请求，请求接收文件
				if (!isPaused) {// 如果自身处于可见状态则响应广播,弹出一个提示框是否要接收发过来的文件
					receivedFileNames = mService.getReceivedFileNames();// 从服务层获得所有需要接收的文件的文件名
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
									if (!finishedSendFile) {// 如果本次文件并未接收就关闭接收窗口，说明放弃本次接收，同时向远程发送一个拒绝接收的指令。
										Intent intent = new Intent();
										intent
												.setAction(Constant.refuseReceiveFileAction);
										sendBroadcast(intent);
									}
									finishedSendFile = false;// 关闭文件接收对话框，本表示本次文件接收完成，把本次文件接收状态置为false
								}
							});
					ListView lv = (ListView) vi
							.findViewById(R.id.receive_file_list);// 需要接收的文件清单
					lv.setAdapter(receiveFileListAdapter);
					btn_receive = (Button) vi
							.findViewById(R.id.receive_file_okbtn);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);
					// 如果该按钮被点击则打开文件选择器，并设置成文件夹选择模式，选择一个用来接收对方文件的文件夹
					btn_receive.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							btn_receive.setEnabled(false);
							if (!finishedSendFile) {// 如果本次文件已经接收过了则不再打开文件夹选择器
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
					// 如果该按钮被点击则向服务层发送用户拒绝接收文件的广播
					btn_cancle.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							recDialog.dismiss();
						}
					});
					
					Constant.sendFileIsOver=false;
					/**启动handler监听文件是否发送完毕*/
					if(Constant.sendFileIsOver==false){
						handler_SendFile.sendEmptyMessage(1);
					}else{
						if(receivedFileNames.size()==1){
							btn_receive.setText("打开");
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
							Toast.makeText(MainActivity.this, "可以通过“设置”里的“已收文件”进行查看", Toast.LENGTH_SHORT).show();
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
						btn_receive.setText("打开");
						btn_receive.setEnabled(true);
						btn_receive.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								System.out.println("ooooooooooooooooooo9");
								//如果存到了默认文件夹就这样写，否则
								openFile(filePath+"/"+receivedFileNames.get(0).fileName);
							}
						});
					}else{
						recDialog.dismiss();
						Toast.makeText(MainActivity.this, "可以通过“设置”里的“已收文件”进行查看", Toast.LENGTH_SHORT).show();
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
	
	/** 打开文件 **/
		private void openFile(String path) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);

			File f = new File(path);
			String type =Constant.getMIMEType(f.getName());
			intent.setDataAndType(Uri.fromFile(f), type);
			startActivity(intent);
		}

	// =========================广播接收器结束==========================================================

	// 广播接收器注册
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

	// 跳转到聊天页面
	private void openChartPage(Person person) {
		String personId=String.valueOf(person.personId);
		clickPersonIp=person.ipAddress;
		
		new SaveData().execute(personId,clickPersonIp);
	}
	
	/**
	 * 退出对话框
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		// final Bundle bu=bundle;
		Dialog dialog = null;
		switch (id) {
		case 0:
			dialog = new AlertDialog.Builder(MainActivity.this).setIcon(
					android.R.drawable.ic_dialog_info).setTitle("确定退出吗？")
					.setPositiveButton("确定",
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
							}).setNeutralButton("后台运行",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									moveTaskToBack(true);// 后台运行
								}
							}).setNegativeButton("取消", null).create();
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
	
	// 返回键监听事件
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
			txtShowWifi.setText(getWifiInfo());// 显示wifi信息
		} else {
			txtShowWifi.setText("未连接网络");
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