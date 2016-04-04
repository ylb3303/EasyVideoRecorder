package org.easydarwin.video.recoder.view;

import java.util.Iterator;
import java.util.LinkedList;

import org.easydarwin.video.recoder.conf.RecorderConfig;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoProgressView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

	Context context;
	RecorderConfig config;

	public VideoProgressView(Context context) {
		super(context);
		this.context = context;
	}

	public VideoProgressView(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);
		this.context = context;
	}

	public VideoProgressView(Context context, AttributeSet paramAttributeSet, int paramInt) {
		super(context, paramAttributeSet, paramInt);
		this.context = context;
	}

	public void init(RecorderConfig config) {
		this.config = config;
		this.setZOrderOnTop(true);
		this.setZOrderMediaOverlay(true);
		displayMetrics = getResources().getDisplayMetrics();
		screenWidth = displayMetrics.widthPixels;
		perWidth = screenWidth * 1.0f / config.getRecordTimeMax() * 1.0f;

		progressPaint = new Paint();
		flashPaint = new Paint();
		minTimePaint = new Paint();
		breakPaint = new Paint();
		rollbackPaint = new Paint();
		backgroundPaint = new Paint();
		//
		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setColor(Color.parseColor("#222222"));

		progressPaint.setStyle(Paint.Style.FILL);
		progressPaint.setColor(Color.parseColor("#4db288"));

		flashPaint.setStyle(Paint.Style.FILL);
		flashPaint.setColor(Color.parseColor("#FFFF00"));

		minTimePaint.setStyle(Paint.Style.FILL);
		minTimePaint.setColor(Color.parseColor("#ff0000"));

		breakPaint.setStyle(Paint.Style.FILL);
		breakPaint.setColor(Color.parseColor("#000000"));

		rollbackPaint.setStyle(Paint.Style.FILL);
		//        rollbackPaint.setColor(Color.parseColor("#FF3030"));
		rollbackPaint.setColor(Color.parseColor("#f15369"));

		holder = getHolder();
		holder.addCallback(this);
		inited = true;
	}

	private volatile State currentState = State.PAUSE;

	private boolean isVisible = true;
	private float countWidth = 0;
	private float perProgress = 0;
	private long initTime;
	private long drawFlashTime = 0;

	private long lastStartTime = 0;
	private long lastEndTime = 0;

	private volatile boolean drawing = false;

	private DisplayMetrics displayMetrics;
	private int screenWidth, progressHeight;
	private Paint backgroundPaint, progressPaint, flashPaint, minTimePaint, breakPaint, rollbackPaint;
	private float perWidth;
	private float flashWidth = 3f;
	private float minTimeWidth = 5f;
	private float breakWidth = 2f;

	private LinkedList<Integer> timeList = new LinkedList<Integer>();

	private Canvas canvas = null;
	private Thread thread = null;
	private SurfaceHolder holder = null;

	boolean inited = false;

	private void myDraw() {
		if (!inited) {
			return;
		}
		canvas = holder.lockCanvas();
		progressHeight = getMeasuredHeight();

		if (canvas != null) {
			canvas.drawRect(0, 0, screenWidth, progressHeight, backgroundPaint);
		}

		long curSystemTime = System.currentTimeMillis();
		countWidth = 0;
		if (!timeList.isEmpty()) {
			long preTime = 0;
			long curTime = 0;
			Iterator<Integer> iterator = timeList.iterator();
			while (iterator.hasNext()) {
				lastStartTime = preTime;
				curTime = iterator.next();
				lastEndTime = curTime;
				float left = countWidth;
				countWidth += (curTime - preTime) * perWidth;
				if (canvas != null) {

					canvas.drawRect(left, 0, countWidth, progressHeight, progressPaint);
					canvas.drawRect(countWidth, 0, countWidth + breakWidth, progressHeight, breakPaint);
				}
				countWidth += breakWidth;
				preTime = curTime;
			}
		}
		if (timeList.isEmpty() || (!timeList.isEmpty() && timeList.getLast() <= config.getRecordTimeMax())) {
			float left = perWidth * config.getRecordTimeMin();
			if (canvas != null) {
				canvas.drawRect(left, 0, left + minTimeWidth, progressHeight, minTimePaint);
			}
		}
		if (currentState == State.BACKSPACE) {
			float left = countWidth - (lastEndTime - lastStartTime) * perWidth;
			float right = countWidth;
			if (canvas != null) {
				canvas.drawRect(left, 0, right, progressHeight, rollbackPaint);
			}
		}
		if (currentState == State.START) {
			perProgress += perWidth * (curSystemTime - initTime);
			float right = (countWidth + perProgress) >= screenWidth ? screenWidth : (countWidth + perProgress);
			if (canvas != null) {
				canvas.drawRect(countWidth, 0, right, progressHeight, progressPaint);
			}
		}
		if (drawFlashTime == 0 || curSystemTime - drawFlashTime >= 500) {
			isVisible = !isVisible;
			drawFlashTime = System.currentTimeMillis();
		}
		if (isVisible) {
			if (currentState == State.START) {
				if (canvas != null) {
					canvas.drawRect(countWidth + perProgress, 0, countWidth + flashWidth + perProgress, progressHeight, flashPaint);
				}
			} else {
				if (canvas != null) {
					canvas.drawRect(countWidth, 0, countWidth + flashWidth, progressHeight, flashPaint);
				}
			}
		}
		initTime = System.currentTimeMillis();
		if (canvas != null) {
			holder.unlockCanvasAndPost(canvas);
		}
	}

	@Override
	public void run() {
		while (drawing) {
			try {
				myDraw();
				Thread.sleep(40);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void putTimeList(int time) {
		timeList.add(time);
	}

	public void clearTimeList() {
		timeList.clear();
	}

	public int getLastTime() {
		if ((timeList != null) && (!timeList.isEmpty())) {
			return timeList.getLast();
		}
		return 0;
	}

	public boolean isTimeListEmpty() {
		return timeList.isEmpty();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread = new Thread(this);
		drawing = true;
		thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		drawing = false;
	}

	public void setCurrentState(State state) {
		currentState = state;
		if (state != State.START) {
			perProgress = perWidth;
		}
		if (state == State.DELETE) {
			if ((timeList != null) && (!timeList.isEmpty())) {
				timeList.removeLast();
			}
		}
	}

	public static enum State {

		START(0x1),
		PAUSE(0x2),
		BACKSPACE(0x3),
		DELETE(0x4);

		static State mapIntToValue(final int stateInt) {
			for (State value : State.values()) {
				if (stateInt == value.getIntValue()) {
					return value;
				}
			}
			return PAUSE;
		}

		private int mIntValue;

		State(int intValue) {
			mIntValue = intValue;
		}

		int getIntValue() {
			return mIntValue;
		}
	}
}
