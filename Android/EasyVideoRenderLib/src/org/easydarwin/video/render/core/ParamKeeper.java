package org.easydarwin.video.render.core;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

import org.easydarwin.video.render.model.RenderDisplyer;
import org.easydarwin.video.render.template.AudioClip;
import org.easydarwin.video.render.template.AudioEffect;
import org.easydarwin.video.render.template.Tittle;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

public class ParamKeeper {

	private Uri videoUri; // 视频地址
	private boolean isdubAudio; // 是否开启了录音效果
	private String dubAudioUri; // 录音地址
	private int dubAudioTime; // 录音开始时间 ,以毫秒为单位
	private int dubAudioTimeLength; // 录音时长
	private int isLocalMusic; // 是否开启了本地音乐
	private String localMusicUri; // 本地音乐地址
	private int localMusicBinTime; // 本地音乐开始时间
	private int localMusicTimeLength;// 本地音乐时长
	private int subtitlePosition = 0;// 字幕游标位置
	private int subtitleBegin = 0; // 字幕开始阶段
	private int subitileEnd = 0; // 字幕结束阶段
	private int subtitleShadow = 0;// 字幕时长
	private Bitmap subitileBitmap; // 字幕图片
	private Tittle[] titlles = null; // 字幕
	private AudioClip[] audioClips = null; // 配音
	private AudioClip music = null; // 配乐

	private boolean isPreview = true;
	private boolean isMute = false;
	private boolean addEndLogo = true;

	private String themeId;
	private String filterId;
	private String frameId;
	private String musicId;
	private String outputFile;

	private int frameRate = 25;
	private int videoMaxLength = 60 * 1000;

	private int endLogoDuration = 1;
	private GPUImageView mGPUImageView;

	public static Context context;
	private static ParamKeeper mInstance;

	public static ParamKeeper get() {
		if (mInstance == null) {
			mInstance = new ParamKeeper();
		}
		return mInstance;
	}

	public static void reset() {
		if (mInstance != null) {
			mInstance = null;
		}
	}

	public void from(RenderDisplyer displayer) {
		if (displayer.getType().equals(RenderResHelper.RES_FILTER)) {
			setFilterId(displayer.getId());
		}
		if (displayer.getType().equals(RenderResHelper.RES_THEME)) {
			setThemeId(displayer.getId());
		}
		if (displayer.getType().equals(RenderResHelper.RES_FRAME)) {
			setFrameId(displayer.getId());
		}
		if (displayer.getType().equals(RenderResHelper.RES_MUSIC)) {
			setMusicId(displayer.getId());
		}
	}

	public int getVideoMaxLength() {
		return videoMaxLength;
	}

	public ParamKeeper setVideoMaxLength(int videoMaxLength) {
		this.videoMaxLength = videoMaxLength;
		return this;
	}

	public Context getContext() {
		return context;
	}

	public ParamKeeper setContext(Context context) {
		ParamKeeper.context = context;
		return this;
	}

	public GPUImageView getGPUImageView() {
		return mGPUImageView;
	}

