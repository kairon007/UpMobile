package ru.johnlife.lifetoolsmp3.activity;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseMiniPlayerActivity extends Activity {

	protected final String ARRAY_SAVE = "extras_array_save";
	protected PlaybackService service;
	private AbstractSong song;
	
	private TextView title;
	private TextView artist;
	private ImageView cover;
	private ImageButton button;
	private ProgressBar progress;
	private boolean isMiniPlayerPrepared = false;
	private int checkIdCover;
	private boolean isShow;

	protected abstract int getMiniPlayerID();
	protected abstract int getMiniPlayerClickableID();
	protected abstract void showPlayerFragment();
	
	@Override
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		initMiniPlayer();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				setListeners();
				checkOnStart();
			}
		}).start();
		super.onStart();
	}
	
	private void initMiniPlayer() {
		title = (TextView)findViewById(R.id.mini_player_title);
		artist = (TextView)findViewById(R.id.mini_player_artist);
		cover = (ImageView)findViewById(R.id.mini_player_cover);
		button = (ImageButton)findViewById(R.id.mini_player_play_pause);
		progress = (ProgressBar)findViewById(R.id.mini_player_progress);
	}
	
	private void setListeners() {
		if (null == service) {
			service = PlaybackService.get(this);
		}
		service.addStatePlayerListener(new OnStatePlayerListener() {
			
			@Override
			public void update(AbstractSong song) {}
			
			@Override
			public void stop(AbstractSong song) {}
			
			@Override
			public void start(AbstractSong song) {
				progress.setVisibility(View.GONE);
				button.setVisibility(View.VISIBLE);
				setPlayPauseMini(false);
			}

			@Override
			public void play(AbstractSong song) {
				button.setImageResource(R.drawable.mini_player_pause);
				setPlayPauseMini(false);
			}
			
			@Override
			public void pause(AbstractSong song) {
				button.setImageResource(R.drawable.mini_player_play);
				setPlayPauseMini(true);
			}
			
			@Override
			public void error() {}
			
		});
//		button.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if (service.isPlaying()) {
//					service.pause();
//				} else {
//					service.play();
//				}
//			}
//		});
//		findViewById(getMiniPlayerClickableID()).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				showPlayerFragment();
//			}
//		});
	}
	
	private void checkOnStart() {
		if (service.isPlaying()) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					setData(service.getPlayingSong());
					progress.setVisibility(View.GONE);
					button.setVisibility(View.VISIBLE);
				}
			});
		}
	}
	
	public void showMiniPlayer(boolean isShow) {
		this.isShow = isShow;
		showMiniPlayer(isShow, false);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void showMiniPlayer(boolean isShow, final boolean isShift) {
		final View view = findViewById(getMiniPlayerID());
		if (null == view) return;
		if (isShow && isMiniPlayerPrepared) {
			if ((view.getVisibility() == View.VISIBLE) && !isShift) return;
			Animation slideUp = AnimationUtils.loadAnimation(this, isShift ? R.anim.slide_down : R.anim.slide_up);
			slideUp.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					if (isShift) return;
					view.setVisibility(View.VISIBLE);
					setData(song);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					if (isShift) {
						view.setVisibility(View.GONE);
						showMiniPlayer(true);
					}
				}
			});
			view.setAnimation(slideUp);
			view.startAnimation(slideUp);
		} else {
			if (view.getVisibility() == View.GONE) return;
			Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
			slideDown.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					view.setVisibility(View.GONE);
				}
			});
			view.setAnimation(slideDown);
			view.startAnimation(slideDown);
		}
	}
	
	public void startSong(AbstractSong song) {
		this.song = song;
		if (isMiniPlayerPrepared) {
			isMiniPlayerPrepared = true;
			showMiniPlayer(true, true);
		} else {
			isMiniPlayerPrepared = true;
			showMiniPlayer(true);
		}
		if (null == service) {
			service = PlaybackService.get(this);
		}
		service.play(song);
	}
	
	private void setData(final AbstractSong song) {
		title.setText(song.getTitle());
		artist.setText(song.getArtist());
		setCover(null);
		button.setVisibility(service.isPlaying() ? View.VISIBLE : View.GONE);
		progress.setVisibility(service.isPlaying() ? View.GONE : View.VISIBLE);
		if (song.getClass() == RemoteSong.class) {
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover)return;
					if (null != bmp) {
						((RemoteSong) song).setHasCover(true);
						setCover(bmp);
					} else {
						setCover(null);
					}
				}
			};
			checkIdCover  = readyListener.hashCode();
			((RemoteSong) song).getCover(readyListener);		
		} else {
			setCover(song.getCover(this));
		}
	}
	
	/**
	 * Change background of button play/pause in mini player
	 * 
	 * @param playPayse true - image play, false - image pause
	 */
	protected void setPlayPauseMini(boolean playPayse) {
		if (playPayse) {
			button.setImageResource(R.drawable.mini_player_pause);
		} else {
			button.setImageResource(R.drawable.mini_player_play);
		}
	}
	
	protected void setCover(Bitmap bmp) {
		if (null == bmp) {
			cover.setImageResource(R.drawable.no_cover_art_big);
		} else {
			cover.setImageBitmap(bmp);
		}
	}
	
	public boolean isMiniPlayerVisible() {
		return isShow;
	}
}