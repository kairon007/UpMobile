package org.kreed.vanilla.engines;

import java.util.List;

import org.kreed.vanilla.Song;

public interface FinishedParsingSongs {

	void onFinishParsing(List<Song> songsList);

}
