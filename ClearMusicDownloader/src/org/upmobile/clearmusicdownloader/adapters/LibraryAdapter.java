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

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
		BTN_PAUSE = context.getResources().getDrawable(R.drawable.pause_white);
		BTN_PLAY = context.getResources().getDrawable(R.drawable.play_white);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}
	
	@Override
	public void onItemSwipeVisible(int pos) {
		if (!getItem(pos).check(MusicData.MODE_VISIBLITY)) {
			timer(getItem(pos));
		}
		getItem(pos).turnOn(MusicData.MODE_VISIBLITY);	
	}

	@Override
	public void onItemSwipeGone(int pos) {
		if (getItem(pos).check(MusicData.MODE_VISIBLITY)) {
			cancelTimer();
		}
		getItem(pos).turnOff(MusicData.MODE_VISIBLITY);
	}

	public void cancelTimer() {
		task.cancel();
		timer.cancel();
	}

	private void timer(MusicData musicData) {
		timer = new Timer();
		task = new RemoveTimer(musicData);
		timer.schedule(task, DELAY);
	}

	private class RemoveTimer extends TimerTask {

		private MusicData musicData;

		public RemoveTimer(MusicData musicData) {
			this.musicData = musicData;
		}

		public void run() {
			if (musicData.check(MusicData.MODE_VISIBLITY)) {
				musicData.reset(getContext());
				((MainActivity) getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						remove(musicData);
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
				        v.getLocationOnScreen(screen_location);
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
						((UISwipableList)parent).setSelectedPosition(position);
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
					onItemSwipeGone(position);
					hidenView.setVisibility(View.GONE);
					frontView.setX(0);
				}
			});
		}
	}

}
