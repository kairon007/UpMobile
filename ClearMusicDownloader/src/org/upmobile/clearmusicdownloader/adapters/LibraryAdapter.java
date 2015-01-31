package org.upmobile.clearmusicdownloader.adapters;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.graphics.Bitmap;
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

public class LibraryAdapter extends BaseAdapter<MusicData>{
	
	private static final int DELAY = 5000;
	private PlaybackService service;
	private final int BTN_PLAY = R.drawable.play_white;
	private final int BTN_PAUSE= R.drawable.pause_white;
	private MusicData currentPlayData; 
    private String PACKAGE = "IDENTIFY";
    private Timer timer;
	private Animation anim;
	private MusicData previous;
	private LibraryViewHolder libraryViewHolder;
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {
		
		@Override
		public void start(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			if (data != null) {
				data.turnOn(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}

		@Override
		public void play(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			if (data != null) {
				data.turnOn(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}

		@Override
		public void pause(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			if (data != null) {
				data.turnOff(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}

		@Override
		public void stop(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			if (data != null) {
				data.turnOff(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}

		@Override
		public void error() {
			
		}

		@Override
		public void update(AbstractSong song) {
			
		}
		
	};
	
	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
		if (PlaybackService.hasInstance()) {
			initService();
		}
	}
	
	public LibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
		if (PlaybackService.hasInstance()) {
			initService();
		}
	}
	
	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		libraryViewHolder = new LibraryViewHolder(v);
		return libraryViewHolder;
	}
	
	@Override
	public void onItemSwipeVisible(Object selected, View v) {
		if (!((MusicData) selected).check(MusicData.MODE_VISIBLITY)) {
			if (null != previous) {
				cancelTimer();
				previous.reset(getContext());
				remove(previous);
				service.remove(previous);
			}
			timer((MusicData) selected, v);
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
	
	public MusicData get(AbstractSong data) {
		if (data == null) return null;
		for (int i = 0; i < getCount(); i++) {
			MusicData buf = getItem(i);
			if (buf.equals(data)) {
				return getItem(i);
			}
		}
		return null;
	}

	public void cancelTimer() {
		if (null != timer) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}

	private void timer(MusicData musicData, View v) {
		timer = new Timer();
		RemoveTimer task = new RemoveTimer(musicData);
		timer.schedule(task, DELAY);
	}

	private class RemoveTimer extends TimerTask {

		private MusicData musicData;

		public RemoveTimer(MusicData musicData) {
			this.musicData = musicData;
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
						int wantedPosition = getPosition(musicData);
						int firstPosition  = ((UISwipableList)parent).getFirstVisiblePosition() - ((UISwipableList)parent).getHeaderViewsCount();
						int wantedChild = wantedPosition - firstPosition;
						if (wantedChild < 0 || wantedChild >= ((UISwipableList)parent).getChildCount()) return;
						parent.getChildAt(wantedChild).startAnimation(anim);
					}
				});
			}
			timer.cancel();
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
				currentPlayData = item;
			} else {
				setButtonBackground(BTN_PLAY);
			}
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			Bitmap bitmap = item.getCover(getContext());
			if (bitmap == null) {
				image.setImageResource(R.drawable.def_cover_circle);
			} else {
				image.setImageBitmap(bitmap);
			}
			setListener(item);
		}

		private void setButtonBackground(int resid) {
			button.setBackgroundResource(resid);
		}

		private void setListener(final MusicData item) {
			frontView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						if (service == null) {
							initService();
						}
						if (!service.isCorrectlyState(MusicData.class, getCount())) {
							ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
							service.setArrayPlayback(list);
						}
				        int[] screen_location = new int[2];
				        View view = v.findViewById(R.id.item_image);
				        view.getLocationOnScreen(screen_location);
				        Bundle bundle = new Bundle();
				        bundle.putParcelable(Constants.KEY_SELECTED_SONG, item);
				        bundle.putInt(PACKAGE + ".left", screen_location[0]);
				        bundle.putInt(PACKAGE + ".top", screen_location[1]);
				        bundle.putInt(PACKAGE + ".width", view.getWidth());
				        bundle.putInt(PACKAGE + ".height", view.getHeight());
						PlayerFragment playerFragment = new PlayerFragment();
						playerFragment.setArguments(bundle);
						((MainActivity) v.getContext()).changeFragment(playerFragment);
						break;
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_MOVE:
						((UISwipableList) parent).setSelectedPosition(item, v);
						break;
					}
	  				return true;
				}
			});
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (service == null) {
						initService();
					}
					if (!service.isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						service.setArrayPlayback(list);
					}
					((MainActivity) getContext()).showPlayerElement();
					service.play(item);
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
					onItemSwipeGone(item, paramView);
					hidenView.setVisibility(View.GONE);
					frontView.setX(0);
				}
			});
		}
	}

	@Override
	protected boolean isSetListener() {
		return true;
	}
	
	private void initService() {
		service = PlaybackService.get(getContext());
		service.addStatePlayerListener(stateListener);
	}
}
