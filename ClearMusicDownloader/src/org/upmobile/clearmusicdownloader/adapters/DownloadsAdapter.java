package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
import android.app.DownloadManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.special.utils.UICircularImage;

public class DownloadsAdapter extends BaseAdapter<MusicData> {

	private class DownloadsViewHolder extends ViewHolder<MusicData> {
		TextView title;
		TextView artist;
		TextView duration;
		TextView cancel;
		ProgressBar progress;
		UICircularImage image;

		private View v;
		private MusicData item;

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
					cancelDownload(item.getId());
					remove(item);
				}
			});
		}

		@Override
		protected void hold(MusicData item, int position) {
			this.item = item;
			if (!item.check(MusicData.MODE_VISIBLITY) && cancel.getVisibility() == View.VISIBLE) {
				cancel.setVisibility(View.GONE);
				ViewGroup box = (ViewGroup) v.findViewById(R.id.front_layout);
				box.setX(0);
			} else if (item.check(MusicData.MODE_VISIBLITY) && cancel.getVisibility() == View.GONE){
				ViewGroup box = (ViewGroup) v.findViewById(R.id.front_layout);
				int startPosition = 0 - parent.getContext().getResources().getDimensionPixelSize(R.dimen.swipe_size);
				box.setX(startPosition);
				cancel.setVisibility(View.VISIBLE);
			}
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			image.setImageResource(R.drawable.def_cover_circle);
			progress.setIndeterminate(item.getProgress() == 0);
			progress.setProgress(item.getProgress());
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
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
	protected void onItemSwipeVisible(int pos) {
		getItem(pos).turnOn(MusicData.MODE_VISIBLITY);
	}

	@Override
	protected void onItemSwipeGone(int pos) {
		getItem(pos).turnOff(MusicData.MODE_VISIBLITY);
	}

	private void cancelDownload(long id) {
		DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		manager.remove(id);
	}
}
