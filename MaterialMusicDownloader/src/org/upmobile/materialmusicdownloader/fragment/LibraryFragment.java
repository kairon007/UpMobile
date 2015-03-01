package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.ui.LibraryView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class LibraryFragment extends Fragment implements BaseMaterialFragment {

	private LibraryView libraryView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(inflater);
		return libraryView.getView();
	}
	
	@Override
	public void onResume() {
		((UIMainActivity) getActivity()).setSelectedItem(2);
		((UIMainActivity) getActivity()).setTitle(getDrawerTitle());
		((UIMainActivity)getActivity()).setDrawerEnabled(true);
		((UIMainActivity) getActivity()).invalidateOptionsMenu();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		clearFilter();
		super.onPause();
	}

	@Override
	public int getDrawerIcon() {
		return R.string.font_musics;
	}
	
	public void setFilter(String filter) {
		libraryView.applyFilter(filter);
	}
	
	public void clearFilter() {
		libraryView.clearFilter();
	}

	@Override
	public int getDrawerTitle() {
		return R.string.tab_library;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}
}
