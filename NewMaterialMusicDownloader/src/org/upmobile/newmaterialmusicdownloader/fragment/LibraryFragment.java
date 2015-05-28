package org.upmobile.newmaterialmusicdownloader.fragment;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.ui.LibraryView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LibraryFragment extends Fragment implements Constants {
	
	private LibraryView libraryView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(inflater);
		return libraryView.getView();
	}
	
	public void setFilter(final String filter) {
		libraryView.applyFilter(filter);
		libraryView.getMessageView(getView()).setText(new StringBuilder(getString(R.string.search_no_results_for)).append(" ").append(filter));
	}
	
	public void clearFilter() {
		libraryView.getMessageView(getView()).setText(R.string.library_empty);
		libraryView.clearFilter();
	}
	
	@Override
	public void onResume() {
		MainActivity act = (MainActivity) getActivity();
		act.setCurrentFragmentId(ManagerFragmentId.libraryFragment());
		act.setDraverEnabled(true);
		act.setTitle(R.string.tab_library);
		act.invalidateOptionsMenu();
		act.showToolbarShadow(true);
		libraryView.onResume();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		libraryView.onPause();
		super.onPause();
	}
	
	public void forceDelete () {
		libraryView.forceDelete();
	}
	
}
