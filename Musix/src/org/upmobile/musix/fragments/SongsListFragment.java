package org.upmobile.musix.fragments;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import org.upmobile.musix.R;
import org.upmobile.musix.activities.MainActivity;
import org.upmobile.musix.activities.SongDetailsActivity;
import org.upmobile.musix.listadapters.SongListAdapter;
import org.upmobile.musix.utils.TypefaceHelper;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.services.PlaybackService.OnPlaybackServiceDestroyListener;
import ru.johnlife.lifetoolsmp3.services.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;

public class SongsListFragment extends Fragment implements MediaController.MediaPlayerControl, OnStatePlayerListener, OnPlaybackServiceDestroyListener {

	private static final String EXTRA_SONG_DETAIL = "EXTRA_SONG_DETAIL";

	public SongsListFragment() {
		// Required empty public constructor
	}

	private Context mContext;
	private ListView listView;
	private TextView emptyMessage;
	private static View rootView;
	private TypefaceHelper typefaceHelper;
	private ArrayList<MusicData> abstractSongArrayList;
	private PlaybackService musicService;
	private boolean musicBound = Boolean.FALSE;
	private AbstractSong song;
	private boolean paused = Boolean.FALSE, playbackPaused = Boolean.FALSE;

	// controller variables
	private double startTime = 0;
	private double finalTime = 0;

