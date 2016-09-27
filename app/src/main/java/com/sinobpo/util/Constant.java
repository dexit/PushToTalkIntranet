package com.sinobpo.util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import android.content.Intent;
import android.net.Uri;

import com.sinobpo.R;


/**
 * Copyright (C) 2012 移动应用研发组-神聊对讲机
 * 版本：
 * 日期：
 * 描述：常量
 * 操作：整理
 * 操作人：移动应用研发组-gxc
 */
public class Constant {
	
	public static Map<String,Integer> exts = new HashMap<String,Integer>();
	  static{
		  exts.put("doc", R.drawable.doc);exts.put("docx", R.drawable.doc);exts.put("xls", R.drawable.xls);exts.put("xlsx", R.drawable.xls);exts.put("ppt", R.drawable.ppt);exts.put("pptx", R.drawable.ppt);
		  exts.put("jpg", R.drawable.image);exts.put("jpeg", R.drawable.image);exts.put("gif", R.drawable.image);exts.put("png", R.drawable.image);exts.put("ico", R.drawable.image);
		  exts.put("apk", R.drawable.apk);exts.put("jar", R.drawable.jar);exts.put("rar", R.drawable.rar);exts.put("zip", R.drawable.rar);
		  exts.put("mp3", R.drawable.music);exts.put("wma", R.drawable.music);exts.put("aac", R.drawable.music);exts.put("ac3", R.drawable.music);exts.put("ogg", R.drawable.music);exts.put("flac", R.drawable.music);exts.put("midi", R.drawable.music);
		  exts.put("pcm", R.drawable.music);exts.put("wav", R.drawable.music);exts.put("amr", R.drawable.music);exts.put("m4a", R.drawable.music);exts.put("ape", R.drawable.music);exts.put("mid", R.drawable.music);exts.put("mka", R.drawable.music);
		  exts.put("svx", R.drawable.music);exts.put("snd", R.drawable.music);exts.put("vqf", R.drawable.music);exts.put("aif", R.drawable.music);exts.put("voc", R.drawable.music);exts.put("cda", R.drawable.music);exts.put("mpc", R.drawable.music);
		  exts.put("mpeg", R.drawable.video);exts.put("mpg", R.drawable.video);exts.put("dat", R.drawable.video);exts.put("ra", R.drawable.video);exts.put("rm", R.drawable.video);exts.put("rmvb", R.drawable.video);exts.put("mp4", R.drawable.video);
		  exts.put("flv", R.drawable.video);exts.put("mov", R.drawable.video);exts.put("qt", R.drawable.video);exts.put("asf", R.drawable.video);exts.put("wmv", R.drawable.video);exts.put("avi", R.drawable.video);
		  exts.put("3gp", R.drawable.video);exts.put("mkv", R.drawable.video);exts.put("f4v", R.drawable.video);exts.put("m4v", R.drawable.video);exts.put("m4p", R.drawable.video);exts.put("m2v", R.drawable.video);exts.put("dat", R.drawable.video);
		  exts.put("xvid", R.drawable.video);exts.put("divx", R.drawable.video);exts.put("vob", R.drawable.video);exts.put("mpv", R.drawable.video);exts.put("mpeg4", R.drawable.video);exts.put("mpe", R.drawable.video);exts.put("mlv", R.drawable.video);
		  exts.put("ogm", R.drawable.video);exts.put("m2ts", R.drawable.video);exts.put("mts", R.drawable.video);exts.put("ask", R.drawable.video);exts.put("trp", R.drawable.video);exts.put("tp", R.drawable.video);exts.put("ts", R.drawable.video);
	  }
	
