package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.adapter.ExpandableAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import android.content.Context;

public class ExpandableAdapterWrapper extends ExpandableAdapter {

	public ExpandableAdapterWrapper(Context context) {
		super(context);
	}
	
	@Override
	public void playAll(PlaylistData item, Context context) {
		if (item.getSongs().isEmpty()) {
			((MainActivity) context).showMessage(R.string.playlist_is_empty);
			return;
		}
		PlaybackService player = PlaybackService.get(context);
		MusicApp.getSharedPreferences().edit().putLong(Constants.PREF_LAST_PLAYLIST_ID, item.getId()).commit();
		player.setArrayPlayback(new ArrayList<AbstractSong>(item.getSongs()));
		((BaseMiniPlayerActivity) context).startSong((item.getSongs().get(0)));
	}
}
