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
 * Copyright (C) 2012 移动应用研发组-神聊对讲机 版本： 日期： 描述：Logo界面 操作：添加 操作人：移动应用研发组-gxc
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
		if (isFirst) { // 如果是第一次运行，跳转到新手指引页面
			intent = new Intent(LoadingAct.this, GuiderActivity.class); // 通过Intent打开引导界面
		} else {// 如果不是第一次运行，跳转到主页面
			intent = new Intent(LoadingAct.this, MainActivity.class); // 通过Intent打开最终真正的主界面
		}

		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isfrist", false);
		editor.commit();

		Thread
				.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					public void uncaughtException(Thread thread, Throwable ex) {
						// 任意一个线程异常后统一的处理
						finish();
					}
				});
		
		new Handler().postDelayed(new Runnable() { // 为了减少代码使用匿名Handler创建一个延时的调用
					public void run() {
						startActivity(intent);
						finish(); 
					}
				}, 2000); // 2秒停留
	}

	/**
	 * 创建快捷方式
	 * http://www.open-open.com/lib/view/open1329995632249.html
	 */
	public void createDeskShortCut() {

		// 创建快捷方式的Intent
		Intent shortcutIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// 不允许重复创建
		shortcutIntent.putExtra("duplicate", false);
		// 需要现实的名称
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));

		// 快捷图片
		Parcelable icon = Intent.ShortcutIconResource.fromContext(
				getApplicationContext(), R.drawable.icon);

		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

		Intent intent = new Intent(getApplicationContext(), this.getClass())
				.setAction(Intent.ACTION_MAIN);

		// 点击快捷图片，运行的程序主入口
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		// 发送广播
		sendBroadcast(shortcutIntent);
	}
}
