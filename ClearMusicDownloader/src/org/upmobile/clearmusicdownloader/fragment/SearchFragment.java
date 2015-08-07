package org.upmobile.clearmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.ui.SearchView;

public class SearchFragment extends Fragment {
	private SearchView searchView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		searchView = new SearchView(getActivity().getLayoutInflater());
		((MainActivity) getActivity()).showTopFrame();
		return searchView.getView();
	}
	
	@Override
	public void onResume() {
		String query = ((MainActivity) getActivity()).getQuery();
		if (null != query) {
			searchView.setExtraSearch(query);
			searchView.setSearchField(query);
			searchView.trySearch();
			query = null;
		}
		searchView.onResume();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		searchView.saveState();
		super.onPause();
	}
}
