package org.kreed.musicdownloader.engines;

import java.util.List;

import org.kreed.musicdownloader.Song;

public interface FinishedParsingSongs {

	void onFinishParsing(List<Song> songsList);

}
