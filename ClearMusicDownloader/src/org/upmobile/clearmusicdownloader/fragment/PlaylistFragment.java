package org.upmobile.clearmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.menu.ResideMenu;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.ui.PlaylistView;

public class PlaylistFragment extends Fragment {
	
	private PlaylistView playlistView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		playlistView = new PlaylistView(getActivity().getLayoutInflater());
		((MainActivity) getActivity()).showTopFrame();
		((MainActivity) getActivity()).setResideMenuListener(new ResideMenu.OnMenuListener() {

			@Override
			public void openMenu() {
				playlistView.forceDelete();
			}

			@Override
			public void closeMenu() {
			}
		});
		return playlistView.getView();
	}
	
	@Override
	public void onResume() {
		playlistView.onResume();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		playlistView.onPause();
		super.onPause();
	}
	
	public void setFilter(String filter) {
		playlistView.applyFilter(filter);
		playlistView.getMessageView(getView()).setText(getString(R.string.search_no_results_for) + " " + filter);
	}
	
	public void clearFilter() {
		playlistView.clearFilter();
		playlistView.getMessageView(getView()).setText(R.string.playlist_is_empty);
	}
	
	public void collapseAll() {
		playlistView.collapseAll();
	}

}
