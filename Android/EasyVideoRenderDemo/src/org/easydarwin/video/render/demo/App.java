package org.easydarwin.video.render.demo;

import java.io.File;

import org.easydarwin.video.render.core.EasyVideoRender;
import org.easydarwin.video.render.core.RenderResHelper;

import android.app.Application;
import android.os.Environment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		EasyVideoRender.getInstance().init(this).regist(getKey());
		initImageLoader();
		copyTestVideo();
	}

	/**
	 * 获取密钥 ，正式环境中 应妥善保管密钥以免被盗取,并保证密钥正确，不正确的密钥将导致崩溃
	 * 
	 * @return key
	 */
	private String getKey() {
		return "WWpOS22JreHRWbWhqTTJ4cldWaEtNMkZYTkhWa2JXeHJXbGM0ZFdOdFZuVmFSMVo1VEcxU2JHSlhPVUZOVkZFelRsUkpNVTFVU1hkTlJVRjVkZzU6n"; //测试key
	}

	private void initImageLoader() {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
		ImageLoader.getInstance().init(config);
	}

	private void copyTestVideo() {
		if (new File(Environment.getExternalStorageDirectory() + "/1/test.mp4").exists()) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				RenderResHelper.copyFileFromAssets(App.this, "test.mp4", Environment.getExternalStorageDirectory() + "/1/");
			}
		}).start();
	}
}
