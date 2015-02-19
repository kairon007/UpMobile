package ru.johnlife.lifetoolsmp3.adapter;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseDownloadsAdapter extends BaseAbstractAdapter<MusicData> {
	
	protected void setListener(ViewGroup parent, View view, final int position) { }

	public BaseDownloadsAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup p) {
		View view = super.getView(position, convertView, p);
		if (isSetListener()) setListener(p, view, position);
		return view;
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
			progress.setIndeterminate(item.getProgress() == 0);
			progress.setProgress(item.getProgress());
			if (duration != null) {
				duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			}
			if (getDefaultCover() > 0) {
				image.setImageResource(getDefaultCover());
			} else {
				image.setImageBitmap(getDefaultBitmap());
			}
		}
	}
	
	protected void removeItem(MusicData item) {
		DownloadCache.getInstanse().remove(item.getArtist(), item.getTitle());
		remove(item);
	}
	
	protected abstract int getDefaultCover();
	protected Bitmap getDefaultBitmap() { return null; }
}
