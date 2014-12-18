package org.upmobile.clearmusicdownloader.adapters;

import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.Util;
import android.app.DownloadManager;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;

public class DownloadsAdapter extends BaseAdapter<MusicData> {

	private Object lock = new Object();
	private Timer timer;
	private RemoveTimer task;
	private final static int DELAY = 2000;

	private class DownloadsViewHolder extends ViewHolder<MusicData> {
		private TextView title;
		private TextView artist;
		private TextView duration;
		private TextView cancel;
		private LinearLayout hidenView;
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
			cancel = (TextView) v.findViewById(R.id.cancel);
			hidenView = (LinearLayout) v.findViewById(R.id.hidden_view);
		}

		@Override
		protected void hold(MusicData item, int position) {
			this.item = item;
			if (!item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.VISIBLE) {
				hidenView.setVisibility(View.GONE);
				ViewGroup box = (ViewGroup) v.findViewById(R.id.front_layout);
				box.setX(0);
			} else if (item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.GONE) {
				ViewGroup box = (ViewGroup) v.findViewById(R.id.front_layout);
				int startPosition = 0 - parent.getWidth();
				box.setX(startPosition);
				hidenView.setVisibility(View.VISIBLE);
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
						((UISwipableList) parent).setSelectedPosition(position, v);
						break;
					}
					return true;
				}
			});
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onItemSwipeGone(position, v);
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
	public void onItemSwipeVisible(int pos, View v) {
		if (getCount() > pos) {
			if (!getItem(pos).check(MusicData.MODE_VISIBLITY)) {
				timer(getItem(pos), v);
			}
			getItem(pos).turnOn(MusicData.MODE_VISIBLITY);	
		}
	}
	
	@Override
	public void onItemSwipeGone(int pos, View v) {
		if (getCount() > pos) {
			if (getItem(pos).check(MusicData.MODE_VISIBLITY)) {
				cancelTimer();
			}
			getItem(pos).turnOff(MusicData.MODE_VISIBLITY);
		}
	}
	
	public void removeItem(MusicData item) {
		DownloadCache.getInstanse().remove(item.getArtist(), item.getTitle());
		remove(item);
		if (item.getId() == -1) return;
		cancelDownload(item.getId());
	}
	
	public void cancelTimer() {
		task.cancel();
		timer.cancel();
	}

	private void timer(MusicData musicData, View v) {
		timer = new Timer();
		task = new RemoveTimer(musicData, v);
		timer.schedule(task, DELAY );
	}

	private class RemoveTimer extends TimerTask {

		private MusicData musicData;
		private Animation anim;
		private View v;

		public RemoveTimer(MusicData musicData, View v) {
			this.musicData = musicData;
			this.v = v;
		}

		public void run() {
			if (musicData.check(MusicData.MODE_VISIBLITY)) {
				((MainActivity) getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
//						anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
//						anim.setDuration(200);
//						anim.setAnimationListener(new AnimationListener() {
//
//							@Override
//							public void onAnimationStart(Animation paramAnimation) {
//							}
//
//							@Override
//							public void onAnimationRepeat(Animation paramAnimation) {
//							}
//
//							@Override
//							public void onAnimationEnd(Animation paramAnimation) {
								removeItem(musicData);
//							}
//						});
//						v.startAnimation(anim);
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
