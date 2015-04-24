package org.upmobile.newmusicdownloader.adapter;

import org.upmobile.newmusicdownloader.R;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.adapter.BaseDownloadsAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.DownloadManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsAdapter extends BaseDownloadsAdapter {

	private class DownloadsViewHolder extends BaseDownloadsViewHolder {

		private ImageView cancel;
		
		public DownloadsViewHolder(View v) {
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			duration = (TextView) v.findViewById(R.id.item_duration);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
			image = (ImageView) v.findViewById(R.id.item_image);
			cancel = (ImageView) v.findViewById(R.id.cancel);
		}

		@Override
		protected void hold(MusicData item, int position) {
			super.hold(item, position);
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

	@Override
	protected void removeItem(MusicData item) {
		super.removeItem(item);
		if (item.getId() == -1)	return;
		DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		StateKeeper.getInstance().removeSongInfo(item.getComment());
		try {
			manager.remove(item.getId());
		} catch (UnsupportedOperationException e) {
			android.util.Log.d(getClass().getSimpleName(), e + "");
		}
	}

	@Override
	protected int getDefaultCover() {
		return R.drawable.no_cover_art_light_big_dark;
	}
}
