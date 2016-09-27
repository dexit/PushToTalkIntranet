package com.sinobpo.godtalk;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sinobpo.R;
import com.sinobpo.adapter.Adapter_LV_Chat;
import com.sinobpo.daseHelper.ChartContentDaoImpl;
import com.sinobpo.service.MainService;
import com.sinobpo.util.ChatContent;
import com.sinobpo.util.Constant;
import com.sinobpo.util.FileName;
import com.sinobpo.util.FileState;
import com.sinobpo.util.Message;
import com.sinobpo.util.Person;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Copyright (C) 2012 移动应用研发组-神聊对讲机 版本： 日期： 描述：聊天界面 操作：添加 操作人：移动应用研发组-gxc
 */
public class ChartMsgActivity extends Activity implements OnClickListener {
	private Person person = null;
	private Person me = null;
	/** 输入框 */
	private EditText chartMsg = null;
	/** 发送按钮 */
	private Button chartMsgSend = null;
	/** 文字、语音模式切换按钮 */
	private ImageButton chartMsgFile = null;

	private MainService mService = null;
	private Intent mMainServiceIntent = null;
	private MyBroadcastRecv broadcastRecv = null;
	private IntentFilter bFilter = null;
	/** 聊天的ListView */
	private ListView lv_chat;
	/** 判断本身是否可见 */
	private boolean isPaused = false;
	/** 是否远程用户已经关闭了通话。 */
	private boolean isRemoteUserClosed = false;
	/** 接收到的对方传过来的文件名 */
	private ArrayList<FileState> receivedFileNames = null;
	/** 发送到对方的文件名信息 */
	private ArrayList<FileState> beSendFileNames = null;
	/** 接收文件Dialog中的ListView的适配器 */
	private Adapter_ReceiveSendFileList receiveFileListAdapter = new Adapter_ReceiveSendFileList(
			this, 1);
	/** 发送文件Dialog中的ListView的适配器 */
	private Adapter_ReceiveSendFileList sendFileListAdapter = new Adapter_ReceiveSendFileList(
			this, 0);
	/** 显示“fraLayChartMore”（选择发送文件布局：图片，音频等）布局按钮 */
	private ImageButton btnChartMore;
	/** 选择发送文件布局（图片、音频、视频、应用和已接受文件） */
	private LinearLayout fraLayChartMore;
	/** 底部的输入框和发送按钮布局 */
	private LinearLayout linLaySendMsg;
	/** 留言按钮 */
	private Button btnRecordVoice;
	/** 发送图片 */
	private Button btnChartSendImage;
	/** 点击“按住留言”按钮后在屏幕中间显示的提示窗口，提示用户开始说话 */
	private LinearLayout linLaySpeaker;
	/** 返回按钮 */
	private Button btnBack;
	/** 发送音频 */
	private Button btnChartSendAudio;
	/** 发送视频 */
	private Button btnChartSendMedia;
	/** 发送应用（apk） */
	private Button btnChartSendApk;
	/** 已接受文件 */
	private Button btnChartReceiveFile;
	// 接受文件的dialog的OK按钮
	private Button btn_receiver;
	private String filePath = "//sdcard/shenliao-sinobpo/";

	/** 发送文件Dialog */
	private AlertDialog fileListDialog;
	private AlertDialog revFileDialog;

	private Adapter_LV_Chat adapter;
	private ArrayList<ChatContent> list_data = new ArrayList<ChatContent>();

	private ChartContentDaoImpl chartDaoImpl;
    /**聊天记录*/
	private Button btn_chatLog;
	private String chatId = "";
	private String maxChatId = "";
	
	/**
	 * MainService服务与当前Activity的绑定连接器
	 */
	private ServiceConnection sConnection = new ServiceConnection() {
		// 当客户端与Service建立连接时
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((MainService.ServiceBinder) service).getService();
			showAudioMsg(person.personId);
			System.out.println("Service connected to activity...");
		}

