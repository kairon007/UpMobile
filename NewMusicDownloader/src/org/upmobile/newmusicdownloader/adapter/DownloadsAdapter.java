package org.upmobile.newmusicdownloader.adapter;

import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.Util;
import android.app.DownloadManager;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsAdapter extends BaseAdapter<MusicData> {

	private class DownloadsViewHolder extends ViewHolder<MusicData> {
		private TextView title;
		private TextView artist;
		private TextView duration;
		private TextView cancel;
		private LinearLayout hidenView;
		private ViewGroup frontView;
		private ProgressBar progress;
		private View v;
		private MusicData item;

		public DownloadsViewHolder(View v) {
			this.v = v;
			frontView = (ViewGroup) v.findViewById(R.id.front_layout);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			duration = (TextView) v.findViewById(R.id.item_duration);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
			cancel = (TextView) v.findViewById(R.id.cancel);
			hidenView = (LinearLayout) v.findViewById(R.id.hidden_view);
		}

		@Override
		protected void hold(MusicData item, int position) {
			this.item = item;
			if (!item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.VISIBLE) {
				hidenView.setVisibility(View.GONE);
				frontView.setX(0);
			} else if (item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.GONE) {
				hidenView.setVisibility(View.VISIBLE);
			}
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			progress.setIndeterminate(item.getProgress() == 0);
			progress.setProgress(item.getProgress());
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			setListener(item);
		}

		private void setListener(final MusicData item) {
			frontView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
						break;
					}
					return true;
				}
			});
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					hidenView.setVisibility(View.GONE);
					frontView.setX(0);
				}
			});
		}
	}

	public DownloadsAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new DownloadsViewHolder(v);
	}

	public void removeItem(MusicData item) {
		DownloadCache.getInstanse().remove(item.getArtist(), item.getTitle());
		remove(item);
		if (item.getId() == -1) return;
		cancelDownload(item.getId());
	}
	
	private void cancelDownload(long id) {
		DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		try {
			manager.remove(id);
		} catch (UnsupportedOperationException e) {
			android.util.Log.d(getClass().getSimpleName(), e + "");
		}
	}

	public boolean contains(MusicData song) {
		for (int i = 0; i < getCount(); i++) {
			if (getItem(i).equals(song)) {
				return true;
			}
		}
		return false;
	}
}
