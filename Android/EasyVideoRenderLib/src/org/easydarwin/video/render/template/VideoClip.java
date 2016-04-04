package org.easydarwin.video.render.template;

import android.graphics.Bitmap;

public class VideoClip extends MediaClip {

	public VideoClip() {
		super();
	}

	public VideoClip(Asset asset) {
		super(asset);
	}

	@Override
	public VideoAsset getAsset() {
		return (VideoAsset) super.getAsset();
	}

	//	public long getCursor() {
	//		return getAsset() == null ? 0 : ((VideoAsset) getAsset()).getCursor();
	//	}
	//
	//	public void setCursor(long cursor) {
	//		if (getAsset() != null) {
	//			((VideoAsset) getAsset()).setCursor(cursor);
	//		}
	//	}

	public void startDecode() {
		getAsset().startDecode();
		// 跳转到素材入点
		if (getOffset() > getAsset().getCursor()) {
			int deffer = (int) (getOffset() - getAsset().getCursor());
			for (int i = 0; i < deffer; i++) {
				getNextFrame();
			}
		}
	}

	public Bitmap getNextFrame() {
		Bitmap curFrame = null;
		try {
			curFrame = getAsset().getNextFrame();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return curFrame;
	}

}
