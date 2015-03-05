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
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseMiniPlayerActivity extends Activity {

	protected final String ARRAY_SAVE = "extras_array_save";
	protected PlaybackService service;
	
	private TextView title;
	private TextView artist;
	private ImageView cover;
	private ImageButton button;
	private int checkIdCover;

	protected abstract int getMiniPlayerID();
	
	@Override
	protected void onStart() {
		android.util.Log.d("logd", "onStart()");
		startService(new Intent(this, PlaybackService.class));
		initMiniPlayer();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				setListeners();				
			}
		}).start();
		super.onStart();
	}
	
	private void initMiniPlayer() {
		title = (TextView)findViewById(R.id.mini_player_title);
		artist = (TextView)findViewById(R.id.mini_player_artist);
		cover = (ImageView)findViewById(R.id.mini_player_cover);
		button = (ImageButton)findViewById(R.id.mini_player_play_pause);
	}
	
	private void setListeners() {
		if (null == service) {
			service = PlaybackService.get(this);
		}
		service.addStatePlayerListener(new OnStatePlayerListener() {
			
			@Override
			public void update(AbstractSong song) {
				android.util.Log.d("logd", "update()");
			}
			
			@Override
			public void stop(AbstractSong song) {
				android.util.Log.d("logd", "stop()");
			}
			
			@Override
			public void start(AbstractSong song) {
				android.util.Log.d("logd", "start()");
			}
			
			@Override
			public void play(AbstractSong song) {
				android.util.Log.d("logd", "play()");
			}
			
			@Override
			public void pause(AbstractSong song) {
				android.util.Log.d("logd", "pause()");
			}
			
			@Override
			public void error() {
				android.util.Log.d("logd", "error()");
			}
		});
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				android.util.Log.d("logd", "onClick()");
				if (service.isPlaying()) {
					service.pause();
				} else {
					service.play();
				}
			}
		});
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void showMiniPlayer(boolean isShow) {
		final View view = (View) findViewById(getMiniPlayerID());
		if (isShow) {
			Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
			slideUp.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					view.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) { }
				
				@Override
				public void onAnimationEnd(Animation animation) { }
			});
			view.setAnimation(slideUp);
			view.startAnimation(slideUp);
		} else {
			Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
			slideDown.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) { }
				
				@Override
				public void onAnimationRepeat(Animation animation) { }
				
				@Override
				public void onAnimationEnd(Animation animation) {
					view.setVisibility(View.GONE);
				}
			});
			view.setAnimation(slideDown);
			view.startAnimation(slideDown);
		}
	}
	
	public void startSong(final AbstractSong song) {
		title.setText(song.getTitle());
		artist.setText(song.getArtist());
		cover.setImageResource(R.drawable.no_cover_art_big);
		OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {
			
			@Override
			public void onBitmapReady(Bitmap bmp) {
				if (this.hashCode() != checkIdCover)return;
				if (null != bmp) {
					((RemoteSong) song).setHasCover(true);
					cover.setImageResource(0);
					cover.setImageBitmap(bmp);
				} else {
					cover.setImageResource(R.drawable.no_cover_art_big);
				}
			}
		};
		checkIdCover  = readyListener.hashCode();
		((RemoteSong) song).getCover(readyListener);		
		if (null == service) {
			service = PlaybackService.get(this);
		}
		service.play(song);
	}
}
