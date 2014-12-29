package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.data.MusicData;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.service.PlayerService;
import org.upmobile.newmusicdownloader.service.PlayerService.OnStatePlayerListener;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LibraryAdapter extends BaseAdapter<MusicData> {
	
	private static final int DELAY = 5000;
	private PlayerService service;
	private HashSet<MusicData> removingData = new HashSet<MusicData>();
	private final Drawable BTN_PLAY;
	private final Drawable BTN_PAUSE;
	private MusicData currentPlayData; 
    private String PACKAGE = "IDENTIFY";
	private ArrayList<Timer> timers = new ArrayList<Timer>();
	private Animation anim;
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {
		
		@Override
		public void update(AbstractSong song, int position) {
			if (song.getClass() != MusicData.class) return;
			if (position > 0) {
				((MusicData) getItem(position - 1)).turnOff(MusicData.MODE_PLAYING);
			} else if (position == 0) {
				((MusicData) getItem(getCount() - 1)).turnOff(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}
		
		@Override
		public void start(AbstractSong song, int position) {
			if (song.getClass() != MusicData.class || position == -1) return;
			if (position == getCount()) --position;
			((MusicData) getItem(position)).turnOn(MusicData.MODE_PLAYING);
			notifyDataSetChanged();
		}
		
		@Override
		public void play() {
			
		}
		
		@Override
		public void pause() {
			
		}
	};
	
	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
		BTN_PAUSE = context.getResources().getDrawable(org.upmobile.sevenplayer.R.drawable.pause_white);
		BTN_PLAY = context.getResources().getDrawable(org.upmobile.sevenplayer.R.drawable.play_white);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				service = PlayerService.get(getContext());
				service.setStatePlayerListener(stateListener);
			}
		}).start();
		
	}
	
	public LibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
		BTN_PAUSE = context.getResources().getDrawable(org.upmobile.sevenplayer.R.drawable.pause_white);
		BTN_PLAY = context.getResources().getDrawable(org.upmobile.sevenplayer.R.drawable.play_white);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				service = PlayerService.get(getContext());
				service.setStatePlayerListener(stateListener);
			}
		}).start();
	}
	
	public HashSet<MusicData> getRemovingData() {
		return removingData;
	}
	
	public void deleteRemovingData(MusicData data) {
		removingData.remove(data);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
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
		int position;

		public RemoveTimer(MusicData musicData, int position) {
			this.musicData = musicData;
			this.position = position;
		}

		public void run() {
			if (musicData.check(MusicData.MODE_VISIBLITY)) {
				musicData.turnOff(MusicData.MODE_VISIBLITY);
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
								musicData.reset(getContext());
								remove(musicData);
								service.remove(musicData);
							}
						});
					}
				});
			}
			timers.get(position).cancel();
			this.cancel();
		}
	}

	private class LibraryViewHolder extends ViewHolder<MusicData> {
		
		private ViewGroup frontView;
		private View button;
		private TextView title;
		private TextView artist;
		private TextView duration;
		private LinearLayout hidenView;
		private TextView cancel;

		public LibraryViewHolder(View v) {
			frontView = (ViewGroup) v.findViewById(org.upmobile.sevenplayer.R.id.front_layout);
			button = v.findViewById(org.upmobile.sevenplayer.R.id.item_play);
			title = (TextView) v.findViewById(org.upmobile.sevenplayer.R.id.item_title);
			artist = (TextView) v.findViewById(org.upmobile.sevenplayer.R.id.item_description);
			duration = (TextView) v.findViewById(org.upmobile.sevenplayer.R.id.item_duration);
			hidenView = (LinearLayout) v.findViewById(org.upmobile.sevenplayer.R.id.hidden_view);
			cancel = (TextView) v.findViewById(org.upmobile.sevenplayer.R.id.cancel);
		}

		@Override
		protected void hold(MusicData item, int position) {
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			if (!item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.VISIBLE) {
				hidenView.setVisibility(View.GONE);
				frontView.setX(0);
			} else if (item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.GONE){
			}
			if (item.check(MusicData.MODE_PLAYING)) {
				setButtonBackground(BTN_PAUSE);
				currentPlayData = item;
			} else {
				setButtonBackground(BTN_PLAY);
			}
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			Bitmap bitmap = item.getCover(getContext());
			setListener(item);
		}

		@SuppressLint("NewApi")
		private void setButtonBackground(Drawable drawable) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN){
				button.setBackgroundDrawable(drawable);
			} else {
				button.setBackground(drawable);
			}
		}

		private void setListener(final MusicData item) {
			frontView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						if (!service.isCorrectlyState(MusicData.class, getCount())) {
							ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
							service.setArrayPlayback(list);
						}
				        int[] screen_location = new int[2];
				        Bundle bundle = new Bundle();
				        bundle.putParcelable(Constants.KEY_SELECTED_SONG, item);
				        bundle.putInt(Constants.KEY_SELECTED_POSITION, getPosition(item));
				        bundle.putInt(PACKAGE + ".left", screen_location[0]);
				        bundle.putInt(PACKAGE + ".top", screen_location[1]);
						PlayerFragment playerFragment = new PlayerFragment();
						playerFragment.setArguments(bundle);
						break;
					case MotionEvent.ACTION_MOVE:
						break;
					}
	  				return true;
				}
			});
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!service.isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						service.setArrayPlayback(list);
					}
					service.play(getPosition(item));
					if (item.check(MusicData.MODE_PLAYING)) {
						item.turnOff(MusicData.MODE_PLAYING);
						setButtonBackground(BTN_PLAY);
					} else {
						if (!item.equals(currentPlayData)) {
							if (null != currentPlayData) {
								currentPlayData.turnOff(MusicData.MODE_PLAYING);
							}
							notifyDataSetChanged();
						}
						item.turnOn(MusicData.MODE_PLAYING);
						currentPlayData = item;
						setButtonBackground(BTN_PAUSE);
					}
				}
			});
			cancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View paramView) {
					hidenView.setVisibility(View.GONE);
					frontView.setX(0);
				}
			});
		}
	}

}
