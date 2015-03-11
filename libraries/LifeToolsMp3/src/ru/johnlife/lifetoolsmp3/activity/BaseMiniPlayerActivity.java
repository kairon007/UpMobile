package ru.johnlife.lifetoolsmp3.activity;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseMiniPlayerActivity extends Activity {

	protected final String ARRAY_SAVE = "extras_array_save";
	protected PlaybackService service;
	private AbstractSong song;
	
	private TextView title;
	private TextView artist;
	private ImageView cover;
	private ImageButton button;
	private View miniPlayer;
	private View fakeView;
	protected View progress;
	private boolean isMiniPlayerPrepared = false;
	private int checkIdCover;

	protected abstract int getMiniPlayerID();
	protected abstract int getMiniPlayerClickableID();
	protected abstract int getFakeViewID();
	protected abstract void showPlayerFragment();
	protected abstract void showPlayerElement(boolean flag);
	protected void lockListViewAnimation() {}
	
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
		progress = findViewById(R.id.mini_player_progress);
		miniPlayer = findViewById(getMiniPlayerID());
		fakeView = findViewById(getFakeViewID());
	}
	
	private void setListeners() {
		if (null == service) {
			service = PlaybackService.get(this);
		}
		service.addStatePlayerListener(new OnStatePlayerListener() {
			
			@Override
			public void update(AbstractSong song) {
				BaseMiniPlayerActivity.this.song = song;
				if (miniPlayer.getVisibility() == View.GONE) {
					setData(song);
				} else {
					showMiniPlayer(true, true);
				}
			}
			
			@Override
			public void stop(AbstractSong song) { }
			
			@Override
			public void stopPressed() {
				setPlayPauseMini(false);
				isMiniPlayerPrepared = false;
				showMiniPlayer(false);
				showPlayerElement(false);
			}
			
			@Override
			public void start(AbstractSong song) {
				refreshButton();
				setPlayPauseMini(false);
			}

			@Override
			public void play(AbstractSong song) {
				refreshButton();
				setPlayPauseMini(false);
			}
			
			@Override
			public void pause(AbstractSong song) {
				refreshButton();
				setPlayPauseMini(true);
			}

			private void refreshButton() {
				showProgress(false);
				button.setVisibility(View.VISIBLE);
			}

			@Override
			public void error() {}
			
		});
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (service.isPlaying()) {
					service.pause();
				} else {
					service.play();
				}
			}
		});
		findViewById(getMiniPlayerClickableID()).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewGroup parent = (ViewGroup) v.getParent();
				if (parent.getAnimation() != null && parent.getAnimation().hasStarted()) {
					return;
				}
				showPlayerFragment();
			}
		});
	}
	
	private void checkOnStart() {
		if (service.isPrepared()) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					isMiniPlayerPrepared = true;
					BaseMiniPlayerActivity.this.song = service.getPlayingSong();
					showMiniPlayer(true);
					setPlayPauseMini(!service.isPlaying());
				}
			});
		}
	}
	
	public void showMiniPlayer(boolean isShow) {
		showMiniPlayer(isShow, false);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void showMiniPlayer(boolean isShow, final boolean isShift) {
		if (null == miniPlayer) return;
		if (isShow && isMiniPlayerPrepared) {
			if ((miniPlayer.getVisibility() == View.VISIBLE) && !isShift) return;
			Animation slideUp = AnimationUtils.loadAnimation(this, isShift ? R.anim.slide_down : R.anim.slide_up);
			slideUp.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					if (isShift) {
						fakeView.setVisibility(View.GONE);
					} else {
						setData(song);
					}
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					if (isShift) {
						miniPlayer.setVisibility(View.GONE);
						showMiniPlayer(true);
					} else {
						fakeView.setVisibility(View.VISIBLE);
					}
				}
			});
			if (!isShift) {
				miniPlayer.setVisibility(View.VISIBLE);
			}
			miniPlayer.setAnimation(slideUp);
			miniPlayer.startAnimation(slideUp);
		} else {
			if (miniPlayer.getVisibility() == View.GONE) return;
			Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
			slideDown.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					fakeView.setVisibility(View.GONE);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					miniPlayer.setVisibility(View.GONE);
				}
			});
			miniPlayer.setAnimation(slideDown);
			miniPlayer.startAnimation(slideDown);
		}
	}
	
	public void startSong(AbstractSong song) {
		if (null == service) {
			service = PlaybackService.get(this);
		}
		if (null != service.getPlayingSong() && song.equals(this.song) && service.isPlaying()){
			return;
		}
		service.play(song);
		this.song = song;
		boolean oldIsPrepared = isMiniPlayerPrepared;
		isMiniPlayerPrepared = true;
		showMiniPlayer(true, oldIsPrepared);
	}
	
	private void setData(final AbstractSong song) {
		title.setText(song.getTitle());
		artist.setText(song.getArtist());
		setCover(null);
		button.setVisibility(service.isPrepared() ? View.VISIBLE : View.GONE);
		showProgress(!service.isPrepared());
		if (song.getClass() != MusicData.class) {
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover) return;
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
		button.setImageResource(playPayse ? getResIdFromAttribute(this, R.attr.miniPlayerPlay) : getResIdFromAttribute(this, R.attr.miniPlayerPause));
	}
	
	private int getResIdFromAttribute(final Activity activity, final int attr) {
		if (attr == 0) return 0;
		final TypedValue typedvalueattr = new TypedValue();
		activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
		return typedvalueattr.resourceId;
	}
	
	protected void setCover(Bitmap bmp) {
		if (null == bmp) {
			cover.setImageResource(R.drawable.no_cover_art_big);
		} else {
			cover.setImageBitmap(bmp);
		}
	}
	
	public boolean isMiniPlayerPrepared() {
		return isMiniPlayerPrepared;
	}

	protected void showProgress(boolean flag) {
		progress.setVisibility(flag ? View.VISIBLE : View.GONE);
	}
	
	
}