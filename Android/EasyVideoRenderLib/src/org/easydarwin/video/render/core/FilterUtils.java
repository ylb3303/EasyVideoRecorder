package org.easydarwin.video.render.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoInputFilter;

import org.easydarwin.video.render.core.ImageFilterTools.FilterType;
import org.easydarwin.video.render.template.Filter;
import org.xmlpull.v1.XmlPullParser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;

public class FilterUtils {

	public static Filter build(String location) {
		return buildFromXML(location, "meta.xml");
	}

	public static Filter buildFromXML(String filterPath, String metaName) {
		Filter filter = new Filter();
		try {
			File filterFile = new File(filterPath, metaName);
			InputStream inputStream = new FileInputStream(filterFile);
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(inputStream, "UTF-8");
			int eventType = parser.getEventType();
			String attachment = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						if ("filter".equals(parser.getName())) {
							filter.setId(parser.getAttributeValue(null, "ID"));
							filter.setName(parser.getAttributeValue(null, "name"));
							filter.setFilterType(parser.getAttributeValue(null, "filterType"));
							String temp = parser.getAttributeValue(null, "percentage");
							if (temp != null) {
								filter.setPercentage(Integer.valueOf(temp));
							}
						} else if ("attachment".equals(parser.getName())) {
							String fileName = parser.getAttributeValue(null, "file");
							if (fileName != null) {
								attachment = new File(filterPath, fileName).getAbsolutePath();
							}
						}
						break;
					case XmlPullParser.END_TAG:
						if ("filter".equals(parser.getName())) {
						} else if ("attachment".equals(parser.getName())) {
							filter.setAttachment(attachment);
						}
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filter;
	}

	public static GPUImageFilter genGPUImageFilter(Filter filter) {
		GPUImageFilter gpuImageFilter = null;
		if ("BLANK".equalsIgnoreCase(filter.getFilterType())) {
			return new GPUImageFilter();
		}
		try {
			FilterType type = FilterType.valueOf(filter.getFilterType());
			gpuImageFilter = ImageFilterTools.createFilterForType(type);

			if (gpuImageFilter instanceof GPUImageTwoInputFilter) {
				if (filter.getAttachImage() != null) {
					((GPUImageTwoInputFilter) gpuImageFilter).setBitmap(filter.getAttachImage());
				}
				if (filter.getAttachment() != null) {
					Bitmap image = BitmapFactory.decodeFile(filter.getAttachment());
					((GPUImageTwoInputFilter) gpuImageFilter).setBitmap(image);
				}
			}
			if (gpuImageFilter instanceof GPUImageToneCurveFilter) {
				if (filter.getAttachImage() != null) {
					((GPUImageToneCurveFilter) gpuImageFilter).setFromCurveFileInputStream(new FileInputStream(filter.getAttachment()));
				}
			}
		} catch (Exception e) {
			Log.e("ExcecuteProject", e.toString());
		}
		return gpuImageFilter;
	}

	public static Filter buildWatermarkFilter() {
		Filter filter = new Filter();
		filter.setId("WatermarkFilter");
		filter.setName("WatermarkFilter");
		filter.setFilterType("BLEND_ALPHA");
		filter.setAttachment(RenderResHelper.getInstance().getCommonEndLogo());
		filter.setPercentage(0);
		filter.setFadeIn(1000);
		return filter;
	}

	public static Filter buildTittleFilter() {
		Filter filter = new Filter();
		filter.setId("TittleFilter");
		filter.setName("TittleFilter");
		filter.setFilterType("BLEND_ALPHA");
		filter.setPercentage(0);
		filter.setFadeIn(1000);
		filter.setFadeOut(1000);
		return filter;
	}

	public static Filter buildDecorateFilter() {
		Filter filter = new Filter();
		filter.setId("DecorateFilter");
		filter.setName("DecorateFilter");
		filter.setFilterType("BLEND_NORMAL");
		return filter;
	}

	public static Filter buildBlankFilter() {
		Filter filter = new Filter();
		filter.setId("balnkFilter");
		filter.setName("balnkFilter");
		filter.setFilterType("BLANK");
		filter.setDuration(ParamKeeper.get().getVideoMaxLength() / 1000 * ParamKeeper.get().getFrameRate());
		return filter;
	}
}
