package org.upmobile.newmusicdownloader.ui;

import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistView extends BasePlaylistView {

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected String getDirectory() {
		return NewMusicDownloaderApp.getDirectoryPrefix();
	}

	@Override
	protected int getLayoutId() {
		return 0;
	}

	@Override
	protected ListView getListView(View view) {
		return null;
	}

	@Override
	protected TextView getMessageView(View view) {
		return null;
	}

	@Override
	protected void showPlayerFragment(MusicData data) {
		((MainActivity) getContext()).showPlayerElement(true);
		((MainActivity) getContext()).startSong(data);;
	}

	@Override
	protected Bitmap getDeafultCover() {
		return BitmapFactory.decodeResource(getResources(), org.upmobile.newmusicdownloader.R.drawable.no_cover_art_light_big_dark);
	}

	@Override
	protected Object[] groupItems() {
		return new Object[]{BitmapFactory.decodeResource(getResources(), R.drawable.ic_keyboard_arrow_down_white_18dp),BitmapFactory.decodeResource(getResources(), R.drawable.ic_keyboard_arrow_up_white_18dp)};
	}
}
