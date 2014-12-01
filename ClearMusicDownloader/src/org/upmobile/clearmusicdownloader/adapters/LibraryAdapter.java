package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.special.utils.UICircularImage;

public class LibraryAdapter extends BaseAdapter{

	
	private class LibraryViewHolder extends ViewHolder<Song> {
		TextView title;
		TextView artist;
		TextView duration;
		ProgressBar progress;
		UICircularImage image;

		private View v;

		public LibraryViewHolder(View v) {
			this.v = v;
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
			duration = (TextView) v.findViewById(R.id.item_duration);
		}

		@Override
		protected void hold(Song item) {
			title.setText(item.title);
			artist.setText(item.artist);
			image.setImageResource(R.drawable.fallback_cover);
			progress.setProgress(75);
			duration.setText(Util.getFormatedStrDuration(item.duration));
		}
	}

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected ViewHolder<Song> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}

}
