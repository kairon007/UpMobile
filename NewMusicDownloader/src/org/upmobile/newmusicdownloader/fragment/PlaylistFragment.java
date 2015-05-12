package org.upmobile.newmusicdownloader.fragment;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.ui.PlaylistView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaylistFragment extends Fragment {
	
	private PlaylistView playlistView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		playlistView = new PlaylistView(inflater);
		return playlistView.getView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MainActivity act = (MainActivity) getActivity();
		act.setTitle(R.string.tab_playlist);
		act.setSelectedItem(Constants.PLAYLIST_FRAGMENT);
		act.invalidateOptionsMenu();
	}
	
	public void setFilter(String filter) {
		playlistView.applyFilter(filter);
		playlistView.getMessageView(getView()).setText(getString(R.string.search_no_results_for) + " " + filter);
	}
	
	public void clearFilter() {
		playlistView.clearFilter();
		playlistView.getMessageView(getView()).setText(R.string.playlist_is_empty);
	}

}
