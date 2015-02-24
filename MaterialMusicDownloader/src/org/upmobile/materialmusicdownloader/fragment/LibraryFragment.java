package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.R;
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
		super.onResume();
	}

	@Override
	public int getDrawerIcon() {
		return R.string.font_musics;
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
