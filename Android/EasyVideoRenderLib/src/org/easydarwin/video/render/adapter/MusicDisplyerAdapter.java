package org.easydarwin.video.render.adapter;

import java.util.List;

import org.easydarwin.video.render.R;
import org.easydarwin.video.render.model.RenderDisplyer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MusicDisplyerAdapter extends RenderDisplyerAdapter {

	public MusicDisplyerAdapter(Context mContext, List<RenderDisplyer> models) {
		super(mContext, models);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.video_beautify_music_item, null);
			holder.txtMusicName = (TextView) convertView.findViewById(R.id.txtMusicName);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RenderDisplyer model = models.get(position);
		holder.txtMusicName.setText(model.getName());
		if (position == selectIndex) {
			convertView.setBackgroundResource(R.color.video_beautify_bottom_bg_selected);
		} else {
			convertView.setBackgroundResource(R.color.video_beautify_bottom_bg);
		}
		return convertView;
	}

	class ViewHolder {
		private TextView txtMusicName;
	}
}
