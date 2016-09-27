package com.sinobpo.util;

import java.io.Serializable;

/**
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ڵ���
 * �汾��
 * ���ڣ�
 * ��������Ա��Ϣ����
 * ����������
 * �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class Person implements Serializable{
	private static final long serialVersionUID = 1L;
	public int personId = 0;
	/**ͷ���Id*/
	public int personHeadIconId = 0;
	/**����*/
	public String personNickeName = null;
	/**IP��ַ*/
	public String ipAddress = null;
	public String loginTime = null;
	public long timeStamp = 0;
	public int groupId = 0;
	
	public Person(int personId,int personHeadIconId,String personNickeName,String ipAddress,String loginTime){
		this.personId = personId;
		this.personHeadIconId = personHeadIconId;
		this.personNickeName = personNickeName;
		this.ipAddress = ipAddress;
		this.loginTime = loginTime;
	}
	public Person(){}
}
