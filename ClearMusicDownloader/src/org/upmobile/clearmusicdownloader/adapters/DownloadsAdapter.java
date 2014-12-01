package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.special.utils.UICircularImage;

public class DownloadsAdapter extends BaseAdapter {
	
	private class DownloadsViewHolder extends ViewHolder<Song> {
		TextView title;
		TextView artist;
		TextView duration;
		UICircularImage image;

		private View v;

		public DownloadsViewHolder(View v) {
			this.v = v;
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
		}

		@Override
		protected void hold(Song item) {
			title.setText(item.title);
			artist.setText(item.artist);
			image.setImageResource(R.drawable.fallback_cover);
			duration.setText(Util.getFormatedStrDuration(item.duration));
		}
	}

	public DownloadsAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected ViewHolder<Song> createViewHolder(View v) {
		return new DownloadsViewHolder(v);
	}

}
