package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
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
	
	private PlayerService service;
	private final Drawable BTN_PLAY;
	private final Drawable BTN_PAUSE;
	private MusicData currentPlayData; 
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
	
	public LibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
		BTN_PAUSE = context.getResources().getDrawable(R.drawable.pause_white);
		BTN_PLAY = context.getResources().getDrawable(R.drawable.play_white);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				service = PlayerService.get(getContext());
				service.setStatePlayerListener(stateListener);
			}
		}).start();
	}
	
	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
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
			frontView = (ViewGroup) v.findViewById(R.id.front_layout);
			button = v.findViewById(R.id.item_play);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			duration = (TextView) v.findViewById(R.id.item_duration);
			hidenView = (LinearLayout) v.findViewById(R.id.hidden_view);
			cancel = (TextView) v.findViewById(R.id.cancel);
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
