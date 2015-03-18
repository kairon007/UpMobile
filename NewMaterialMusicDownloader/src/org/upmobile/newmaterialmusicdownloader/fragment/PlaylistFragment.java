package org.upmobile.newmaterialmusicdownloader.fragment;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.ui.PlaylistView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaylistFragment extends Fragment implements Constants{

	private PlaylistView playlistView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		playlistView = new PlaylistView(getActivity().getLayoutInflater());
		return playlistView.getView();
	}
	
	public void setFilter(String filter) {
		playlistView.applyFilter(filter);
	}
	
	public void clearFilter() {
		playlistView.clearFilter();
	}
	
	@Override
	public void onResume() {
		((MainActivity) getActivity()).setCurrentFragmentId(PLAYLIST_FRAGMENT);
		((MainActivity) getActivity()).setDraverEnabled(true);
		((MainActivity) getActivity()).setTitle(R.string.tab_playlist);
		((MainActivity) getActivity()).invalidateOptionsMenu();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		playlistView.onPause();
		super.onPause();
	}
}