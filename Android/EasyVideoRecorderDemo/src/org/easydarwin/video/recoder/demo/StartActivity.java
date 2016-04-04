package org.easydarwin.video.recoder.demo;

import org.easydarwin.video.common.OnErrorListener;
import org.easydarwin.video.common.ToastFactory;
import org.easydarwin.video.recoder.core.EasyVideoRecorder;
import org.easydarwin.video.recoder.core.EasyVideoRecorder.OnFinishListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends Activity {

	Button startVideoRecord;
	EasyVideoRecorder easyVideoRecorder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		setRecorder();//设置 EasyVideoRecorder SDK 参数

		startVideoRecord = (Button) findViewById(R.id.startVideoRecord);

		startVideoRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EasyVideoRecorder.getInstance().start();//启动拍摄
			}
		});
	}

	private void setRecorder() {
		EasyVideoRecorder.getInstance()//
			.setRecordTimeMax(15 * 1000)
			//设置拍摄的最大长度，单位毫秒
			.setRecordTimeMin(2 * 1000)
			//设置拍摄最短长度 ，单位毫秒
			.setOnErrorListener(new OnErrorListener() { //设置出错回调函数 
				/**
				 * int code 错误码
				 * String message 错误信息
				 */
				@Override
				public void onError(int code, String message) {
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				}
			})
			.setToastFactory(new ToastFactory() {////设置toast 显示函数

				@Override
				public Toast create(Context arg0, String arg1) {
					return Toast.makeText(arg0, arg1, Toast.LENGTH_LONG);
				}
			})
			.setOnFinishListener(new OnFinishListener() {// 设置拍摄结果回调函数
				/**
				 * Activity activity 拍摄界面的activity
				 * String videoFile 拍摄后的视频文件地址，如果是null 则拍摄失败
				 */
				@Override
				public void onFinish(Activity activity, String videoFile) {
					if (videoFile == null) {
						Toast.makeText(getApplicationContext(), "拍摄失败", Toast.LENGTH_LONG).show();
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
