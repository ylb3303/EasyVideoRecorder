package org.easydarwin.video.recoder.conf;

import java.io.File;
import java.io.Serializable;

import android.os.Environment;

public class RecorderConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	private String baseDir;
	private long recordTimeMax;
	private long recordTimeMin;
	private int frameRate;
	private boolean showGuide = false;
	private boolean showMask = true;

	public RecorderConfig() {
		super();
	}

	public static RecorderConfig createDefault() {
		return new RecorderConfig()//
			.setBaseDir(new File(Environment.getExternalStorageDirectory(), "/org.easydarwin.video").getAbsolutePath())
			.setRecordTimeMax(15 * 1000)
			.setRecordTimeMin(2 * 1000)
			.setFrameRate(20);
	}

	public String getBaseDir() {
		return baseDir;
	}

	public RecorderConfig setBaseDir(String baseDir) {
		this.baseDir = baseDir;
		return this;
	}

	public long getRecordTimeMax() {
		return recordTimeMax;
	}

	public RecorderConfig setRecordTimeMax(long recordTimeMax) {
		this.recordTimeMax = recordTimeMax;
		return this;
	}

	public long getRecordTimeMin() {
		return recordTimeMin;
	}

	public RecorderConfig setRecordTimeMin(long recordTimeMin) {
		this.recordTimeMin = recordTimeMin;
		return this;
	}

	public String getVideoTmpDir() {
		return new File(getBaseDir(), ".tmp").getAbsolutePath();
	}

	public int getFrameRate() {
		return frameRate;
	}

	public RecorderConfig setFrameRate(int frameRate) {
		this.frameRate = frameRate;
		return this;
	}

	public boolean isShowGuide() {
		return showGuide;
	}

	public RecorderConfig setShowGuide(boolean showGuide) {
		this.showGuide = showGuide;
		return this;
	}

	public boolean isShowMask() {
		return showMask;
	}

	public RecorderConfig setShowMask(boolean showMask) {
		this.showMask = showMask;
		return this;
	}

}
