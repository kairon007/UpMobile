package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.ui.LibraryView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.menu.ResideMenu.OnMenuListener;

public class LibraryFragment extends Fragment {

	private LibraryView libraryView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(inflater);
		((MainActivity) getActivity()).showTopFrame();
		((MainActivity) getActivity()).setResideMenuListener(new OnMenuListener() {
			
			@Override
			public void openMenu() {
				libraryView.forceDelete();
			}
			
			@Override
			public void closeMenu() {}
		});
		return libraryView.getView();
	}
	
	public void setFilter(String filter) {
		libraryView.applyFilter(filter);
		libraryView.getMessageView(getView()).setText(getString(R.string.search_no_results_for) + " " + filter);
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
		libraryView.forceDelete();
		libraryView.onPause();
		super.onPause();
	}

}
