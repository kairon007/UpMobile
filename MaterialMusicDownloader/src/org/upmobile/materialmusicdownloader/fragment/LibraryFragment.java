package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.ui.LibraryView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LibraryFragment extends Fragment{

	private LibraryView libraryView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(getActivity().getLayoutInflater());
		return libraryView.getView();
	}
	
	@Override
	public void onResume() {
//		((MainActivity) getActivity()).setSelectedItem(2);
		super.onResume();
	}
	
}
