package org.kreed.musicdownloader;

import java.util.ArrayList;

public interface TaskSuccessListener {
	void success(ArrayList<MusicData> result);
}
