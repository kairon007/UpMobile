package org.upmobile.newmusicdownloader.fragment;

import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.ui.SearchView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SearchFragment extends Fragment {
	private SearchView searchView;
	private String query;

	public SearchFragment(String query) {
		this.query = query;
	}

	public SearchFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		searchView = new SearchView(getActivity().getLayoutInflater());
		if (null != query) {
			searchView.setExtraSearch(query);
			searchView.trySearch();
			query = null;
		}
		return searchView.getView();
	}
	
	@Override
	public void onPause() {
		searchView.saveState();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		((MainActivity) getActivity()).setSelectedItem(0);
		super.onResume();
	}
}
