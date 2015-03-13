package org.upmobile.newmaterialmusicdownloader.ui;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;

public class PlaylistView extends BasePlaylistView {

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected Object[] groupItems() {
		return null;
	}

	@Override
	protected Bitmap getDeafultCover() {
		return null;
	}

	@Override
	protected String getDirectory() {
		return null;
	}

	@Override
	protected int getLayoutId() {
		return 0;
	}

	@Override
	protected void showPlayerFragment(MusicData musicData) {

	}

	@Override
	protected ListView getListView(View view) {
		return null;
	}

	@Override
	protected TextView getMessageView(View view) {
		return null;
	}

}
