package org.kreed.vanilla;

import ru.johnlife.lifetoolsmp3.equalizer.MyEqualizer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;

public class CustomEqualizer extends MyEqualizer {

	@Override
	protected BassBoost getBassBoost() {
		return PlaybackService.get(this).getBassBoost();
	}

	@Override
	protected Virtualizer getVirtualizer() {
		return PlaybackService.get(this).getVirtualizer();
	}

	@Override
	protected Equalizer getEqualizer() {
		return PlaybackService.get(this).getEqualizer();
	}
}
