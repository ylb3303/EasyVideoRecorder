package org.easydarwin.video.recoder.conf;

import java.io.File;
import java.io.Serializable;

import android.graphics.Color;
import android.os.Environment;

public class RecorderConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	private String baseDir;
	private long recordTimeMax;
	private long recordTimeMin;
	private int frameRate;
	private int previewSize = PREVIEW_SIZE_SMALL;
	private boolean showGuide = false;

	private int progressBackgroundColor;
	private int progressRecordingColor;
	private int progressFlashColor;
	private int progressMinTimeColor;
	private int progressBreakColor;
	private int progressRollbackColor;
	private int progressFlashWidth;
	private int progressMinTimeWidth;
	private int progressBreakWidth;

	private int progressPostion = PROGRESS_POSTION_BOTTOM;

	public static final int PREVIEW_SIZE_BIG = 1;
	public static final int PREVIEW_SIZE_SMALL = 2;
	public static final int PROGRESS_POSTION_TOP = 3;
	public static final int PROGRESS_POSTION_BOTTOM = 4;

	private RecorderConfig() {
		super();
	}

	public static RecorderConfig create() {
		return new RecorderConfig()//
			.setBaseDir(new File(Environment.getExternalStorageDirectory(), "/org.easydarwin.video").getAbsolutePath())
			.setRecordTimeMax(15 * 1000)
			.setRecordTimeMin(4 * 1000)
			.setFrameRate(25)
			.setProgressBackgroundColor(Color.parseColor("#222222"))
			.setProgressRecordingColor(Color.parseColor("#E40077"))
			.setProgressFlashColor(Color.parseColor("#FFDAEE"))
			.setProgressMinTimeColor(Color.parseColor("#ff0000"))
			.setProgressBreakColor(Color.parseColor("#000000"))
			.setProgressRollbackColor(Color.parseColor("#F44CA3"))
			.setProgressFlashWidth(5)
			.setProgressMinTimeWidth(3)
			.setProgressBreakWidth(1);
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

	public int getProgressBackgroundColor() {
		return progressBackgroundColor;
	}

	public RecorderConfig setProgressBackgroundColor(int progressBackgroundColor) {
		this.progressBackgroundColor = progressBackgroundColor;
		return this;
	}

	public int getProgressRecordingColor() {
		return progressRecordingColor;
	}

	public RecorderConfig setProgressRecordingColor(int progressRecordingColor) {
		this.progressRecordingColor = progressRecordingColor;
		return this;
	}

	public int getProgressFlashColor() {
		return progressFlashColor;
	}

	public RecorderConfig setProgressFlashColor(int progressFlashColor) {
		this.progressFlashColor = progressFlashColor;
		return this;
	}

	public int getProgressMinTimeColor() {
		return progressMinTimeColor;
	}

	public RecorderConfig setProgressMinTimeColor(int progressMinTimeColor) {
		this.progressMinTimeColor = progressMinTimeColor;
		return this;
	}

	public int getProgressBreakColor() {
		return progressBreakColor;
	}

	public RecorderConfig setProgressBreakColor(int progressBreakColor) {
		this.progressBreakColor = progressBreakColor;
		return this;
	}

	public int getProgressRollbackColor() {
		return progressRollbackColor;
	}

	public RecorderConfig setProgressRollbackColor(int progressRollbackColor) {
		this.progressRollbackColor = progressRollbackColor;
		return this;
	}

	public int getProgressFlashWidth() {
		return progressFlashWidth;
	}

	public RecorderConfig setProgressFlashWidth(int progressFlashWidth) {
		this.progressFlashWidth = progressFlashWidth;
		return this;
	}

	public int getProgressMinTimeWidth() {
		return progressMinTimeWidth;
	}

	public RecorderConfig setProgressMinTimeWidth(int progressMinTimeWidth) {
		this.progressMinTimeWidth = progressMinTimeWidth;
		return this;
	}

	public int getProgressBreakWidth() {
		return progressBreakWidth;
	}

	public RecorderConfig setProgressBreakWidth(int progressBreakWidth) {
		this.progressBreakWidth = progressBreakWidth;
		return this;
	}

	public int getPreviewSize() {
		return previewSize;
	}

	public RecorderConfig setPreviewSize(int previewSize) {
		this.previewSize = previewSize;
		return this;
	}

	public int getProgressPostion() {
		return progressPostion;
	}

	public RecorderConfig setProgressPostion(int progressPostion) {
		this.progressPostion = progressPostion;
		return this;
	}

}
