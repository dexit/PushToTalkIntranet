package com.sinobpo.util;

public class ChatContent {

	/**������*/
	public String fromIP;
	/**�����ߣ�������˭��*/
	public String toIP;
	/**ʱ��*/
	public String time;
	/**��������*/
	public String body;
	/**���*/
	public String chatID;
	/**ͷ��*/
	public Integer imgHead;
	/**����*/
	public String name;
	/**0���ҷ���   1���Է�����*/
	public String Tag;
	/**�������ļ��ȵ�·��*/
	public String filePath;
	
	public ChatContent(){
		
	}
	
	public ChatContent(String fromIp,String toIp,String time,String body,String chatId
			,Integer imgHead,String name,String Tag,String filePath){
	this.fromIP=fromIp;
	this.toIP=toIp;
	this.time=time;
	this.body=body;
	this.chatID=chatId;
	this.imgHead=imgHead;
	this.name=name;
	this.Tag=Tag;
	this.filePath=filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getImgHead() {
		return imgHead;
	}

	public void setImgHead(Integer imgHead) {
		this.imgHead = imgHead;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return Tag;
	}

	public void setTag(String tag) {
		Tag = tag;
	}

	public String getFromIP() {
		return fromIP;
	}

	public void setFromIP(String fromIP) {
		this.fromIP = fromIP;
	}

	public String getToIP() {
		return toIP;
	}

	public void setToIP(String toIP) {
		this.toIP = toIP;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getChatID() {
		return chatID;
	}

	public void setChatID(String chatID) {
		this.chatID = chatID;
	}

	
}
