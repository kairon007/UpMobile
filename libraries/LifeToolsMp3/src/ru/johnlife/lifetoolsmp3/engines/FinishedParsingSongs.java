package ru.johnlife.lifetoolsmp3.engines;

import java.util.List;

import ru.johnlife.lifetoolsmp3.song.Song;

public interface FinishedParsingSongs {

	void onFinishParsing(List<Song> songsList);

}
