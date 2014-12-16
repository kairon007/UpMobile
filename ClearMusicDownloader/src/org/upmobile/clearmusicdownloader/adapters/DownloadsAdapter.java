package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
import android.app.DownloadManager;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;

public class DownloadsAdapter extends BaseAdapter<MusicData> {

	private Object lock = new Object();

	private class DownloadsViewHolder extends ViewHolder<MusicData> {
		private TextView title;
		private TextView artist;
		private TextView duration;
		private TextView cancel;
		private ViewGroup frontView;
		private ProgressBar progress;
		private UICircularImage image;

		private View v;
		private MusicData item;

		public DownloadsViewHolder(View v) {
			this.v = v;
			frontView = (ViewGroup) v.findViewById(R.id.front_layout);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
			cancel = (TextView) v.findViewById(R.id.hidden_view);
		}

		@Override
		protected void hold(MusicData item, int position) {
			this.item = item;
			if (!item.check(MusicData.MODE_VISIBLITY) && cancel.getVisibility() == View.VISIBLE) {
				cancel.setVisibility(View.GONE);
				ViewGroup box = (ViewGroup) v.findViewById(R.id.front_layout);
				box.setX(0);
			} else if (item.check(MusicData.MODE_VISIBLITY) && cancel.getVisibility() == View.GONE) {
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
			setListener(position);
		}

		private void setListener(final int position) {
			frontView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
						((UISwipableList) parent).setSelectedPosition(position);
						break;
					}
					return true;
				}
			});
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					remove(item);
					cancelDownload(item.getId());
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
	protected void onItemSwipeVisible(int pos) {
		if (getCount()  >= pos - 1) {
			getItem(pos).turnOn(MusicData.MODE_VISIBLITY);
		}
	}

	@Override
	protected void onItemSwipeGone(int pos) {
		if (getCount()  >= pos - 1) {
			getItem(pos).turnOff(MusicData.MODE_VISIBLITY);
		}
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
		synchronized (lock) {
			for (int i = 0; i < getCount(); i++) {
				if (getItem(i).equals(song)) {
					return true;
				}
			}
			return false;
		}
	}
}