	//自定义Action
	  /***/
	public static final String updateMyInformationAction = "com.android.flypigeon.updateMyInformation";
	/***/
	public static final String personHasChangedAction = "com.android.flypigeon.personHasChanged";
	/**接受到文字消息*/
	public static final String hasMsgUpdatedAction = "com.android.flypigeon.hasMsgUpdated";
	/***/
	public static final String receivedSendFileRequestAction = "com.android.flypigeon.receivedSendFileRequest";
	/***/
	public static final String refuseReceiveFileAction = "com.android.flypigeon.refuseReceiveFile";
	/***/
	public static final String remoteUserRefuseReceiveFileAction = "com.android.flypigeon.remoteUserRefuseReceiveFile";
	/***/
	public static final String dataReceiveErrorAction = "com.android.flypigeon.dataReceiveError";
	/***/
	public static final String dataSendErrorAction = "com.android.flypigeon.dataSendError";
	/***/
	public static final String whoIsAliveAction = "com.android.flypigeon.whoIsAlive";//询问当前那个Activity是激活状态
	/***/
	public static final String imAliveNow = "com.android.flypigeon.imAliveNow";
	/***/
	public static final String remoteUserUnAliveAction = "com.android.flypigeon.remoteUserUnAlive";
	/**收到来自服务层的文件发送状态通知*/
	public static final String fileSendStateUpdateAction = "com.android.flypigeon.fileSendStateUpdate";
	/**收到来自服务层的文件接收状态通知*/
	public static final String fileReceiveStateUpdateAction = "com.android.flypigeon.fileReceiveStateUpdate";
	/**收到来自服务层的语音接收状态通知*/
	public static final String receivedTalkRequestAction = "com.android.flypigeon.receivedTalkRequest";
	/***/
	public static final String acceptTalkRequestAction = "com.android.flypigeon.acceptTalkRequest";
	/***/
	public static final String remoteUserClosedTalkAction = "com.android.flypigeon.remoteUserClosedTalk";
	/**收到来自服务层的语音发送状态通知*/
	public static final String sendAudioAction="com.android.sinobpo.sendAudioAction";
	/**收到来自服务层的录音失败状态通知*/
	public static final String recorderFailAction="com.android.sinobpo.recorderFailAction";
	/***/
	public static final String remoteCallingAction="com.android.remoteCallingAction";
	/***/
	public static final String remoteFinishCallingAction="com.android.remoteFinishCallingAction";
	
	//系统Action
	//System Action declare
	public static final String bootCompleted = "android.intent.action.BOOT_COMPLETED";
	public static final String WIFIACTION="android.net.conn.CONNECTIVITY_CHANGE";
	public static final String ETHACTION = "android.intent.action.ETH_STATE";
	
	//生成唯一ID码
	public static int getMyId(){
		int id = (int)(Math.random()*1000000);
		return id;
	}
	
	//other 其它定义，另外消息长度为60个汉字，utf-8中定义一个汉字占3个字节，所以消息长度为180bytes
	//文件长度为30个汉字，所以总长度为90个字节
	public static final int bufferSize = 256;
	public static final int msgLength = 180;
	public static final int fileNameLength = 90;
	public static final int readBufferSize = 4096;//文件读写缓存
	public static final byte[] pkgHead = "AND".getBytes();
	public static final int CMD80 = 80;
	public static final int CMD81 = 81;
	public static final int CMD82 = 82;
	public static final int CMD83 = 83;
	public static final int CMD_TYPE1 = 1;
	public static final int CMD_TYPE2 = 2;
	public static final int CMD_TYPE3 = 3;
	public static final int OPR_CMD1 = 1;
	public static final int OPR_CMD2 = 2;
	public static final int OPR_CMD3 = 3;
	public static final int OPR_CMD4 = 4;
	public static final int OPR_CMD5 = 5;
	public static final int OPR_CMD6 = 6;
	public static final int OPR_CMD10 = 10;
	public static final String MULTICAST_IP = "239.9.9.1";
	public static final int PORT = 5760;
	public static final int AUDIO_PORT = 5761;
	
	//int to ip转换
	public static String intToIp(int i) {   
		String ip = ( (i >> 24) & 0xFF) +"."+((i >> 16 ) & 0xFF)+"."+((i >> 8 ) & 0xFF)+"."+(i & 0xFF );
		
		return ip;
	}
	
	/**其它定义*/
	public static final int FILE_RESULT_CODE = 1;
	/**是否要在文件选择器中显示文件*/
	public static final int SELECT_FILES = 1;
	/**文件选择器只显示文件夹*/
	public static final int SELECT_FILE_PATH = 2;
	//文件选择状态保存
	public static TreeMap<Integer,Boolean> fileSelectedState = new TreeMap<Integer,Boolean>();
	
	/***/
	public final static String TABLE_NAME_OF_CHARTCONTENT="chartContent"; 
	public final static String DATA_COLLECT="guo.db";
	public final static int DATEBASEVERSION=1;
	
	
	//转换文件大小  
 	  public static String formatFileSize(long fileS) {
	      DecimalFormat df = new DecimalFormat("#.00");
	      String fileSizeString = "";
	      if (fileS < 1024) {
	    	  fileSizeString = fileS+"B";
	       //   fileSizeString = df.format((double) fileS) + "B";
	      } else if (fileS < 1048576) {
	          fileSizeString = df.format((double) fileS / 1024) + "K";
	      } else if (fileS < 1073741824) {
	          fileSizeString = df.format((double) fileS / 1048576) + "M";
	      } else {
	          fileSizeString = df.format((double) fileS / 1073741824) + "G";
	      }
	      return fileSizeString;
	  }
 	  
 	  /**发送文件的设置该值为false,发送完毕设置为真（点击关闭后也设置为false）*/
 	  public static Boolean sendFileIsOver=false;
 	  /**判断接受语音是否接受完毕*/
 	  public static Boolean receiveAudio=false;
 	  
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
