package org.kreed.musicdownloader.interfaces;

import java.util.ArrayList;

import org.kreed.musicdownloader.data.MusicData;

public interface TaskSuccessListener {
	void success(ArrayList<MusicData> result);
}
