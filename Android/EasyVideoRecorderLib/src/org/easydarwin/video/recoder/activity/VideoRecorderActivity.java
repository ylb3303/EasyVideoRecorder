package org.easydarwin.video.recoder.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.easydarwin.video.recoder.R;
import org.easydarwin.video.recoder.conf.RecorderConfig;
import org.easydarwin.video.recoder.core.EasyVideoRecorder;
import org.easydarwin.video.recoder.core.RecorderManager;
import org.easydarwin.video.recoder.core.RecorderManager.VideoMergeListener;
import org.easydarwin.video.recoder.core.RecorderManager.VideoRecordListener;
import org.easydarwin.video.recoder.core.RecorderManager.VideoStatusChangeListener;
import org.easydarwin.video.recoder.view.VideoFocusView;
import org.easydarwin.video.recoder.view.VideoPreviewView;
import org.easydarwin.video.recoder.view.VideoProgressView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

@SuppressWarnings("deprecation")
public class VideoRecorderActivity extends Activity implements VideoRecordListener, VideoStatusChangeListener, Handler.Callback {
	private final static String TAG = VideoRecorderActivity.class.getSimpleName();

	private PowerManager.WakeLock mWakeLock;
	private RecorderManager recorderManager;

	private Handler handler;
	public int[] screenSize;
	private static final int MSG_VIDEOTIME_UPDATE = 0;
	private static final int MSG_VIDEOSEGMENT_UPDATE = 1;
	private static final int MSG_VIDEOCAMERA_READY = 2;
	private static final int MSG_STARTRECORD = 3;
	private static final int MSG_PAUSERECORD = 4;
	private static final int MSG_CHANGE_FLASH = 66;
	private static final int MSG_CHANGE_CAMERA = 8;
	private static final int MSG_AUTO_FOCUS = 9;
	private static final int MSG_FOCUS_FINISH = 10;

	public static final int REQUEST_VIDEOPROCESS = 5;

	private boolean mAllowTouchFocus = false;

	private Button btnBack;
	private Button btnSwitchFlash;
	private Button btnCancelRecord;
	private Button btnFinishRecord;
	private Button btnStartRecord;
	private Button btnSwitchCamera;
	private VideoProgressView videoProgressView;
	private VideoPreviewView videoPreviewView;
	private VideoFocusView videoFocusView;
	private RecorderConfig config;

