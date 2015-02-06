package org.upmobile.musix.fragments;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.upmobile.musix.R;
import org.upmobile.musix.activities.MainActivity;
import org.upmobile.musix.activities.SongDetailsActivity;
import org.upmobile.musix.listadapters.SongListAdapter;
import org.upmobile.musix.utils.TypefaceHelper;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnPlaybackServiceDestroyListener;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class SongsListFragment extends Fragment implements MediaController.MediaPlayerControl, OnStatePlayerListener, OnPlaybackServiceDestroyListener {

	private static final String EXTRA_SONG_DETAIL = "EXTRA_SONG_DETAIL";

	public SongsListFragment() {
		// Required empty public constructor
	}

	private Context mContext;
	private ListView listView;
	private static View rootView;
	private TypefaceHelper typefaceHelper;
	private ArrayList<AbstractSong> abstractSongArrayList;
	private PlaybackService musicService;
	private boolean musicBound = false;
	private AbstractSong song;
	private Object lock = new Object();
	private boolean paused = false, playbackPaused = false;

	// controller variables
	private double startTime = 0;
	private double finalTime = 0;

	private SongListAdapter songListAdapter;
	private SeekBar seekBar;
	private ImageView playingAlbumCover;
	private ImageButton btnPrev, btnNext, btnStop, btnPlayPause, btnShuffle;
	private TextView txtStartTimeField, txtEndTimeField, txtCurrentSongTitle, txtCurrentSongArtistName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		abstractSongArrayList = new ArrayList<AbstractSong>();
		try {
			rootView = inflater.inflate(R.layout.fragment_songs, container, false);
			mContext = rootView.getContext();
			setupViews(rootView);
		} catch (Exception ex) {
		}
		mContext.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
		musicService = PlaybackService.get(getActivity());
		musicService.addStatePlayerListener(this);
		musicService.setDestroyListener(this);
		if (!musicService.hasArray()) {
			setPlayback();
		}
		if (null != musicService.getPlayingSong()) {
			song = musicService.getPlayingSong();
			setPlayerCover();
			setTime();
			seekBar.setMax(musicService.getDuration());
			seekBar.setProgress(musicService.getCurrentPosition());
			abstractSongArrayList = musicService.getArrayPlayback();
		} else {
			if (!abstractSongArrayList.isEmpty()) {
				song = abstractSongArrayList.get(0);
			}
		}
		if (!musicService.enabledShuffle()) {
			btnShuffle.setAlpha((float) 0.5);
		} else {
			btnShuffle.setAlpha((float) 1);
		}
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
		}

		public boolean deliverSelfNotifications() {
			return false;
		};

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
				synchronized (lock) {
					songListAdapter.clear();
					querySong();
					abstractSongArrayList = songListAdapter.getList();
					setPlayback();
				}
			}
		};
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

	private void setupViews(View root) {
		setHasOptionsMenu(true);
		typefaceHelper = new TypefaceHelper(mContext);
		seekBar = (SeekBar) root.findViewById(R.id.songSeekBar);
		txtStartTimeField = (TextView) root.findViewById(R.id.songStartTime);
		txtEndTimeField = (TextView) root.findViewById(R.id.songTotalTime);
		btnNext = (ImageButton) root.findViewById(R.id.btnNext);
		btnPlayPause = (ImageButton) root.findViewById(R.id.btnPlayPause);
		btnPrev = (ImageButton) root.findViewById(R.id.btnPrev);
		btnShuffle = (ImageButton) root.findViewById(R.id.btnRandom);
		btnStop = (ImageButton) root.findViewById(R.id.btnStop);
		txtCurrentSongTitle = (TextView) root.findViewById(R.id.txtCurrentSongTitle);
		txtCurrentSongArtistName = (TextView) root.findViewById(R.id.txtCurrentSongArtistName);
		playingAlbumCover = (ImageView) root.findViewById(R.id.imageAlbumPlaying);
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

		listView = (ListView) root.findViewById(R.id.listViewSongs);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				stop();
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
				musicService.shift(-1);
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
				musicService.shift(1);
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
					if (abstractSongArrayList.isEmpty()) return;
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
				if (!musicService.enabledShuffle()) {
					btnShuffle.setAlpha((float) 0.5);
				} else {
					btnShuffle.setAlpha((float) 1);
				}
			}
		});
		registerForContextMenu(listView);
		songListAdapter = new SongListAdapter(mContext, abstractSongArrayList);
		listView.setAdapter(songListAdapter);
		querySong();
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
		synchronized (lock) {
			ArrayList<MusicData> result = new ArrayList<MusicData>();
			Cursor cursor = buildQuery(mContext.getContentResolver());
			if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return result;
			}
			try {
				for (int i = 0; i < cursor.getCount(); i++) {
					MusicData data = new MusicData();
					data.populate(cursor);
					songListAdapter.add(data);
					cursor.moveToNext();
				}
			} catch (Exception e) {
			}
			cursor.close();
			return result;
		}
	}

	private Cursor buildQuery(ContentResolver resolver) {
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, null, null, null);
		return cursor;
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
		Bitmap albumArt = musicService.getPlayingSong().getCover(mContext);
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
	}

	@Override
	public void pause(AbstractSong song) {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop(AbstractSong song) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(AbstractSong song) {
		this.song = song;
	}

	@Override
	public void error() {
		// TODO Auto-generated method stub
	}

	@Override
	public void playbackServiceIsDestroyed() {
		seekBar.removeCallbacks(UpdateSongTime);
		((MainActivity) mContext).finish();
	}

}
