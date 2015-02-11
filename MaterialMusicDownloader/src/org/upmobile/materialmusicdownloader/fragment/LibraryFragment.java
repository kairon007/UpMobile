package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.ui.LibraryView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class LibraryFragment extends Fragment implements BaseMaterialFragment {

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

	@Override
	public int getDrawerIcon() {
		return com.csform.android.uiapptemplate.R.string.drawer_icon_search_bars;
	}

	@Override
	public int getDrawerTitle() {
		return R.string.navigation_library;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}
}
