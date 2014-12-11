package org.upmobile.clearmusicdownloader.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.DownloadListener;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.RenameTask;
import ru.johnlife.lifetoolsmp3.RenameTaskSuccessListener;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.app.Dialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
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
	private ArrayList<AbstractSong> list;
	private AbstractSong song;
	private RenameTask renameTask;
	private String folderFilter;
	private PlayerService player;
	protected DownloadListener downloadListener;
	private View parentView;
	private SeekBar playerProgress;
	private ImageButton play;
	private ImageButton previous;
	private ImageButton forward;
	private ImageButton showLyrics;
	private ImageButton editTag;
	private ImageView playerCover;
	private Button download;
	private Button playerSaveTags;
	private Button playerCancelTags;
	private Button playerCancelLyrics;
	private TextView playerTitle;
	private TextView playerArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private TextView playerLyricsView;
	private EditText playerTagsAlbum;
	private EditText playerTagsTitle;
	private EditText playerTagsArtist;
	private int selectedPosition;
    private int delta_top;
    private int delta_left;
	private int top;
	private int left;
	private int width;
	private int height;
    private float scale_width;
    private float scale_height;
	private Dialog dialog;
	private CheckBox playerTagsCheckBox;
	private FrameLayout mNavigationTop;
	private TextView mNavigationTitle;
	private Button mNavigationBackBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		player = PlayerService.get(getActivity());
		parentView = inflater.inflate(R.layout.player, container, false);
		((MainActivity) getActivity()).hideTopFrame();
		list = new ArrayList<AbstractSong>();
		folderFilter = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
		((UIParallaxScroll) parentView.findViewById(R.id.scroller)).setOnScrollChangedListener(mOnScrollChangedListener);
		init();
		 mNavigationTop.getBackground().setAlpha(0);   
		    mNavigationTitle.setVisibility(View.INVISIBLE);
		if (null != getArguments() && getArguments().containsKey(Constants.KEY_SELECTED_POSITION)) {
			list = getArguments().getParcelableArrayList(Constants.KEY_SELECTED_SONG);
			selectedPosition = getArguments().getInt(Constants.KEY_SELECTED_POSITION);
	        top = getArguments().getInt(PACKAGE + ".top");
	        left = getArguments().getInt(PACKAGE + ".left");
	        width = getArguments().getInt(PACKAGE + ".width");
	        height = getArguments().getInt(PACKAGE + ".height");    
			play(0);
		} else {
			cheñkWhereQueue();
			song = list.get(selectedPosition);
			changeView();
		}
		startImageAnimation();
		return parentView;
	}

	private void init() {
		play = (ImageButton) parentView.findViewById(R.id.player_play);
		previous = (ImageButton) parentView.findViewById(R.id.player_previous);
		forward = (ImageButton) parentView.findViewById(R.id.player_forward);
		download = (Button) parentView.findViewById(R.id.player_download);
		showLyrics = (ImageButton) parentView.findViewById(R.id.player_lyrics);
		editTag = (ImageButton) parentView.findViewById(R.id.player_edit_tags);
		mNavigationBackBtn = (Button) parentView.findViewById(R.id.title_bar_left_menu);
		playerProgress = (SeekBar) parentView.findViewById(R.id.player_progress);
		playerTitle = (TextView) parentView.findViewById(R.id.player_title);
		playerArtist = (TextView) parentView.findViewById(R.id.player_artist);
		playerCurrTime = (TextView) parentView.findViewById(R.id.player_current_time);
		playerTotalTime = (TextView) parentView.findViewById(R.id.player_total_time);
	    mNavigationTop = (FrameLayout) parentView.findViewById(R.id.layout_top);   
	    mNavigationTitle = (TextView) parentView.findViewById(R.id.titleBar);
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

	private void showLyrics() {
		if (parentView.findViewById(R.id.player_lyrics_frame).getVisibility() == View.GONE) {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.VISIBLE);
			LyricsFetcher lyricsFetcher = new LyricsFetcher(getActivity());
			lyricsFetcher.fetchLyrics(song.getTitle(), song.getArtist());
			lyricsFetcher.setOnLyricsFetchedListener(new OnLyricsFetchedListener() {
				
				@Override
				public void onLyricsFetched(boolean foundLyrics, String lyrics) {
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
		}
	}
	

	private void saveTags() {
		if (!manipulateText() && playerTagsCheckBox.isChecked()) {
			Toast toast = Toast.makeText(getActivity(), R.string.nothing_changed, Toast.LENGTH_LONG);
			toast.show();
			return;
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
				}
			};
			renameTask = new RenameTask(new File(song.getPath()), getActivity(), renameListener, playerTagsArtist.getText().toString(), playerTagsTitle.getText().toString(), playerTagsAlbum.getText().toString());
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

	private void updateObject() {
		song.setAlbum(playerTagsAlbum.getText().toString());
		song.setNewArtist(playerTagsArtist.getText().toString());
		song.setNewTitle(playerTagsTitle.getText().toString());
		if (song.getClass() == MusicData.class) {
			song.setPath(folderFilter + playerTagsArtist.getText().toString() + playerTagsTitle.getText().toString() + ".mp3");
		}
		playerArtist.setText(song.getArtist());
		playerTitle.setText(song.getTitle());
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
		switch (delta) {
		case 1:
			if (selectedPosition < list.size() - 1) ++selectedPosition;
			playerProgress.setProgress(0);
			playerCurrTime.setText("");
			getUri();
			break;
		case -1:
			if (0 != selectedPosition) --selectedPosition;
			playerProgress.setProgress(0);
			playerCurrTime.setText("");
		case 0:
			getUri();
			break;
		default:
			throw new IllegalArgumentException("delta must be 1, -1 or 0");
		}
	}

	private void getUri() {
		song = list.get(selectedPosition);
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
			player.play(song.getPath());
			changeView();
		} else {
			((RemoteSong) song).getCover(new OnBitmapReadyListener() {
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (null != bmp) {
						playerCover.setImageBitmap(bmp);
					}
				}
			});
			showCustomDialog();
			song.getDownloadUrl(new DownloadUrlListener() {

				@Override
				public void success(String url) {
					player.play(url);
					changeView();
					dialog.dismiss();
				}

				@Override
				public void error(String error) {
					dialog.dismiss();
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

	private void changeView() {
		playerProgress.removeCallbacks(progressAction);
		playerArtist.setText(list.get(selectedPosition).getArtist());
		playerTitle.setText(list.get(selectedPosition).getTitle());
		mNavigationTitle.setText(list.get(selectedPosition).getArtist());
		long totalTime = list.get(selectedPosition).getDuration();
		playerTotalTime.setText(Util.getFormatedStrDuration(totalTime));
		playerProgress.setMax((int) totalTime);
		changePlayPauseView(player.showPlayPause());
		playerProgress.post(progressAction);
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
				if (player.isComplete()) {
					playerProgress.setProgress(0);
					playerCurrTime.setText("");
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
	
	/**
	 * @param isPlaying
	 *  - 
	 *  If "true", the button changes the picture to "play", if "false" changes to "pause"
	 */
	private void changePlayPauseView(boolean isPlaying) {
		if (isPlaying) {
			play.setImageResource(R.drawable.play);
		} else {
			play.setImageResource(R.drawable.pause);
		}
	}
	
	private void cheñkWhereQueue() {
		if (player.getCurrentPath().contains(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX)) {
			list = new ArrayList<AbstractSong>(querySong());
			for (int i = 0; i < list.size(); i ++ ) {
				if (player.getCurrentPath().equals(list.get(i).getPath())) {
					selectedPosition = i;
				}
			}
		} else if (player.getCurrentPath().contains("http")) {
			list = new ArrayList<AbstractSong>(StateKeeper.getInstance().getResults());
			for (int i = 0; i < list.size(); i ++ ) {
				if (player.getCurrentPath().equals(list.get(i).getPath())) {
					selectedPosition = i;
				}
			}
		} else {
			Log.d(getClass().getSimpleName(), "Unknown queue! Check the method of updating the queue player!!!");
		}
	}
	
	private ArrayList<MusicData> querySong() {
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		Cursor cursor = buildQuery(getActivity().getContentResolver());
		if (!cursor.moveToFirst()) {
			return new ArrayList<MusicData>();
		}
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);	
			result.add(data);
		}
		cursor.close();
		return result;
	}
	
	private Cursor buildQuery(ContentResolver resolver) {
		String selection =  MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%'" ;
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		return cursor;
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
        	//Difference between the heights, important to not add margin or remove mNavigationTitle.
        	final float headerHeight = ViewHelper.getY(playerArtist) - (mNavigationTop.getHeight() - playerArtist.getHeight());
            final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
            final int newAlpha = (int) (ratio * 255);
            mNavigationTop.getBackground().setAlpha(newAlpha);
            Animation animationFadeIn = AnimationUtils.loadAnimation(getActivity(),R.anim.fadein);
            Animation animationFadeOut = AnimationUtils.loadAnimation(getActivity(),R.anim.fadeout);
            if (newAlpha == 255 && mNavigationTitle.getVisibility() != View.VISIBLE && !animationFadeIn.hasStarted()){
            	mNavigationTitle.setVisibility(View.VISIBLE);
            	mNavigationTitle.startAnimation(animationFadeIn);
            } else if (newAlpha < 255 && !animationFadeOut.hasStarted() && mNavigationTitle.getVisibility() != View.INVISIBLE)  { 	
            	mNavigationTitle.startAnimation(animationFadeOut);
            	mNavigationTitle.setVisibility(View.INVISIBLE);
            }
        }
    };
    
    
    
//    @Override
//    public void onBackPressed() {
//
//        runExitAnimation(new Runnable() {
//            @Override
//            public void run() {
//                finish();
//            }
//        });
//
//    }
//    
//    @Override
//    public void finish() {
//
//        super.finish();
//        overridePendingTransition(0, 0);
//    }
}
