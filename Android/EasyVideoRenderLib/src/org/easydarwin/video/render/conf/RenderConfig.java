package org.easydarwin.video.render.conf;

import java.io.File;
import java.io.Serializable;

import android.os.Environment;

public class RenderConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	private String baseDir;
	private boolean endLogoShow = true;
	private int endLogoDuration = 1;

	private RenderConfig() {
		super();
	}

	public static RenderConfig create() {
		return new RenderConfig()//
			.setBaseDir(new File(Environment.getExternalStorageDirectory(), "/org.easydarwin.video").getAbsolutePath());
	}

	public String getBaseDir() {
		return baseDir;
	}

	public RenderConfig setBaseDir(String baseDir) {
		this.baseDir = baseDir;
		return this;
	}

	public String getVideoOutputDir() {
		return new File(getBaseDir(), "output").getAbsolutePath();
	}

	public String getNewVideoOutputName() {
		return new File(getVideoOutputDir(), System.currentTimeMillis() + ".mp4").getAbsolutePath();
	}

	public boolean isEndLogoShow() {
		return endLogoShow;
	}

	public RenderConfig setEndLogoShow(boolean endLogoShow) {
		this.endLogoShow = endLogoShow;
		return this;
	}

	public int getEndLogoDuration() {
		return endLogoDuration;
	}

	public RenderConfig setEndLogoDuration(int endLogoDuration) {
		this.endLogoDuration = endLogoDuration;
		return this;
	}

}
