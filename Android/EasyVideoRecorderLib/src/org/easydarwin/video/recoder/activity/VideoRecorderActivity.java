package org.easydarwin.video.recoder.activity;

import java.util.ArrayList;
import java.util.List;

import org.easydarwin.video.recoder.R;
import org.easydarwin.video.recoder.conf.RecorderConfig;
import org.easydarwin.video.recoder.core.EasyVideoRecorder;
import org.easydarwin.video.recoder.core.EventPoster;
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
	private VideoProgressView progressView;
	private VideoPreviewView previewView;
	private VideoFocusView focusView;
	private RecorderConfig config;

	private boolean finished = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.easy_video_record_activity);
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
		progressView = (VideoProgressView) findViewById(R.id.videoProgressView);
		progressView.init(config);
		previewView = (VideoPreviewView) findViewById(R.id.videoPreviewView);
		focusView = (VideoFocusView) findViewById(R.id.videoFocusView);

		focusView.setShowGrid(false);
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
			btnSwitchCamera.setVisibility(View.VISIBLE);
		}

		btnStartRecord.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return onBtnStartRecordTouch(v, event);
			}
		});
		focusView.setOnTouchListener(new OnTouchListener() {
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
		recorderManager = new RecorderManager(this, previewView, progressView, config);
		recorderManager.setVideoRecordListener(this);
		recorderManager.setVideoStatusChangeListener(this);
		recorderManager.setScreenSize(screenSize);

		if (!recorderManager.checkHasStorage()) {
			EventPoster.getInstance().postEvent(201, "存储卡不可用");
			finish();
		}
		if (recorderManager.getSDFreeSize() < 10) {
			EventPoster.getInstance().postEvent(202, "存储卡内存不足");
			finish();
		}
		recorderManager.checkPermission(this);
		if (recorderManager.getCheckPermissionReslut(RecorderManager.NO_CAMERA_PERMISSION)) {
			EventPoster.getInstance().postEvent(203, "无系统相机权限");
			finish();
		} else if (recorderManager.getCheckPermissionReslut(RecorderManager.NO_AUDIO_PERMISSION)) {
			EventPoster.getInstance().postEvent(204, "无系统录音权限");
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
				btnStartRecord.setBackgroundResource(R.drawable.easy_video_record_start_btn_pressed);
				pauseAudioPlayback();
				if (config.isShowGuide()) {
					focusView.showGuideStep(2);
				}
				recorderManager.startRecord();
				break;
			case MSG_PAUSERECORD:
				btnStartRecord.setBackgroundResource(R.drawable.easy_video_record_start_btn);
				if (config.isShowGuide()) {
					focusView.showGuideStep(3);
				}
				recorderManager.pauseRecord();
				break;
			case MSG_VIDEOCAMERA_READY:
				resetVideoLayout();
				handler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 300);
				break;
			case MSG_CHANGE_CAMERA:
				focusView.hideChangeCamera();
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
				focusView.setHaveTouch(false, new Rect(0, 0, 0, 0));
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
				btnCancelRecord.setBackgroundResource(R.drawable.easy_video_record_delete);
				break;
			case 2:
				btnCancelRecord.setBackgroundResource(R.drawable.easy_video_record_backspace);
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
							EventPoster.getInstance().postEvent(101, VideoRecorderActivity.this, file);
						} else {
							EventPoster.getInstance().postEvent(101, VideoRecorderActivity.this, null);
						}
						finish();
					}
				});
			}
		});
	}

	private void doAutoFocus() {
		boolean con = recorderManager.cameraManager().supportFocus() && recorderManager.cameraManager().isPreviewing();
		if (con) {
			if (mAllowTouchFocus && focusView != null && focusView.getWidth() > 0) {
				mAllowTouchFocus = false;
				int w = focusView.getWidth();
				Rect rect = doTouchFocus(w / 2, w / 2);
				if (rect != null) {
					focusView.setHaveTouch(true, rect);
				}
			}
		}
	}

	private Rect doTouchFocus(float x, float y) {
		int w = previewView.getWidth();
		int h = previewView.getHeight();
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
			focusView.hideChangeCamera();
		}
		if (config.isShowGuide()) {
			focusView.showGuideStep(1);
		}
		if (config.isShowMask()) {
			int screenWidth = screenSize[0];
			FrameLayout cameraPreviewArea = (FrameLayout) findViewById(R.id.cameraPreviewArea);
			int cameraPreviewAreaHeight = cameraPreviewArea.getHeight();
			int salt = screenWidth + (cameraPreviewAreaHeight - screenWidth) / 2;
			//
			View recorder_surface_mask1 = findViewById(R.id.recorder_surface_mask1);
			View recorder_surface_mask2 = findViewById(R.id.recorder_surface_mask2);
			recorder_surface_mask1.setVisibility(View.VISIBLE);
			recorder_surface_mask2.setVisibility(View.VISIBLE);
			//
			FrameLayout.LayoutParams layoutParam2 = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
			layoutParam2.bottomMargin = salt;

			FrameLayout.LayoutParams layoutParam3 = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
			layoutParam3.topMargin = salt;
			//
			recorder_surface_mask1.setLayoutParams(layoutParam2);
			recorder_surface_mask2.setLayoutParams(layoutParam3);
			//
			FrameLayout.LayoutParams layoutParam4 = (FrameLayout.LayoutParams) progressView.getLayoutParams();
			layoutParam4.topMargin = salt;
			//layoutParam4.bottomMargin = (cameraPreviewAreaHeight - screenWidth) / 2;
			progressView.setLayoutParams(layoutParam4);

			FrameLayout recorder_handl_area = (FrameLayout) findViewById(R.id.recorder_handl_area);
			FrameLayout.LayoutParams layoutParam5 = new FrameLayout.LayoutParams(screenWidth, screenWidth);
			layoutParam5.topMargin = (cameraPreviewAreaHeight - screenWidth) / 2;
			recorder_handl_area.setLayoutParams(layoutParam5);
		}
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
					if (focusView.isChangeCameraShow()) {
						focusView.hideChangeCamera();
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
		focusView.hideChangeCamera();
		return super.onTouchEvent(event);
	}

	public boolean onFocusViewTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				focusView.setDownY(event.getY());
				boolean con = recorderManager.cameraManager().supportFocus() && recorderManager.cameraManager().isPreviewing();
				if (con) {// 对焦
					if (mAllowTouchFocus) {
						mAllowTouchFocus = false;
						Rect rect = doTouchFocus(event.getX(), event.getY());
						if (rect != null) {
							focusView.setHaveTouch(true, rect);
						}
						handler.sendEmptyMessageDelayed(MSG_FOCUS_FINISH, 1000);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				float upY = event.getY();
				float dis = upY - focusView.getDownY();
				if (Math.abs(dis) >= 100) {
					if (recorderManager.cameraManager().cameraChangeEnable()) {
						focusView.changeCamreaFlipUp();
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
		recordEnd();
		focusView.finishGuide();
	}

	public void onBtnSwitchFlashClick(View v) {
		handler.sendEmptyMessage(MSG_CHANGE_FLASH);
	}

	public void onBtnBackClick(View v) {
		EventPoster.getInstance().postEvent(100, this);
	}
}
