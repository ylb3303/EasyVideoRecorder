package org.easydarwin.video.render.activity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

import org.easydarwin.video.common.LoadCallbackListener;
import org.easydarwin.video.common.SimpleListener;
import org.easydarwin.video.render.R;
import org.easydarwin.video.render.adapter.MusicDisplyerAdapter;
import org.easydarwin.video.render.adapter.RenderDisplyerAdapter;
import org.easydarwin.video.render.conf.RenderConfig;
import org.easydarwin.video.render.core.EasyVideoRender;
import org.easydarwin.video.render.core.ParamKeeper;
import org.easydarwin.video.render.core.RenderProcessTask;
import org.easydarwin.video.render.core.RenderResHelper;
import org.easydarwin.video.render.model.RenderDisplyer;
import org.easydarwin.video.render.util.AndroidUtils;
import org.easydarwin.video.render.util.AnimatorListenerWraper;
import org.easydarwin.video.render.view.HorizontalListView;
import org.easydarwin.video.render.view.SwitchButton;
import org.easydarwin.video.render.view.SwitchButton.OnStateChangedListener;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class VideoRenderActivity extends Activity implements OnClickListener, RenderProcessTask.ProcessListener {
	private final static String TAG = "VideoBeautifyActivity";

	private Button mBackBtn;
	private Button mSubmitBtn;
	private Button vPlayBtn;
	private ProgressDialog processDialog;
	private GPUImageView mGPUImageView;

	private RenderProcessTask processTask;

	private PowerManager.WakeLock mWakeLock;

	private RenderConfig renderConfig;
	private Map<Integer, String> moreRenderMap = new HashMap<Integer, String>();
	private boolean finished = false;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_beautify_activity);
		init();
		initAction();
		initBottomArea();
	}

	@Override
	public void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		if (mWakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
			mWakeLock.acquire();
		}
		mGPUImageView.onResume();
		startPreview(false);
	}

	@Override
	protected void onPause() {
		mGPUImageView.onPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		cancelLastTask();
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		cancelLastTask();
		ParamKeeper.reset();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EasyVideoRender.RENDER_TYPE_FILTER) {
			if (resultCode == RESULT_OK && data != null) {
				String resUrl = data.getStringExtra(EasyVideoRender.INPUT_RES_URL);
				boolean vaild = RenderResHelper.getInstance().isValidRenderRes(resUrl);
				if (vaild) {
					addNewRenderRes(resUrl, RenderResHelper.RES_FILTER);
				} else {
					showToast("无效的资源包");
				}
			}
		}
		if (requestCode == EasyVideoRender.RENDER_TYPE_THEME) {
			if (resultCode == RESULT_OK && data != null) {
				String resUrl = data.getStringExtra(EasyVideoRender.INPUT_RES_URL);
				boolean vaild = RenderResHelper.getInstance().isValidRenderRes(resUrl);
				if (vaild) {
					addNewRenderRes(resUrl, RenderResHelper.RES_THEME);
				} else {
					showToast("无效的资源包");
				}
			}
		}
		if (requestCode == EasyVideoRender.RENDER_TYPE_FRAME) {
			if (resultCode == RESULT_OK && data != null) {
				String resUrl = data.getStringExtra(EasyVideoRender.INPUT_RES_URL);
				boolean vaild = RenderResHelper.getInstance().isValidRenderRes(resUrl);
				if (vaild) {
					addNewRenderRes(resUrl, RenderResHelper.RES_FRAME);
				} else {
					showToast("无效的资源包");
				}
			}
		}
		if (requestCode == EasyVideoRender.RENDER_TYPE_MUSIC) {
			if (resultCode == RESULT_OK && data != null) {
				String resUrl = data.getStringExtra(EasyVideoRender.INPUT_RES_URL);
				boolean vaild = RenderResHelper.getInstance().isValidRenderRes(resUrl);
				if (vaild) {
					addNewRenderRes(resUrl, RenderResHelper.RES_MUSIC);
				}
			} else {
				showToast("无效的资源包");
			}
		}
	}

	@Override
	public void finish() {
		if (!finished) {
			super.finish();
			finished = true;
		}
	}

