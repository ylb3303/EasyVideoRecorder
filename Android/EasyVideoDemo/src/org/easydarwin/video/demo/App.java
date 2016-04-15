package org.easydarwin.video.demo;

import org.easydarwin.video.recoder.core.EasyVideoRecorder;
import org.easydarwin.video.render.core.EasyVideoRender;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		CrashReport.initCrashReport(getApplicationContext(), "900025989", false);
		Bugly.init(getApplicationContext(), "900025989", false);
		
		EasyVideoRecorder.getInstance().init(this).regist(getRecorderKey());
		EasyVideoRender.getInstance().init(this).regist(getRenderKey());

		initImageLoader();
	}

	/**
	 * 获取密钥 ，正式环境中 应妥善保管密钥以免被盗取,并保证密钥正确，不正确的密钥将导致崩溃
	 * 
	 * @return key
	 */
	private String getRecorderKey() {
		return "WWpOS2JreUHRWbWhqTTJ4cldWaEtNMkZYTkhWa2JXeHJXbGM0ZFZwSFZuUmlNRUY0VGtSWmVVMUVUWGxOUkVGM1VVUkZNUTA90"; //测试key
	}

	private String getRenderKey() {
		return "WWpOS2JreLHRWbWhqTTJ4cldWaEtNMkZYTkhWa2JXeHJXbGM0ZFZwSFZuUmlNRUY0VGtSWmVVMUVUWGxOUkVGM1VVUkpUZ0g96"; //测试key
	}

	private void initImageLoader() {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
		ImageLoader.getInstance().init(config);
	}

}
