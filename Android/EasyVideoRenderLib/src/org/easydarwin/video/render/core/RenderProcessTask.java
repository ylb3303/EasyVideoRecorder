package org.easydarwin.video.render.core;

import java.io.File;
import java.util.List;

import org.easydarwin.video.render.template.AssetType;
import org.easydarwin.video.render.template.AudioClip;
import org.easydarwin.video.render.template.AudioEffect;
import org.easydarwin.video.render.template.AudioGroup;
import org.easydarwin.video.render.template.Filter;
import org.easydarwin.video.render.template.FilterGroup;
import org.easydarwin.video.render.template.MediaClip;
import org.easydarwin.video.render.template.MediaMgr;
import org.easydarwin.video.render.template.NodeType;
import org.easydarwin.video.render.template.TimeLineNode;
import org.easydarwin.video.render.template.VideoAsset;
import org.easydarwin.video.render.template.VideoClip;
import org.easydarwin.video.render.template.VideoProject;
import org.easydarwin.video.render.template.VideoTemplate;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class RenderProcessTask extends Thread {
	public static final String LOG_TAG = RenderProcessTask.class.getSimpleName();

	private ProcessListener processListener;
	private Context contex;
	private Handler handler;
	private volatile boolean canceled = false;

	public RenderProcessTask(Context contex) {
		this.contex = contex;
		handler = new Handler(Looper.getMainLooper());
	}

	public void realse() {
		this.contex = null;
		handler = null;
	}

	private void prepare() {
		if (processListener == null) {
			processListener = new ProcessListener() {

				@Override
				public void onProcessFinish(String path) {
				}

				@Override
				public void onProcessCancle() {
				}

				@Override
				public void onProcessProgress(int progress) {
				}
			};
		}
	}

	@Override
	public void run() {
		prepare();
		canceled = false;
		ParamKeeper param = ParamKeeper.get();
		VideoProject project = buildVideoProject(param);
		excuteProject(contex, project, param);
	}

	public void cancel(boolean callback) {
		prepare();
		if (!callback) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (RenderProcessTask.this.isAlive()) {
						canceled = true;
					}
					try {
						RenderProcessTask.this.join();
					} catch (InterruptedException e) {
					}
				}
			}).start();
		} else {
			if (RenderProcessTask.this.isAlive()) {
				canceled = true;
			}
			try {
				RenderProcessTask.this.join();
			} catch (InterruptedException e) {
			}
			handler.post(new Runnable() {

				@Override
				public void run() {
					processListener.onProcessCancle();
				}
			});
		}
	}

	private VideoProject buildVideoProject(ParamKeeper param) {
		VideoProject project = null;
		VideoTemplate template = null;
		if (param.getThemeId() != null && !param.getThemeId().equals("0")) {
			String location = RenderResHelper.getInstance().getResLocation(RenderResHelper.RES_THEME, param.getThemeId());
			template = RenderTemplate.buildThemeTemplate(param.getThemeId(), location);
		} else {
			template = RenderTemplate.createBlankTemplate();
		}
		if (param.getFilterId() != null && !param.getFilterId().equals("0")) {
			String location = RenderResHelper.getInstance().getResLocation(RenderResHelper.RES_FILTER, param.getFilterId());
			Filter filter = FilterUtils.build(location);
			filter.setOffset(0);
			filter.setDuration(param.getVideoMaxLength() / 1000 * param.getFrameRate());
			template.addFilter(filter, 0);
		}
		if (param.getFrameId() != null && !param.getFrameId().equals("0")) {
			String location = RenderResHelper.getInstance().getResLocation(RenderResHelper.RES_FRAME, param.getFrameId());
			Filter filter = FilterUtils.build(location);
			filter.setOffset(0);
			filter.setDuration(param.getVideoMaxLength() / 1000 * param.getFrameRate());
			template.addFilter(filter);
		}
		if (param.getMusic() != null) {
			template.addMusic(param.getMusic());
		}

		if (param.getMusicId() != null && !param.getMusicId().equals("0")) {
			String location = RenderResHelper.getInstance().getResLocation(RenderResHelper.RES_MUSIC, param.getMusicId());
			template.addMusic(MediaMgr.createAudio(Uri.fromFile(new File(location, "music.mp3")), 0, param.getVideoMaxLength() / 1000 * param
				.getFrameRate()));
		}
		if (param.getAudioClips() != null) {
			template.addAudio(param.getAudioClips());
		}

		project = new VideoProject(template);
		List<MediaClip> mediaClips = MediaMgr.buildMediaClip(AssetType.VIDEO, param.getVideoUri());
		project.addMedia(mediaClips);

		if (ParamKeeper.get().isAddEndLogo()) {
			RenderTemplate.addWatermarkNode(template);
		}
		return project;
	}

	private void excuteProject(Context contex, VideoProject videoProject, ParamKeeper param) {
		VideoTemplate videoTemplate = videoProject.getVideoTemplate();
		Uri outVideo = null;
		VideoAsset outVideoAsset = null;
		if (!param.isPreview()) {
			outVideo = Uri.fromFile(new File(param.getOutputFile()));
			outVideoAsset = new VideoAsset();
			outVideoAsset.setUri(outVideo);
		}
		long totalFrame = videoTemplate.getTotalFrame();
		TimeLineNode renderTree = videoTemplate.getRenderTree();
		List<TimeLineNode> trackList = renderTree.getChildNodeList();
		Bitmap curFrame = null;
		FilterGroup filter = null;
		VideoClip videoClip = null;

		AudioGroup audioClips = getAudioGroup(videoProject);
		try {
			long time = System.currentTimeMillis();
			Log.i(LOG_TAG, "start filter:" + time);
			for (int frameIndex = 0; frameIndex < totalFrame; frameIndex++) {
				if (canceled) {
					break;
				}
				curFrame = null;
				filter = null;
				long framestart = System.currentTimeMillis();
				for (TimeLineNode track : trackList) {
					switch (track.getNodeType()) {
						case VIDEO_TRACK:
							List<TimeLineNode> videoNodes = track.getChildNodeList();
							for (TimeLineNode videoNode : videoNodes) {
								if (frameIndex >= videoNode.getOffset() && frameIndex < videoNode.getOutPoint()) {
									switch (videoNode.getNodeType()) {
										case VIDEO_NODE:
											if (videoClip != null && videoClip.getAsset() != ((VideoClip) videoNode.getNodeData()).getAsset()) {
												videoClip.getAsset().closeDecode();
											}
											videoClip = (VideoClip) videoNode.getNodeData();
											List<TimeLineNode> filterNodes = videoNode.getChildNodeList();
											if (filterNodes != null) {
												TimeLineNode filterNode = filterNodes.get(0);
												if (filter != null && filter != filterNode.getNodeData()) {
													filter.close();
												}
												filter = (FilterGroup) filterNode.getNodeData();
											}
											break;
										case IMAGE_NODE:
											break;
										default:
											break;
									} // end switch
								} // end if
							} // end vedioNodes
							break;
						case AUDIO_TRACK:
							break;
						case TRANSITION_TRACK:
							break;
						default:
							break;
					} // end switch
				} // end iterator
				if (frameIndex == 0) {
					videoClip.startDecode();

					if (!param.isPreview()) {
						outVideoAsset.setWidth(videoClip.getAsset().getWidth());
						outVideoAsset.setHeight(videoClip.getAsset().getHeight());
						outVideoAsset.startEncode();
						videoClip.getAsset().setMediaTarget(outVideoAsset.getMediaTarget());
						videoClip.getAsset().setUseSrcAudio(!param.isMute());
					} else {// 预览
						AudioEffect.playSrcAudio(videoClip.getAsset().getUri(), contex);
						AudioEffect.setSrcPlayerMute(param.isMute());
					}
				}
				curFrame = videoClip.getNextFrame();
				if (curFrame == null) {
					break;
				}
				if (filter != null) {
					filter.setFrameIndex(frameIndex);
					curFrame = filter.applyEffect(curFrame, param.isPreview());
					if (!param.isPreview()) {
						outVideoAsset.appendFrame(curFrame);
					}
				}
				audioClips.setFrameIndex(frameIndex);
				if (param.isPreview()) {
					audioClips.applyEffect(contex);
				} else {
					videoClip.getAsset().setMusic(audioClips);
				}
				if (param.isPreview()) {
					long end = System.currentTimeMillis() - framestart;
					if (end < param.getFrameRate()) {
						SystemClock.sleep(param.getFrameRate() - end);
					}
				}
				publicProgress((int) (frameIndex / totalFrame));
			} // end for
			if (param.isSaveFile()) {
				outVideoAsset.colseEncode();
			}
			Log.i(LOG_TAG, "end filter:" + (System.currentTimeMillis() - time));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (videoClip != null) {
			videoClip.getAsset().closeDecode();
		}
		if (filter != null) {
			filter.close();
		}
		if (audioClips != null) {
			audioClips.close();
		}
		AudioEffect.stopSrcPlayer();
		if (!canceled) {
			if (param.isPreview()) {
				publicFinish(null);
			}
			if (param.isSaveFile()) {
				publicFinish(outVideo.getPath());
			}
		}
	}

	private AudioGroup getAudioGroup(VideoProject videoProject) {
		AudioGroup audioClips = new AudioGroup();
		TimeLineNode renderTree = videoProject.getVideoTemplate().getRenderTree();
		List<TimeLineNode> trackList = renderTree.getChildNodeList();
		for (TimeLineNode track : trackList) {
			if (track.getNodeType() == NodeType.AUDIO_TRACK) {
				List<TimeLineNode> audioNodes = track.getChildNodeList();
				for (TimeLineNode audioNode : audioNodes) {
					audioClips.addAudio((AudioClip) audioNode.getNodeData());
				}
				break;
			}
		}
		return audioClips;
	}

	public ProcessListener getProcessListener() {
		return processListener;
	}

	public RenderProcessTask setProcessTaskListener(ProcessListener processListener) {
		this.processListener = processListener;
		return this;
	}

	private void publicFinish(final String reslut) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				processListener.onProcessFinish(reslut);
			}
		});
	}

	private void publicProgress(final int progress) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				processListener.onProcessProgress(progress);
			}
		});
	}

	public static interface ProcessListener {
		public void onProcessFinish(String path);

		public void onProcessCancle();

		public void onProcessProgress(int progress);
	}
}
