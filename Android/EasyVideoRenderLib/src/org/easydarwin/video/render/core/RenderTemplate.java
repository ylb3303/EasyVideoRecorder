package org.easydarwin.video.render.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.easydarwin.video.render.template.AssetMgr;
import org.easydarwin.video.render.template.AssetType;
import org.easydarwin.video.render.template.AudioAsset;
import org.easydarwin.video.render.template.Filter;
import org.easydarwin.video.render.template.FilterGroup;
import org.easydarwin.video.render.template.ImageSeqAsset;
import org.easydarwin.video.render.template.MediaClip;
import org.easydarwin.video.render.template.MediaMgr;
import org.easydarwin.video.render.template.NodeType;
import org.easydarwin.video.render.template.TimeLineNode;
import org.easydarwin.video.render.template.VideoTemplate;
import org.xmlpull.v1.XmlPullParser;

import android.net.Uri;
import android.util.Log;
import android.util.Xml;

public class RenderTemplate {

	//	private static Map<String, VideoTemplate> templateCache = new HashMap<String, VideoTemplate>();

	public static VideoTemplate buildThemeTemplate(String id, String location) {
		//		if(templateCache.containsKey(id)){
		//			return templateCache.get(id);
		//		}

		VideoTemplate videoTemplate = new VideoTemplate(id);
//		// 模板存放的路径
//		String videoTemplatePath = ProjectUtils.getThemePath();
//		// 描述文件的存放目录名默认为Id值
//		videoTemplatePath = videoTemplatePath + id;
		try {
			File videoTemplateFile = new File(location, "meta.xml");

			InputStream inStream = new FileInputStream(videoTemplateFile);
			XmlPullParser parser = Xml.newPullParser();

			parser.setInput(inStream, "UTF-8");
			int eventType = parser.getEventType();

			TimeLineNode videoTrack = null;
			TimeLineNode mediaNode = null;
			Filter filter = null;
			FilterGroup filterGroup = null;

			TimeLineNode audioTrack = null;
			TimeLineNode audioNode = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:// 开始元素事件
						if ("RootNode".equalsIgnoreCase(parser.getName())) {
							videoTemplate.setName(parser.getAttributeValue(null, "name"));
							videoTemplate.setTotalFrame(Long.valueOf(parser.getAttributeValue(null, "totalframe")));
							videoTemplate.setFrameRate(Integer.valueOf(parser.getAttributeValue(null, "framerate")));
							String[] framesize = parser.getAttributeValue(null, "framesize").split("-");
							videoTemplate.setWidth(Integer.valueOf(framesize[0]));
							videoTemplate.setHeight(Integer.valueOf(framesize[1]));

							TimeLineNode renderTree = new TimeLineNode();
							videoTemplate.setRenderTree(renderTree);
							List<TimeLineNode> trackList = new ArrayList<TimeLineNode>();
							renderTree.setChildNodeList(trackList);
						} else if ("VideoTrack".equalsIgnoreCase(parser.getName())) {
							videoTrack = new TimeLineNode();
							videoTrack.setNodeType(NodeType.VIDEO_TRACK);

							videoTemplate.getRenderTree().getChildNodeList().add(videoTrack);
						} else if ("MediaNode".equalsIgnoreCase(parser.getName())) {
							if (videoTrack.getChildNodeList() == null) {
								videoTrack.setChildNodeList(new ArrayList<TimeLineNode>());
							}
							mediaNode = new TimeLineNode();
							mediaNode.setId(parser.getAttributeValue(null, "ID"));
							mediaNode.setName(parser.getAttributeValue(null, "name"));
							// ///////////////////////////////////////////////////////////////
							String MediaType = parser.getAttributeValue(null, "MediaType");
							if ("1".equals(MediaType)) {
								mediaNode.setNodeType(NodeType.VIDEO_NODE);
							} else {
								mediaNode.setNodeType(NodeType.IMAGE_NODE);
							}
							// ////////////////////////////////////////////////////////////////
							mediaNode.setOffset(Long.valueOf(parser.getAttributeValue(null, "In")));
							mediaNode.setDuration(Long.valueOf(parser.getAttributeValue(null, "totalframe")));
							//
							mediaNode.setNodeData(null);
							//

							videoTrack.getChildNodeList().add(mediaNode);
						} else if ("FilterNode".equalsIgnoreCase(parser.getName())) {
							TimeLineNode filterNode = null;
							if (mediaNode.getChildNodeList() == null) {
								mediaNode.setChildNodeList(new ArrayList<TimeLineNode>());
								filterNode = new TimeLineNode();
								filterNode.setNodeType(NodeType.FILER_NODE);
								mediaNode.getChildNodeList().add(filterNode);
							} else {
								filterNode = mediaNode.getChildNodeList().get(0);
							}

							if (filterGroup == null) {
								filterGroup = new FilterGroup();
							}

							String FilterID = parser.getAttributeValue(null, "FilterID");
							//if (filter == null) 
							{
								filter = new Filter();
								filter.setId(FilterID);
								filter.setName(FilterID);
								filter.setFilterType(FilterID);
								//filter = FilterUtils.buildFromXML(videoTemplatePath, FilterID + ".xml");
							}
							try {
								filter.setDuration(Long.valueOf(parser.getAttributeValue(null, "totalframe")));
								filter.setOffset(Long.valueOf(parser.getAttributeValue(null, "In")));
							} catch (Exception e) {
								e.printStackTrace();
							}

							// /////////////////
							String attachType = parser.getAttributeValue(null, "attachType");
							// 滤镜附件类型，支持图片序列和视频
							AssetType type = AssetType.IMAGE;
							if (attachType != null && attachType.trim().length() > 0) {
								type = AssetType.valueOf(attachType);
								filter.setAsset(AssetMgr.buildAsset(type, null));
							}

							filterGroup.addFilter(filter);

							filterNode.setNodeData(filterGroup);
							// //////////////////

							//mediaNode.getChildNodeList().add(filterNode);
						} else if ("attachment".equals(parser.getName())) {
							if (filter != null) {
								String fileName = parser.getAttributeValue(null, "file");
								if (filter.getAssetType() == AssetType.IMAGE) {
									//Uri attachment = Uri.fromFile(new File(videoTemplatePath, fileName));
									//((ImageSeqAsset) filter.getAsset()).getImageUriList().add(attachment);
									String fileNames[] = fileName.split(",");
									List<Uri> imageUriList = new ArrayList<Uri>(fileNames.length);
									for (String fileName2 : fileNames) {
										imageUriList.add(Uri.fromFile(new File(location, fileName2)));
									}
									((ImageSeqAsset) filter.getAsset()).setImageUriList(imageUriList);
								} else {
									Uri attachment = Uri.fromFile(new File(location, fileName));
									filter.getAsset().setUri(attachment);
								}
							}
							if (audioNode != null) {
								String fileName = parser.getAttributeValue(null, "file");
								if (fileName != null && fileName.trim().length() > 0) {
									Uri uri = Uri.fromFile(new File(location, fileName));
									AudioAsset asset = (AudioAsset) AssetMgr.buildAsset(AssetType.AUDIO, uri);
									MediaClip audio = MediaMgr.buildMediaClip(asset, audioNode.getOffset(), audioNode.getDuration());
									audioNode.setNodeData(audio);
								}
							}
						}
						// ////////////
						else if ("AudioTrack".equalsIgnoreCase(parser.getName())) {
							audioTrack = new TimeLineNode();
							audioTrack.setNodeType(NodeType.AUDIO_TRACK);

							videoTemplate.getRenderTree().getChildNodeList().add(audioTrack);
						} else if ("AudioNode".equalsIgnoreCase(parser.getName())) {
							if (audioTrack.getChildNodeList() == null) {
								audioTrack.setChildNodeList(new ArrayList<TimeLineNode>());
							}

							audioNode = new TimeLineNode();
							audioNode.setId(parser.getAttributeValue(null, "ID"));
							audioNode.setName(parser.getAttributeValue(null, "name"));
							//
							audioNode.setNodeType(NodeType.AUDIO_TRACK);
							//
							audioNode.setOffset(Long.valueOf(parser.getAttributeValue(null, "In")));
							audioNode.setDuration(Long.valueOf(parser.getAttributeValue(null, "totalframe")));
							//

							audioTrack.getChildNodeList().add(audioNode);
						} else if ("attachment".equals(parser.getName())) {
							if (audioNode != null) {
								String fileName = parser.getAttributeValue(null, "file");
								Uri uri = Uri.fromFile(new File(location, fileName));
								AudioAsset asset = (AudioAsset) AssetMgr.buildAsset(AssetType.AUDIO, uri);
								MediaClip audio = MediaMgr.buildMediaClip(asset, 0, asset.getDuration());
								audioNode.setNodeData(audio);
							}
						}
						break;

					case XmlPullParser.END_TAG:// 结束元素事件
						if ("VideoTrack".equalsIgnoreCase(parser.getName())) {
							videoTrack = null;
							mediaNode = null;
						} else if ("MediaNode".equalsIgnoreCase(parser.getName())) {
							filter = null;
						} else if ("FilterNode".equalsIgnoreCase(parser.getName())) {

						} else if ("AudioTrack".equalsIgnoreCase(parser.getName())) {
							audioTrack = null;
							audioNode = null;
						}

						break;
				}
				eventType = parser.next();
			}

