package org.upmobile.clearmusicdownloader.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.io.File;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.DownloadListener;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.service.PlayerService;
import org.upmobile.clearmusicdownloader.service.PlayerService.OnStatePlayerListener;

import ru.johnlife.lifetoolsmp3.RenameTask;
import ru.johnlife.lifetoolsmp3.RenameTaskSuccessListener;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.special.utils.UIParallaxScroll;

public class PlayerFragment  extends Fragment implements OnClickListener, OnSeekBarChangeListener {

    public static final int DURATION = 500; // in ms
    private String PACKAGE = "IDENTIFY";
	private AbstractSong song;
	private RenameTask renameTask;
	private PlayerService player;
	private DownloadListener downloadListener;
	private LyricsFetcher lyricsFetcher;
	private View parentView;
	private SeekBar playerProgress;
	private CheckBox playerTagsCheckBox;
	private FrameLayout playerTitleBar;
	private ImageButton play;
	private ImageButton previous;
	private ImageButton forward;
	private ImageView playerBtnTitle;
	private ImageView playerBtnArtist;
	private EditText playerEtTitle;
	private EditText playerEtArtist;
	private TextView playerTvTitle;
	private TextView playerTvArtist;
	private ImageView playerCover;
	private ImageView lyricsLoader;
	private ImageView playerTitleBarCover;
	private Button download;
	private Button playerTitleBarBack;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private TextView playerLyricsView;
	private TextView playerTitleBarTitle;
	private TextView playerTitleBarArtis;
	private int currentPosition;
    private int delta_top;
    private int delta_left;
	private int top;
	private int left;
	private int width;
	private int height;
	private int deltaLeftTitleBar;
	private int deltaTopTitleBar;
	private float scaleWidthTitleBar;
	private float scaleHeightTitleBar;
    private float scale_width;
    private float scale_height;
    private boolean isDestroy;
    private boolean hadInstance;
    private boolean isUseAlbumCover = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		parentView = inflater.inflate(R.layout.player, container, false);
		((MainActivity) getActivity()).hideTopFrame();
		((MainActivity) getActivity()).showPlayerElement();
		((UIParallaxScroll) parentView.findViewById(R.id.scroller)).setOnScrollChangedListener(mOnScrollChangedListener);
		init();
		setListener();
		playerTitleBar.getBackground().setAlpha(0);
		playerTitleBarArtis.setVisibility(View.INVISIBLE);
		playerTitleBarTitle.setVisibility(View.INVISIBLE);
		playerCover.bringToFront();
		((MainActivity) getActivity()).getResideMenu().addIgnoredView(playerProgress);
		if (null != getArguments() && getArguments().containsKey(Constants.KEY_SELECTED_SONG)) {
			hadInstance = false;
			song = getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
			currentPosition = getArguments().getInt(Constants.KEY_SELECTED_POSITION);
			getCover(song);
			top = getArguments().getInt(PACKAGE + ".top");
			left = getArguments().getInt(PACKAGE + ".left");
			width = getArguments().getInt(PACKAGE + ".width");
			height = getArguments().getInt(PACKAGE + ".height");
		} else {
			hadInstance = true;
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				player = PlayerService.get(getActivity());
				bindToPlayer();
				final int current;
				final int mode;
				final boolean enabledPlayerElement;
				if (hadInstance) {
					song = player.getPlayingSong();
					currentPosition = player.getPlayingPosition();
					if (player.isGettingURl() || !player.isPrepared()) {
						enabledPlayerElement = false;
						current = 0;
						mode = 0;
					} else {
						boolean check = player.isPlaying();
						current = player.getCurrentPosition();
						if (check) {
							mode = -1;
						} else {
							mode = 1;
						}
						enabledPlayerElement = true;
					}
				} else {
					if (player.hasValidSong(song.getClass()) && player.getPlayingPosition() == currentPosition) {
						boolean check = player.isPlaying();
						current = player.getCurrentPosition();
						enabledPlayerElement = true;
						if (check) {
							mode = -1;
						} else {
							mode = 1;
						}
					} else {
						mode = 0;
						current = 0;
						enabledPlayerElement = false;
						player.setPlayingPosition(-1);
						player.play(currentPosition);
					}
				}
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					
					@Override
					public void run() {
						getCover(song);
						showLyrics();
						startImageAnimation(playerCover);
						setElementsView(current);
						if (!enabledPlayerElement) {
							setClickablePlayerElement(false);
						}
						if (mode > 0) {
							changePlayPauseView(true);
						} else if (mode < 0){
							changePlayPauseView(false);
						}
					}
				});
			}
		}).start();
		return parentView;
	}

	private void bindToPlayer() {
		player.setStatePlayerListener(new OnStatePlayerListener() {
			
			@Override
			public void start(AbstractSong song, int position) {
				if (song.getClass() != MusicData.class) {
					PlayerFragment.this.song = ((RemoteSong) song).cloneSong();
				} else {
					PlayerFragment.this.song = song;
				}
				if (isDestroy) return;
				setClickablePlayerElement(true);
				setElementsView(0);
				playerProgress.post(progressAction);
			}
			
			@Override
			public void pause() {
				if (isDestroy) return;
				changePlayPauseView(true);
			}

			@Override
			public void play() {
				if (isDestroy) return;
				changePlayPauseView(false);
			}

			@Override
			public void update(final AbstractSong song, int position) {
				if (isDestroy) return;
				PlayerFragment.this.song = song;
				showLyrics();
				setElementsView(0);
				setClickablePlayerElement(false);
			}
			
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					onBackPress();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onDestroy() {
		isDestroy = true;
		super.onDestroy();
	}
	
	@Override
	public void onPause() {
		if (null != lyricsFetcher) {
			lyricsFetcher.cancel();
		}
		super.onPause();
	}

	private void setClickablePlayerElement(boolean isClickable) {
		play.setClickable(isClickable);
		playerProgress.setEnabled(isClickable);
		if (isClickable) {
			play.setImageResource(R.drawable.pause);
			play.setVisibility(View.VISIBLE);
			parentView.findViewById(R.id.player_play_progress).setVisibility(View.GONE);
			
		} else {
			playerCurrTime.setText("0:00");
			playerProgress.setProgress(0);
			play.setVisibility(View.GONE);
			parentView.findViewById(R.id.player_play_progress).setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * @param isPlaying - If "true", the button changes the picture to "play", if "false" changes to "pause"
	 */
	private void changePlayPauseView(boolean isPlaying) {
		if (isPlaying) {
			play.setImageResource(R.drawable.play);
		} else {
			play.setImageResource(R.drawable.pause);
		}
	}
	
	private void init() {
		play = (ImageButton) parentView.findViewById(R.id.player_play);
		previous = (ImageButton) parentView.findViewById(R.id.player_previous);
		forward = (ImageButton) parentView.findViewById(R.id.player_forward);
		download = (Button) parentView.findViewById(R.id.player_download);
		lyricsLoader= (ImageView) parentView.findViewById(R.id.lyrics_load_image);
		playerTitleBarBack = (Button) parentView.findViewById(R.id.title_bar_left_menu);
		playerProgress = (SeekBar) parentView.findViewById(R.id.player_progress);
		playerTvTitle = (TextView) parentView.findViewById(R.id.player_title);
		playerEtTitle = (EditText) parentView.findViewById(R.id.player_title_edit_view);
		playerBtnTitle = (ImageView) parentView.findViewById(R.id.player_edit_title);
		playerTvArtist = (TextView) parentView.findViewById(R.id.player_artist);
		playerEtArtist = (EditText) parentView.findViewById(R.id.player_artist_edit_view);
		playerBtnArtist = (ImageView) parentView.findViewById(R.id.player_edit_artist);
		playerCurrTime = (TextView) parentView.findViewById(R.id.player_current_time);
		playerTotalTime = (TextView) parentView.findViewById(R.id.player_total_time);
	    playerTitleBar = (FrameLayout) parentView.findViewById(R.id.layout_top);   
	    playerTitleBarArtis = (TextView) parentView.findViewById(R.id.titleBarArtist);
	    playerTitleBarTitle = (TextView) parentView.findViewById(R.id.titleBarTitle);
	    playerTitleBarCover = (ImageView) parentView.findViewById(R.id.titleBarCover);
		playerLyricsView = (TextView) parentView.findViewById(R.id.player_lyrics_view);
		playerCover = (ImageView) parentView.findViewById(R.id.player_cover);
		playerTagsCheckBox = (CheckBox) parentView.findViewById(R.id.isUseCover);
	}

	private void setListener() {
		playerProgress.setOnSeekBarChangeListener(this);
		play.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		playerBtnArtist.setOnClickListener(this);
		playerBtnTitle.setOnClickListener(this);
		download.setOnClickListener(this);
		playerTitleBarBack.setOnClickListener(this);
		((UIParallaxScroll) parentView.findViewById(R.id.scroller)).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				editTag(v);
				return false;
			}

		});
	}
	
	
	private void setElementsView(int progress) {
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
		}
		playerProgress.removeCallbacks(progressAction);
		playerTvArtist.setText(song.getArtist());
		playerTvTitle.setText(song.getTitle());
		playerTitleBarArtis.setText(song.getArtist());
		playerTitleBarTitle.setText(song.getTitle());
		long totalTime = song.getDuration();
		playerTotalTime.setText(Util.getFormatedStrDuration(totalTime));
		playerProgress.setMax((int) totalTime);
		if (progress > 0) {
			playerProgress.setProgress(progress);
			playerCurrTime.setText(Util.getFormatedStrDuration(progress));
			playerProgress.post(progressAction);
		}
	}
		
	private void updateObject() {
		playerTvArtist.setText(song.getArtist());
		playerTvTitle.setText(song.getTitle());
		playerTitleBarArtis.setText(song.getArtist());
		playerTitleBarTitle.setText(song.getTitle());
		if (song.getClass() != MusicData.class) ((RemoteSong) song).setHasCover(true);
	}
	
	@Override
	public void onClick(View v) {
		editTag(v);
		switch (v.getId()) {
		case R.id.player_play:
			play(0);
			break;
		case R.id.player_previous:
			play(-1);
			break;
		case R.id.player_forward:
			play(1);
			break;
		case R.id.player_download:
			download();
			break;
		case R.id.title_bar_left_menu: 
			onBackPress();
			break;
		case R.id.player_edit_title:
			openTitleField();
			break;
		case R.id.player_edit_artist:
			openArtistField();
			break;
		default:
			break;
		}
	}

	private void openArtistField() {
		if (playerTvArtist.getVisibility() == View.VISIBLE) {
			playerTvArtist.setVisibility(View.GONE);
			playerEtArtist.setVisibility(View.VISIBLE);
			playerEtArtist.setText(song.getArtist());
			playerBtnArtist.setVisibility(View.GONE);
		} else {
			playerTvArtist.setVisibility(View.VISIBLE);
			playerEtArtist.setVisibility(View.GONE);
		}
	}

	private void openTitleField() {
		if (playerTvTitle.getVisibility() == View.VISIBLE) {
			playerTvTitle.setVisibility(View.GONE);
			playerEtTitle.setVisibility(View.VISIBLE);
			playerEtTitle.setText(song.getTitle());
			playerBtnTitle.setVisibility(View.GONE);
		} else {
			playerTvTitle.setVisibility(View.VISIBLE);
			playerEtTitle.setVisibility(View.GONE);
		}
	}
	
	private void editTag(View v) {
        if (playerEtArtist.getVisibility() == View.VISIBLE) {
        	playerTvArtist.setVisibility(View.VISIBLE);
			playerEtArtist.setVisibility(View.GONE);
			playerBtnArtist.setVisibility(View.VISIBLE);
			String artist =  playerEtArtist.getText().toString();
			if (!artist.equals(song.getArtist())){
				song.setArtist(artist);
				saveTag();
			}
        } else if (playerEtTitle.getVisibility() == View.VISIBLE) {
        	playerTvTitle.setVisibility(View.VISIBLE);
			playerEtTitle.setVisibility(View.GONE);
			playerBtnTitle.setVisibility(View.VISIBLE);
			String title = playerEtTitle.getText().toString();
			if (!title.equals(song.getTitle())){
				song.setTitle(title);
				saveTag();
			}
        }
	}
	
	private void saveTag() {
		updateObject();
		if (song.getClass() != MusicData.class) return;
		RenameTaskSuccessListener renameListener = new RenameTaskSuccessListener() {

			@Override
			public void success(String path) {
				song.setPath(path);
				renameTask.cancelProgress();
			}

			@Override
			public void error() {
			}
		};
		renameTask = new RenameTask(new File(song.getPath()), getActivity(), renameListener, song.getArtist(), song.getTitle(), song.getAlbum());
		renameTask.start(true, false);
	}
	
//	private void hideOpenViews() {
//		if (parentView.findViewById(R.id.player_edit_tag_dialog).getVisibility() == View.VISIBLE) {
//			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
//		}
//		if (parentView.findViewById(R.id.player_lyrics_frame).getVisibility() == View.VISIBLE) {
//			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
//		}
//		isUseAlbumCover = true;
//	}

	private Runnable progressAction = new Runnable() {

		@Override
		public void run() {
			try {
				playerProgress.removeCallbacks(this);
				if (player.isPrepared()) {
					int current = player.getCurrentPosition();
					playerProgress.setProgress(current);
					playerCurrTime.setText(Util.getFormatedStrDuration(current));
				} 
				playerProgress.postDelayed(this, 1000);
			} catch (Exception e) {
				Log.d(getClass().getSimpleName(), e + "");
			}
		}

	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			try {
				player.seekTo(progress);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
	
	private void showLyrics() {
		parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.VISIBLE);
		playerLyricsView.setText("");
		lyricsLoader.setVisibility(View.VISIBLE);
		lyricsLoader.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
		lyricsFetcher = new LyricsFetcher(getActivity());
		lyricsFetcher.fetchLyrics(song.getTitle(), song.getArtist());
		lyricsFetcher.setOnLyricsFetchedListener(new OnLyricsFetchedListener() {

			@Override
			public void onLyricsFetched(boolean foundLyrics, String lyrics) {
				lyricsLoader.clearAnimation();
				lyricsLoader.setVisibility(View.GONE);
				try {	
					if (foundLyrics) {
						playerLyricsView.setVisibility(View.VISIBLE);
						playerLyricsView.setText(Html.fromHtml(lyrics));
					} else {
						playerLyricsView.setVisibility(View.GONE);
					}
				} catch (Exception e) {
				}
			}
		});
	}


	private void animateOpenViews(final int[] location) {
		
		((UIParallaxScroll) parentView.findViewById(R.id.scroller)).post(new Runnable() {
			public void run() {
	            Animation open = AnimationUtils.loadAnimation(getActivity(),R.anim.open);
	            ((UIParallaxScroll) parentView.findViewById(R.id.scroller)).setAnimation(open);
				((UIParallaxScroll) parentView.findViewById(R.id.scroller)).scrollTo(0, location[1]);
			}
		});
	}
	
	
	/**
	 * @param delta
	 *  - 
	 * delta must be 1 or -1 or 0, 1 - next, -1 - previous, 0 - current song
	 */
	private void play(int delta) throws IllegalArgumentException {
		if (delta > 0) {
			player.shift(1);
			getCover(player.getPlayingSong());
		} else if (delta < 0) {
			player.shift(-1);
			getCover(player.getPlayingSong());
		} else {
			player.play(player.getPlayingPosition());
		}
	}
	
	private void getCover(final AbstractSong song) {
		if (song.getClass() != MusicData.class) {
			((RemoteSong) song).getCover(new OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (null != bmp) {
						((RemoteSong) song).setHasCover(true);
						playerCover.setImageBitmap(bmp);
						playerTitleBarCover.setImageBitmap(bmp);
					} else {
						playerCover.setImageResource(R.drawable.def_cover_circle_web);
						playerTitleBarCover.setImageResource(R.drawable.def_cover_circle);
					}
				}
				
			});
		} else {
			final Bitmap bitmap = ((MusicData) song).getCover(getActivity());
			if (bitmap != null) {
				playerCover.post(new Runnable() {
					
					@Override
					public void run() {
						playerCover.setImageBitmap(bitmap);
						playerTitleBarCover.setImageBitmap(bitmap);
					}
				});
			} else {
				playerCover.setImageResource(R.drawable.def_cover_circle_web);
				playerTitleBarCover.setImageResource(R.drawable.def_cover_circle);
			}
		}
	}

	private void download() {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		downloadListener = new DownloadListener(getActivity(), (RemoteSong) song, id);
		if (downloadListener.isBadInet()) return;
		downloadListener.setUseAlbumCover(isUseAlbumCover);
		((RemoteSong) song).getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				if (!url.startsWith("http")) {
					Toast toast = Toast.makeText(player, R.string.error_retrieving_the_url, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				((RemoteSong) song).setDownloadUrl(url);
				Runnable callbackRun = new Runnable() {

					@Override
					public void run() {
						downloadListener.onClick(parentView);
					}
				};
				new Handler(Looper.getMainLooper()).post(callbackRun);
			}

			@Override
			public void error(String error) {
			}
		});
	}
	
	private void startImageAnimation(ImageView view) {
		ViewTreeObserver observer = playerCover.getViewTreeObserver();
		observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				playerCover.getViewTreeObserver().removeOnPreDrawListener(this);
				int[] screen_location = new int[2];
				playerCover.getLocationOnScreen(screen_location);
				delta_left = left - screen_location[0];
				delta_top = top - screen_location[1];
				scale_width = (float) width / playerCover.getWidth();
				scale_height = (float) height / playerCover.getHeight();
				runEnterAnimation(playerCover, false);
				return true;
			}
		});
	}
	
	private void startTitleBarAnimation (final ImageView view) {
		ViewTreeObserver observer = view.getViewTreeObserver();
		observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				view.getViewTreeObserver().removeOnPreDrawListener(this);
				int[] screen_location = new int[2];
				view.getLocationOnScreen(screen_location);
				int[] playerCoverScreenLocation = new int[2];
				playerCover.getLocationOnScreen(playerCoverScreenLocation);
				deltaLeftTitleBar = playerCoverScreenLocation[0] - screen_location[0];
				deltaTopTitleBar = playerCoverScreenLocation[1] - screen_location[1];
				scaleWidthTitleBar = (float) playerCover.getWidth() / view.getWidth();
				scaleHeightTitleBar = (float) playerCover.getHeight() / view.getHeight();
				runEnterAnimation(view, true);
				return true;
			}
		});
	}
	
	private void onBackPress() {
		runExitAnimation(new Runnable() {
			
			@Override
			public void run() {
				if (isDestroy) return;
				((MainActivity) getActivity()).onBackPressed();
				((MainActivity) getActivity()).overridePendingTransition(0, 0);
			}
		}, playerCover, false);
	}
	
    private void runEnterAnimation(ImageView view, boolean flag) {
        ViewHelper.setPivotX(view, 0.f);
        ViewHelper.setPivotY(view, 0.f);
        ViewHelper.setScaleX(view, flag ? scaleWidthTitleBar : scale_width);
        ViewHelper.setScaleY(view, flag ? scaleHeightTitleBar : scale_height);
        ViewHelper.setTranslationX(view,flag ? deltaLeftTitleBar : delta_left);
        ViewHelper.setTranslationY(view, flag ? deltaTopTitleBar : delta_top);
        animate(view).
                setDuration(DURATION).
                scaleX(1.f).
                scaleY(1.f).
                translationX(0.f).
                translationY(0.f).
                setInterpolator(new DecelerateInterpolator()).
        		setListener(new AnimatorListenerAdapter() {  

        			@Override
	        	    public void onAnimationEnd(Animator animation) {        	    
        			}
        		});

        ObjectAnimator bg_anim = ObjectAnimator.ofFloat(((RelativeLayout) parentView.findViewById(R.id.bg_layout)), "alpha", flag ? 1f : 0f, 1f);
        bg_anim.setDuration(DURATION);
        bg_anim.start();
    }
    
    private void runExitAnimation(final Runnable end_action, ImageView view, boolean flag) {
    	ViewHelper.setPivotX(view, 0.f);
    	ViewHelper.setPivotY(view, 0.f);
    	ViewHelper.setScaleX(view, 1.f);
    	ViewHelper.setScaleY(view, 1.f);
    	ViewHelper.setTranslationX(view, 0.f);
    	ViewHelper.setTranslationY(view, 0.f);

    	animate(view).
                setDuration(DURATION).
                scaleX(flag ? scaleWidthTitleBar : scale_width).
                scaleY(flag ? scaleHeightTitleBar : scale_height).
                translationX(flag ? deltaLeftTitleBar : delta_left).
                translationY(flag ? deltaTopTitleBar : delta_top).
                setInterpolator(new DecelerateInterpolator()).
                setListener(new AnimatorListenerAdapter() {  

                	@Override
                	    public void onAnimationEnd(Animator animation) {
                		if (null == end_action) return;
                	    end_action.run();
                	}
                });

        ObjectAnimator bg_anim = ObjectAnimator.ofFloat(((RelativeLayout) parentView.findViewById(R.id.bg_layout)), "alpha", 1f, flag ? 1f : 0f);
        bg_anim.setDuration(DURATION);
        bg_anim.start();
    }
    
    private UIParallaxScroll.OnScrollChangedListener mOnScrollChangedListener = new UIParallaxScroll.OnScrollChangedListener() {
        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
        	final float headerHeight = ViewHelper.getY(parentView.findViewById(R.id.player_artist_frame)) - (playerTitleBar.getHeight() - parentView.findViewById(R.id.player_artist_frame).getHeight());
            final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
            final int newAlpha = (int) (ratio * 255);
            playerTitleBar.getBackground().setAlpha(newAlpha);
            Animation animationFadeIn = AnimationUtils.loadAnimation(getActivity(),R.anim.fadein);
            Animation animationFadeOut = AnimationUtils.loadAnimation(getActivity(),R.anim.fadeout);
            if (newAlpha == 255 && playerTitleBarArtis.getVisibility() != View.VISIBLE && !animationFadeIn.hasStarted()){
            	playerTitleBarArtis.setVisibility(View.VISIBLE);
            	playerTitleBarTitle.setVisibility(View.VISIBLE);
            	playerTitleBarCover.setVisibility(View.VISIBLE);
            	playerTitleBarArtis.startAnimation(animationFadeIn);
            	playerTitleBarTitle.startAnimation(animationFadeIn);
            	startTitleBarAnimation(playerTitleBarCover);
            } else if (newAlpha < 255 && !animationFadeOut.hasStarted() && playerTitleBarArtis.getVisibility() != View.INVISIBLE)  { 	
            	playerTitleBarArtis.startAnimation(animationFadeOut);
            	playerTitleBarArtis.setVisibility(View.INVISIBLE);
            	playerTitleBarTitle.startAnimation(animationFadeOut);
            	playerTitleBarTitle.setVisibility(View.INVISIBLE);
            	runExitAnimation(null, playerTitleBarCover, true);
            }
        	if (newAlpha < 220) {
        		playerTitleBarCover.setVisibility(View.GONE);
        	}
        }
    };
}
