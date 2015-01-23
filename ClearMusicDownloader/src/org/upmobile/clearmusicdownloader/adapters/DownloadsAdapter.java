package org.upmobile.clearmusicdownloader.adapters;

import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.DownloadManager;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;

public class DownloadsAdapter extends BaseAdapter<MusicData> {

	private Object lock = new Object();
	private Timer timer;
	private final static int DELAY = 2000;
	private MusicData previous;

	private class DownloadsViewHolder extends ViewHolder<MusicData> {
		private TextView title;
		private TextView artist;
		private TextView duration;
		private TextView cancel;
		private LinearLayout hidenView;
		private ViewGroup frontView;
		private ProgressBar progress;
		private UICircularImage image;
		private MusicData item;

		public DownloadsViewHolder(View v) {
			frontView = (ViewGroup) v.findViewById(R.id.front_layout);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
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
				int startPosition = 0 - parent.getWidth();
				frontView.setX(startPosition);
				hidenView.setVisibility(View.VISIBLE);
			}
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			image.setImageResource(R.drawable.def_cover_circle);
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
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_MOVE:
						((UISwipableList) parent).setSelectedPosition(item, v);
						break;
					}
					return true;
				}
			});
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onItemSwipeGone(item, v);
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

	@Override
	public void onItemSwipeVisible(Object selected, View v) {
		if (!((MusicData) selected).check(MusicData.MODE_VISIBLITY)) {
			timer((MusicData) selected, v);
			if (null != previous) {
				removeItem(previous);
			}
			previous = ((MusicData) selected);
		}
		((MusicData) selected).turnOn(MusicData.MODE_VISIBLITY);
	}
	
	@Override
	public void onItemSwipeGone(Object selected, View v) {
		if (((MusicData) selected).check(MusicData.MODE_VISIBLITY)) {
			cancelTimer();
			previous = null;
		}
		((MusicData) selected).turnOff(MusicData.MODE_VISIBLITY);
	}
	
	public void removeItem(MusicData item) {
		DownloadCache.getInstanse().remove(item.getArtist(), item.getTitle());
		remove(item);
		if (item.getId() == -1) return;
		cancelDownload(item.getId());
	}
	
	public void cancelTimer() {
		timer.cancel();
	}

	private void timer(MusicData musicData, View v) {
		timer = new Timer();
		RemoveTimer task = new RemoveTimer(musicData);
		timer.schedule(task, DELAY);
	}

	private class RemoveTimer extends TimerTask {

		private MusicData musicData;
		private Animation anim;

		public RemoveTimer(MusicData musicData) {
			this.musicData = musicData;
		}

		public void run() {
			if (musicData.check(MusicData.MODE_VISIBLITY)) {
				((MainActivity) getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
						anim.setDuration(200);
						anim.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation paramAnimation) {
							}

							@Override
							public void onAnimationRepeat(Animation paramAnimation) {
							}

							@Override
							public void onAnimationEnd(Animation paramAnimation) {
								removeItem(musicData);
							}
						});
						int wantedPosition = getPosition(musicData);
						int firstPosition = ((UISwipableList) parent).getFirstVisiblePosition() - ((UISwipableList) parent).getHeaderViewsCount();
						int wantedChild = wantedPosition - firstPosition;
						if (wantedChild < 0 || wantedChild >= ((UISwipableList) parent).getChildCount()) return;
						parent.getChildAt(wantedChild).startAnimation(anim);
					}
				});
			}
			timer.cancel();
			this.cancel();
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
