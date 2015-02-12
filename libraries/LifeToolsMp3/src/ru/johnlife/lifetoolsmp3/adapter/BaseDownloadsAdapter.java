package ru.johnlife.lifetoolsmp3.adapter;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseDownloadsAdapter extends BaseAbstractAdapter<MusicData> {

	public BaseDownloadsAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	protected abstract class BaseDownloadsViewHolder extends ViewHolder<MusicData> {
		
		protected TextView title;
		protected TextView artist;
		protected TextView duration;
		protected ImageView image;
		protected ProgressBar progress;
		
		@Override
		protected void hold(MusicData item, int position) {
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			image.setImageResource(getDefaultCover());
			progress.setIndeterminate(item.getProgress() == 0);
			progress.setProgress(item.getProgress());
			if (duration != null) {
				duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			}
		}
	}
	
	protected void removeItem(MusicData item) {
		DownloadCache.getInstanse().remove(item.getArtist(), item.getTitle());
		remove(item);
	}
	
	protected abstract int getDefaultCover();
}
