package org.upmobile.newmusicdownloader.fragment;

import org.upmobile.newmusicdownloader.ui.SearchView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SearchFragment extends Fragment {
	private SearchView searchView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		searchView = new SearchView(getActivity().getLayoutInflater());
		return searchView.getView();
	}
	
	@Override
	public void onPause() {
		searchView.saveState();
		super.onPause();
	}
}
