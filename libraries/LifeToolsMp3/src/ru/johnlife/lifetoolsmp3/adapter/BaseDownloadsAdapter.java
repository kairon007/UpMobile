package ru.johnlife.lifetoolsmp3.adapter;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseDownloadsAdapter extends BaseAbstractAdapter<MusicData> {

	public BaseDownloadsAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup p) {
		View view = super.getView(position, convertView, p);
		return view;
	}
	
	protected abstract class BaseDownloadsViewHolder extends ViewHolder<MusicData> {
		
		protected TextView title;
		protected TextView artist;
		protected TextView duration;
		protected ImageView image;
		protected ProgressBar progress;
		protected View threeDot;
		
		@Override
		protected void hold(final MusicData item, int position) {
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			progress.setIndeterminate(item.getProgress() == 0);
			progress.setProgress(item.getProgress());
			if (null != duration) {
				duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			}
			if (getDefaultCover() > 0) {
				image.setImageResource(getDefaultCover());
			} else {
				image.setImageBitmap(getDefaultBitmap());
			}
			if (null != threeDot) {
				threeDot.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View view) {
						showMenu(view, item);	
					}
				});
			}
		}
	}
	
	protected void showMenu(final View view, MusicData item) {
		//for children if necessary
	}
	
	protected void removeItem(MusicData item) {
		if (null == item) return;
		String comment =  item.getComment();
		if (AbstractSong.EMPTY_COMMENT.equals(comment)) {
			comment = DownloadCache.getInstanse().getCommentFromItem(item.getTitle(), item.getArtist());
		}
		PlaybackService service = PlaybackService.get(getContext());
		if (service.isPrepared() && comment.equals(service.getPlayingSong().getComment())) {
			((BaseMiniPlayerActivity) getContext()).hideDownloadButton(false);
		}
		StateKeeper.getInstance().removeSongInfo(comment);
		DownloadCache.getInstanse().remove(item);
		remove(item);
	}
	
	protected abstract int getDefaultCover();
	protected Bitmap getDefaultBitmap() { return null; }
}
