package com.sinobpo.util;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	public String receivedTime = null;
	/**录音文件的绝对路径*/
	public String msg = null;
	
	public Message(){};

	public Message(String receivedTime,String msg){
		this.receivedTime = receivedTime;
		this.msg = msg;
	}
}
