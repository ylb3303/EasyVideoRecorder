package org.easydarwin.video.render.demo;

import org.easydarwin.video.render.core.EasyVideoRender;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DownloadFilterActivity extends Activity {

	Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_download_activity);
		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent in = new Intent();
				in.putExtra(EasyVideoRender.INPUT_RES_URL, "xxxxxx.zip");//回传资源部本地地址
				setResult(RESULT_OK, in);
				finish();
			}
		});
	}
}
