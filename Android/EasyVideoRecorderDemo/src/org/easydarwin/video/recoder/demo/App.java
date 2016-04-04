package org.easydarwin.video.recoder.demo;

import org.easydarwin.video.recoder.core.EasyVideoRecorder;

import android.app.Application;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		EasyVideoRecorder.getInstance().init(this).regist(getKey());
	}

	/**
	 * 获取密钥 ，正式环境中 应妥善保管密钥以免被盗取,并保证密钥正确，不正确的密钥将导致崩溃
	 * 
	 * @return key
	 */
	private String getKey() {
		return "WWpOS2JqreHRWbWhqTTJ4cldWaEtNMkZYTkhWa2JXeHJXbGM0ZFdOdFZtcGlNbEpzWTJrMWExcFhNWFpSUkVVd1RucFZlVTVVUlhsTlJFSkJUVkZUTw7i"; //测试key
	}

}
