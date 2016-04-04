package org.easydarwin.video.render.template;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

import org.easydarwin.video.render.core.ParamKeeper;

import android.graphics.Bitmap;

public class FilterEffect extends Effect {

	private GPUImage gpuImage = null;

	private GPUImageView gpuImageView = null;

	private GPUImageFilterGroup filterGroup = null;

	public FilterEffect() {
		super();
	}

	private GPUImage getGPUImage() {
		if (gpuImage != null) {
			return gpuImage;
		}
		gpuImage = new GPUImage(ParamKeeper.get().getContext());
		if (filterGroup.getFilters().size() == 1) {
			gpuImage.setFilter(filterGroup.getFilters().get(0));
		} else {
			filterGroup.updateMergedFilters();
			gpuImage.setFilter(filterGroup);
		}

		return gpuImage;
	}

	private void setGPUImage() {
		if (gpuImageView != null) {
			return;
		}
		gpuImageView = ParamKeeper.get().getGPUImageView();
		if (filterGroup.getFilters().size() == 1) {
			gpuImageView.setFilter(filterGroup.getFilters().get(0));
		} else {
			filterGroup.updateMergedFilters();
			gpuImageView.setFilter(filterGroup);
		}
	}

	@Override
	public Bitmap applyEffect(Bitmap curFrame, boolean isPreview) {
		if (isPreview) {
			if (gpuImageView == null) {
				setGPUImage();
			}
			gpuImageView.setImage(curFrame);
			return curFrame;
		} else {
			if (gpuImage == null) {
				gpuImage = getGPUImage();
			}
			gpuImage.setImage(curFrame);
			curFrame = gpuImage.getBitmapWithFilterApplied();
			return curFrame;
		}
	}

	protected void addEffect(GPUImageFilter gpuImageFilter, int index) {
		if (filterGroup == null) {
			filterGroup = new GPUImageFilterGroup();
		}
		if (index < 0) {
			index = filterGroup.getFilters().size();
		}
		if (!filterGroup.getFilters().contains(gpuImageFilter)) {
			filterGroup.getFilters().add(index, gpuImageFilter);
			filterGroup.updateMergedFilters();
			if (gpuImageView != null) {
				gpuImageView.setFilter(filterGroup);
			} else if (gpuImage != null) {
				gpuImage.setFilter(filterGroup);
			}
		}
	}

	protected void removeEffect(GPUImageFilter gpuImageFilter) {
		if (filterGroup.getFilters().contains(gpuImageFilter)) {
			filterGroup.getFilters().remove(gpuImageFilter);
			filterGroup.updateMergedFilters();
			if (gpuImageView != null) {
				gpuImageView.setFilter(filterGroup);
			} else if (gpuImage != null) {
				gpuImage.setFilter(filterGroup);
			}
		}
	}

	public void clear() {
//		if (filterGroup != null) {
//			filterGroup.onDestroy();
//			filterGroup = null;
//		}
//		System.gc();
	}
}
