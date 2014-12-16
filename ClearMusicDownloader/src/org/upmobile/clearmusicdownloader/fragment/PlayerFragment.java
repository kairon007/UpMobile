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
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
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
	private String folderFilter;
	private PlayerService player;
	private DownloadListener downloadListener;
	private View parentView;
	private SeekBar playerProgress;
	private ImageButton play;
	private ImageButton previous;
	private ImageButton forward;
	private ImageButton showLyrics;
	private ImageButton editTag;
	private ImageView playerCover;
	private ImageView lyricsLoader;
	private Button download;
	private Button playerSaveTags;
	private Button playerCancelTags;
	private Button playerCancelLyrics;
	private Button playerTitleBarBack;
	private TextView playerTitle;
	private TextView playerArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private TextView playerLyricsView;
	private TextView playerTitleBarTitle;
	private TextView playerTitleBarArtis;
	private EditText playerTagsAlbum;
	private EditText playerTagsTitle;
	private EditText playerTagsArtist;
    private int delta_top;
    private int delta_left;
	private int top;
	private int left;
	private int width;
	private int height;
    private float scale_width;
    private float scale_height;
    private boolean isDestroy;
	private Dialog dialog;
	private CheckBox playerTagsCheckBox;
	private FrameLayout playerTitleBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		player = PlayerService.get(getActivity());
		bindToPlayer();
		parentView = inflater.inflate(R.layout.player, container, false);
		((MainActivity) getActivity()).hideTopFrame();
		((MainActivity) getActivity()).showPlayerElement();
		folderFilter = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
		((UIParallaxScroll) parentView.findViewById(R.id.scroller)).setOnScrollChangedListener(mOnScrollChangedListener);
		init();
		playerTitleBar.getBackground().setAlpha(0);
		playerTitleBarArtis.setVisibility(View.INVISIBLE);
		playerTitleBarTitle.setVisibility(View.INVISIBLE);
		playerCover.bringToFront();
		((MainActivity) getActivity()).getResideMenu().addIgnoredView(playerProgress);
		if (null != getArguments() && getArguments().containsKey(Constants.KEY_SELECTED_SONG)) {
			song = getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
			int pos = getArguments().getInt(Constants.KEY_SELECTED_POSITION);
			if (song.getClass() != MusicData.class) {
				((RemoteSong) song).getCover(new OnBitmapReadyListener() {
					
					@Override
					public void onBitmapReady(Bitmap bmp) {
						if (null != bmp) {
							playerCover.setImageBitmap(bmp);
						}
					}
				});
			}
			top = getArguments().getInt(PACKAGE + ".top");
			left = getArguments().getInt(PACKAGE + ".left");
			width = getArguments().getInt(PACKAGE + ".width");
			height = getArguments().getInt(PACKAGE + ".height");
			if (!player.isPrepared() || player.getPlayingSong().getClass() != song.getClass() || player.getPlayingPosition() != pos) {
				if (player.isPrepared()) {
					player.reset();
				} 
				setClickablePlayerElement(false);
				player.play(pos);
				play(0);
				setElementsView(0);
			} else {
				boolean check = player.isPlaying();
				int current = player.getCurrentPosition();
				setElementsView(current);
				if (check) changePlayPauseView(false);
				else changePlayPauseView(true);
			}
		} else {
			if (player.gettingURl() || !player.isPrepared()) {
				song = player.getPlayingSong();
				setClickablePlayerElement(false);
				setElementsView(0);
			} else {
				song = player.getPlayingSong();
				boolean check = player.isPlaying();
				int current = player.getCurrentPosition();
				setElementsView(current);
				changePlayPauseView(!check);
			}
		}
		startImageAnimation();
		return parentView;
	}

	private void bindToPlayer() {
		player.setStatePlayerListener(new OnStatePlayerListener() {
			
			@Override
			public void start(AbstractSong song) {
				PlayerFragment.this.song = song; 
				if (isDestroy) return;
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						setClickablePlayerElement(true);
						setElementsView(0);
						playerProgress.post(progressAction);
					}
				});
			}
			
			@Override
			public void reset() {
				if (isDestroy) return;
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						setClickablePlayerElement(false);
						hideOpenViews();
					}
				});
			}

			@Override
			public void pause() {
				if (isDestroy) return;
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						changePlayPauseView(true);
					}
				});
			}

			@Override
			public void play() {
				if (isDestroy) return;
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						changePlayPauseView(false);
					}
				});
			}

			@Override
			public void complete() {
				if (isDestroy) return;
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						playerProgress.removeCallbacks(progressAction);
						playerProgress.setProgress(0);
						playerCurrTime.setText("0:00");
					}
					
				});
			}
			
			@Override
			public void update(final AbstractSong song) {
				if (isDestroy)
					return;
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						PlayerFragment.this.song = song;
						setElementsView(0);
						setClickablePlayerElement(false);
					}
				});
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
		showLyrics = (ImageButton) parentView.findViewById(R.id.player_lyrics);
		lyricsLoader= (ImageView) parentView.findViewById(R.id.lyrics_load_image);
		editTag = (ImageButton) parentView.findViewById(R.id.player_edit_tags);
		playerTitleBarBack = (Button) parentView.findViewById(R.id.title_bar_left_menu);
		playerProgress = (SeekBar) parentView.findViewById(R.id.player_progress);
		playerTitle = (TextView) parentView.findViewById(R.id.player_title);
		playerArtist = (TextView) parentView.findViewById(R.id.player_artist);
		playerCurrTime = (TextView) parentView.findViewById(R.id.player_current_time);
		playerTotalTime = (TextView) parentView.findViewById(R.id.player_total_time);
	    playerTitleBar = (FrameLayout) parentView.findViewById(R.id.layout_top);   
	    playerTitleBarArtis = (TextView) parentView.findViewById(R.id.titleBarArtist);
	    playerTitleBarTitle = (TextView) parentView.findViewById(R.id.titleBarTitle);
		playerLyricsView = (TextView) parentView.findViewById(R.id.player_lyrics_view);
		playerSaveTags = (Button) parentView.findViewById(R.id.player_save_tags);
		playerCancelTags = (Button) parentView.findViewById(R.id.player_cancel_tags);
		playerCover = (ImageView) parentView.findViewById(R.id.player_cover);
		playerCancelLyrics = (Button) parentView.findViewById(R.id.player_cancel_lyrics);
		playerTagsArtist = (EditText) parentView.findViewById(R.id.editTextArtist);
		playerTagsTitle = (EditText) parentView.findViewById(R.id.editTextTitle);
		playerTagsAlbum = (EditText) parentView.findViewById(R.id.editTextAlbum);
		playerTagsCheckBox = (CheckBox) parentView.findViewById(R.id.isUseCover);
		playerProgress.setOnSeekBarChangeListener(this);
		playerCancelLyrics.setOnClickListener(this);
		play.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		download.setOnClickListener(this);
		editTag.setOnClickListener(this);
		showLyrics.setOnClickListener(this);
		playerCancelTags.setOnClickListener(this);
		playerSaveTags.setOnClickListener(this);
		playerTitleBarBack.setOnClickListener(this);
	}
	
	
	private void setElementsView(int progress) {
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
		}
		playerProgress.removeCallbacks(progressAction);
		playerArtist.setText(song.getArtist());
		playerTitle.setText(song.getTitle());
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
		song.setAlbum(playerTagsAlbum.getText().toString());
		song.setNewArtist(playerTagsArtist.getText().toString());
		song.setNewTitle(playerTagsTitle.getText().toString());
		if (song.getClass() == MusicData.class) {
			song.setPath(folderFilter + playerTagsArtist.getText().toString() + playerTagsTitle.getText().toString() + ".mp3");
		}
		playerArtist.setText(song.getArtist());
		playerTitle.setText(song.getTitle());
		playerTitleBarArtis.setText(song.getArtist());
		playerTitleBarTitle.setText(song.getTitle());
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.player_play:
			play(0);
			break;
		case R.id.player_previous:
			play(-1);
			hideOpenViews();
			break;
		case R.id.player_forward:
			play(1);
			hideOpenViews();
			break;
		case R.id.player_download:
			download();
			break;
		case R.id.player_lyrics:
			showLyrics();
			break;
		case R.id.player_edit_tags:
			showEditTagDialog();
			break;
		case R.id.player_save_tags:
			saveTags();
		case R.id.player_cancel_tags:
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
			break;
		case R.id.player_cancel_lyrics:
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
			break;
		case R.id.title_bar_left_menu: 
			onBackPress();
			break;
		default:
			break;
		}
	}

	private void hideOpenViews() {
		if (parentView.findViewById(R.id.player_edit_tag_dialog).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
		}
		if (parentView.findViewById(R.id.player_lyrics_frame).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
		}
	}

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
		if (parentView.findViewById(R.id.player_lyrics_frame).getVisibility() == View.GONE) {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.VISIBLE);
			lyricsLoader.setVisibility(View.VISIBLE);
			lyricsLoader.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
			final int [] location = new int[2];
			playerLyricsView.getLocationOnScreen(location);
			animateOpenViews(location);
			LyricsFetcher lyricsFetcher = new LyricsFetcher(getActivity());
			lyricsFetcher.fetchLyrics(song.getTitle(), song.getArtist());
			lyricsFetcher.setOnLyricsFetchedListener(new OnLyricsFetchedListener() {
				
				@Override
				public void onLyricsFetched(boolean foundLyrics, String lyrics) {
					lyricsLoader.clearAnimation();
					lyricsLoader.setVisibility(View.GONE);
					if (foundLyrics) {
						playerLyricsView.setText(Html.fromHtml(lyrics));
					} else {
						String songName = song.getArtist() + " - " + song.getTitle();
						playerLyricsView.setText(getResources().getString(R.string.download_dialog_no_lyrics, songName));
					}
				}
			});
		} else {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
		}
	}

	private void showEditTagDialog() {
		if (parentView.findViewById(R.id.player_edit_tag_dialog).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
		} else {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.VISIBLE);
			playerTagsArtist.setText(song.getArtist());
			playerTagsTitle.setText(song.getTitle());
			playerTagsAlbum.setText(song.getAlbum());
			playerTagsCheckBox.setChecked(true);                     //temporary 
			final int [] location = new int[2];
			playerTagsArtist.getLocationOnScreen(location);
			animateOpenViews(location);
		}
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
	
	private void saveTags() {
		if (!manipulateText() && playerTagsCheckBox.isChecked()) {
			Toast toast = Toast.makeText(getActivity(), R.string.nothing_changed, Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		if (song.getClass() == MusicData.class) {
			File f = new File(song.getPath());
			if (new File(f.getParentFile() + "/" + playerTagsArtist.getText() + " - " + playerTagsTitle.getText() + ".mp3").exists()) {
				Toast toast = Toast.makeText(getActivity(), R.string.file_with_the_same_name_already_exists, Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
		}
		if (song.getClass() == MusicData.class) {
			RenameTaskSuccessListener renameListener = new RenameTaskSuccessListener() {

				@Override
				public void success(String path) {
					renameTask.cancelProgress();
					parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
				}

				@Override
				public void error() {
					// TODO Auto-generated method stub

				}
			};
			renameTask = new RenameTask(new File(song.getPath()), getActivity(), renameListener, playerTagsArtist.getText().toString(),
					playerTagsTitle.getText().toString(), playerTagsAlbum.getText().toString());
		}
		if (manipulateText() && playerTagsCheckBox.isChecked()) { 			//if we change only text
			if (song.getClass() == MusicData.class) {
				renameTask.start(true, false);
			}
			updateObject();
		} else if (!manipulateText() && !playerTagsCheckBox.isChecked()) { 	// if we change only cover
			if (song.getClass() == MusicData.class) {
				renameTask.start(false, true);
			}
		} else if (manipulateText() && !playerTagsCheckBox.isChecked()) { 	// if we change cover and fields
			if (song.getClass() == MusicData.class) {
				renameTask.start(false, false);
			}
			updateObject();
		}
	}

	public boolean manipulateText() {
		if (!song.getTitle().equals(playerTagsTitle.getText())) {
			return true;
		}
		if (!song.getArtist().equals(playerTagsArtist.getText())) {
			return true;
		}
		if (!song.getAlbum().equals(playerTagsAlbum.getText())) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param delta
	 *  - 
	 * delta must be 1 or -1 or 0, 1 - next, -1 - previous, 0 - current song
	 */
	private void play(int delta) throws IllegalArgumentException {
		if (delta > 0) player.shift(1);
		else if (delta < 0) player.shift(-1);
		else {
			player.play(player.getPlayingPosition());
		}
		if (delta != 0 && song.getClass() != MusicData.class) {
			((RemoteSong) song).getCover(new OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (null != bmp) {
						playerCover.setImageBitmap(bmp);
					}
				}
				
			});
		}
	}
	
    private void showCustomDialog() {
		dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(false);
		dialog.setContentView(R.layout.layout_dialog);
		((TextView)dialog.findViewById(R.id.dialog_title)).setText(R.string.message_please_wait);
		((TextView)dialog.findViewById(R.id.dialog_text)).setText(R.string.message_loading);
		Button btnCancel = (Button) dialog.findViewById(R.id.btncancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				((RemoteSong) song).cancelTasks();
				dialog.cancel();
			}

		});
		final ImageView myImage = (ImageView) dialog.findViewById(R.id.loader);
        myImage.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate) );
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x7f000000));
		dialog.show();
    }

	private void download() {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		downloadListener = new DownloadListener(getActivity(), (RemoteSong) song, id);
		if (downloadListener.isBadInet()) return;
		downloadListener.onClick(parentView);
	}

	
	
	private void startImageAnimation() {
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
				runEnterAnimation();
				return true;
			}
		});
	}
	
	private void onBackPress() {
		runExitAnimation(new Runnable() {
			
			@Override
			public void run() {
				((MainActivity) getActivity()).onBackPressed();
				((MainActivity) getActivity()).overridePendingTransition(0, 0);
			}
		});
	}
	
    private void runEnterAnimation() {
        ViewHelper.setPivotX(playerCover, 0.f);
        ViewHelper.setPivotY(playerCover, 0.f);
        ViewHelper.setScaleX(playerCover, scale_width);
        ViewHelper.setScaleY(playerCover, scale_height);
        ViewHelper.setTranslationX(playerCover, delta_left);
        ViewHelper.setTranslationY(playerCover, delta_top);
        animate(playerCover).
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

        ObjectAnimator bg_anim = ObjectAnimator.ofFloat(((RelativeLayout) parentView.findViewById(R.id.bg_layout)), "alpha", 0f, 1f);
        bg_anim.setDuration(DURATION);
        bg_anim.start();
    }
    
    private void runExitAnimation(final Runnable end_action) {
    	ViewHelper.setPivotX(playerCover, 0.f);
    	ViewHelper.setPivotY(playerCover, 0.f);
    	ViewHelper.setScaleX(playerCover, 1.f);
    	ViewHelper.setScaleY(playerCover, 1.f);
    	ViewHelper.setTranslationX(playerCover, 0.f);
    	ViewHelper.setTranslationY(playerCover, 0.f);

    	animate(playerCover).
                setDuration(DURATION).
                scaleX(scale_width).
                scaleY(scale_height).
                translationX(delta_left).
                translationY(delta_top).
                setInterpolator(new DecelerateInterpolator()).
                setListener(new AnimatorListenerAdapter() {  

                	@Override
                	    public void onAnimationEnd(Animator animation) {
                	    end_action.run();
                	}
                });

        ObjectAnimator bg_anim = ObjectAnimator.ofFloat(((RelativeLayout) parentView.findViewById(R.id.bg_layout)), "alpha", 1f, 0f);
        bg_anim.setDuration(DURATION);
        bg_anim.start();
    }
    
    private UIParallaxScroll.OnScrollChangedListener mOnScrollChangedListener = new UIParallaxScroll.OnScrollChangedListener() {
        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
        	final float headerHeight = ViewHelper.getY(playerArtist) - (playerTitleBar.getHeight() - playerArtist.getHeight());
            final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
            final int newAlpha = (int) (ratio * 255);
            playerTitleBar.getBackground().setAlpha(newAlpha);
            Animation animationFadeIn = AnimationUtils.loadAnimation(getActivity(),R.anim.fadein);
            Animation animationFadeOut = AnimationUtils.loadAnimation(getActivity(),R.anim.fadeout);
            if (newAlpha == 255 && playerTitleBarArtis.getVisibility() != View.VISIBLE && !animationFadeIn.hasStarted()){
            	playerTitleBarArtis.setVisibility(View.VISIBLE);
            	playerTitleBarTitle.setVisibility(View.VISIBLE);
            	playerTitleBarArtis.startAnimation(animationFadeIn);
            	playerTitleBarTitle.startAnimation(animationFadeIn);
            } else if (newAlpha < 255 && !animationFadeOut.hasStarted() && playerTitleBarArtis.getVisibility() != View.INVISIBLE)  { 	
            	playerTitleBarArtis.startAnimation(animationFadeOut);
            	playerTitleBarArtis.setVisibility(View.INVISIBLE);
            	playerTitleBarTitle.startAnimation(animationFadeOut);
            	playerTitleBarTitle.setVisibility(View.INVISIBLE);
            }
        }
    };
}