		// 当客户端与Service建立连接时
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			System.out.println("Service disconnected to activity...");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chart_msg_activity);

		/** 取得正在运行中的工作程序， 不知道这两句有什么作用 */
		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(this.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);

		// 设置软键盘弹出模式（将控件顶起，不挡住控件）
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		chartDaoImpl = new ChartContentDaoImpl(this);

		init();

		// 如果不存在SD卡则禁用录音按钮
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			btnRecordVoice.setEnabled(false);
		}

		// 当前Activity与后台MainService进行绑定
		mMainServiceIntent = new Intent(this, MainService.class);
		bindService(mMainServiceIntent, sConnection, BIND_AUTO_CREATE);

		regBroadcastRecv();
	}
	

	/**
	 * 控件初始化
	 */
	private void init() {
		/** 获得传递过来的Person */
		Intent intent = getIntent();
		person = (Person) intent.getExtras().getSerializable("person");
		me = (Person) intent.getExtras().getSerializable("me");

		// ((ImageView)findViewById(R.id.my_head_icon)).setImageResource(person.personHeadIconId);
		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);
		((TextView) findViewById(R.id.my_nickename))
				.setText(person.personNickeName);
		chartMsg = (EditText) findViewById(R.id.chart_msg);
		chartMsgSend = (Button) findViewById(R.id.chart_msg_send);
		chartMsgSend.setOnClickListener(this);
		chartMsgFile = (ImageButton) findViewById(R.id.chart_msg_file);
		chartMsgFile.setOnClickListener(this);
		btnChartMore = (ImageButton) findViewById(R.id.btnChartMore);
		btnChartMore.setOnClickListener(this);
		fraLayChartMore = (LinearLayout) findViewById(R.id.fraLayChartMore);
		linLaySendMsg = (LinearLayout) findViewById(R.id.linLaySendMsg);
		btnRecordVoice = (Button) findViewById(R.id.btnRecordVoice);
		btnRecordVoice.setOnTouchListener(recordVoiceKeyListener);

		btnChartSendImage = (Button) findViewById(R.id.btnChartSendImage);
		btnChartSendAudio = (Button) findViewById(R.id.btnChartSendAudio);
		btnChartSendMedia = (Button) findViewById(R.id.btnChartSendMedia);
		btnChartSendApk = (Button) findViewById(R.id.btnChartSendApk);
		btnChartReceiveFile = (Button) findViewById(R.id.btnChartReceiveFile);
		btn_chatLog = (Button) findViewById(R.id.btn_chatLog);

		linLaySpeaker = (LinearLayout) findViewById(R.id.linLaySpeaker);
		btnChartSendImage.setOnClickListener(this);
		btnChartSendAudio.setOnClickListener(this);
		btnChartSendMedia.setOnClickListener(this);
		btnChartSendApk.setOnClickListener(this);
		btnChartReceiveFile.setOnClickListener(this);
		btn_chatLog.setOnClickListener(this);

		lv_chat = (ListView) findViewById(R.id.chat_list_CMAct);
		lv_chat.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		lv_chat.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				TextView tv_filePath = (TextView) view
						.findViewById(R.id.send_msg_filePath);
				String filePath = tv_filePath.getText().toString();

				TextView tv_body = (TextView) view
						.findViewById(R.id.send_msg_content);

				System.out.println("filePath=" + filePath);
				if (tv_body.getText().toString().substring(0, 2).equals("语音")) {
					System.out.println(" play yuyin!!!!");
					DataInputStream dis;
					try {
						System.out.println("aaaaaaaaaa");
						dis = new DataInputStream(new BufferedInputStream(
								new FileInputStream(filePath)));

						// 获得音频缓冲区大小
						int bufferSize = android.media.AudioTrack
								.getMinBufferSize(11025,
										AudioFormat.CHANNEL_CONFIGURATION_MONO,
										AudioFormat.ENCODING_PCM_16BIT);

						// 获得音轨对象
						AudioTrack player = new AudioTrack(
								AudioManager.STREAM_MUSIC, 11025,
								AudioFormat.CHANNEL_CONFIGURATION_MONO,
								AudioFormat.ENCODING_PCM_16BIT, bufferSize,
								AudioTrack.MODE_STREAM);

						// 设置喇叭音量
						// player.setStereoVolume(1.0f, 1.0f);
						// 开始播放声音
						player.play();
						byte[] audiodata = new byte[bufferSize / 4];// 音频读取缓存
						while (dis.available() > 0) {
							dis.read(audiodata);
							player.write(audiodata, 0, audiodata.length);
							player.flush();
						}
						player.stop();
						player.release();
						dis.close();
					} catch (Exception e) {
						System.out.println("bbbbbbbbbbbb");
						e.printStackTrace();
					}
				} else {
					System.out.println("not yuyin!!!!");
				}
			}
		});
		
		new InitChatContent().execute("");
	}

	/**
	 * 初始化聊天记录
	 * 
	 * @author Buddy
	 * 
	 */
	private class InitChatContent extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
