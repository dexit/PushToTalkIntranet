package com.sinobpo.util;

import java.io.File;
import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * 
 * @author george
 *该类用来保存文件路径和文件名，同时实现Comparable接口，根据type的值来进行排序，
 *type=1代表当前存的是目录信息
 *type=2代表当前存的是文件信息
 *根据type的值从小到大排例，这样文件夹均被排在前面，文件排在后面
 */
public class FileName implements Comparable<FileName>,Serializable{
	private static final long serialVersionUID = 1L;
	
	/**w类型*/
	public int type = 0;
	/**大小*/
	public long fileSize = -1;
	/**文件名字*/
	public String fileName = "";
	/**是否是目录*/
	public boolean isDirectory = true;
	/**图标*/
	public Bitmap bitmap;
	/**名字*/
	public String name="";
	
	public FileName(){}
	
	public FileName(int type,String fileName){
		this.type = type;
		this.fileName = fileName;
	}
	public FileName(int type,String fileName,long fileSize,boolean isDirectory,Bitmap bitmap,String name){
		this.type = type;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.isDirectory = isDirectory;
		this.bitmap=bitmap;
		this.name=name;
	}
	
	public String getFileName(){
		int index = fileName.lastIndexOf(File.separator);
		return fileName.substring(index+1);
	}
	public String getName(){
		int index = name.lastIndexOf(File.separator);
		return name.substring(index+1);
	}
	public String getFullPath(){
		return fileName;
	}
	
	@Override
	public int compareTo(FileName fileN) {
		int result = -2;
		if(type < fileN.type)result = -1;
		if(type == fileN.type)result = 0;
		if(type > fileN.type)result = 1;
		return result;
	}
	
    @Override
	public int hashCode() {
	   int result = 56;
	   result = 56 * result + type ;
	   result = 56 * result + fileName.hashCode();
	   return result;
	}  
    
     @Override
	public boolean equals(Object o) {
    	if (!(o instanceof FileName))
    		return false ;
    	FileName fileN = (FileName) o;
    	return (type == fileN.type ) && ( fileName.equals(fileN.fileName ));
     }
}
