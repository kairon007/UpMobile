package org.upmobile.musicpro.fragment;

import org.upmobile.musicpro.BaseFragment;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.slidingmenu.SlidingMenu;
import org.upmobile.musicpro.widget.SearchView;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class OnlineSearchFragment extends BaseFragment{
	
	private View viewSearchActivity;
	private SearchView searchView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search_online, container, false);
		FrameLayout layout = (FrameLayout) view.findViewById(R.id.container_search_online);
		searchView = new SearchView(inflater);
		viewSearchActivity = searchView.getView();
		layout.addView(viewSearchActivity);
		initUIBase(view);
		setButtonMenu(view);
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
	
	@Override
	public void onDetach() {
		StateKeeper.getInstance().saveStateAdapter(searchView);
		super.onDetach();
	}
	
}