			inStream.close();

		} catch (Exception e) {
			String errorInfo = "parse videoTemplate fail:" + e.getMessage() + "@" + location;
			Log.e("ExcecuteProject", errorInfo);
			// e.printStackTrace();
			throw new IllegalStateException(errorInfo);
		}

		// templateCache.put(id, videoTemplate);

		return videoTemplate;
	}

	public static void addWatermarkNode(VideoTemplate videoTemplate) {
		TimeLineNode videoTrack = null;
		Iterator<TimeLineNode> iterator = videoTemplate.getRenderTree().getChildNodeList().iterator();
		for (; iterator.hasNext();) {
			TimeLineNode temp = iterator.next();
			if (temp.getNodeType() == NodeType.VIDEO_TRACK) {
				videoTrack = temp;
				break;
			}
		}

		TimeLineNode videoNode = videoTrack.getChildNodeList().get(videoTrack.getChildNodeList().size() - 1);

		FilterGroup filter = (FilterGroup) videoNode.getChildNodeList().get(0).getNodeData();

		Filter lastFilter = filter.getFilters().get(filter.getFilters().size() - 1);
		if (lastFilter.getFilterType().startsWith("BLEND")) {
			Filter blank = FilterUtils.buildBlankFilter();
			blank.setOffset(videoTemplate.getTotalFrame() - ParamKeeper.get().getEndLogoDuration());
			blank.setDuration(ParamKeeper.get().getEndLogoDuration());
			filter.addFilter(blank);
		}
		//		Filter blur = new Filter();
		//		blur.setFilterType("GAUSSIAN_BLUR");
		//		blur.setPercentage(50);
		//		blur.setOffset(videoTemplate.getTotalFrame() - WATERMARK_DURATION + 10);
		//		blur.setDuration(WATERMARK_DURATION);
		//		filter.addFilter(blur);

		Filter watermark = FilterUtils.buildWatermarkFilter();
		watermark.setOffset(videoTemplate.getTotalFrame() - ParamKeeper.get().getEndLogoDuration());
		watermark.setDuration(ParamKeeper.get().getEndLogoDuration());
		watermark.setFadeIn(ParamKeeper.get().getEndLogoDuration() * (1000 / ParamKeeper.get().getFrameRate()));
		filter.addFilter(watermark);

	}

	public static VideoTemplate createBlankTemplate() {

		//
		VideoTemplate simpleTemplate = new VideoTemplate();
		simpleTemplate.setId("0");
		simpleTemplate.setName("blankTemplate");
		simpleTemplate.setWidth(480);
		simpleTemplate.setHeight(480);

		TimeLineNode renderTree = new TimeLineNode();
		renderTree.setId("root");
		renderTree.setName("root");
		renderTree.setChildNodeList(new ArrayList<TimeLineNode>(1));

		simpleTemplate.setRenderTree(renderTree);

		TimeLineNode videoTrackNode = new TimeLineNode();
		videoTrackNode.setNodeType(NodeType.VIDEO_TRACK);
		videoTrackNode.setChildNodeList(new ArrayList<TimeLineNode>(1));

		renderTree.getChildNodeList().add(videoTrackNode);

		TimeLineNode videoNode = new TimeLineNode();
		videoNode.setNodeType(NodeType.VIDEO_NODE);
		// videoNode.setNodeData(videoClip);
		videoNode.setChildNodeList(new ArrayList<TimeLineNode>(1));

		videoTrackNode.getChildNodeList().add(videoNode);

		TimeLineNode filterNode = new TimeLineNode();
		filterNode.setNodeType(NodeType.FILER_NODE);
		FilterGroup filterGroup = new FilterGroup();
		/*
		 * Filter filter = FilterUtils.buildBlankFilter();
		 * filter.setDuration(Conf.VIDEO_RECORD_MAXTIME / 1000 * Conf.VIDEO_FRAMERATE);
		 * filterGroup.addFilter(filter);
		 */
		filterNode.setNodeData(filterGroup);

		videoNode.getChildNodeList().add(filterNode);

		return simpleTemplate;
	}

}
