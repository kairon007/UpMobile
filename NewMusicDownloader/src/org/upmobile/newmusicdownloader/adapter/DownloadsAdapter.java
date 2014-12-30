package org.upmobile.newmusicdownloader.adapter;

import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.Util;
import android.app.DownloadManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsAdapter extends BaseAdapter<MusicData> {

	private class DownloadsViewHolder extends ViewHolder<MusicData> {
		private TextView title;
		private TextView artist;
		private TextView duration;
		private ImageView cancel;
		private ImageView image;
		private ProgressBar progress;
		private View v;
		private MusicData item;

		public DownloadsViewHolder(View v) {
			this.v = v;
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			duration = (TextView) v.findViewById(R.id.item_duration);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
			image = (ImageView) v.findViewById(R.id.item_image);
			cancel = (ImageView) v.findViewById(R.id.cancel);
		}

		@Override
		protected void hold(MusicData item, int position) {
			this.item = item;
			image.setImageResource(R.drawable.def_cover_circle);
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			progress.setIndeterminate(item.getProgress() == 0);
			progress.setProgress(item.getProgress());
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			setListener(item);
		}

		private void setListener(final MusicData item) {
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					removeItem(item);
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
		if (item.getId() == -1)
			return;
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
