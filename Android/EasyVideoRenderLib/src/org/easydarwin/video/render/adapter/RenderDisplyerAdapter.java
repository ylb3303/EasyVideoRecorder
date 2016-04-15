package org.easydarwin.video.render.adapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.easydarwin.video.render.R;
import org.easydarwin.video.render.model.RenderDisplyer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class RenderDisplyerAdapter extends BaseAdapter {

	protected Context mContext;
	protected List<RenderDisplyer> models;
	protected int selectIndex = -1;
	protected DisplayImageOptions imageLoaderOption;

	protected RenderDisplyer selectedData;

	public void setSelectIndex(int i) {
		selectIndex = i;
		selectedData = null;
		notifyDataSetChanged();
	}

	public void setSelectNone() {
		setSelectIndex(-1);
	}

	public RenderDisplyerAdapter(Context mContext, List<RenderDisplyer> models) {
		this.mContext = mContext;
		this.models = models;
		imageLoaderOption = new DisplayImageOptions.Builder()//
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.displayer(new RoundedBitmapDisplayer(1000))
			.build();
	}

	public void setData(List<RenderDisplyer> lists) {
		this.models = lists;
		notifyDataSetChanged();
	}

	public int addData(RenderDisplyer data) {
		int loc = -1;
		do {
			for (int i = 0; i < models.size(); i++) {
				if (data.getId().equals(models.get(i).getId())) {
					data = models.get(i);
					loc = i;
					break;
				}
			}
			this.models.add(data);
			Collections.sort(this.models, new Comparator<RenderDisplyer>() {

				@Override
				public int compare(RenderDisplyer lhs, RenderDisplyer rhs) {
					return lhs.getOrder() - rhs.getOrder();
				}
			});

			for (int i = 0; i < models.size(); i++) {
				if (data == models.get(i)) {
					loc = i;
				}
			}
		} while (false);
		selectedData = data;
		selectIndex = -1;
		notifyDataSetChanged();
		return loc * width;
	}

	@Override
	public int getCount() {
		return models != null ? models.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return models != null && models.size() > 0 ? models.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	int width = 1;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.video_beautify_render_icon_item, null);
			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			convertView.measure(w, h);
			width = convertView.getMeasuredWidth();
			holder.imageView = (ImageView) convertView.findViewById(R.id.secected_item_image);
			holder.textView = (TextView) convertView.findViewById(R.id.secected_item_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (models != null && models.size() > 0) {
			RenderDisplyer model = models.get(position);
			if (model != null) {
				holder.textView.setText(model.getName());
				ImageLoader.getInstance().displayImage("file://" + model.getIcon(), holder.imageView, imageLoaderOption);
			}
			if (selectIndex >= 0) {
				convertView.setSelected(position == selectIndex);
			} else {
				if (selectedData == null) {
					convertView.setSelected(model.getId().equals("0"));
				} else {
					convertView.setSelected(model == selectedData);
				}
			}
		}
		return convertView;
	}

	class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}
}
