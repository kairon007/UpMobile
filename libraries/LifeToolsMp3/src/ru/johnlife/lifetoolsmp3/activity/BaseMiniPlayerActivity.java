package ru.johnlife.lifetoolsmp3.activity;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.services.HelperService;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.services.PlaybackService.OnErrorListener;
import ru.johnlife.lifetoolsmp3.services.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;

public abstract class BaseMiniPlayerActivity extends AppCompatActivity implements OnClickListener {

	protected final String ARRAY_SAVE = "extras_array_save";
	protected PlaybackService service;
	private AbstractSong song;
	
	private TextView title;
	private TextView artist;
	private ImageView cover;
	private View playPause;
	private View download;
	private View miniPlayer;
	private View fakeView;
	private View fakeMiniPlayer;
	protected View progress;
	private boolean isMiniPlayerPrepared = false;
	
	private boolean isPlayerFragmentVisible = false;
	private boolean isAnimated = false;
	private boolean methodIsCalled = false;
	private boolean isShown = false;
	protected boolean currentFragmentIsPlayer = false;
	
	private int checkIdCover;

	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void update(AbstractSong s) {
			song = s;
			if (miniPlayer.getVisibility() == View.GONE || miniPlayer.getVisibility() == View.INVISIBLE) {
				setData(s);
			} else {
				showMiniPlayer(true, true);
			}
		}

		@Override
		public void stop(AbstractSong s) {
			showPlayerElement(currentFragmentIsPlayer);
		}

		@Override
		public void stopPressed() {
			setPlayPauseMini(false);
			isMiniPlayerPrepared = false;
			showMiniPlayer(false);
			showPlayerElement(currentFragmentIsPlayer);
		}

		@Override
		public void start(AbstractSong s) {
			song = s;
//			setData(s);
			isMiniPlayerPrepared = true;
			showProgress(false);
			showPlayerElement(true);
			setPlayPauseMini(false);
//			if (!currentFragmentIsPlayer && s.getClass() == MusicData.class) {
//				boolean oldIsPrepared = isMiniPlayerPrepared;
//				isMiniPlayerPrepared = true;
//				showMiniPlayer(isShown, oldIsPrepared);
//			}
		}
		
		@Override
		public void play(AbstractSong song) {
			showProgress(false);
			setPlayPauseMini(false);
		}

		@Override
		public void pause(AbstractSong song) {
			showProgress(false);
			setPlayPauseMini(true);
		}

		@Override
		public void error() {}

		@Override
		public void onTrackTimeChanged(int time, boolean isOverBuffer) {}