<<<<<<< .mine
			Date date1=new Date();
			System.out.println("start "+date1.getTime());
			
			list_data=new ArrayList<ChatContent>();
			list_data=chartDaoImpl.getData(me.ipAddress, person.ipAddress);
=======
			System.out.println("person.personId=="+person.personId);
			List<Message> list_msgs = mService.getMessagesById(person.personId);
			System.out.println("list_msgs.size=="+list_msgs.size());
			Message msg = list_msgs.remove(0);
			
			chatId = chartDaoImpl.getChatID(me.ipAddress, person.ipAddress);
			System.out.println("InitChatContent   "+me.ipAddress + "    " + person.ipAddress
					+ "    chatId=" + chatId);

			if (chatId.equals("") || chatId.equals("null") || chatId == null) {// 如果charID等于，说明没有跟该人有过信息交互
				System.out.println("InitChatContent   "+" initData-- chatId == null");
				maxChatId = chartDaoImpl.getMaxChatID();
				System.out.println("maxChatId=" + maxChatId);
				System.out.println("list_msgs.size222=="+list_msgs.size());
				if(list_msgs.size()>0){
					System.out.println("zhi xing l ma ?");
					ChatContent chatContent = new ChatContent(person.ipAddress,
							me.ipAddress, msg.receivedTime, msg.msg, maxChatId,
							person.personHeadIconId, person.personNickeName,
							"1", "");
					System.out.println(person.ipAddress+"  "+me.ipAddress+"  "+msg.receivedTime+"  "+msg.msg
							+"  "+"  "+ maxChatId+"  "+"  "+person.personHeadIconId+"  "+person.personNickeName);
					chartDaoImpl.saveToCallLog(chatContent);
					System.out.println("save over!  ");
				}
			
				
				list_data = chartDaoImpl.getDataByChatId(maxChatId);
			} else {// 如果跟这个人交互过，就直接使用这个chatId
				System.out.println("InitChatContent   "+"initData-- chatId != null    chatId="
						+ chatId);
				System.out.println("list_msgs.size3333=="+list_msgs.size());
				if(list_msgs.size()>0){
					System.out.println("zhi xing l ma ?");
					ChatContent chatContent = new ChatContent(person.ipAddress,
							me.ipAddress, msg.receivedTime, msg.msg, maxChatId,
							person.personHeadIconId, person.personNickeName,
							"1", "");
					System.out.println(person.ipAddress+"  "+me.ipAddress+"  "+msg.receivedTime+"  "+msg.msg
							+"  "+"  "+ maxChatId+"  "+"  "+person.personHeadIconId+"  "+person.personNickeName);
					chartDaoImpl.saveToCallLog(chatContent);
					System.out.println("save over!  ");
				}
				
				list_data = chartDaoImpl.getDataByChatId(chatId);
			}
>>>>>>> .r1743
			return "over";
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
<<<<<<< .mine
			Date date2=new Date();
			System.out.println("start "+date2.getTime());
=======
			System.out.println("bbbbbbbbbbbb :  "+list_data.size());
>>>>>>> .r1743
			adapter = new Adapter_LV_Chat(ChartMsgActivity.this, list_data,
					lv_chat);
			lv_chat.setAdapter(adapter);
			System.out.println("InitChatContent   "+"initData over!!!!" + result);
		}

	}

	/**
	 * 留言按钮点击事件
	 */
	private OnTouchListener recordVoiceKeyListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				System.out.println("start talk......");
				linLaySpeaker.setVisibility(View.VISIBLE);
				mService.startTalk(person.personId);
				break;
			case MotionEvent.ACTION_UP:
				System.out.println("stop talk......");
				linLaySpeaker.setVisibility(View.GONE);
				mService.stopTalk(person.personId);
				break;
			}
			return false;
		}
	};
	
	/**
	 * 通过userID从服务器上获取数据
	 * @param personId
	 */
