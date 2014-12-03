package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
import android.app.DownloadManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;

public class DownloadsAdapter extends BaseAdapter {

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
//					v.findViewById(R.id.hidden_view).setVisibility(View.GONE);
//					int startPosition = 0 - v.getContext().getResources().getDimensionPixelSize(R.dimen.swipe_size);
//					((UISwipableList) v.getParent()).slideOutView(v.findViewById(R.id.front_layout), startPosition, false);
					remove(item);
				}
			});
		}

		@Override
		protected void hold(MusicData item) {
			this.item = item;
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			image.setImageResource(R.drawable.fallback_cover);
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

	private void cancelDownload(long id) {
		DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		manager.remove(id);
	}
}
