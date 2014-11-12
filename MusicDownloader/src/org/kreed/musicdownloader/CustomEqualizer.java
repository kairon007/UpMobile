package org.kreed.musicdownloader;

import org.kreed.musicdownloader.app.MusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.equalizer.MyEqualizer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;

public class CustomEqualizer extends MyEqualizer {

	@Override
	protected BassBoost getBassBoost() {
		return MusicDownloaderApp.getService().getPlayer().getBassBoost();
	}

	@Override
	protected Virtualizer getVirtualizer() {
		return MusicDownloaderApp.getService().getPlayer().getVirtualizer();
	}

	@Override
	protected Equalizer getEqualizer() {
		return MusicDownloaderApp.getService().getPlayer().getEqualizer();
	}
}
