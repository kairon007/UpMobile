package org.kreed.vanilla.engines;

import java.util.List;

import org.kreed.vanilla.song.Song;

public interface FinishedParsingSongs {

	void onFinishParsing(List<Song> songsList);

}
