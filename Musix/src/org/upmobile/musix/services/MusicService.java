package org.upmobile.musix.services;

import java.util.ArrayList;
import java.util.Random;

import org.upmobile.musix.R;
import org.upmobile.musix.activities.MainActivity;
import org.upmobile.musix.models.Song;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

	private final IBinder musicBind = new MusicBinder();

	// media player
	private MediaPlayer player;

	// song list
	private ArrayList<Song> songs;

	// current position
	private int songPosition;

	private boolean shuffle = false;
	private Random random;

	public MusicService() {
	}

	public void onCreate() {
		// create the service
		super.onCreate();

		random = new Random();

		// initialize position
		songPosition = 0;

		// create player
		player = new MediaPlayer();

		initMusicPlayer();
	}

	public void initMusicPlayer() {
		// set player properties
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}

	public void setList(ArrayList<Song> theSongs) {
		songs = theSongs;
	}

	public class MusicBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	public void playPrev() {
		songPosition--;
		if (songPosition < 0) {
			songPosition = songs.size() - 1;
		}
		playSong();
	}

	// skip to next
	public void playNext() {

		if (shuffle) {
			int newSong = songPosition;
			while (newSong == songPosition) {
				newSong = random.nextInt(songs.size());
			}
			songPosition = newSong;
		} else {
			songPosition++;
			if (songPosition > songs.size()) {
				songPosition = 0;
			}
		}

		playSong();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		player.stop();
		player.release();
		return false;
	}

	public int getPosition() {
		return player.getCurrentPosition();
	}

	public int getCurrenListPosition() {
		return songPosition;
	}

	public int getDuration() {
		return player.getDuration();
	}

	public boolean isPlaying() {
		return player.isPlaying();
	}

	public void pausePlayer() {
		player.pause();
	}

	public void stopPlayer() {
		player.stop();
	}

	public void seek(int posn) {
		player.seekTo(posn);
	}

	public void start() {
		player.start();
	}

	public void setShuffle() {
		if (shuffle) {
			shuffle = false;
		} else {
			shuffle = true;
		}
	}

	public void playSong() {
		try {
			// play a song
			player.reset();
			// get song
			Song playSong = songs.get(songPosition);
			// get id
			long currentSong = playSong.getId();
			// set uri
			Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);

			player.setDataSource(getApplicationContext(), trackUri);
			player.prepareAsync();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mp.getCurrentPosition() == 0) {
			mp.reset();
			playNext();
		} else {
			playNext();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	@SuppressLint("NewApi")
	@Override
	public void onPrepared(MediaPlayer mp) {
		// start playback
		mp.start();

		Song playingSong = songs.get(songPosition);

		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(this);

		builder.setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_action_play).setTicker(playingSong.getTitle()).setOngoing(true)
				.setContentTitle(playingSong.getTitle()).setContentText(playingSong.getArtistName());

		Notification notification = builder.build();

		startForeground((int) playingSong.getId(), notification);
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
	}

	public void setSong(int songIndex) {
		songPosition = songIndex;
	}
}