//	private void getDataForService(int personId) {
//		// TODO Auto-generated method stub
//		List<Message> msgs = mService.getMessagesById(personId);
//		Message msg = msgs.remove(0);
//		System.out.println("ChartAct  personId="+personId);
//		new saveReceiveDatabase().execute(msg.msg, msg.receivedTime);
//	}

	@Override
	public void onClick(View vi) {
		switch (vi.getId()) {
		case R.id.btnBack:// 返回
			finish();
			break;
		case R.id.chart_msg_send:// 发送
			String msg = chartMsg.getText().toString();
			if (null == msg || msg.length() <= 0) {
				Toast.makeText(this, getString(R.string.content_is_empty),
						Toast.LENGTH_SHORT).show();
				return;
			}
			chartMsg.setText("");

			mService.sendMsg(person.personId, msg);
			// 代码混乱不规范，效率叫低，直接保存数据会导致查询死机，所以起异步来保存数据
			new saveToDatabase().execute(msg);
			break;
		case R.id.chart_msg_file:// 文字、语音模式切换按钮
			if (chartMsgFile.getTag().equals("0")) {
				chartMsgFile.setBackgroundResource(R.drawable.btn_send_msg);
				linLaySendMsg.setVisibility(View.VISIBLE);
				fraLayChartMore.setVisibility(View.GONE);
				chartMsgFile.setTag("1");
			} else if (chartMsgFile.getTag().equals("1")) {
				chartMsgFile.setBackgroundResource(R.drawable.btn_talk_bg);
				linLaySendMsg.setVisibility(View.GONE);
				fraLayChartMore.setVisibility(View.GONE);
				chartMsgFile.setTag("0");
			}
			break;
		case R.id.btnChartMore:// 显示“fraLayChartMore”（选择发送文件布局：图片，音频等）布局按钮
			if (fraLayChartMore.getVisibility() == View.GONE)
				fraLayChartMore.setVisibility(View.VISIBLE);
			else if (fraLayChartMore.getVisibility() == View.VISIBLE)
				fraLayChartMore.setVisibility(View.GONE);
			break;
		case R.id.btnChartSendImage:// 选择发送文件类型中的“图片”
			Intent intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "image");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		case R.id.btnChartSendAudio:// 选择发送文件类型中的“音频”
			intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "audio");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		case R.id.btnChartSendMedia:// 选择发送文件类型中的“视频”
			intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "media");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		case R.id.btnChartSendApk:// 选择发送文件类型中的“应用”
			intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "apk");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		case R.id.btnChartReceiveFile:// 选择发送文件类型中的“已接受文件”
			intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "received");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			// intent = new Intent(this, ReceiveFileBrowserActivity.class);
			// startActivity(intent);
			break;
		case R.id.btn_chatLog:
<<<<<<< .mine
			intent = new Intent(this, ChatLogAct.class);
			intent.putExtra("meIP",me.ipAddress);
			intent.putExtra("personIP", person.ipAddress);
			startActivity(intent);
=======
			chatId = chartDaoImpl.getChatID(me.ipAddress, person.ipAddress);
			if(chatId.equals("")){
				chatId="";
			}
				intent = new Intent(this, ChatLogAct.class);
				intent.putExtra("chatID", chatId);
				startActivity(intent);
