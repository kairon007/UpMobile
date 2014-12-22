package org.upmobile.clearmusicdownloader.adapters;

import java.util.ArrayList;
import java.util.HashSet;
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
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;

public class LibraryAdapter extends BaseAdapter<MusicData> {
	
	private static final int DELAY = 5000;
	private HashSet<MusicData> removingData = new HashSet<MusicData>();
	private final Drawable BTN_PLAY;
	private final Drawable BTN_PAUSE;
	private int currentPlayPosition; 
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
			if (song.getClass() != MusicData.class) return;
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
		BTN_PAUSE = context.getResources().getDrawable(R.drawable.pause_white);
		BTN_PLAY = context.getResources().getDrawable(R.drawable.play_white);
		PlayerService.get(getContext()).setStatePlayerListener(stateListener);
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
	
	@Override
	public void onItemSwipeVisible(int pos, View v) {
		MusicData data = getItem(pos);
		if (!data.check(MusicData.MODE_VISIBLITY)) {
			timer(getItem(pos), v);
		}
		data.turnOn(MusicData.MODE_VISIBLITY);
		removingData.add(data);
	}

	@Override
	public void onItemSwipeGone(int pos, View v) {
		MusicData data = getItem(pos);
		if (data.check(MusicData.MODE_VISIBLITY)) {
			cancelTimer();
		}
		data.turnOff(MusicData.MODE_VISIBLITY);
		removingData.remove(data);
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
								PlayerService.get(getContext()).remove(musicData);
							}
						});
						int wantedChild = getPosition(musicData);
						View view = parent.getChildAt(wantedChild);
						if (view != null) {
							view.startAnimation(anim);
						} else {
							musicData.reset(getContext());
							setNotifyOnChange(false);
							remove(musicData);
							setNotifyOnChange(true);
							PlayerService.get(getContext()).remove(musicData);
						}
					}
				});
			}
			timers.get(position).cancel();
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
					PlayerService service = PlayerService.get(getContext());
					if (!service.isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						service.setArrayPlayback(list);
					}
					((MainActivity) getContext()).showPlayerElement();
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
