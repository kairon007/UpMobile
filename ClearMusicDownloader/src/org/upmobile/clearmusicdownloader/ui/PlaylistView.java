package org.upmobile.clearmusicdownloader.ui;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;

import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
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
		return Constants.DIRECTORY;
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
	protected void showPlayerFragment() {
		((MainActivity) getContext()).changeFragment(new PlayerFragment());
	}
}
