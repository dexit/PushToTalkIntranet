package com.sinobpo.godtalk;

import com.sinobpo.R;
import com.sinobpo.daseHelper.ChartContentDaoImpl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

/**
 * Copyright (C) 2012 �ƶ�Ӧ���з���-���ĶԽ��� �汾�� ���ڣ� ������Logo���� ��������� �����ˣ��ƶ�Ӧ���з���-gxc
 */
public class LoadingAct extends Activity {
	private boolean isFirst;
    private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen_activity);
		
		SharedPreferences preferences = getSharedPreferences("shortcut",
				Context.MODE_PRIVATE);
		final boolean isFirst = preferences.getBoolean("isfrist", true);
		if (isFirst) { // ����ǵ�һ�����У���ת������ָ��ҳ��
			intent = new Intent(LoadingAct.this, GuiderActivity.class); // ͨ��Intent����������
		} else {// ������ǵ�һ�����У���ת����ҳ��
			intent = new Intent(LoadingAct.this, MainActivity.class); // ͨ��Intent������������������
		}

		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isfrist", false);
		editor.commit();

		Thread
				.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					public void uncaughtException(Thread thread, Throwable ex) {
						// ����һ���߳��쳣��ͳһ�Ĵ���
						finish();
					}
				});
		
		new Handler().postDelayed(new Runnable() { // Ϊ�˼��ٴ���ʹ������Handler����һ����ʱ�ĵ���
					public void run() {
						startActivity(intent);
						finish(); 
					}
				}, 2000); // 2��ͣ��
	}

	/**
	 * ������ݷ�ʽ
	 * http://www.open-open.com/lib/view/open1329995632249.html
	 */
	public void createDeskShortCut() {

		// ������ݷ�ʽ��Intent
		Intent shortcutIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// �������ظ�����
		shortcutIntent.putExtra("duplicate", false);
		// ��Ҫ��ʵ������
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));

		// ���ͼƬ
		Parcelable icon = Intent.ShortcutIconResource.fromContext(
				getApplicationContext(), R.drawable.icon);

		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

		Intent intent = new Intent(getApplicationContext(), this.getClass())
				.setAction(Intent.ACTION_MAIN);

		// ������ͼƬ�����еĳ��������
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		// ���͹㲥
		sendBroadcast(shortcutIntent);
	}
}
