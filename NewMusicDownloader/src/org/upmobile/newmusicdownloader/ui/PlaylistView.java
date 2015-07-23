package org.upmobile.newmusicdownloader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.adapter.PlaylistAdapter;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BasePlaylistView;

public class PlaylistView extends BasePlaylistView {

	private ListView lView;

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected String getDirectory() {
		return NewMusicDownloaderApp.getDirectoryPrefix();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.playlist_view;
	}

	@Override
	protected ListView getListView(View view) {
		lView = (ListView) view.findViewById(R.id.list);
		return lView;
	}
	
	@Override
	public TextView getMessageView(View view) {
		return (TextView) view.findViewById(R.id.emptyText);
	}

	@Override
	protected void showPlayerFragment(MusicData data) {
		((MainActivity) getContext()).startSong(data);
	}

	@Override
	protected Bitmap getDeafultCover() {
		return BitmapFactory.decodeResource(getResources(), org.upmobile.newmusicdownloader.R.drawable.no_cover_art_light_big_dark);
	}

	@Override
	protected BasePlaylistsAdapter getAdapter(Context context) {
		return new PlaylistAdapter(context, R.layout.playlist_group_item);
	}

	@Override
	protected void forceDelete() {}

	@Override
	protected void collapseSearchView () {
		((MainActivity) getContext()).getSearchView().onActionViewCollapsed();
	}
}
