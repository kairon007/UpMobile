package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.newmusicdownloader.activity.MainActivity;
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
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsAdapter extends BaseAdapter<MusicData> {

	private Object lock = new Object();
	private ArrayList<Timer> timers = new ArrayList<Timer>();
	private final static int DELAY = 2000;

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
			frontView = (ViewGroup) v.findViewById(org.upmobile.sevenplayer.R.id.front_layout);
			title = (TextView) v.findViewById(org.upmobile.sevenplayer.R.id.item_title);
			artist = (TextView) v.findViewById(org.upmobile.sevenplayer.R.id.item_description);
			duration = (TextView) v.findViewById(org.upmobile.sevenplayer.R.id.item_duration);
			progress = (ProgressBar) v.findViewById(org.upmobile.sevenplayer.R.id.item_progress);
			cancel = (TextView) v.findViewById(org.upmobile.sevenplayer.R.id.cancel);
			hidenView = (LinearLayout) v.findViewById(org.upmobile.sevenplayer.R.id.hidden_view);
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
	
	public void cancelTimer() {
		for (Timer timer : timers) {
			timer.cancel();
		}
	}

	private void timer(MusicData musicData, View v) {
		Timer timer = new Timer();
		timers.add(timer);
		int pos = timers.indexOf(timer);
		RemoveTimer task = new RemoveTimer(musicData, pos);
		timer.schedule(task, DELAY);
	}

	private class RemoveTimer extends TimerTask {

		private MusicData musicData;
		private Animation anim;
		private int position;

		public RemoveTimer(MusicData musicData, int position) {
			this.musicData = musicData;
			this.position = position;
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
					}
				});
			}
			timers.get(position).cancel();
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
