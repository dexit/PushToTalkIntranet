package com.sinobpo.util;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	public String receivedTime = null;
	/**¼���ļ��ľ���·��*/
	public String msg = null;
	
	public Message(){};

	public Message(String receivedTime,String msg){
		this.receivedTime = receivedTime;
		this.msg = msg;
	}
}
