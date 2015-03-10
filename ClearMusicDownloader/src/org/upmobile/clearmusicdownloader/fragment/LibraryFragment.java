package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.ui.LibraryView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LibraryFragment extends Fragment {

	private LibraryView libraryView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(inflater);
		((MainActivity) getActivity()).showTopFrame();
		return libraryView.getView();
	}
	
	public void setFilter(String filter) {
		libraryView.applyFilter(filter);
	}
	
	public void clearFilter() {
		libraryView.clearFilter();
	}
	
	@Override
	public void onResume() {
		libraryView.onResume();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		libraryView.onPause();
		super.onPause();
	}

}
