package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.special.utils.UICircularImage;

public class DownloadsAdapter extends BaseAdapter {

	private class DownloadsViewHolder extends ViewHolder<Song> {
		TextView title;
		TextView artist;
		TextView duration;
		TextView cancel;
		ProgressBar progress;
		UICircularImage image;

		private View v;
		private Song item;

		public DownloadsViewHolder(View v) {
			this.v = v;
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
			cancel = (TextView) v.findViewById(R.id.hidden_view);
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					remove(item);
				}
			});
		}

		@Override
		protected void hold(Song item) {
			this.item = item;
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
