package com.sinobpo.util;

import java.io.Serializable;

/**
 * Copyright (C) 2012 移动应用研发组-室内导览
 * 版本：
 * 日期：
 * 描述：人员信息基类
 * 操作：整理
 * 操作人：移动应用研发组-gxc
 */
public class Person implements Serializable{
	private static final long serialVersionUID = 1L;
	public int personId = 0;
	/**头像的Id*/
	public int personHeadIconId = 0;
	/**名字*/
	public String personNickeName = null;
	/**IP地址*/
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
