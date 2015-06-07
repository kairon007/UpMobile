package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

public class PlaylistAdapter extends BasePlaylistsAdapter implements UndoAdapter {

	public PlaylistAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	public View getUndoView(int paramInt, View paramView, ViewGroup paramViewGroup) {
		View view = paramView;
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_undo_view, paramViewGroup, false);
		}
		return view;
	}

	@Override
	public View getUndoClickView(View paramView) {
		return paramView.findViewById(R.id.undo_button);
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

	@Override
	protected String getDirectory() {
		return NewMusicDownloaderApp.getDirectoryPrefix();
	}

	@Override
	protected Bitmap getDefaultCover() {
		return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.no_cover_art_light_big_dark);
	}

}
