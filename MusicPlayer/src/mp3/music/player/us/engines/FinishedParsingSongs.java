package mp3.music.player.us.engines;

import java.util.List;


public interface FinishedParsingSongs {

	void onFinishParsing(List<OnlineSong> songsList);

}
