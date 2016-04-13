package org.easydarwin.video.recoder.core;

import java.io.File;

import org.easydarwin.video.common.OnErrorListener;
import org.easydarwin.video.common.ProgressDialogFactory;
import org.easydarwin.video.common.SimpleProgressDialogFactory;
import org.easydarwin.video.common.SimpleToastFactory;
import org.easydarwin.video.common.ToastFactory;
import org.easydarwin.video.recoder.VideoRecoder;
import org.easydarwin.video.recoder.activity.VideoRecorderActivity;
import org.easydarwin.video.recoder.conf.RecorderConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EasyVideoRecorder {
	private Context context;
	private OnCancelListener onCancelListener;
	private OnFinishListener onFinishListener;
	private OnErrorListener onErrorListener;
	private ProgressDialogFactory progressDialogFactory = new SimpleProgressDialogFactory();
	private ToastFactory toastFactory = new SimpleToastFactory();

	private RecorderConfig recorderConfig = RecorderConfig.create();

	public static final String TAG = "EasyVideoRecorder";

	private String key;

	private static EasyVideoRecorder easyVideoRecorder;

	private EasyVideoRecorder() {
	}

	public static EasyVideoRecorder getInstance() {
		if (easyVideoRecorder == null) {
			easyVideoRecorder = new EasyVideoRecorder();
		}
		return easyVideoRecorder;
	}

	public EasyVideoRecorder init(Context context) {
		this.context = context;
		return this;
	}

	public EasyVideoRecorder regist(String key) {
		this.key = key;
		return this;
	}

	public void start() {
		if (prepare()) {
			Intent itent = new Intent();
			itent.putExtra("config", recorderConfig);
			itent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			itent.setClass(context, VideoRecorderActivity.class);
			context.startActivity(itent);
		}
	}

	public RecorderConfig getRecorderConfig() {
		return recorderConfig;
	}

	public EasyVideoRecorder setRecorderConfig(RecorderConfig recorderConfig) {
		this.recorderConfig = recorderConfig;
		return this;
	}

	private boolean prepare() {
		createDefaultListener();
		File tmpDir = new File(recorderConfig.getVideoTmpDir());
		if (!tmpDir.exists()) {
			if (!tmpDir.mkdirs()) {
				onErrorListener.onError(205, "创建文件夹失败");
				return false;
			}
		}
		int rst = VideoRecoder.init(context, key);
		if (rst != 0) {
			onErrorListener.onError(206, "key无效:" + rst);
			return false;
		}
		return true;
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
	}

	public OnCancelListener getOnCancelListener() {
		return onCancelListener;
	}

	public EasyVideoRecorder setOnCancelListener(OnCancelListener onRecordCancelListener) {
		this.onCancelListener = onRecordCancelListener;
		return this;
	}

	public OnFinishListener getOnFinishListener() {
		return onFinishListener;
	}

	public EasyVideoRecorder setOnFinishListener(OnFinishListener onRecordFinishListener) {
		this.onFinishListener = onRecordFinishListener;
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

	public static class OnToastShowListener {
		public void onToastShow(String msg) {
		}
	}

//	@Override
//	public void onEvent(int code, Object... msg) {
//		if (code >= 200 && code <= 299) {
//			onErrorListener.onError(code, String.valueOf(msg));
//		}
//		if (code == 100) {
//			this.onCancelListener.onCancel((Activity) msg[0]);
//		}
//		if (code == 101) {
//			this.onFinishListener.onFinish((Activity) msg[0], msg[1] == null ? null : (String) msg[1]);
//		}
//	}

	public OnErrorListener getOnErrorListener() {
		return onErrorListener;
	}

	public EasyVideoRecorder setOnErrorListener(OnErrorListener onErrorListener) {
		this.onErrorListener = onErrorListener;
		return this;
	}

	public ProgressDialogFactory getProgressDialogFactory() {
		return progressDialogFactory;
	}

	public EasyVideoRecorder setProgressDialogFactory(ProgressDialogFactory progressDialogFactory) {
		this.progressDialogFactory = progressDialogFactory;
		return this;
	}

	public ToastFactory getToastFactory() {
		return toastFactory;
	}

	public EasyVideoRecorder setToastFactory(ToastFactory toastFactory) {
		this.toastFactory = toastFactory;
		return this;
	}

}
