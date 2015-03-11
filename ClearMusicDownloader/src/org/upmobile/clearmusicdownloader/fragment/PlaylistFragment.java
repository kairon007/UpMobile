package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.ui.PlaylistView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaylistFragment extends Fragment {
	
	private PlaylistView playlistView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		playlistView = new PlaylistView(getActivity().getLayoutInflater());
		((MainActivity) getActivity()).showTopFrame();
		return playlistView.getView();
	}
	

	public void setFilter(String filter) {
		playlistView.applyFilter(filter);
	}
	
	public void clearFilter() {
		playlistView.clearFilter();
	}

}
