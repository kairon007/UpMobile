package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.ui.PlaylistView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class PlaylistFragment extends Fragment implements BaseMaterialFragment, Constants {
	
	private PlaylistView playlistView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		playlistView = new PlaylistView(getActivity().getLayoutInflater());
		return playlistView.getView();
	}
	
	@Override
	public void onResume() {
		MainActivity activity = (MainActivity) getActivity();
		activity.setSelectedItem(PLAYLIST_FRAGMENT);
		activity.setTitle(getDrawerTitle());
		activity.setDrawerEnabled(true);
		activity.setVisibleSearchView(true);
		super.onResume();
	}

	@Override
	public int getDrawerIcon() {
		return R.string.font_playlist;
	}

	@Override
	public int getDrawerTitle() {
		return R.string.tab_playlist;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}
	
	public void setFilter(String filter) {
		playlistView.applyFilter(filter);
		playlistView.getMessageView(getView()).setText(getString(R.string.search_no_results_for) + " " + filter);
	}
	
	public void clearFilter() {
		playlistView.clearFilter();
		playlistView.getMessageView(getView()).setText(R.string.playlists_are_missing);
	}

}
