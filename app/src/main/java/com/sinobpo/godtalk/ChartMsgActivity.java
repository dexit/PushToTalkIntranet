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
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ĶԽ��� �汾�� ���ڣ� ������������� ��������� �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class ChartMsgActivity extends Activity implements OnClickListener {
	private Person person = null;
	private Person me = null;
	/** ����� */
	private EditText chartMsg = null;
	/** ���Ͱ�ť */
	private Button chartMsgSend = null;
	/** ���֡�����ģʽ�л���ť */
	private ImageButton chartMsgFile = null;

	private MainService mService = null;
	private Intent mMainServiceIntent = null;
	private MyBroadcastRecv broadcastRecv = null;
	private IntentFilter bFilter = null;
	/** �����ListView */
	private ListView lv_chat;
	/** �жϱ����Ƿ�ɼ� */
	private boolean isPaused = false;
	/** �Ƿ�Զ���û��Ѿ��ر���ͨ���� */
	private boolean isRemoteUserClosed = false;
	/** ���յ��ĶԷ����������ļ��� */
	private ArrayList<FileState> receivedFileNames = null;
	/** ���͵��Է����ļ�����Ϣ */
	private ArrayList<FileState> beSendFileNames = null;
	/** �����ļ�Dialog�е�ListView�������� */
	private Adapter_ReceiveSendFileList receiveFileListAdapter = new Adapter_ReceiveSendFileList(
			this, 1);
	/** �����ļ�Dialog�е�ListView�������� */
	private Adapter_ReceiveSendFileList sendFileListAdapter = new Adapter_ReceiveSendFileList(
			this, 0);
	/** ��ʾ��fraLayChartMore����ѡ�����ļ����֣�ͼƬ����Ƶ�ȣ����ְ�ť */
	private ImageButton btnChartMore;
	/** ѡ�����ļ����֣�ͼƬ����Ƶ����Ƶ��Ӧ�ú��ѽ����ļ��� */
	private LinearLayout fraLayChartMore;
	/** �ײ��������ͷ��Ͱ�ť���� */
	private LinearLayout linLaySendMsg;
	/** ���԰�ť */
	private Button btnRecordVoice;
	/** ����ͼƬ */
	private Button btnChartSendImage;
	/** �������ס���ԡ���ť������Ļ�м���ʾ����ʾ���ڣ���ʾ�û���ʼ˵�� */
	private LinearLayout linLaySpeaker;
	/** ���ذ�ť */
	private Button btnBack;
	/** ������Ƶ */
	private Button btnChartSendAudio;
	/** ������Ƶ */
	private Button btnChartSendMedia;
	/** ����Ӧ�ã�apk�� */
	private Button btnChartSendApk;
	/** �ѽ����ļ� */
	private Button btnChartReceiveFile;
	// �����ļ���dialog��OK��ť
	private Button btn_receiver;
	private String filePath = "//sdcard/shenliao-sinobpo/";

	/** �����ļ�Dialog */
	private AlertDialog fileListDialog;
	private AlertDialog revFileDialog;

	private Adapter_LV_Chat adapter;
	private ArrayList<ChatContent> list_data = new ArrayList<ChatContent>();

	private ChartContentDaoImpl chartDaoImpl;
    /**�����¼*/
	private Button btn_chatLog;
	private String chatId = "";
	private String maxChatId = "";
	
	/**
	 * MainService�����뵱ǰActivity�İ�������
	 */
	private ServiceConnection sConnection = new ServiceConnection() {
		// ���ͻ�����Service��������ʱ
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((MainService.ServiceBinder) service).getService();
			showAudioMsg(person.personId);
			System.out.println("Service connected to activity...");
		}

		// ���ͻ�����Service��������ʱ
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

		/** ȡ�����������еĹ������� ��֪����������ʲô���� */
		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(this.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);

		// ��������̵���ģʽ�����ؼ����𣬲���ס�ؼ���
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		chartDaoImpl = new ChartContentDaoImpl(this);

		init();

		// ���������SD�������¼����ť
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			btnRecordVoice.setEnabled(false);
		}

		// ��ǰActivity���̨MainService���а�
		mMainServiceIntent = new Intent(this, MainService.class);
		bindService(mMainServiceIntent, sConnection, BIND_AUTO_CREATE);

		regBroadcastRecv();
	}
	

	/**
	 * �ؼ���ʼ��
	 */
	private void init() {
		/** ��ô��ݹ�����Person */
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
				if (tv_body.getText().toString().substring(0, 2).equals("����")) {
					System.out.println(" play yuyin!!!!");
					DataInputStream dis;
					try {
						System.out.println("aaaaaaaaaa");
						dis = new DataInputStream(new BufferedInputStream(
								new FileInputStream(filePath)));

						// �����Ƶ��������С
						int bufferSize = android.media.AudioTrack
								.getMinBufferSize(11025,
										AudioFormat.CHANNEL_CONFIGURATION_MONO,
										AudioFormat.ENCODING_PCM_16BIT);

						// ����������
						AudioTrack player = new AudioTrack(
								AudioManager.STREAM_MUSIC, 11025,
								AudioFormat.CHANNEL_CONFIGURATION_MONO,
								AudioFormat.ENCODING_PCM_16BIT, bufferSize,
								AudioTrack.MODE_STREAM);

						// ������������
						// player.setStereoVolume(1.0f, 1.0f);
						// ��ʼ��������
						player.play();
						byte[] audiodata = new byte[bufferSize / 4];// ��Ƶ��ȡ����
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
	 * ��ʼ�������¼
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

			if (chatId.equals("") || chatId.equals("null") || chatId == null) {// ���charID���ڣ�˵��û�и������й���Ϣ����
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
			} else {// ���������˽���������ֱ��ʹ�����chatId
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
	 * ���԰�ť����¼�
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
	 * ͨ��userID�ӷ������ϻ�ȡ����
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
		case R.id.btnBack:// ����
			finish();
			break;
		case R.id.chart_msg_send:// ����
			String msg = chartMsg.getText().toString();
			if (null == msg || msg.length() <= 0) {
				Toast.makeText(this, getString(R.string.content_is_empty),
						Toast.LENGTH_SHORT).show();
				return;
			}
			chartMsg.setText("");

			mService.sendMsg(person.personId, msg);
			// ������Ҳ��淶��Ч�ʽеͣ�ֱ�ӱ������ݻᵼ�²�ѯ�������������첽����������
			new saveToDatabase().execute(msg);
			break;
		case R.id.chart_msg_file:// ���֡�����ģʽ�л���ť
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
		case R.id.btnChartMore:// ��ʾ��fraLayChartMore����ѡ�����ļ����֣�ͼƬ����Ƶ�ȣ����ְ�ť
			if (fraLayChartMore.getVisibility() == View.GONE)
				fraLayChartMore.setVisibility(View.VISIBLE);
			else if (fraLayChartMore.getVisibility() == View.VISIBLE)
				fraLayChartMore.setVisibility(View.GONE);
			break;
		case R.id.btnChartSendImage:// ѡ�����ļ������еġ�ͼƬ��
			Intent intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "image");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		case R.id.btnChartSendAudio:// ѡ�����ļ������еġ���Ƶ��
			intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "audio");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		case R.id.btnChartSendMedia:// ѡ�����ļ������еġ���Ƶ��
			intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "media");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		case R.id.btnChartSendApk:// ѡ�����ļ������еġ�Ӧ�á�
			intent = new Intent(this, FileManagerActivity.class);
			intent.putExtra("selectType", Constant.SELECT_FILES);
			intent.putExtra("type", "apk");
			startActivityForResult(intent, Constant.FILE_RESULT_CODE);
			break;
		case R.id.btnChartReceiveFile:// ѡ�����ļ������еġ��ѽ����ļ���
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
	 * ��������
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
					.makeText(ChartMsgActivity.this, "����ɹ�������",
							Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * ��������
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
				if (chatId.equals("")) {// ���charID���ڣ�˵��û�и������й���Ϣ����
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
					.makeText(ChartMsgActivity.this, "����ɹ�������",
							Toast.LENGTH_SHORT).show();
		}

	}


	/**
	 * ��ʾ�յ���������Ϣ
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
					String content = "����"
							+ msg.msg.substring(msg.msg.length() - 6, msg.msg
									.length());
					chatId = chartDaoImpl.getChatID(me.ipAddress,
							person.ipAddress);
					if (chatId.equals("")) {// ���charID���ڣ�˵��û�и������й���Ϣ����
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
	 * ��Ƶ������
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

				// �����Ƶ��������С
				int bufferSize = android.media.AudioTrack.getMinBufferSize(
						11025, AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);

				// ����������
				AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC,
						11025, AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize,
						AudioTrack.MODE_STREAM);

				// ������������
				// player.setStereoVolume(1.0f, 1.0f);
				// ��ʼ��������
				player.play();
				byte[] audiodata = new byte[bufferSize / 4];// ��Ƶ��ȡ����
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
						filePath = fileSavePath;
						System.out
								.println("save wen jian !!!!!!!!!!!!!!!!!!!!");
					} else {// �ļ��в���д���޷������ļ���
						Toast.makeText(this,
								getString(R.string.folder_can_not_write),
								Toast.LENGTH_SHORT).show();
					}
				} else if (selectType == Constant.SELECT_FILES) {// ����յ������ļ�ѡ��ģʽ��˵��������Ҫ�����ļ�����ѵ�ǰѡ��������ļ����ظ�����㡣
					@SuppressWarnings("unchecked")
					final ArrayList<FileName> files = (ArrayList<FileName>) data
							.getExtras().get("files");
					mService.sendFiles(person.personId, files);// �ѵ�ǰѡ��������ļ����ظ������

					System.out.println("send wen jian !!!!!!!!!!!!!!!!!!!!");
					// ��ʾ�ļ������б�
					beSendFileNames = mService.getBeSendFileNames();// �ӷ������������Ҫ���͵��ļ����ļ���
					if (beSendFileNames.size() <= 0)
						return;
					sendFileListAdapter.setResources(beSendFileNames);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					// �����ҵ�����ΪTitle
					builder.setTitle(me.personNickeName);
					// ����Message�������ڷ����ļ����������ļ�ʱΪ�����ڸ��������ļ�����
					builder.setMessage(R.string.start_to_send_file);
					// ����IconΪ�ҵ�ͷ��
					builder.setIcon(me.personHeadIconId);
					// �Զ�λView,��ʾ�����ļ��б�
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
							.findViewById(R.id.receive_file_list);// ��Ҫ���յ��ļ��嵥
					lv.setAdapter(sendFileListAdapter);
					Button btn_ok = (Button) vi
							.findViewById(R.id.receive_file_okbtn);
					// �����ļ�ʱ���á����ܡ���ťΪ���ɼ�
					btn_ok.setVisibility(View.GONE);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);
					// ����ð�ť���������ļ�ѡ�����������ó��ļ���ѡ��ģʽ��ѡ��һ���������նԷ��ļ����ļ���
					btn_ok.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!finishedSendFile) {// ��������ļ��Ѿ����չ������ٴ��ļ���ѡ����
								Intent intent = new Intent(
										ChartMsgActivity.this,
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

					Constant.sendFileIsOver = false;
					/** ����handler�����ļ��Ƿ������ */
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
						btn_receiver.setText("��");
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
								"����ͨ�������á���ġ������ļ������в鿴", Toast.LENGTH_SHORT)
								.show();
					}
				}
				break;
			}

		}

	};

	// �㲥������
	private class MyBroadcastRecv extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.hasMsgUpdatedAction)) {// �յ�������Ϣ
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
					Constant.fileSendStateUpdateAction)) {// �յ����Է������ļ�����״̬֪ͨ
				beSendFileNames = mService.getBeSendFileNames();// ��õ�ǰ�����ļ�����״̬
				sendFileListAdapter.setResources(beSendFileNames);
				sendFileListAdapter.notifyDataSetChanged();// �����ļ������б�
			} else if (intent.getAction().equals(
					Constant.fileReceiveStateUpdateAction)) {// �յ����Է������ļ�����״̬֪ͨ
				receivedFileNames = mService.getReceivedFileNames();// ��õ�ǰ�����ļ�����״̬
				receiveFileListAdapter.setResources(receivedFileNames);
				receiveFileListAdapter.notifyDataSetChanged();// �����ļ������б�
			} else if (intent.getAction().equals(// �յ����Է�����������״̬֪ͨ
					Constant.receivedTalkRequestAction)) {
				showAudioMsg(person.personId);
			} else if (intent.getAction().equals(Constant.sendAudioAction)) {// ��������
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
//				smcView.setText("�����ѷ���");
//				smtView.setText(new Date().toLocaleString());
//				nView.setText(me.personNickeName);
				// chartMsgPanel.addView(view);
				
				new saveToDatabase().execute("�����ѷ���!");
			} else if (intent.getAction().equals(Constant.recorderFailAction)) {// ¼�����ʹ���
				linLaySpeaker.setVisibility(View.GONE);
				Toast.makeText(ChartMsgActivity.this, "¼����������",
						Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(
					Constant.remoteUserRefuseReceiveFileAction)) {// �Է��ܾ������ļ�
				Toast.makeText(ChartMsgActivity.this,
						getString(R.string.refuse_receive_file),
						Toast.LENGTH_SHORT).show();
				if (fileListDialog != null) {
					fileListDialog.dismiss();
				} else {
				}
			} else if (intent.getAction().equals(
					Constant.receivedSendFileRequestAction)) {// �����ļ�֪ͨ
				if (!isPaused) {// ��������ڿɼ�״̬����Ӧ�㲥,����һ����ʾ���Ƿ�Ҫ���շ��������ļ�
					receivedFileNames = mService.getReceivedFileNames();// �ӷ������������Ҫ���յ��ļ����ļ���
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
					btn_receiver = (Button) vi
							.findViewById(R.id.receive_file_okbtn);
					btn_receiver.setEnabled(true);
					Button btn_cancle = (Button) vi
							.findViewById(R.id.receive_file_cancel);

					// ����ð�ť���������ļ�ѡ�����������ó��ļ���ѡ��ģʽ��ѡ��һ���������նԷ��ļ����ļ���
					btn_receiver.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							btn_receiver.setEnabled(false);
							if (!finishedSendFile) {// ��������ļ��Ѿ����չ������ٴ��ļ���ѡ����
								Intent intent = new Intent(
										ChartMsgActivity.this,
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
							revFileDialog.dismiss();
//							ll
						}
					});

					Constant.sendFileIsOver = false;
					/** ����handler�����ļ��Ƿ������ */
					if (Constant.sendFileIsOver == false) {
						handler_SendFile.sendEmptyMessage(2);
					} else {
						if (receivedFileNames.size() == 1) {
							btn_receiver.setText("��");
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
									"����ͨ�������á���ġ������ļ������в鿴", Toast.LENGTH_SHORT)
									.show();
						}
					}
				}
			}
		}
	}

	/** ���ļ� **/
	private void openFile(String path) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		File f = new File(path);
		String type = Constant.getMIMEType(f.getName());
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}

	// �㲥������ע��
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
			// ���µ������BACK��ͬʱû���ظ�
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
