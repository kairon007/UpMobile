package ru.johnlife.lifetoolsmp3;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;

/**
 * Framework methods only in ICS or above go here.
 */
@TargetApi(14)
public class CompatIcs {
	/**
	 * Used with updateRemote method.
	 */
	private static RemoteControlClient sRemote;
	
	public static void requestAudioFocus(AudioManager manager) {
		manager.requestAudioFocus(new OnAudioFocusChangeListener() {
			
			@Override
			public void onAudioFocusChange(int focusChange) {
			}
		}, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	/**
	 * Perform initialization required for RemoteControlClient.
	 *
	 * @param context
	 *            A context to use.
	 * @param am
	 *            The AudioManager service.
	 */
	@SuppressWarnings("deprecation")
	public static void registerRemote(Context context, AudioManager am) {
		MediaButtonReceiver.registerMediaButton(context);
		ComponentName myEventReceiver = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
		am.requestAudioFocus(new OnAudioFocusChangeListener() {

			@Override
			public void onAudioFocusChange(int focusChange) {}
		},AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		am.registerMediaButtonEventReceiver(myEventReceiver);
		// build the PendingIntent for the remote control client
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(myEventReceiver);
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, 0);
		// create and register the remote control client
		RemoteControlClient myRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
		int flags = RemoteControlClient.FLAG_KEY_MEDIA_NEXT 
				| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
				| RemoteControlClient.FLAG_KEY_MEDIA_PLAY 
				| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE;
		myRemoteControlClient.setTransportControlFlags(flags);
		am.registerRemoteControlClient(myRemoteControlClient);
		sRemote = myRemoteControlClient;
	}

	/**
	 * Update the remote with new metadata.
	 * {@link #registerRemote(Context, AudioManager)} must have been called
	 * first.
	 *
	 * @param context
	 *            A context to use.
	 * @param song
	 *            The song containing the new metadata.
	 * @param state
	 *            PlaybackService state, used to determine playback state.
	 */
	@SuppressWarnings("deprecation")
	public static void updateRemote(Context context, AbstractSong song) {
		RemoteControlClient remote = sRemote;
		if (remote == null) return;
		remote.setPlaybackState(PlaybackService.get(context).isPlaying() ? RemoteControlClient.PLAYSTATE_PLAYING : RemoteControlClient.PLAYSTATE_PAUSED);
		RemoteControlClient.MetadataEditor editor = remote.editMetadata(true);
		if (song != null) {
			editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.getArtist());
			editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.getTitle());
			editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, song.getAlbum());
			Bitmap bitmap = song.getCover();
			if (bitmap != null) {
				bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);
				editor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap);
			}
		}
		editor.apply();
	}

}