>>>>>>> .r1743
			break;
		}
	}

	/**
	 * 保存数据
	 * 
	 * @author Buddy
	 * 
	 */
	private class saveToDatabase extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			ChatContent chatContent = new ChatContent(me.ipAddress,
					person.ipAddress, new Date().toLocaleString(),
					params[0].toString(), "", me.personHeadIconId,
					me.personNickeName, "0", "");
			chartDaoImpl.saveToCallLog(chatContent);
			list_data.add(chatContent);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			adapter.refreshList(list_data);
			Toast
					.makeText(ChartMsgActivity.this, "保存成功！！！",
							Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * 保存数据
	 * 
	 * @author Buddy
	 * 
	 */
	private class saveReceiveDatabase extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
<<<<<<< .mine
			ChatContent chatContent = new ChatContent(person.ipAddress,
					me.ipAddress, params[1], params[0], "",
					person.personHeadIconId, person.personNickeName,
					"1", "");
			chartDaoImpl.saveToCallLog(chatContent);
			list_data.add(chatContent);
=======
			chatId = chartDaoImpl.getChatID(me.ipAddress, person.ipAddress);
			if (me.ipAddress.equals("")) {
				System.out.println("MyIp==null");
			} else {
				if (chatId.equals("")) {// 如果charID等于，说明没有跟该人有过信息交互
					System.out.println("saveReceiveDatabase  charId == null");
					maxChatId = chartDaoImpl.getMaxChatID();
					System.out.println("maxChatId=" + maxChatId);
					ChatContent chatContent = new ChatContent(person.ipAddress,
							me.ipAddress, params[1], params[0], chatId,
							person.personHeadIconId, person.personNickeName,
							"1", "");
					chartDaoImpl.saveToCallLog(chatContent);
					list_data.add(chatContent);
				} else {
					System.out.println("charId != null");
					ChatContent chatContent = new ChatContent(person.ipAddress,
							me.ipAddress, params[1], params[0], chatId,
							person.personHeadIconId, person.personNickeName,
							"1", "");
					chartDaoImpl.saveToCallLog(chatContent);
					list_data.add(chatContent);
				}
			}
>>>>>>> .r1743
			System.out.println("receive over!!!");
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			adapter.refreshList(list_data);
			Toast
					.makeText(ChartMsgActivity.this, "保存成功！！！",
							Toast.LENGTH_SHORT).show();
		}

	}


	/**
	 * 显示收到的语音信息
	 * 
	 * @param userId
	 */
	private void showAudioMsg(int userId) {
		List<Message> msgsAudio = mService.getAudioMessageById(userId);
		if (null != msgsAudio) {
			while (msgsAudio.size() > 0) {
				Message msg = msgsAudio.remove(0);
				File tempFile = new File(msg.msg);
				if (tempFile.length() > 0) {
					String content = "语音"
							+ msg.msg.substring(msg.msg.length() - 6, msg.msg
									.length());
					chatId = chartDaoImpl.getChatID(me.ipAddress,
							person.ipAddress);
					if (chatId.equals("")) {// 如果charID等于，说明没有跟该人有过信息交互
						maxChatId = chartDaoImpl.getMaxChatID();
						ChatContent chatContent = new ChatContent(
								person.ipAddress, me.ipAddress,
								msg.receivedTime, content, maxChatId,
								person.personHeadIconId,
								person.personNickeName, "1", msg.msg);
						chartDaoImpl.saveToCallLog(chatContent);
						list_data.add(chatContent);
					} else {
						ChatContent chatContent = new ChatContent(
								person.ipAddress, me.ipAddress,
								msg.receivedTime, content, chatId,
								person.personHeadIconId,
								person.personNickeName, "1", msg.msg);
						chartDaoImpl.saveToCallLog(chatContent);
						list_data.add(chatContent);
					}
					adapter.refreshList(list_data);
				} else {
					return;
				}
			}
		}
	}

	/**
	 * 音频播放类
	 * 
	 * @author Buddy
	 * 
	 */
	public class AudioPlay implements OnClickListener {
		String pcmPath;
		File recordingFile;

		AudioPlay(String pcmPath) {
			this.pcmPath = pcmPath;
			recordingFile = new File(pcmPath);
		}

		@Override
		public void onClick(View v) {
			DataInputStream dis;
			try {
				dis = new DataInputStream(new BufferedInputStream(
						new FileInputStream(recordingFile)));

				// 获得音频缓冲区大小
				int bufferSize = android.media.AudioTrack.getMinBufferSize(
						11025, AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);

				// 获得音轨对象
				AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC,
						11025, AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize,
						AudioTrack.MODE_STREAM);

				// 设置喇叭音量
				// player.setStereoVolume(1.0f, 1.0f);
				// 开始播放声音
				player.play();
				byte[] audiodata = new byte[bufferSize / 4];// 音频读取缓存
				while (dis.available() > 0) {
					dis.read(audiodata);
					player.write(audiodata, 0, audiodata.length);
					player.flush();
				}
				player.stop();
				player.release();
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
						filePath = fileSavePath;
						System.out
								.println("save wen jian !!!!!!!!!!!!!!!!!!!!");
					} else {// 文件夹不可写，无法保存文件！
						Toast.makeText(this,
								getString(R.string.folder_can_not_write),
								Toast.LENGTH_SHORT).show();
					}
				} else if (selectType == Constant.SELECT_FILES) {// 如果收到的是文件选择模式，说明现在是要发送文件，则把当前选择的所有文件返回给服务层。
					@SuppressWarnings("unchecked")
					final ArrayList<FileName> files = (ArrayList<FileName>) data
							.getExtras().get("files");
					mService.sendFiles(person.personId, files);// 把当前选择的所有文件返回给服务层

					System.out.println("send wen jian !!!!!!!!!!!!!!!!!!!!");
					// 显示文件发送列表
					beSendFileNames = mService.getBeSendFileNames();// 从服务层获得所有需要发送的文件的文件名
					if (beSendFileNames.size() <= 0)
						return;
					sendFileListAdapter.setResources(beSendFileNames);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					// 设置我的名字为Title
					builder.setTitle(me.personNickeName);
					// 设置Message：“正在发送文件”（接受文件时为：正在给您发送文件！）
					builder.setMessage(R.string.start_to_send_file);
					// 设置Icon为我的头像
					builder.setIcon(me.personHeadIconId);
					// 自定位View,显示发送文件列表
					View vi = getLayoutInflater().inflate(
							R.layout.request_file_popupwindow_layout, null);
					builder.setView(vi);
					fileListDialog = builder.show();
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
					// 发送文件时设置“接受”按钮为不可见
					btn_ok.setVisibility(View.GONE);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);
					// 如果该按钮被点击则打开文件选择器，并设置成文件夹选择模式，选择一个用来接收对方文件的文件夹
					btn_ok.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!finishedSendFile) {// 如果本次文件已经接收过了则不再打开文件夹选择器
								Intent intent = new Intent(
										ChartMsgActivity.this,
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

					Constant.sendFileIsOver = false;
					/** 启动handler监听文件是否发送完毕 */
					if (Constant.sendFileIsOver == false) {
						handler_SendFile.sendEmptyMessage(1);
					} else {
						fileListDialog.dismiss();
					}

				}
			}
		}
	}

	private Handler handler_SendFile = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if (Constant.sendFileIsOver == false) {
					sendEmptyMessageDelayed(1, 300);
				} else {
					fileListDialog.dismiss();
				}
				break;
			case 2:
				if (Constant.sendFileIsOver == false) {
					sendEmptyMessageDelayed(2, 300);
				} else {
					if (receivedFileNames.size() == 1) {
						btn_receiver.setText("打开");
						btn_receiver.setEnabled(true);
						btn_receiver.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								openFile(filePath + "/"
										+ receivedFileNames.get(0).fileName);
							}
						});
					} else {
						fileListDialog.dismiss();
						Toast.makeText(ChartMsgActivity.this,
								"可以通过“设置”里的“已收文件”进行查看", Toast.LENGTH_SHORT)
								.show();
					}
				}
				break;
			}

		}

	};

	// 广播接收器
	private class MyBroadcastRecv extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.hasMsgUpdatedAction)) {// 收到文字信息
				List<Message> msgs = mService.getMessagesById(person.personId);
				Message msg = msgs.remove(0);

				new saveReceiveDatabase().execute(msg.msg, msg.receivedTime);
			} else if (intent.getAction().equals(
					Constant.dataReceiveErrorAction)
					|| intent.getAction().equals(Constant.dataSendErrorAction)) {
				Toast
						.makeText(ChartMsgActivity.this,
								intent.getExtras().getString("msg"),
								Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(
					Constant.fileSendStateUpdateAction)) {// 收到来自服务层的文件发送状态通知
				beSendFileNames = mService.getBeSendFileNames();// 获得当前所有文件发送状态
				sendFileListAdapter.setResources(beSendFileNames);
				sendFileListAdapter.notifyDataSetChanged();// 更新文件接收列表
			} else if (intent.getAction().equals(
					Constant.fileReceiveStateUpdateAction)) {// 收到来自服务层的文件接收状态通知
				receivedFileNames = mService.getReceivedFileNames();// 获得当前所有文件接收状态
				receiveFileListAdapter.setResources(receivedFileNames);
				receiveFileListAdapter.notifyDataSetChanged();// 更新文件接收列表
			} else if (intent.getAction().equals(// 收到来自服务层的语音接状态通知
					Constant.receivedTalkRequestAction)) {
				showAudioMsg(person.personId);
			} else if (intent.getAction().equals(Constant.sendAudioAction)) {// 发送语音
//				View view = getLayoutInflater().inflate(
//						R.layout.send_msg_layout, null);
//				ImageView iView = (ImageView) view
//						.findViewById(R.id.send_head_icon);
//				TextView smcView = (TextView) view
//						.findViewById(R.id.send_msg_content);
//				TextView smtView = (TextView) view
//						.findViewById(R.id.send_msg_time);
//				TextView nView = (TextView) view
//						.findViewById(R.id.send_nickename);
//				iView.setImageResource(me.personHeadIconId);
//				smcView.setText("语音已发出");
//				smtView.setText(new Date().toLocaleString());
//				nView.setText(me.personNickeName);
				// chartMsgPanel.addView(view);
				
				new saveToDatabase().execute("语音已发出!");
			} else if (intent.getAction().equals(Constant.recorderFailAction)) {// 录音发送错误
				linLaySpeaker.setVisibility(View.GONE);
				Toast.makeText(ChartMsgActivity.this, "录音发生错误！",
						Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(
					Constant.remoteUserRefuseReceiveFileAction)) {// 对方拒绝接收文件
				Toast.makeText(ChartMsgActivity.this,
						getString(R.string.refuse_receive_file),
						Toast.LENGTH_SHORT).show();
				if (fileListDialog != null) {
					fileListDialog.dismiss();
				} else {
				}
			} else if (intent.getAction().equals(
					Constant.receivedSendFileRequestAction)) {// 接受文件通知
				if (!isPaused) {// 如果自身处于可见状态则响应广播,弹出一个提示框是否要接收发过来的文件
					receivedFileNames = mService.getReceivedFileNames();// 从服务层获得所有需要接收的文件的文件名
					receiveFileListAdapter.setResources(receivedFileNames);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							context);
					builder.setTitle(person.personNickeName);
					builder.setMessage(R.string.sending_file_to_you);
					builder.setIcon(person.personHeadIconId);
					View vi = getLayoutInflater().inflate(
							R.layout.request_file_popupwindow_layout, null);
					builder.setView(vi);
					revFileDialog = builder.show();
					revFileDialog
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
					btn_receiver = (Button) vi
							.findViewById(R.id.receive_file_okbtn);
					btn_receiver.setEnabled(true);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);

					// 如果该按钮被点击则打开文件选择器，并设置成文件夹选择模式，选择一个用来接收对方文件的文件夹
					btn_receiver.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							btn_receiver.setEnabled(false);
							if (!finishedSendFile) {// 如果本次文件已经接收过了则不再打开文件夹选择器
								Intent intent = new Intent(
										ChartMsgActivity.this,
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
							revFileDialog.dismiss();
//							ll
						}
					});

					Constant.sendFileIsOver = false;
					/** 启动handler监听文件是否发送完毕 */
					if (Constant.sendFileIsOver == false) {
						handler_SendFile.sendEmptyMessage(2);
					} else {
						if (receivedFileNames.size() == 1) {
							btn_receiver.setText("打开");
							btn_receiver.setEnabled(true);
							btn_receiver
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											// TODO Auto-generated method stub
											openFile(filePath
													+ "/"
													+ receivedFileNames.get(0).fileName);
										}
									});
						} else {
							fileListDialog.dismiss();
							Toast.makeText(ChartMsgActivity.this,
									"可以通过“设置”里的“已收文件”进行查看", Toast.LENGTH_SHORT)
									.show();
						}
					}
				}
			}
		}
	}

	/** 打开文件 **/
	private void openFile(String path) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		File f = new File(path);
		String type = Constant.getMIMEType(f.getName());
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}

	// 广播接收器注册
	private void regBroadcastRecv() {
		broadcastRecv = new MyBroadcastRecv();
		bFilter = new IntentFilter();
		bFilter.addAction(Constant.hasMsgUpdatedAction);
		bFilter.addAction(Constant.receivedSendFileRequestAction);
		bFilter.addAction(Constant.fileReceiveStateUpdateAction);
		bFilter.addAction(Constant.fileSendStateUpdateAction);
		bFilter.addAction(Constant.receivedTalkRequestAction);
		bFilter.addAction(Constant.sendAudioAction);
		bFilter.addAction(Constant.remoteUserRefuseReceiveFileAction);
		bFilter.addAction(Constant.recorderFailAction);
		registerReceiver(broadcastRecv, bFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isPaused = false;
		// new InitChatContent().execute(null);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// 按下的如果是BACK，同时没有重复
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(sConnection);
		unregisterReceiver(broadcastRecv);
	}
}