		@Override
		public void onBufferingUpdate(double percent) {}
	};
	

	protected abstract String getDirectory();
	protected abstract int getMiniPlayerID();
	protected abstract int getMiniPlayerDuplicateID();
	protected abstract int getMiniPlayerClickableID();
	protected abstract int getFakeViewID();
	protected abstract void showPlayerFragment();
	protected abstract void showPlayerElement(boolean flag);
	protected abstract boolean isAnimationEnabled();
	protected void lockListViewAnimation() {}
	protected void setImageDownloadButton() {}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateKeeper.getInstance().initSongHolder(getDirectory());
		checkDownloadingUrl(true);
		if (null != savedInstanceState && savedInstanceState.containsKey(ARRAY_SAVE) && null != service) {
			ArrayList<AbstractSong> list = savedInstanceState.getParcelableArrayList(ARRAY_SAVE);
			service.setArrayPlayback(list);
		}
		methodIsCalled = true;
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		initMiniPlayer();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				setListeners();
			}
		}).start();
	}
	
	@Override
	protected void onDestroy() {
		ArrayList<RemoteSong> downloadedSongs = checkDownloadingUrl(true);
		if (!downloadedSongs.isEmpty()) {
			Intent trasferData = new Intent(this, HelperService.class);
			trasferData.putParcelableArrayListExtra(Constants.EXTRA_DATA, downloadedSongs);
			startService(trasferData);
		}
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		if (!methodIsCalled) {
			checkDownloadingUrl(false);
		}
		startService(new Intent(this, PlaybackService.class));
		setImageDownloadButton();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				checkOnStart(true);
			}
		}).start();
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		methodIsCalled = false;
		if (null != service) {
			service.removeStatePlayerListener(stateListener);
		}
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (!PlaybackService.hasInstance()) return;
		if (null == service) {
			service = PlaybackService.get(this);
		}
		boolean isPrepared = service.isEnqueueToStream();
		StateKeeper keeper = StateKeeper.getInstance();
		AbstractSong curr = service.getPlayingSong();
		showPlayerElement(isPrepared);
		if (!currentFragmentIsPlayer) {
			showMiniPlayer(isPrepared);
		}
		keeper.notifyLable(true);
		if (isPrepared) {
			setData(curr);
		}
		service.addStatePlayerListener(stateListener);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (null != service && service.hasArray()) {
			out.putParcelableArrayList(ARRAY_SAVE, service.getArrayPlayback());
		}
	}
	
	private void initMiniPlayer() {
		title = (TextView)findViewById(R.id.mini_player_title);
		artist = (TextView)findViewById(R.id.mini_player_artist);
		cover = (ImageView)findViewById(R.id.mini_player_cover);
		playPause = findViewById(R.id.mini_player_play_pause);
		progress = findViewById(R.id.mini_player_progress);
		download = findViewById(R.id.mini_player_download);
		miniPlayer = findViewById(getMiniPlayerID());
		fakeView = findViewById(getFakeViewID());
	}
	
	protected void setListeners() {
		if (null == service) {
			service = PlaybackService.get(this);
		}
		service.addStatePlayerListener(stateListener);
		service.setOnErrorListener(new OnErrorListener() {

			@Override
			public void error(final String error) {
				runOnUiThread(new Runnable() {
					public void run() {
						setPlayPauseMini(false);
						isMiniPlayerPrepared = false;
						showMiniPlayer(false);
						showPlayerElement(false); 
						showMessage(error);
					}
				});
			}
		});
		playPause.setOnClickListener(this);
		download.setOnClickListener(this);
		findViewById(getMiniPlayerClickableID()).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		int miniPlayerClickableID = getMiniPlayerClickableID();
		int id = view.getId();
		Util.hideKeyboard(this, view);
		if (id == R.id.mini_player_play_pause) {
			if (service.isPlaying()) {
				service.pause();
			} else {
				service.play();
			}
		} else if (id == R.id.mini_player_download) {
				downloadSong();
		} else if (id == miniPlayerClickableID) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (isAnimationEnabled() && (isAnimated || isPlayerFragmentVisible || (parent.getAnimation() != null && parent.getAnimation().hasStarted())))	{
				return;
			}
			isAnimated = true;
			showPlayerFragment();
		}
	}
	
	protected void checkOnStart(final boolean showMiniPlayer) {
		if (PlaybackService.hasInstance()) {
			service = PlaybackService.get(this);
		} else return;
		if ((service.isPlaying() || service.isPaused())) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					song = service.getPlayingSong();
					if (showMiniPlayer && !currentFragmentIsPlayer) {
						isMiniPlayerPrepared = true;
						showMiniPlayer(true);
						setPlayPauseMini(!service.isPlaying());
					}
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
		if (miniPlayer.getVisibility() == View.VISIBLE && isShift) {
			customDownloadButton();
			startFakeAnimation();
		}
		if (isShow && isMiniPlayerPrepared) {
			if (miniPlayer.getVisibility() == View.VISIBLE && !isAnimated) {
				return;
			}
			if (!isAnimationEnabled()) {
				miniPlayer.setVisibility(View.VISIBLE);
				View parentMiniPlayer = (View) miniPlayer.getParent();
				parentMiniPlayer.setVisibility(View.VISIBLE);
				setData(song);
				if (null != fakeView) fakeView.setVisibility(View.VISIBLE);
				customDownloadButton();
				return;
			}
            miniPlayerAnimationStart(true);
			Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.miniplayer_slide_in_up);
			slideUp.setAnimationListener(new AnimationListener() {
			
				@Override
				public void onAnimationStart(Animation animation) {
					setData(song);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					isAnimated = false;
					if (null != fakeView) fakeView.setVisibility(View.VISIBLE);
					customDownloadButton();
                    miniPlayerAnimationEnd(true);
				}
			});
			miniPlayer.setVisibility(View.VISIBLE);
			View parentMiniPlayer = (View) miniPlayer.getParent();
			parentMiniPlayer.setVisibility(View.VISIBLE);
			parentMiniPlayer.setAnimation(slideUp);
			parentMiniPlayer.startAnimation(slideUp);
			isAnimated = true;
		} else {
			if (miniPlayer.getVisibility() == View.GONE) return;
			if (!isAnimationEnabled()) {
				if (null != fakeView)
					fakeView.setVisibility(View.GONE);
				((View)miniPlayer.getParent()).setVisibility(View.GONE);
				miniPlayer.setVisibility(View.GONE);
				((View)miniPlayer.getParent()).setVisibility(View.GONE);
				return;
			}
            miniPlayerAnimationStart(false);
			final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.miniplayer_slide_out_down);
			slideDown.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					if (null != fakeView) fakeView.setVisibility(View.GONE);
					((View)miniPlayer.getParent()).setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {}

				@Override
				public void onAnimationEnd(Animation animation) {
					isAnimated = false;
					miniPlayer.setVisibility(View.GONE);
					((View)miniPlayer.getParent()).setVisibility(View.GONE);
                    miniPlayerAnimationEnd(false);
				}
			});
			final View parentMiniPlayer = (View) miniPlayer.getParent();
			parentMiniPlayer.setAnimation(slideDown);
            parentMiniPlayer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    parentMiniPlayer.startAnimation(slideDown);
                }
            }, 300);
			isAnimated = true;
		}
	}

	private void customDownloadButton() {
		hideDownloadButton(song.getClass() == MusicData.class);
	}

	public void hideDownloadButton(boolean hide) {
		download.setVisibility(hide ? View.GONE : View.VISIBLE);
//		if (null != fakeMiniPlayer) {
//			fakeMiniPlayer.findViewById(R.id.mini_player_download).setVisibility(hide ? View.GONE : View.VISIBLE);
//		}
	}

	private void startFakeAnimation () {
		if (!isAnimationEnabled()) {
			setData(song);
			return;
		}
		fakeMiniPlayer = findViewById(getMiniPlayerDuplicateID());
		((TextView)fakeMiniPlayer.findViewById(R.id.mini_player_artist)).setText(artist.getText());
		((TextView)fakeMiniPlayer.findViewById(R.id.mini_player_title)).setText(title.getText());
		((ImageView)fakeMiniPlayer.findViewById(R.id.mini_player_cover)).setImageDrawable(cover.getDrawable());
		if (playPause.getClass() == ImageView.class) {
			((ImageView) fakeMiniPlayer.findViewById(R.id.mini_player_play_pause)).setImageDrawable(((ImageButton)playPause).getDrawable());
		}
		fakeMiniPlayer.findViewById(R.id.mini_player_progress).setVisibility(View.GONE);
		final Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.miniplayer_slide_out_left);
        slideOutLeft.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				fakeMiniPlayer.setVisibility(View.GONE);
			}
		});
		final Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.miniplayer_slide_in_right);
		fakeMiniPlayer.setVisibility(View.VISIBLE);
		fakeMiniPlayer.findViewById(R.id.mini_player_download).setVisibility(View.GONE);
        long startTime = System.currentTimeMillis();
        slideOutLeft.setStartTime(startTime);
        fakeMiniPlayer.setAnimation(slideOutLeft);
		slideInRight.setStartTime(startTime);
        miniPlayer.setAnimation(slideInRight);
        miniPlayer.post(new Runnable() {
			@Override
			public void run() {
				miniPlayer.startAnimation(slideInRight);
				fakeMiniPlayer.startAnimation(slideOutLeft);
			}
		});
		setData(song);
	}
	
	public void startSong(AbstractSong song) {
		startSong(song, true);
	}
	
	public void startSong(AbstractSong song, boolean show) {
		if (null == service) {
			service = PlaybackService.get(this);
		}
		if (service.isPlaying() && song.equals(service.getPlayingSong())) return;
		this.song = song;
		isShown = show;
		service.play(song);
		if (!currentFragmentIsPlayer) {
			boolean oldIsPrepared = isMiniPlayerPrepared;
			isMiniPlayerPrepared = true;
			showMiniPlayer(isShown, oldIsPrepared);
		}
	}
	
	private void setData(final AbstractSong song) {
		hideDownloadButton(MusicData.class == song.getClass());
		title.setText(song.getTitle());
		artist.setText(song.getArtist());
		showProgress(!service.isPrepared());
		RemoteSong.OnBitmapReadyListener readyListener;
		if (song.getClass() != MusicData.class) {
			setCover(null);
			readyListener = new RemoteSong.OnBitmapReadyListener() {

				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover) return;
					((RemoteSong) song).setCover(bmp);
					((RemoteSong) song).setHasCover(null != bmp);
					setCover(bmp);
				}
			};
			checkIdCover = readyListener.hashCode();
			((RemoteSong) song).getCover(readyListener);
		} else {
			readyListener = new RemoteSong.OnBitmapReadyListener() {
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover) return;
					if (bmp != null) {
						int size = Util.dpToPx(BaseMiniPlayerActivity.this, 64);
						bmp = Util.resizeBitmap(bmp, size, size);
					}
					setCoverInUI(bmp);
				}
			};
			checkIdCover = readyListener.hashCode();
			((MusicData)song).getCover(readyListener);
		}
	}

	public void setCoverInUI(final Bitmap bmp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setCover(bmp);
			}
		});
	}
	
	/**
	 * Change background of button play/pause in mini player
	 * 
	 * @param playPause true - image play, false - image pause
	 */
	protected void setPlayPauseMini(boolean playPause) {
		if (this.playPause.getClass() == ImageButton.class) {
			((ImageButton) this.playPause).setImageResource(playPause ? Util.getResIdFromAttribute(this, R.attr.miniPlayerPlay) : Util.getResIdFromAttribute(this, R.attr.miniPlayerPause));
		}
	}
	
	private void downloadSong() {
		song.getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				((RemoteSong) song).setDownloadUrl(url);
				download(((RemoteSong) song));
			}

			@Override
			public void error(final String error) {
				runOnUiThread(new Runnable() {
					public void run() {
						showMessage(getString(R.string.error_getting_url_songs));
					}
				});
			}
		});
	}
	
	protected void download(RemoteSong song) {
        BaseDownloadSongTask downloadListener = new BaseDownloadSongTask(BaseMiniPlayerActivity.this, song, 0);
		downloadListener.setDownloadPath(getDirectory());
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
	}
	
	protected void setCover(Bitmap bmp) {
		if (null == bmp) {
			cover.setImageResource(R.drawable.no_cover_art_big);
		} else {
			cover.setImageBitmap(bmp);
			service.updatePictureNotification(bmp);
		}
	}

	public View getMiniPlayer() {
		return miniPlayer;
	}
	
	public boolean isMiniPlayerPrepared() {
		return isMiniPlayerPrepared;
	}

	protected void showProgress(boolean isShowProgres) {
        progress.setVisibility(isShowProgres ? View.VISIBLE : View.GONE);
        playPause.setVisibility(isShowProgres ? View.GONE : View.VISIBLE);
	}
	
	protected void setPlayerFragmentVisible(boolean value) {
		isPlayerFragmentVisible = value;
	}
	
	public void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	public void showMessage(int message) {
		showMessage(getString(message));
	}
	
	protected ArrayList<RemoteSong> checkDownloadingUrl(boolean expandAction) {
		ArrayList<RemoteSong> result = new ArrayList<>();
		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		if (null != manager) {
			Cursor pending = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING));
			if (pending != null) {
				updateList(pending, result, expandAction);
			}
			Cursor paused = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PAUSED));
			if (paused != null) {
				updateList(paused, result, expandAction);
			}
			Cursor waitingNetwork = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.PAUSED_WAITING_FOR_NETWORK));
			if (waitingNetwork != null) {
				updateList(waitingNetwork, result, expandAction);
			}
			Cursor unknown = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.PAUSED_UNKNOWN));
			if (unknown != null) {
				updateList(unknown, result, expandAction);
			}
			Cursor running = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING));
			if (running != null) {
				updateList(running, result, expandAction);
			}
		}
		return result;
	}

	private void updateList(Cursor c, ArrayList<RemoteSong> result, boolean expandAction) {
		DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		while (c.moveToNext()) {
			String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
			String path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
			if (null != path && path.contains(getDirectory())) {
				String strComment = c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
				StateKeeper.getInstance().putSongInfo(strComment, AbstractSong.EMPTY_PATH, StateKeeper.DOWNLOADING);
				if (expandAction) {
					String strTitle = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
					String strArtist = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
					int id = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID));
					RemoteSong checkSong = new RemoteSong(url);
					checkSong.setTitle(strTitle);
					checkSong.setArtist(strArtist);
					checkSong.setPath(path);
					checkSong.setComment(strComment);
					checkSong.id = id;
					BaseDownloadSongTask listener = createDownloadListener(checkSong);
					listener.createUpdater(manager, id);
					if (!result.contains(checkSong)) {
						result.add(checkSong);
					}
				}
			}
		}
		c.close();
	}
	
	protected BaseDownloadSongTask createDownloadListener (RemoteSong song) {
		return new BaseDownloadSongTask(this, song, 0);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		View view = (View) miniPlayer.getParent();
		if (null != view) {
			view.clearAnimation();
		}
		super.onConfigurationChanged(newConfig);
	}

    protected void miniPlayerAnimationStart(boolean isUpAnimation) {
    }

    protected void miniPlayerAnimationEnd(boolean isUpAnimation) {
    }
}