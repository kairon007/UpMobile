package org.upmobile.musicpro.fragment;

import org.upmobile.musicpro.BaseFragment;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.slidingmenu.SlidingMenu;
import org.upmobile.musicpro.widget.LibraryView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LibraryFragment extends BaseFragment {

	private LibraryView libraryView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(getActivity().getLayoutInflater());
		View view = libraryView.getView();
		initUIBase(view);
		setButtonMenu(view);
		setHeaderTitle(R.string.library);
		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			getMainActivity().menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			getMainActivity().setVisibilityFooter();
		}
	}

}
