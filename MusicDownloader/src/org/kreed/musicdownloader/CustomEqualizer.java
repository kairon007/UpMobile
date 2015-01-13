package org.kreed.musicdownloader;

import org.kreed.musicdownloader.app.MusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.equalizer.MyEqualizer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;

public class CustomEqualizer extends MyEqualizer {

	@Override
	protected BassBoost getBassBoost() {
		if (null == MusicDownloaderApp.getService() || !MusicDownloaderApp.getService().containsPlayer()) {
			return null;
		} else return MusicDownloaderApp.getService().getPlayer().getBassBoost();
	}

	@Override
	protected Virtualizer getVirtualizer() {
		if (null == MusicDownloaderApp.getService() || !MusicDownloaderApp.getService().containsPlayer()) {
			return null;
		} else return MusicDownloaderApp.getService().getPlayer().getVirtualizer();
	}

	@Override
	protected Equalizer getEqualizer() {
		if (null == MusicDownloaderApp.getService() || !MusicDownloaderApp.getService().containsPlayer()) {
			return null;
		} else return MusicDownloaderApp.getService().getPlayer().getEqualizer();
	}
}
