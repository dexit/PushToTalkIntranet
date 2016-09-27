package com.sinobpo.godtalk;

import com.sinobpo.R;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutGodTalkActivity extends Activity{
	WebView webAboutGodTalk;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_godtalk_activity);
		webAboutGodTalk=(WebView)findViewById(R.id.webAboutGodTalk);
		webAboutGodTalk.loadUrl("file:///android_asset/about.html");
	}
}
