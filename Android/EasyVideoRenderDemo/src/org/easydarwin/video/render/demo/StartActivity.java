package org.easydarwin.video.render.demo;

import java.io.File;

import org.easydarwin.video.render.core.EasyVideoRender;
import org.easydarwin.video.render.core.EasyVideoRender.OnFinishListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends Activity {

	Button startVideoRecord;
	EasyVideoRender easyVideoRecorder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		setRender();//初始化 EasyVideoRender SDK

		startVideoRecord = (Button) findViewById(R.id.startVideoRecord);

		startVideoRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!new File(Environment.getExternalStorageDirectory() + "/1/test.mp4").exists()) {
					Toast.makeText(getApplicationContext(), "test video not ready", Toast.LENGTH_LONG).show();
					return;
				}
				EasyVideoRender.getInstance().start();//启动拍摄
			}
		});
	}

	private void setRender() {
		EasyVideoRender.getInstance()//
			.setInputVideo(Environment.getExternalStorageDirectory() + "/1/test.mp4")
			.setMoreRenderAction(EasyVideoRender.RENDER_TYPE_FILTER, "org.easydarwin.video.render.demo.DownloadFilterActivity")
			.setOnFinishListener(new OnFinishListener() {// 设置渲染结果回调函数
				/**
				 * Activity activity 渲染界面的activity
				 * String videoFile 渲染后的视频文件地址，如果是null 则渲染失败
				 */
				@Override
				public void onFinish(Activity activity, String videoFile) {
					if (videoFile == null) {
						Toast.makeText(getApplicationContext(), "失败", Toast.LENGTH_LONG).show();
					} else {
						Intent intent = new Intent(getBaseContext(), VideoPlayActivity.class);
						intent.putExtra("path", videoFile);
						startActivity(intent);
						Toast.makeText(getApplicationContext(), videoFile, Toast.LENGTH_LONG).show();
					}
				}
			});
	}
}
