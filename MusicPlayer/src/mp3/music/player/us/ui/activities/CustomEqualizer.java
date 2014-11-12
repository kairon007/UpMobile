package mp3.music.player.us.ui.activities;

import mp3.music.player.us.utils.MusicUtils;
import ru.johnlife.lifetoolsmp3.equalizer.MyEqualizer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public class CustomEqualizer extends MyEqualizer {

	private int audioSessionID = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			audioSessionID = MusicUtils.mService.getAudioSessionId();
		} catch (RemoteException e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected BassBoost getBassBoost() {
		return new BassBoost(2, audioSessionID);
	}

	@Override
	protected Virtualizer getVirtualizer() {
		return new Virtualizer(3, audioSessionID);
	}

	@Override
	protected Equalizer getEqualizer() {
		return new Equalizer(1, audioSessionID);
	}
}