	public ParamKeeper setGPUImageView(GPUImageView mGPUImageView) {
		this.mGPUImageView = mGPUImageView;
		return this;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public ParamKeeper setFrameRate(int frameRate) {
		this.frameRate = frameRate;
		return this;
	}

	public boolean isMute() {
		return isMute;
	}

	public ParamKeeper setMute(boolean isMute) {
		this.isMute = isMute;
		AudioEffect.setSrcPlayerMute(isMute);
		return this;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public ParamKeeper setOutputFile(String outputPath) {
		this.outputFile = outputPath;
		return this;
	}

	public String getThemeId() {
		return themeId;
	}

	public ParamKeeper setThemeId(String themeId) {
		this.themeId = themeId;
		return this;
	}

	public String getFilterId() {
		return filterId;
	}

	public ParamKeeper setFilterId(String filterId) {
		this.filterId = filterId;
		return this;
	}

	public boolean isPreview() {
		return isPreview;
	}

	public ParamKeeper setPreview(boolean isPreview) {
		this.isPreview = isPreview;
		return this;
	}

	public boolean isSaveFile() {
		return !isPreview;
	}

	public Uri getVideoUri() {
		return videoUri;
	}

	public ParamKeeper setVideoUri(Uri videoUri) {
		this.videoUri = videoUri;
		return this;
	}

	public boolean isIsdubAudio() {
		return isdubAudio;
	}

	public ParamKeeper setIsdubAudio(boolean isdubAudio) {
		this.isdubAudio = isdubAudio;
		return this;
	}

	public String getDubAudioUri() {
		return dubAudioUri;
	}

	public ParamKeeper setDubAudioUri(String dubAudioUri) {
		this.dubAudioUri = dubAudioUri;
		return this;
	}

	public int getDubAudioTime() {
		return dubAudioTime;
	}

	public ParamKeeper setDubAudioTime(int dubAudioTime) {
		this.dubAudioTime = dubAudioTime;
		return this;
	}

	public int getDubAudioTimeLength() {
		return dubAudioTimeLength;
	}

	public ParamKeeper setDubAudioTimeLength(int dubAudioTimeLength) {
		this.dubAudioTimeLength = dubAudioTimeLength;
		return this;
	}

	public int getIsLocalMusic() {
		return isLocalMusic;
	}

	public ParamKeeper setIsLocalMusic(int isLocalMusic) {
		this.isLocalMusic = isLocalMusic;
		return this;
	}

	public String getLocalMusicUri() {
		return localMusicUri;
	}

	public ParamKeeper setLocalMusicUri(String localMusicUri) {
		this.localMusicUri = localMusicUri;
		return this;
	}

	public int getLocalMusicBinTime() {
		return localMusicBinTime;
	}

	public ParamKeeper setLocalMusicBinTime(int localMusicBinTime) {
		this.localMusicBinTime = localMusicBinTime;
		return this;
	}

	public int getLocalMusicTimeLength() {
		return localMusicTimeLength;
	}

	public ParamKeeper setLocalMusicTimeLength(int localMusicTimeLength) {
		this.localMusicTimeLength = localMusicTimeLength;
		return this;
	}

	public int getSubtitlePosition() {
		return subtitlePosition;
	}

	public ParamKeeper setSubtitlePosition(int subtitlePosition) {
		this.subtitlePosition = subtitlePosition;
		return this;
	}

	public int getSubtitleBegin() {
		return subtitleBegin;
	}

	public ParamKeeper setSubtitleBegin(int subtitleBegin) {
		this.subtitleBegin = subtitleBegin;
		return this;
	}

	public int getSubitileEnd() {
		return subitileEnd;
	}

	public ParamKeeper setSubitileEnd(int subitileEnd) {
		this.subitileEnd = subitileEnd;
		return this;
	}

	public int getSubtitleShadow() {
		return subtitleShadow;
	}

	public ParamKeeper setSubtitleShadow(int subtitleShadow) {
		this.subtitleShadow = subtitleShadow;
		return this;
	}

	public Bitmap getSubitileBitmap() {
		return subitileBitmap;
	}

	public ParamKeeper setSubitileBitmap(Bitmap subitileBitmap) {
		this.subitileBitmap = subitileBitmap;
		return this;
	}

	public Tittle[] getTitlles() {
		return titlles;
	}

	public ParamKeeper setTitlles(Tittle[] titlles) {
		this.titlles = titlles;
		return this;
	}

	public AudioClip[] getAudioClips() {
		return audioClips;
	}

	public ParamKeeper setAudioClips(AudioClip[] audioClips) {
		this.audioClips = audioClips;
		return this;
	}

	public AudioClip getMusic() {
		return music;
	}

	public ParamKeeper setMusic(AudioClip music) {
		this.music = music;
		return this;
	}

	public boolean isAddEndLogo() {
		return addEndLogo;
	}

	public ParamKeeper setAddEndLogo(boolean addEndLogo) {
		this.addEndLogo = addEndLogo;
		return this;
	}

	public String getFrameId() {
		return frameId;
	}

	public ParamKeeper setFrameId(String frameId) {
		this.frameId = frameId;
		return this;
	}

	public String getMusicId() {
		return musicId;
	}

	public ParamKeeper setMusicId(String musicId) {
		this.musicId = musicId;
		return this;
	}

	public int getEndLogoDuration() {
		return endLogoDuration * frameRate;
	}

	public ParamKeeper setEndLogoDuration(int endLogoDuration) {
		this.endLogoDuration = endLogoDuration;
		return this;
	}

}
