package org.upmobile.clearmusicdownloader.adapters;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.app.ClearMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;
import com.special.utils.UISwipableList.OnSwipableListener;

public class LibraryAdapter extends BaseLibraryAdapter {
	
	private static final int DELAY = 5000;
    private Timer timer;
	private Animation anim;
	private MusicData previous;
	
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
		return new LibraryViewHolder(v);
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

	private class LibraryViewHolder extends BaseLibraryViewHolder {
		
		private ViewGroup frontView;
		private LinearLayout hidenView;
		private FrameLayout cancel;

		public LibraryViewHolder(View v) {
			frontView = (ViewGroup) v.findViewById(R.id.front_layout);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			cover = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
			hidenView = (LinearLayout) v.findViewById(R.id.hidden_view);
			cancel = (FrameLayout) v.findViewById(R.id.cancel);
			threeDot = v.findViewById(R.id.threeDot);
		}

		@Override
		protected void hold(MusicData item, int position) {
			super.hold(item, position);
			if (!item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.VISIBLE) {
				hidenView.setVisibility(View.GONE);
				frontView.setX(0);
			} else if (item.check(MusicData.MODE_VISIBLITY) && hidenView.getVisibility() == View.GONE){
				int startPosition = 0 - parent.getWidth();
				frontView.setX(startPosition);
				hidenView.setVisibility(View.VISIBLE);
			}
			setListener(item);
		}

		private void setListener(final MusicData item) {
			frontView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						frontView.setBackgroundColor(getContext().getResources().getColor(R.color.theme_color));
						break;
					case MotionEvent.ACTION_SCROLL:
						break;
					case MotionEvent.ACTION_UP:
						frontView.setBackgroundColor(getContext().getResources().getColor(R.color.white_transparent));
						if (service == null) {
							initService();
						}
						if (!service.isCorrectlyState(MusicData.class, getCount())) {
							ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
							service.setArrayPlayback(list);
						}
						if (service.isPlaying() && service.getPlayingSong().equals(item)) return true;
						((MainActivity) getContext()).showPlayerElement();
						((MainActivity) getContext()).startSong(item);
						break;
					case MotionEvent.ACTION_CANCEL:
						frontView.setBackgroundColor(getContext().getResources().getColor(R.color.white_transparent));
					case MotionEvent.ACTION_MOVE:
						((UISwipableList) parent).setSelectedPosition(item, v);
						break;
					}
	  				return true;
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
	
	@Override
	protected int getDefaultCover() {
		return R.drawable.def_cover_circle;
	}

	@Override
	protected void setListener(ViewGroup p, View view, int position) {
		if (null == parent) {
			parent = p;
		}
		((UISwipableList) parent).setOnSwipableListener(new OnSwipableListener() {

					@Override
					public void onSwipeVisible(Object selected, View v) {
						onItemSwipeVisible(selected, v);
					}

					@Override
					public void onSwipeGone(Object selected, View v) {
						onItemSwipeGone(selected, v);
					}
				});
	}

	@Override
	protected boolean showDeleteItemMenu() {
		return false;
	}

	@Override
	protected String getDirectory() {
		return ClearMusicDownloaderApp.getDirectory();
	}

	@Override
	protected void startSong(AbstractSong abstractSong) {
		((MainActivity) getContext()).startSong(abstractSong);
	}

}
