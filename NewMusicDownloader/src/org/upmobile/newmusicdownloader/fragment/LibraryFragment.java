package org.upmobile.newmusicdownloader.fragment;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.ui.LibraryView;

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
		return libraryView.getView();
	}

	@Override
	public void onResume() {
		((MainActivity) getActivity()).invalidateOptionsMenu();
		((MainActivity) getActivity()).setSelectedItem(Constants.LIBRARY_FRAGMENT);
		((MainActivity)	getActivity()).setCurrentTag(getClass().getSimpleName());
		((MainActivity) getActivity()).setTitle(R.string.tab_library);
		libraryView.onResume();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		libraryView.onPause();
		super.onPause();
	}

	public void setFilter(String filter) {
		libraryView.applyFilter(filter);
	}

	public void clearFilter() {
		libraryView.clearFilter();
	}

}
