/*
	Copyright (c) 2013-2016 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/
package org.easydarwin.video;

import org.easydarwin.video.recoder.activity.VideoRecorderActivity;
import org.easydarwin.video.recoder.base.BaseActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import butterknife.InjectView;
import butterknife.OnClick;

public class StartActivity extends BaseActivity {

	@InjectView(R.id.start)
	Button start;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
	}

	@OnClick(R.id.start)
	public void onStartClick(View v) {
		startActivity(VideoRecorderActivity.class);
	}
}
