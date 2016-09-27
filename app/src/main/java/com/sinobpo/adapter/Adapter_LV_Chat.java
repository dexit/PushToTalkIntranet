package com.sinobpo.adapter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import com.sinobpo.R;
import com.sinobpo.util.ChatContent;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 */
public class Adapter_LV_Chat extends BaseAdapter {
	private Context con;
	private LayoutInflater inflater;
	private ArrayList<ChatContent> list_data;
	private ListView lv_chat;

	public Adapter_LV_Chat(Context con, ArrayList<ChatContent> list_data,
			ListView lv_chat) {
		this.con = con;
		this.list_data = list_data;
		this.lv_chat = lv_chat;
	}

	public void refreshList(ArrayList<ChatContent> list_data) {
		this.list_data = list_data;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list_data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		this.inflater = (LayoutInflater) this.con
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		String tag = list_data.get(position).getTag();

		if (tag.equals("0")) {
			convertView = this.inflater.inflate(R.layout.received_msg_layout,
					null);

		} else {
			convertView = this.inflater.inflate(R.layout.send_msg_layout, null);
		}

		ImageView imgHead = (ImageView) convertView
				.findViewById(R.id.send_head_icon);
		TextView tv_name = (TextView) convertView
				.findViewById(R.id.send_nickename);
		TextView tv_time = (TextView) convertView
				.findViewById(R.id.send_msg_time);
		TextView tv_Boay = (TextView) convertView
				.findViewById(R.id.send_msg_content);
		TextView tv_filePath = (TextView) convertView
				.findViewById(R.id.send_msg_filePath);

		imgHead.setBackgroundResource(list_data.get(position).getImgHead());
		tv_name.setText(list_data.get(position).getName());
		tv_time.setText(list_data.get(position).getTime());
		tv_Boay.setText(list_data.get(position).getBody());
		tv_filePath.setText(list_data.get(position).getFilePath());
		return convertView;
	}
}
