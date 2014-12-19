package org.upmobile.clearmusicdownloader.adapters;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;
import org.upmobile.clearmusicdownloader.service.PlayerService;
import org.upmobile.clearmusicdownloader.service.PlayerService.OnStatePlayerListener;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;

public class LibraryAdapter extends BaseAdapter<MusicData> {
	
	private static final int DELAY = 5000;
	private final Drawable BTN_PLAY;
	private final Drawable BTN_PAUSE;
	private int currentPlayPosition; 
    private String PACKAGE = "IDENTIFY";
	private Timer timer;
	private RemoveTimer task;
	private Animation anim;
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {
		
		@Override
		public void update(final AbstractSong song) {
			if (song.getClass() != MusicData.class) {
				return;
			}
			((MainActivity)getContext()).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					int position = getPosition((MusicData) song);
					((MusicData)getItem(position)).turnOn(MusicData.MODE_PLAYING);
					((MusicData)getItem(position - 1)).turnOff(MusicData.MODE_PLAYING);
					notifyDataSetChanged();
				}
			});
		}
		
		@Override
		public void start(AbstractSong song) {
			if (song.getClass() != MusicData.class) {
				return;
			}
		}
		
		@Override
		public void reset() {
			
		}
		
		@Override
		public void play() {
			
		}
		
		@Override
		public void pause() {
			
		}
		
		@Override
		public void complete() {
			
		}
	};

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
		BTN_PAUSE = context.getResources().getDrawable(R.drawable.pause_white);
		BTN_PLAY = context.getResources().getDrawable(R.drawable.play_white);
		PlayerService.get(getContext()).setStatePlayerListener(stateListener);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}
	
	@Override
	public void onItemSwipeVisible(int pos, View v) {
		if (!getItem(pos).check(MusicData.MODE_VISIBLITY)) {
			timer(getItem(pos), v);
		}
		getItem(pos).turnOn(MusicData.MODE_VISIBLITY);	
	}

	@Override
	public void onItemSwipeGone(int pos, View v) {
		if (getItem(pos).check(MusicData.MODE_VISIBLITY)) {
			cancelTimer();
		}
		getItem(pos).turnOff(MusicData.MODE_VISIBLITY);
	}

	public void cancelTimer() {
		task.cancel();
		timer.cancel();
	}

	private void timer(MusicData musicData, View v) {
		timer = new Timer();
		task = new RemoveTimer(musicData, v);
		timer.schedule(task, DELAY);
	}

	private class RemoveTimer extends TimerTask {

		private MusicData musicData;
		private View v;

		public RemoveTimer(MusicData musicData, View v) {
			this.musicData = musicData;
			this.v = v;
		}

		public void run() {
			PlayerService.get(getContext()).remove(musicData);
			musicData.reset(getContext());
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
								
								remove(musicData);
								
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

	private class LibraryViewHolder extends ViewHolder<MusicData> {
		
		private MusicData item;
		private ViewGroup frontView;
		private View button;
		private TextView title;
		private TextView artist;
		private TextView duration;
		private LinearLayout hidenView;
		private TextView cancel;
		private UICircularImage image;

		public LibraryViewHolder(View v) {
			frontView = (ViewGroup) v.findViewById(R.id.front_layout);
			button = v.findViewById(R.id.item_play);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
			hidenView = (LinearLayout) v.findViewById(R.id.hidden_view);
			cancel = (TextView) v.findViewById(R.id.cancel);
		}

		@Override
		protected void hold(MusicData item, int position) {
			this.item = item;
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			if (!item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.VISIBLE) {
				hidenView.setVisibility(View.GONE);
				frontView.setX(0);
			} else if (item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.GONE){
				int startPosition = 0 - parent.getWidth();
				frontView.setX(startPosition);
				hidenView.setVisibility(View.VISIBLE);
			}
			if (item.check(MusicData.MODE_PLAYING)) {
				setButtonBackground(BTN_PAUSE);
				currentPlayPosition = position;
			} else {
				setButtonBackground(BTN_PLAY);
			}
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			image.setImageResource(R.drawable.def_cover_circle);
			setListener(position);
		}

		@SuppressLint("NewApi")
		private void setButtonBackground(Drawable drawable) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN){
				button.setBackgroundDrawable(drawable);
			} else {
				button.setBackground(drawable);
			}
		}

		private void setListener(final int position) {
			frontView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						PlayerService service = PlayerService.get(getContext());
						if (!service.isCorrectlyState(MusicData.class, getCount())) {
							ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
							service.setArrayPlayback(list);
						}
				        int[] screen_location = new int[2];
				        View view = v.findViewById(R.id.item_image);
				        view.getLocationOnScreen(screen_location);
				        Bundle bundle = new Bundle();
				        bundle.putParcelable(Constants.KEY_SELECTED_SONG, getItem(position));
				        bundle.putInt(Constants.KEY_SELECTED_POSITION, position);
				        bundle.putInt(PACKAGE + ".left", screen_location[0]);
				        bundle.putInt(PACKAGE + ".top", screen_location[1]);
				        bundle.putInt(PACKAGE + ".width", view.getWidth());
				        bundle.putInt(PACKAGE + ".height", view.getHeight());
						PlayerFragment playerFragment = new PlayerFragment();
						playerFragment.setArguments(bundle);
						((MainActivity) v.getContext()).changeFragment(playerFragment);
						break;
					case MotionEvent.ACTION_MOVE:
						((UISwipableList)parent).setSelectedPosition(position, v);
						break;
					}
	  				return true;
				}
			});
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int[] location = new int[2];
					v.getLocationOnScreen(location);
					PlayerService service = PlayerService.get(getContext());
					if (!service.isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						service.setArrayPlayback(list);
					}
					service.play(position);
					if (item.check(MusicData.MODE_PLAYING)) {
						item.turnOff(MusicData.MODE_PLAYING);
						setButtonBackground(BTN_PLAY);
					} else {
						if (currentPlayPosition != position) {
							getItem(currentPlayPosition).turnOff(MusicData.MODE_PLAYING);
							currentPlayPosition = position;
							notifyDataSetChanged();
						}
						item.turnOn(MusicData.MODE_PLAYING);
						setButtonBackground(BTN_PAUSE);
					}
				}
			});
			cancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View paramView) {
					onItemSwipeGone(position, paramView);
					hidenView.setVisibility(View.GONE);
					frontView.setX(0);
				}
			});
		}
	}

}