//==========================  life cycle ======================= 
	public Activity getActivity() {
		return this;
	}

	private void addNewRenderRes(String resUrl, final String type) {
		RenderResHelper.getInstance().newAddResTask(resUrl, type, new SimpleListener() {
			@Override
			public void onResult(Object resut) {
				if (type.equals(RenderResHelper.RES_FILTER)) {
					filterDisplyerAdapter.notifyDataSetChanged();
				}
				if (type.equals(RenderResHelper.RES_THEME)) {
					themeDisplyerAdapter.notifyDataSetChanged();
				}
				if (type.equals(RenderResHelper.RES_FRAME)) {
					frameDisplyerAdapter.notifyDataSetChanged();
				}
				if (type.equals(RenderResHelper.RES_MUSIC)) {
					musicDisplyerAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void init() {
		mBackBtn = (Button) findViewById(R.id.easyVideoRenderBtnBack);
		mSubmitBtn = (Button) findViewById(R.id.easyVideoRenderBtnSubmit);
		vPlayBtn = (Button) findViewById(R.id.easyVideoRenderBtnPlay);
		mGPUImageView = (GPUImageView) findViewById(R.id.easyVideoRenderGpuImage);
		mBackBtn.setOnClickListener(this);
		mSubmitBtn.setOnClickListener(this);
		vPlayBtn.setOnClickListener(this);
		setVideoScale();
		vPlayBtn.setVisibility(View.INVISIBLE);
		ParamKeeper.get().setGPUImageView(mGPUImageView);
	}

	@SuppressWarnings("unchecked")
	public void initAction() {
		String inputVideo = getIntent().getStringExtra("video");
		renderConfig = (RenderConfig) getIntent().getSerializableExtra("config");
		Map<Integer, String> map = (HashMap<Integer, String>) getIntent().getSerializableExtra("moreRender");
		if (map != null) {
			moreRenderMap = map;
		}
		checkVideo(inputVideo);
		ParamKeeper.get().setVideoUri(Uri.parse(inputVideo))//
			.setAddEndLogo(renderConfig.isEndLogoShow())
			.setEndLogoDuration(renderConfig.getEndLogoDuration());
	}

	private void checkVideo(String inputVideo) {
		if (TextUtils.isEmpty(inputVideo)) {
			onError(301, "视频文件为空");
			return;
		}
		if (!new File(inputVideo).exists()) {
			onError(302, "视频文件不存在");
			return;
		}
		if (!inputVideo.endsWith(".mp4")) {
			onError(303, "视频文件不合法");
			return;
		}
	}

	private View areaBottomMenu;
	private View arrowRenderListShow;
	private View areaRenderList;

	private void initBottomArea() {
		areaBottomMenu = findViewById(R.id.areaBottomMenu);
		areaRenderList = findViewById(R.id.areaRenderList);
		arrowRenderListShow = findViewById(R.id.arrowRenderListShow);
		arrowRenderListShow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideRenderList();
			}
		});

		areaRenderList.setOnTouchListener(new OnTouchListener() {
			float lastY = 0;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						lastY = event.getY();
						return true;
					case MotionEvent.ACTION_MOVE:
					case MotionEvent.ACTION_UP:
						System.out.println(lastY - event.getY());
						if (event.getY() - lastY >= 10) {
							hideRenderList();
						}
						break;
				}
				return false;
			}
		});
		initTheme();
		initFilter();
		initFrame();
		initMusic();
	}

	private RenderDisplyerAdapter themeDisplyerAdapter;
	private View btnMenuTheme;
	private HorizontalListView mThemeListView;

	private void initTheme() {
		mThemeListView = (HorizontalListView) findViewById(R.id.themelistview);
		themeDisplyerAdapter = new RenderDisplyerAdapter(this, null);
		mThemeListView.setAdapter(themeDisplyerAdapter);
		mThemeListView.setOnItemClickListener(itemClickListener);
		boolean loadMore = !TextUtils.isEmpty(moreRenderMap.get(EasyVideoRender.RENDER_TYPE_THEME));
		RenderResHelper.getInstance()//
			.loadRenderDisplyer(RenderResHelper.RES_THEME, loadMore, new LoadCallbackListener<List<RenderDisplyer>>() {

				@Override
				public void onCallback(List<RenderDisplyer> list) {
					themeDisplyerAdapter.setData(list);
				}
			});
		btnMenuTheme = areaBottomMenu.findViewById(R.id.video_beautify_menu_theme);
		btnMenuTheme.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showRenderListArea(mThemeListView);
			}
		});
	}

	private RenderDisplyerAdapter filterDisplyerAdapter;
	private HorizontalListView mFilterListView;
	private View btnMenuFilter;

	private void initFilter() {
		mFilterListView = (HorizontalListView) findViewById(R.id.filterlistview);
		filterDisplyerAdapter = new RenderDisplyerAdapter(this, null);
		mFilterListView.setAdapter(filterDisplyerAdapter);
		mFilterListView.setOnItemClickListener(itemClickListener);
		boolean loadMore = !TextUtils.isEmpty(moreRenderMap.get(EasyVideoRender.RENDER_TYPE_FILTER));
		RenderResHelper.getInstance()//
			.loadRenderDisplyer(RenderResHelper.RES_FILTER, loadMore, new LoadCallbackListener<List<RenderDisplyer>>() {
				@Override
				public void onCallback(List<RenderDisplyer> list) {
					filterDisplyerAdapter.setData(list);
				}
			});
		btnMenuFilter = areaBottomMenu.findViewById(R.id.video_beautify_menu_filter);
		btnMenuFilter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showRenderListArea(mFilterListView);
			}
		});
	}

	private RenderDisplyerAdapter frameDisplyerAdapter;
	private HorizontalListView mFrameListView;
	private View btnMenuFrame;

	private void initFrame() {
		mFrameListView = (HorizontalListView) findViewById(R.id.framelistview);
		frameDisplyerAdapter = new RenderDisplyerAdapter(this, null);
		mFrameListView.setAdapter(frameDisplyerAdapter);
		mFrameListView.setOnItemClickListener(itemClickListener);
		boolean loadMore = !TextUtils.isEmpty(moreRenderMap.get(EasyVideoRender.RENDER_TYPE_FRAME));
		RenderResHelper.getInstance()//
			.loadRenderDisplyer(RenderResHelper.RES_FRAME, loadMore, new LoadCallbackListener<List<RenderDisplyer>>() {
				@Override
				public void onCallback(List<RenderDisplyer> list) {
					frameDisplyerAdapter.setData(list);
				}
			});
		btnMenuFrame = areaBottomMenu.findViewById(R.id.video_beautify_menu_frame);
		btnMenuFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showRenderListArea(mFrameListView);
			}
		});
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		boolean clicked = false;

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (clicked) {
				return;
			}
			clicked = true;
			RenderDisplyer displayer = (RenderDisplyer) parent.getItemAtPosition(position);
			if (displayer.getAction() == 1) { //
				goLoadMore(displayer.getType());
			} else if (displayer.getAction() == 0) {
				RenderDisplyerAdapter adapter = (RenderDisplyerAdapter) parent.getAdapter();
				adapter.setSelectIndex(position);
				adapter.notifyDataSetChanged();
				ParamKeeper.get().from(displayer);
				startPreview();
			}
			clicked = false;
		}
	};
	private ListView musiclistview;
	private MusicDisplyerAdapter musicDisplyerAdapter;
	private View btnMenuMusic;
	private View areaSwitchMusic;

	private void initMusic() {
		areaSwitchMusic = findViewById(R.id.areaSwitchMusic);
		musiclistview = (ListView) findViewById(R.id.musiclistview);
		musicDisplyerAdapter = new MusicDisplyerAdapter(this, null);
		musiclistview.setAdapter(musicDisplyerAdapter);
		initMusicSwitch();
		musiclistview.setOnItemClickListener(new OnItemClickListener() {
			boolean clicked = false;

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (clicked) {
					return;
				}
				clicked = true;
				RenderDisplyer displayer = (RenderDisplyer) parent.getItemAtPosition(position);
				if (displayer.getAction() == 1) { //
					goLoadMore(displayer.getType());
				} else if (displayer.getAction() == 0) {
					MusicDisplyerAdapter adapter = (MusicDisplyerAdapter) parent.getAdapter();
					adapter.setSelectIndex(position);
					ParamKeeper.get().from(displayer);
					startPreview();
					btnSwitchOutMusic.setEnabled(true);
					if (!btnSwitchOutMusic.isOpened()) {
						btnSwitchOutMusic.toggleSwitch(true);
					}
				}
				clicked = false;
			}
		});
		boolean loadMore = !TextUtils.isEmpty(moreRenderMap.get(EasyVideoRender.RENDER_TYPE_MUSIC));
		RenderResHelper.getInstance()//
			.loadRenderDisplyer(RenderResHelper.RES_MUSIC, loadMore, new LoadCallbackListener<List<RenderDisplyer>>() {
				@Override
				public void onCallback(List<RenderDisplyer> list) {
					musicDisplyerAdapter.setData(list);
				}
			});
		btnMenuMusic = areaBottomMenu.findViewById(R.id.video_beautify_menu_music);
		btnMenuMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showRenderListArea(musiclistview);
				areaSwitchMusic.setVisibility(View.VISIBLE);
			}
		});
	}

	private SwitchButton btnSwitchSrcMusic;
	private SwitchButton btnSwitchOutMusic;

	private void initMusicSwitch() {
		btnSwitchSrcMusic = (SwitchButton) findViewById(R.id.btnSwitchSrcMusic);
		btnSwitchSrcMusic.setOpened(true);
		btnSwitchOutMusic = (SwitchButton) findViewById(R.id.btnSwitchOutMusic);
		btnSwitchSrcMusic.setOnStateChangedListener(new OnStateChangedListener() {
			@Override
			public void onStateChanged(boolean state) {
				ParamKeeper.get().setMute(!state);
			}
		});
		btnSwitchOutMusic.setEnabled(false);
		btnSwitchOutMusic.setOnStateChangedListener(new OnStateChangedListener() {
			@Override
			public void onStateChanged(boolean state) {
				if (!state) {
					musicDisplyerAdapter.setSelectNone();
					ParamKeeper.get().setMusicId("0");
					startPreview();
					btnSwitchOutMusic.setEnabled(false);
				}
			}
		});
	}

	View showedRenderList;

	private void showRenderListArea(View view) {
		showedRenderList = view;
		view.setVisibility(View.VISIBLE);
		ObjectAnimator an = ObjectAnimator.ofFloat(areaRenderList, "translationY", areaRenderList.getHeight(), 0);
		an.addListener(new AnimatorListenerWraper() {
			@Override
			public void onAnimationStart(Animator animation) {
				areaRenderList.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				areaBottomMenu.setVisibility(View.INVISIBLE);
				arrowRenderListShow.setVisibility(View.VISIBLE);
			}
		});
		an.setDuration(500).start();
	}

	private void hideRenderList() {
		ObjectAnimator an = ObjectAnimator.ofFloat(areaRenderList, "translationY", areaRenderList.getHeight());
		an.addListener(new AnimatorListenerWraper() {

			@Override
			public void onAnimationStart(Animator animation) {
				areaBottomMenu.setVisibility(View.VISIBLE);
				arrowRenderListShow.setVisibility(View.INVISIBLE);
				areaSwitchMusic.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				areaRenderList.setVisibility(View.INVISIBLE);
				showedRenderList.setVisibility(View.INVISIBLE);
			}
		});
		an.setDuration(500).start();
	}

	private void setVideoScale() {
		int[] size = getScreenSize();
		int videoWidth = 480;
		int videoHeight = 480;
		int mWidth = size[0];
		int mHeight = size[1] - 25;
		if (videoWidth > 0 && videoHeight > 0) {
			if (videoWidth * mHeight > mWidth * videoHeight) {
				mHeight = mWidth * videoHeight / videoWidth;
			} else if (videoWidth * mHeight < mWidth * videoHeight) {
				mWidth = mHeight * videoWidth / videoHeight;
			}
		}
		mGPUImageView.setGPUImageScale(mWidth, mHeight);
	}

	private int[] getScreenSize() {
		Point point = AndroidUtils.getScreenSize(getApplicationContext());
		int[] size = new int[2];
		size[0] = point.x;
		size[1] = point.y;
		return size;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.easyVideoRenderBtnBack) {
			onRenderCancel();
		} else if (id == R.id.easyVideoRenderBtnSubmit) {
			startOutput();
		} else if (id == R.id.easyVideoRenderBtnPlay) {
			startPreview();
		}
	}

	private void hidePlayBtn() {
		AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
		aa.setDuration(500);
		aa.setFillAfter(false);
		vPlayBtn.startAnimation(aa);
		vPlayBtn.setVisibility(View.GONE);
	}

	private void showPlayBtn() {
		vPlayBtn.setVisibility(View.VISIBLE);
		AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
		aa.setDuration(500);
		aa.setFillAfter(false);
		vPlayBtn.startAnimation(aa);
	}

	private void goLoadMore(String type) {
		Intent intent = new Intent();
		if (type.equals(RenderResHelper.RES_FILTER)) {
			intent.setAction(moreRenderMap.get(EasyVideoRender.RENDER_TYPE_FILTER));
			startActivityForResult(intent, EasyVideoRender.RENDER_TYPE_FILTER);
		}
		if (type.equals(RenderResHelper.RES_THEME)) {
			intent.setAction(moreRenderMap.get(EasyVideoRender.RENDER_TYPE_THEME));
			startActivityForResult(intent, EasyVideoRender.RENDER_TYPE_THEME);
		}
		if (type.equals(RenderResHelper.RES_FRAME)) {
			intent.setAction(moreRenderMap.get(EasyVideoRender.RENDER_TYPE_FRAME));
			startActivityForResult(intent, EasyVideoRender.RENDER_TYPE_FRAME);
		}
		if (type.equals(RenderResHelper.RES_MUSIC)) {
			intent.setAction(moreRenderMap.get(EasyVideoRender.RENDER_TYPE_MUSIC));
			startActivityForResult(intent, EasyVideoRender.RENDER_TYPE_MUSIC);
		}
	}

	@Override
	public void onProcessFinish(String path) {
		Log.d(TAG, "onProcessFinish: path =" + path);
		dismissProgressDialog();
		showPlayBtn();
		if (ParamKeeper.get().isSaveFile()) {
			EasyVideoRender.getInstance().getOnFinishListener().onFinish(this, path);
			finish();
		}
	}

	@Override
	public void onProcessCancle() {
		Log.d(TAG, "onProcessCancle");
		dismissProgressDialog();
	}

	@Override
	public void onProcessProgress(int progress) {
		if (processDialog != null) {
			processDialog.setProgress(progress > 100 ? 100 : progress);
		}
	}

	private void showProgressDialog() {
		if (processDialog == null) {
			processDialog = newProgressDialog();
			processDialog.setCanceledOnTouchOutside(false);
		}
		processDialog.show();
	}

	private void dismissProgressDialog() {
		if (processDialog != null) {
			processDialog.dismiss();
			processDialog = null;
		}
	}

	public ProgressDialog newProgressDialog() {
		return EasyVideoRender.getInstance().getProgressDialogFactory().create(this);
	}

	public void onRenderCancel() {
		EasyVideoRender.getInstance().getOnCancelListener().onCancel(this);
	}

	private void startNewProcessTask(boolean showLoading, final boolean cancelBack) {
		if (showLoading) {
			showProgressDialog();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				cancelLastTask(cancelBack);
				newProcessTask();
			}
		}).start();
	}

	private void newProcessTask() {
		processTask = new RenderProcessTask(this);
		processTask.setProcessTaskListener(this).start();
	}

	private void cancelLastTask() {
		cancelLastTask(false);
	}

	private void cancelLastTask(boolean callback) {
		if (processTask != null) {
			processTask.cancel(callback);
		}
	}

	private void startPreview() {
		startPreview(true);
	}

	private void startPreview(boolean showLoading) {
		hidePlayBtn();
		mGPUImageView.setVisibility(View.VISIBLE);
		ParamKeeper.get().setPreview(true);
		startNewProcessTask(showLoading, true);
	}

	private void startOutput() {
		hidePlayBtn();
		mGPUImageView.setVisibility(View.VISIBLE);
		ParamKeeper.get().setPreview(false).setOutputFile(renderConfig.getNewVideoOutputName());
		startNewProcessTask(true, false);
	}

	private void onError(int code, String msg) {
		onError(code, msg, true);
	}

	private void onError(int code, String msg, boolean finish) {
		EasyVideoRender.getInstance().getOnErrorListener().onError(code, msg);
		if (finish) {
			finish();
		}
	}

	private void showToast(String msg) {
		EasyVideoRender.getInstance().getToastFactory().create(getActivity(), msg).show();
	}
}