	private SongListAdapter songListAdapter;
	private SeekBar seekBar;
	private ImageView playingAlbumCover;
	private ImageButton btnPrev, btnNext, btnStop, btnPlayPause, btnShuffle;
	private TextView txtStartTimeField, txtEndTimeField, txtCurrentSongTitle, txtCurrentSongArtistName, txtShuffleMode;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		abstractSongArrayList = new ArrayList<MusicData>();
		rootView = inflater.inflate(R.layout.fragment_songs, container, false);
		mContext = rootView.getContext();
		init(rootView);
		registerForContextMenu(listView);
		mContext.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
		musicService = PlaybackService.get(getActivity());
		musicService.addStatePlayerListener(this);
		musicService.setDestroyListener(this);
		if (null != musicService.getPlayingSong()) {
			song = musicService.getPlayingSong();
			setPlayerCover();
			setTime();
			seekBar.setMax(musicService.getDuration());
			seekBar.setProgress(musicService.getCurrentPosition());
			abstractSongArrayList = new ArrayList(musicService.getArrayPlayback());
		} else {
			if (!abstractSongArrayList.isEmpty()) {
				song = abstractSongArrayList.get(0);
			} else {
				abstractSongArrayList = querySong();
				setPlayback();
			}
		}
		setupViews();
		btnShuffle.setAlpha(!musicService.enabledShuffleAll() ? (float) 0.5 : (float) 1);
		musicBound = true;
		return rootView;
	}

	private void setPlayback() {
		ArrayList<AbstractSong> clone = new ArrayList<AbstractSong>();
		for (AbstractSong abstractSong : abstractSongArrayList) {
			try {
				clone.add(abstractSong.cloneSong());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		musicService.setArrayPlayback(clone);
	}
	
	private ContentObserver observer = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			changeArrayPlayback();
		}

		@SuppressLint("NewApi")
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
				changeArrayPlayback();
			}
		}

		private void changeArrayPlayback() {
			abstractSongArrayList = querySong();
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				
				@Override
				public void run() {
					if (abstractSongArrayList.size() <= songListAdapter.getCount()) return;
					songListAdapter.clear();
					setPlayback();
					songListAdapter.addAll(abstractSongArrayList);
				}
			});
		}

	};

	@Override
	public void onPause() {
		super.onPause();
		paused = true;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (paused) {
			paused = false;
		}
		if (isPlaying()) {
			seekBar.removeCallbacks(UpdateSongTime);
			seekBar.post(UpdateSongTime);
			setPlayerCover();
			btnPlayPause.setImageResource(R.drawable.ic_action_pause);
		}
	}

	private void setupViews() {
		setHasOptionsMenu(true);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				if (b) {
					seekTo(i);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		txtStartTimeField.setTypeface(typefaceHelper.getRobotoLight());
		txtEndTimeField.setTypeface(typefaceHelper.getRobotoLight());
		txtCurrentSongTitle.setTypeface(typefaceHelper.getRobotoLight());
		txtCurrentSongArtistName.setTypeface(typefaceHelper.getRobotoLight());

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				song = ((AbstractSong) parent.getAdapter().getItem(position));
				musicService.play(song);
				btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
				if (playbackPaused) {
					playbackPaused = false;
				}
				setPlayerCover();
				seekBar.removeCallbacks(UpdateSongTime);
				seekBar.post(UpdateSongTime);
			}
		});

		btnPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				musicService.shift(-1, true);
				if (playbackPaused) {
					playbackPaused = false;
				}
				setPlayerCover();
				seekBar.removeCallbacks(UpdateSongTime);
				seekBar.post(UpdateSongTime);
				btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
			}
		});

		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				musicService.shift(1, true);
				if (playbackPaused) {
					playbackPaused = false;
				}
				setPlayerCover();
				seekBar.removeCallbacks(UpdateSongTime);
				seekBar.post(UpdateSongTime);
				btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
			}
		});

		btnPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (abstractSongArrayList.isEmpty() || null == song) return;
					if (isPlaying()) {
						seekBar.removeCallbacks(UpdateSongTime);
						pause();
						btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
					} else {
						if (playbackPaused) {
							playbackPaused = false;
							musicService.play(song);
						} else {
							musicService.play(song);
						}
						seekBar.removeCallbacks(UpdateSongTime);
						seekBar.post(UpdateSongTime);
						setPlayerCover();
						btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
					}
				} catch (Resources.NotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stop();
			}
		});

		btnShuffle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				musicService.offOnShuffle();
				if (!musicService.enabledShuffleAll()) {
					txtShuffleMode.setVisibility(View.INVISIBLE);
					btnShuffle.setAlpha((float) 0.5);
				} else {
					txtShuffleMode.setVisibility(View.VISIBLE);
					if (musicService.enabledShuffleAuto()) {
						txtShuffleMode.setText("A");
					} else {
						txtShuffleMode.setText("M");
					}
					btnShuffle.setAlpha((float) 1);
				}
			}
		});
		songListAdapter = new SongListAdapter(mContext, abstractSongArrayList);
		if (!songListAdapter.isEmpty()) {
			listView.setAdapter(songListAdapter);
			emptyMessage.setVisibility(View.GONE);
		} else {
			emptyMessage.setVisibility(View.VISIBLE);
		}
	}

	private void init(View root) {
		typefaceHelper = new TypefaceHelper(mContext);
		seekBar = (SeekBar) root.findViewById(R.id.songSeekBar);
		txtStartTimeField = (TextView) root.findViewById(R.id.songStartTime);
		txtEndTimeField = (TextView) root.findViewById(R.id.songTotalTime);
		btnNext = (ImageButton) root.findViewById(R.id.btnNext);
		btnPlayPause = (ImageButton) root.findViewById(R.id.btnPlayPause);
		btnPrev = (ImageButton) root.findViewById(R.id.btnPrev);
		btnShuffle = (ImageButton) root.findViewById(R.id.btnRandom);
		btnStop = (ImageButton) root.findViewById(R.id.btnStop);
		txtShuffleMode = (TextView) root.findViewById(R.id.txtShuffleMode);
		txtCurrentSongTitle = (TextView) root.findViewById(R.id.txtCurrentSongTitle);
		txtCurrentSongArtistName = (TextView) root.findViewById(R.id.txtCurrentSongArtistName);
		playingAlbumCover = (ImageView) root.findViewById(R.id.imageAlbumPlaying);
		emptyMessage = (TextView) root.findViewById(R.id.tvEmptyMessage);
		listView = (ListView) root.findViewById(R.id.listViewSongs);
	}
	
	private void stop() {
		playbackPaused = false;
		musicService.stop();
		btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
		seekBar.removeCallbacks(UpdateSongTime);
		seekBar.post(UpdateSongTime);
		seekBar.setProgress(0);
	}

	public ArrayList<MusicData> querySong() {
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		Cursor cursor = buildQuery(mContext.getContentResolver());
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			return result;
		}
		MusicData d = new MusicData();
		d.populate(cursor);
		result.add(d);
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);
			result.add(data);
		}
		cursor.close();
		return result;
	}

	private Cursor buildQuery(ContentResolver resolver) {
		return resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, MediaStore.Audio.Media.IS_MUSIC + "> 0", null, null);
	}
	
	private void setTime() {
		finalTime = musicService.getDuration();
		startTime = musicService.getCurrentPosition();

		long minutes, seconds;
		String mins, secs;

		if (finalTime > 0) {
			seekBar.setMax((int) finalTime);
			minutes = TimeUnit.MILLISECONDS.toMinutes((long) finalTime);
			seconds = TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime));
			if (minutes < 10) {
				mins = String.format("0%d", minutes);
			} else {
				mins = String.valueOf(minutes);
			}
			if (seconds < 10) {
				secs = String.format("0%d", seconds);
			} else {
				secs = String.valueOf(seconds);
			}
			txtEndTimeField.setText(mins + ":" + secs);
		}
		minutes = TimeUnit.MILLISECONDS.toMinutes((long) startTime);
		seconds = TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime));

		if (minutes < 10) {
			mins = String.format("0%d", minutes);
		} else {
			mins = String.valueOf(minutes);
		}
		if (seconds < 10) {
			secs = String.format("0%d", seconds);
		} else {
			secs = String.valueOf(seconds);
		}
		try {
			if (song != null) {
				updateViews();
			} else {
				txtCurrentSongTitle.setText("");
				txtCurrentSongArtistName.setText("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		txtStartTimeField.setText(mins + ":" + secs);
	}

	private void viewSongDetails(int position) {
		AbstractSong song = abstractSongArrayList.get(position);
		Intent intent = new Intent(mContext, SongDetailsActivity.class);
		intent.putExtra(EXTRA_SONG_DETAIL, song);
		startActivity(intent);
	}

	@Override
	public void start() {
		musicService.play(song);
	}

	private Runnable UpdateSongTime = new Runnable() {
		public void run() {

			setTime();
			seekBar.setProgress((int) startTime);
			seekBar.postDelayed(this, 1000);
		}
	};

	private void updateViews() {
		txtCurrentSongTitle.setText(song.getTitle());
		txtCurrentSongArtistName.setText(song.getArtist());
	}
	
	private void setPlayerCover() {
		Bitmap albumArt = musicService.getPlayingSong().getCover();
		if (albumArt != null) {
			playingAlbumCover.setImageBitmap(albumArt);
		} else {
			playingAlbumCover.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
		}
	}

	@Override
	public void pause() {
		playbackPaused = true;
		musicService.play(song);
	}

	@Override
	public int getDuration() {
		if (musicService != null && musicBound && musicService.isPrepared()) {
			return musicService.getDuration();
		}
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if (musicService != null && musicBound && musicService.isPrepared()) {
			return musicService.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public void seekTo(int pos) {
		musicService.seekTo(pos);
	}

	@Override
	public boolean isPlaying() {
		if (musicService != null && musicBound && musicService.isPlaying()) {
			return musicService.isPlaying();
		}
		return false;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.songctxmenu, menu);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.action_song_details:
			viewSongDetails(info.position);
			break;
		case R.id.action_song_delete:
			int position = info.position;
			AbstractSong song = abstractSongArrayList.get(info.position);
			((SongListAdapter)listView.getAdapter()).remove(position);
			PlaybackService.get(getActivity()).remove(song);
			if (song.getClass() == MusicData.class) {
				((MusicData)song).reset(getActivity());
			}
			break;
		}
		return true;
	}
	
	@Override
	public void start(AbstractSong song) {
		this.song = song;

	}

	@Override
	public void play(AbstractSong song) {
		this.song = song;
		if (isAdded()) {
			btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
		}
	}

	@Override
	public void pause(AbstractSong song) {
		if (isAdded()) {
			btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
		}
	}

	@Override
	public void stop(AbstractSong song) { }

	@Override
	public void update(AbstractSong song) {
		this.song = song;
		if (isAdded()) {
			btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
		}
	}

	@Override
	public void error() {
	}

	@Override
	public void playbackServiceIsDestroyed() {
		seekBar.removeCallbacks(UpdateSongTime);
		((MainActivity) mContext).finish();
	}

	@Override
	public void stopPressed() {
		
	}

	@Override
	public void onTrackTimeChanged(int time, boolean isOverBuffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBufferingUpdate(double percent) {
		// TODO Auto-generated method stub
		
	}

}