	private boolean finished = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.easy_video_recorder_activity);
		initData();
		initView();
		initAction();
	}

	private void initData() {
		config = (RecorderConfig) getIntent().getSerializableExtra("config");

		int[] res = new int[2];
		Display display = getWindowManager().getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= 13) {
			Point size = new Point();
			display.getSize(size);
			res[0] = size.x;
			res[1] = size.y;
		} else {
			res[0] = display.getWidth();
			res[1] = display.getHeight();
		}
		screenSize = res;
	}

	private void initView() {
		btnBack = (Button) findViewById(R.id.btnBack);
		btnSwitchFlash = (Button) findViewById(R.id.btnSwitchFlash);
		btnCancelRecord = (Button) findViewById(R.id.btnCancelRecord);
		btnFinishRecord = (Button) findViewById(R.id.btnFinishRecord);
		btnStartRecord = (Button) findViewById(R.id.btnStartRecord);
		btnSwitchCamera = (Button) findViewById(R.id.btnSwitchCamera);
		videoProgressView = (VideoProgressView) findViewById(R.id.videoProgressView);
		videoProgressView.init(config);
		videoPreviewView = (VideoPreviewView) findViewById(R.id.videoPreviewView);
		videoFocusView = (VideoFocusView) findViewById(R.id.videoFocusView);

		videoFocusView.setShowGrid(false);
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
			btnSwitchCamera.setVisibility(View.VISIBLE);
		}

		btnStartRecord.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return onBtnStartRecordTouch(v, event);
			}
		});
		videoFocusView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return onFocusViewTouch(v, event);
			}
		});
		btnCancelRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBtnCancelRecordClick(v);
			}
		});
		btnSwitchCamera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBtnSwitchCameraClick(v);
			}
		});
		btnSwitchFlash.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBtnSwitchFlashClick(v);
			}
		});
		btnFinishRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBtnFinishRecordClick(v);
			}
		});
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBtnBackClick(v);
			}
		});
	}

	private void initAction() {
		handler = new Handler(this);
		recorderManager = new RecorderManager(this, videoPreviewView, videoProgressView, config);
		recorderManager.setVideoRecordListener(this);
		recorderManager.setVideoStatusChangeListener(this);
		recorderManager.setScreenSize(screenSize);

		if (!recorderManager.checkHasStorage()) {
			EasyVideoRecorder.getInstance().getOnErrorListener().onError(201, "存储卡不可用");
			finish();
		}
		if (recorderManager.getSDFreeSize() < 10) {
			EasyVideoRecorder.getInstance().getOnErrorListener().onError(202, "存储卡内存不足");
			finish();
		}
		recorderManager.checkPermission(this);
		if (recorderManager.getCheckPermissionReslut(RecorderManager.NO_CAMERA_PERMISSION)) {
			EasyVideoRecorder.getInstance().getOnErrorListener().onError(203, "无系统相机权限");
			finish();
		} else if (recorderManager.getCheckPermissionReslut(RecorderManager.NO_AUDIO_PERMISSION)) {
			EasyVideoRecorder.getInstance().getOnErrorListener().onError(204, "无系统录音权限");
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mWakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
			mWakeLock.acquire();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		recorderManager.pauseRecord();
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recorderManager.release();
	}

	@Override
	public void finish() {
		if (!finished) {
			super.finish();
			finished = true;
		}
	}

	@Override
	public void timeUpdate(long totalTime) {
		handler.obtainMessage(MSG_VIDEOTIME_UPDATE, (int) totalTime, 0).sendToTarget();
	}

	@Override
	public void segmentUpdate(int segment) {
		handler.obtainMessage(MSG_VIDEOSEGMENT_UPDATE, segment, 0).sendToTarget();
	}

	@Override
	public void cameraReady() {
		handler.obtainMessage(MSG_VIDEOCAMERA_READY).sendToTarget();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_VIDEOTIME_UPDATE:
				int tm = msg.arg1;
				if (tm < config.getRecordTimeMin()) {
					btnFinishRecord.setVisibility(View.INVISIBLE);
				} else if (tm >= config.getRecordTimeMin() && tm < config.getRecordTimeMax()) {
					btnFinishRecord.setVisibility(View.VISIBLE);
				} else if (tm >= config.getRecordTimeMax()) {
					recorderManager.pauseRecord();
					recordEnd();
				}
				break;
			case MSG_VIDEOSEGMENT_UPDATE:
				int se = msg.arg1;
				if (se < 1) {
					btnCancelRecord.setVisibility(View.INVISIBLE);
				} else {
					btnCancelRecord.setVisibility(View.VISIBLE);
				}
				break;
			case MSG_STARTRECORD:
				btnStartRecord.setSelected(true);
				pauseAudioPlayback();
				if (config.isShowGuide()) {
					videoFocusView.showGuideStep(2);
				}
				recorderManager.startRecord();
				break;
			case MSG_PAUSERECORD:
				btnStartRecord.setSelected(false);
				if (config.isShowGuide()) {
					videoFocusView.showGuideStep(3);
				}
				recorderManager.pauseRecord();
				break;
			case MSG_VIDEOCAMERA_READY:
				resetVideoLayout();
				handler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 300);
				break;
			case MSG_CHANGE_CAMERA:
				videoFocusView.hideChangeCamera();
				recorderManager.changeCamera();
				handler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 300);
				break;
			case MSG_CHANGE_FLASH:
				recorderManager.cameraManager().changeFlash();
				break;
			case MSG_AUTO_FOCUS:
				doAutoFocus();
				handler.sendEmptyMessageDelayed(MSG_FOCUS_FINISH, 1000);
				break;
			case MSG_FOCUS_FINISH:
				videoFocusView.setHaveTouch(false, new Rect(0, 0, 0, 0));
				mAllowTouchFocus = true;
				break;
		}
		return false;
	}

	public void pauseAudioPlayback() {
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		sendBroadcast(i);
	}

	@Override
	public void onVideoStatusChange(int status) {
		switch (status) {
			case 0:
				break;
			case 1:
				btnCancelRecord.setBackgroundResource(R.drawable.easy_video_recorder_delete);
				break;
			case 2:
				btnCancelRecord.setBackgroundResource(R.drawable.easy_video_recorder_backspace);
				break;
		}
	}

	private void recordEnd() {
		final ProgressDialog dialog = EasyVideoRecorder.getInstance().getProgressDialogFactory().create(this);
		dialog.show();
		recorderManager.stopRecord(new VideoMergeListener() {
			@Override
			public void onComplete(final int status, final String file) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						if (status >= 0) {
							EasyVideoRecorder.getInstance().getOnFinishListener().onFinish(VideoRecorderActivity.this, file);
//							EventPoster.getInstance().postEvent(101,  , file);
						} else {
							EasyVideoRecorder.getInstance().getOnFinishListener().onFinish(VideoRecorderActivity.this, null);
//							EventPoster.getInstance().postEvent(101, VideoRecorderActivity.this, null);
						}
					}
				});
			}
		});
	}

	private void doAutoFocus() {
		boolean con = recorderManager.cameraManager().supportFocus() && recorderManager.cameraManager().isPreviewing();
		if (con) {
			if (mAllowTouchFocus && videoFocusView != null && videoFocusView.getWidth() > 0) {
				mAllowTouchFocus = false;
				int w = videoFocusView.getWidth();
				Rect rect = doTouchFocus(w / 2, w / 2);
				if (rect != null) {
					videoFocusView.setHaveTouch(true, rect);
				}
			}
		}
	}

	private Rect doTouchFocus(float x, float y) {
		int w = videoPreviewView.getWidth();
		int h = videoPreviewView.getHeight();
		int left = 0;
		int top = 0;
		if (x - VideoFocusView.FOCUS_IMG_WH / 2 <= 0) {
			left = 0;
		} else if (x + VideoFocusView.FOCUS_IMG_WH / 2 >= w) {
			left = w - VideoFocusView.FOCUS_IMG_WH;
		} else {
			left = (int) (x - VideoFocusView.FOCUS_IMG_WH / 2);
		}
		if (y - VideoFocusView.FOCUS_IMG_WH / 2 <= 0) {
			top = 0;
		} else if (y + VideoFocusView.FOCUS_IMG_WH / 2 >= w) {
			top = w - VideoFocusView.FOCUS_IMG_WH;
		} else {
			top = (int) (y - VideoFocusView.FOCUS_IMG_WH / 2);
		}
		Rect rect = new Rect(left, top, left + VideoFocusView.FOCUS_IMG_WH, top + VideoFocusView.FOCUS_IMG_WH);
		Rect targetFocusRect = new Rect(rect.left * 2000 / w - 1000, rect.top * 2000 / h - 1000, rect.right * 2000 / w - 1000, rect.bottom * 2000 / h - 1000);
		try {
			List<Camera.Area> focusList = new ArrayList<Camera.Area>();
			Area focusA = new Area(targetFocusRect, 1000);
			focusList.add(focusA);
			recorderManager.cameraManager().doFocus(focusList);
			return rect;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void resetVideoLayout() {
		if (recorderManager.cameraManager().flashEnable()) {
			btnSwitchFlash.setVisibility(View.VISIBLE);
		} else {
			btnSwitchFlash.setVisibility(View.GONE);
		}
		if (!recorderManager.cameraManager().cameraChangeEnable()) {
			videoFocusView.hideChangeCamera();
		}
		if (config.isShowGuide()) {
			videoFocusView.showGuideStep(1);
		}
		int screenWidth = screenSize[0];
		int areaHeight = screenSize[1] - getStatusBarHeight();
//		FrameLayout areaRecorder = (FrameLayout) findViewById(R.id.areaRecorder);
//		Rect outRect = new Rect();
//		areaRecorder.getLocalVisibleRect(outRect);
//		int areaHeight = areaRecorder.getHeight();
		int previewAreaHeight = videoPreviewView.getMeasuredHeight();
		//int salt = (previewAreaHeight - screenWidth) / 2;
		FrameLayout areaPreviewView = (FrameLayout) findViewById(R.id.areaPreviewView);
		RelativeLayout areaToolbar = (RelativeLayout) findViewById(R.id.areaToolbar);
		int toolHeight = areaToolbar.getMeasuredHeight();

		int progressHeight = videoProgressView.getMeasuredHeight();
//		FrameLayout.LayoutParams areaPreviewViewlayout = (LayoutParams) areaPreviewView.getLayoutParams();
//		areaPreviewViewlayout.height = previewAreaHeight;
//		if (config.getPreviewSize() == RecorderConfig.PREVIEW_SIZE_BIG) {
//			areaPreviewViewlayout.topMargin = areaToolbar.getMeasuredHeight();
//		}
//		areaPreviewView.setLayoutParams(areaPreviewViewlayout);

		View viewSurfaceMask1 = areaPreviewView.findViewById(R.id.viewSurfaceMask1);
		View viewSurfaceMask2 = areaPreviewView.findViewById(R.id.viewSurfaceMask2);
		int heightMask1 = 0;
		int heightMask2 = 0;
		LinearLayout.LayoutParams viewSurfaceMask1Layout = (LinearLayout.LayoutParams) viewSurfaceMask1.getLayoutParams();
		LinearLayout.LayoutParams viewSurfaceMask2Layout = (LinearLayout.LayoutParams) viewSurfaceMask2.getLayoutParams();
		if (config.getPreviewSize() == RecorderConfig.PREVIEW_SIZE_SMALL) {
			if (config.getProgressPostion() == RecorderConfig.PROGRESS_POSTION_TOP) {
				viewSurfaceMask1Layout.height = toolHeight + progressHeight;
			} else {
				viewSurfaceMask1Layout.height = toolHeight;
			}
			heightMask1 = viewSurfaceMask1Layout.height;
			viewSurfaceMask1.setLayoutParams(viewSurfaceMask1Layout);

			if (config.getProgressPostion() == RecorderConfig.PROGRESS_POSTION_TOP) {
				viewSurfaceMask2Layout.height = previewAreaHeight - screenWidth - toolHeight - progressHeight;
			} else {
			}
			heightMask2 = viewSurfaceMask2Layout.height;
			viewSurfaceMask2.setLayoutParams(viewSurfaceMask2Layout);
		} else {
			viewSurfaceMask1Layout.height = toolHeight;
			heightMask1 = viewSurfaceMask1Layout.height;
			viewSurfaceMask1.setLayoutParams(viewSurfaceMask1Layout);
		}

		LinearLayout.LayoutParams videoFocusViewLayout = (LinearLayout.LayoutParams) videoFocusView.getLayoutParams();
		videoFocusViewLayout.height = previewAreaHeight - heightMask1 - heightMask2;
		videoFocusView.setLayoutParams(videoFocusViewLayout);

		FrameLayout.LayoutParams videoProgressViewLayout = (LayoutParams) videoProgressView.getLayoutParams();
		if (config.getProgressPostion() == RecorderConfig.PROGRESS_POSTION_BOTTOM) {
			if (config.getPreviewSize() == RecorderConfig.PREVIEW_SIZE_SMALL) {
				videoProgressViewLayout.topMargin = screenWidth + toolHeight;
			} else {
				videoProgressViewLayout.topMargin = previewAreaHeight;
			}
		} else {
			videoProgressViewLayout.topMargin = toolHeight;
		}
		videoProgressView.setLayoutParams(videoProgressViewLayout);

		RelativeLayout areaBottom = (RelativeLayout) findViewById(R.id.areaBottom);
		FrameLayout.LayoutParams areaBottomLayout = (LayoutParams) areaBottom.getLayoutParams();

		if (config.getPreviewSize() == RecorderConfig.PREVIEW_SIZE_SMALL) {
			areaBottomLayout.height = areaHeight - toolHeight - progressHeight - screenWidth;
		} else {
			areaBottomLayout.height = areaHeight - previewAreaHeight;
		}
		areaBottom.setLayoutParams(areaBottomLayout);
		videoProgressView.setVisibility(View.VISIBLE);
		areaBottom.setVisibility(View.VISIBLE);
		areaPreviewView.setVisibility(View.VISIBLE);
	}

	private int getStatusBarHeight() {
		int statusBarHeight = 0;
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object o = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = (Integer) field.get(o);
			statusBarHeight = getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
			Rect frame = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			statusBarHeight = frame.top;
		}
		if (statusBarHeight == 0) {
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
			if (resourceId > 0) {
				statusBarHeight = getResources().getDimensionPixelSize(resourceId);
			}
		}
		return statusBarHeight;
	}

	// events --------------------------------------------------

	long startTime = 0;
	boolean recording = false;

	public boolean onBtnStartRecordTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (recording) {
					handler.sendEmptyMessage(MSG_PAUSERECORD);
					recording = false;
				} else {
					startTime = System.currentTimeMillis();
					recording = true;
					handler.sendEmptyMessage(MSG_STARTRECORD);
					if (videoFocusView.isChangeCameraShow()) {
						videoFocusView.hideChangeCamera();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (System.currentTimeMillis() - startTime > 500) {
					handler.sendEmptyMessage(MSG_PAUSERECORD);
					recording = false;
				}
				break;
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		videoFocusView.hideChangeCamera();
		return super.onTouchEvent(event);
	}

	public boolean onFocusViewTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				videoFocusView.setDownY(event.getY());
				boolean con = recorderManager.cameraManager().supportFocus() && recorderManager.cameraManager().isPreviewing();
				if (con) {// 对焦
					if (mAllowTouchFocus) {
						mAllowTouchFocus = false;
						Rect rect = doTouchFocus(event.getX(), event.getY());
						if (rect != null) {
							videoFocusView.setHaveTouch(true, rect);
						}
						handler.sendEmptyMessageDelayed(MSG_FOCUS_FINISH, 1000);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				float upY = event.getY();
				float dis = upY - videoFocusView.getDownY();
				if (Math.abs(dis) >= 100) {
					if (recorderManager.cameraManager().cameraChangeEnable()) {
						videoFocusView.changeCamreaFlipUp();
						handler.sendEmptyMessage(MSG_CHANGE_CAMERA);
					}
				}
				break;
		}
		return true;
	}

	public void onBtnCancelRecordClick(View v) {
		recorderManager.backspace();
	}

	public void onBtnSwitchCameraClick(View v) {
		handler.sendEmptyMessage(MSG_CHANGE_CAMERA);
	}

	public void onBtnFinishRecordClick(View v) {
		if (recording) {
			btnStartRecord.setSelected(false);
			if (config.isShowGuide()) {
				videoFocusView.showGuideStep(3);
			}
			recorderManager.pauseRecord();
		}
		recording = false;
		recordEnd();
		videoFocusView.finishGuide();
	}

	public void onBtnSwitchFlashClick(View v) {
		handler.sendEmptyMessage(MSG_CHANGE_FLASH);
	}

	public void onBtnBackClick(View v) {
//		EventPoster.getInstance().postEvent(100, this);
		EasyVideoRecorder.getInstance().getOnCancelListener().onCancel(this);
	}
}
