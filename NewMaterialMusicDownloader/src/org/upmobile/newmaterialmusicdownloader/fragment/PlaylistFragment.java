package org.upmobile.newmaterialmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.ui.PlaylistView;

public class PlaylistFragment extends Fragment {

	private PlaylistView playlistView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		playlistView = new PlaylistView(inflater);
		return playlistView.getView();
	}

	public void setFilter(final String filter) {
		playlistView.forceDelete();
		playlistView.applyFilter(filter);
		playlistView.getMessageView(getView()).setText(new StringBuilder(getString(R.string.search_no_results_for)).append(" ").append(filter));
	}

	public void clearFilter() {
		playlistView.clearFilter();
		playlistView.getMessageView(getView()).setText(R.string.playlist_is_empty);
	}

	@Override
	public void onResume() {
		MainActivity act = (MainActivity) getActivity();
		act.setCurrentFragmentId(ManagerFragmentId.playlistFragment());
		act.setDraverEnabled(true);
		act.setTitle(R.string.tab_playlist);
		act.invalidateOptionsMenu();
		act.showToolbarShadow(true);
		playlistView.onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		playlistView.onPause();
		super.onPause();
	}

	public void collapseAll() {
		playlistView.collapseAll();
	}

	public void forceDelete() {
		playlistView.forceDelete();
	}
}