package org.upmobile.newmaterialmusicdownloader.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.ui.SearchView;

import ru.johnlife.lifetoolsmp3.services.PlaybackService;

public class SearchFragment extends Fragment implements Constants{
	
	private SearchView searchView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		searchView = new SearchView(inflater);
		return searchView.getView();
	}

	@Override
	public void onResume() {
		MainActivity act = (MainActivity) getActivity();
		act.setCurrentFragmentId(ManagerFragmentId.searchFragment());
		act.setDraverEnabled(true);
		act.setTitle(R.string.tab_search);
		act.invalidateOptionsMenu();
		act.showToolbarShadow(false);
		searchView.onResume();
		searchView.restoreState();
		if (PlaybackService.hasInstance() && PlaybackService.get(getActivity()).isPrepared()) {
			searchView.notifyAdapter();
		}
		super.onResume();
	}
	
	@Override
	public void onPause() {
		searchView.saveState();
		searchView.onPause();
		super.onPause();
	}
}
