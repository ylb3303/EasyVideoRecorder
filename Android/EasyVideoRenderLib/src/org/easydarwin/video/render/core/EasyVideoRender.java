package org.easydarwin.video.render.core;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.easydarwin.video.common.OnErrorListener;
import org.easydarwin.video.common.ProgressDialogFactory;
import org.easydarwin.video.common.SimpleProgressDialogFactory;
import org.easydarwin.video.common.SimpleToastFactory;
import org.easydarwin.video.common.ToastFactory;
import org.easydarwin.video.render.VideoRender;
import org.easydarwin.video.render.activity.VideoRenderActivity;
import org.easydarwin.video.render.conf.RenderConfig;
import org.easydarwin.video.render.util.AndroidUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class EasyVideoRender {
	private Context context;
	private OnCancelListener onCancelListener;
	private OnFinishListener onFinishListener;
	private OnErrorListener onErrorListener;
	private ProgressDialogFactory progressDialogFactory = new SimpleProgressDialogFactory();
	private ToastFactory toastFactory = new SimpleToastFactory();
	private RenderConfig config = RenderConfig.createDefault();

	public static final String TAG = "EasyVideoRender";
	public static final String INPUT_RES_URL = "input_res_url";

	private String key;
	private String inputVideo;
	private Map<Integer, String> moreRenderMap = new HashMap<Integer, String>();

	public static final int RENDER_TYPE_FILTER = 11;
	public static final int RENDER_TYPE_THEME = 12;
	public static final int RENDER_TYPE_FRAME = 13;
	public static final int RENDER_TYPE_MUSIC = 14;

	private static EasyVideoRender easyVideoRender;
	private EasyVideoRender() {
	}

	public static EasyVideoRender getInstance() {
		if (easyVideoRender == null) {
			easyVideoRender = new EasyVideoRender();
		}
		return easyVideoRender;
	}

	public EasyVideoRender init(Context context) {
		this.context = context;
		if (AndroidUtils.isProjectProcess(context)) {
			RenderResHelper.getInstance().initWithContext(context);
			ParamKeeper.get().setContext(context);
		}
		return this;
	}

	public EasyVideoRender regist(String key) {
		this.key = key;
		return this;
	}

	public void start() {
		if (prepare()) {
			Intent itent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("config", config);
			bundle.putString("video", inputVideo);
			bundle.putSerializable("moreRender", (Serializable) moreRenderMap);
			itent.putExtras(bundle);
			itent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			itent.setClass(context, VideoRenderActivity.class);
			context.startActivity(itent);
		}
	}

	private boolean prepare() {
		createDefaultListener();
		File tmpDir = new File(config.getVideoOutputDir());
		if (!tmpDir.exists()) {
			if (!tmpDir.mkdirs()) {
				onErrorListener.onError(205, "创建文件夹失败");
				return false;
			}
		}
		int rst = VideoRender.init(context, key);
		if (rst != 0) {
			onErrorListener.onError(206, "key无效:" + rst);
			return false;
		}
		return true;
	}

	public EasyVideoRender setMoreRenderAction(int type, String moreRenderAction) {
		moreRenderMap.put(type, moreRenderAction);
		return this;
	}

	private void createDefaultListener() {
		if (onErrorListener == null) {
			setOnErrorListener(new OnErrorListener() {

				@Override
				public void onError(int code, String message) {
					Log.e("EasyVideoRecorder", message);
					toastFactory.create(context, message).show();
				}
			});
		}
		if (onCancelListener == null) {
			setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(Activity activity) {
					super.onCancel(activity);
				}
			});
		}
		if (onFinishListener == null) {
			setOnFinishListener(new OnFinishListener() {

				@Override
				public void onFinish(Activity activity, String videoFile) {
					super.onFinish(activity, videoFile);
				}
			});
		}
	}

	public void destroy() {
		ParamKeeper.reset();
	}

	public EasyVideoRender setBaseDir(String baseDir) {
		config.setBaseDir(baseDir);
		return this;
	}

	public EasyVideoRender setEndLogoShow(boolean endLogoShow) {
		config.setEndLogoShow(endLogoShow);
		return this;
	}

	public String getInputVideo() {
		return inputVideo;
	}

	public EasyVideoRender setInputVideo(String inputVideo) {
		this.inputVideo = inputVideo;
		return this;
	}

	// listeners =================================================================

	public OnCancelListener getOnCancelListener() {
		return onCancelListener;
	}

	public EasyVideoRender setOnCancelListener(OnCancelListener onCancelListener) {
		this.onCancelListener = onCancelListener;
		return this;
	}

	public OnFinishListener getOnFinishListener() {
		return onFinishListener;
	}

	public EasyVideoRender setOnFinishListener(OnFinishListener onFinishListener) {
		this.onFinishListener = onFinishListener;
		return this;
	}

	public static class OnCancelListener {
		public void onCancel(Activity activity) {
			activity.finish();
			Log.i(TAG, "onRecordCancel");
		}
	}

	public static class OnFinishListener {
		public void onFinish(Activity activity, String videoFile) {
			activity.finish();
			Log.i(TAG, "onRecordFinish file:" + videoFile);
		}
	}

	public OnErrorListener getOnErrorListener() {
		return onErrorListener;
	}

	public EasyVideoRender setOnErrorListener(OnErrorListener onErrorListener) {
		this.onErrorListener = onErrorListener;
		return this;
	}

	public ProgressDialogFactory getProgressDialogFactory() {
		return progressDialogFactory;
	}

	public EasyVideoRender setProgressDialogFactory(ProgressDialogFactory progressDialogFactory) {
		this.progressDialogFactory = progressDialogFactory;
		return this;
	}

	public ToastFactory getToastFactory() {
		return toastFactory;
	}

	public EasyVideoRender setToastFactory(ToastFactory toastFactory) {
		this.toastFactory = toastFactory;
		return this;
	}
}
