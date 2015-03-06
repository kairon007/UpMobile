package org.upmobile.newmusicdownloader.fragment;

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
		playlistView = new PlaylistView(getActivity().getLayoutInflater());
		return playlistView.getView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).setSelectedItem(2);
		((MainActivity) getActivity()).invalidateOptionsMenu();
	}

}
