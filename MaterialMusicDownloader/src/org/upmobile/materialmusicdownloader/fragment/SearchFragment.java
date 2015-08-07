package org.upmobile.materialmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.models.BaseMaterialFragment;
import org.upmobile.materialmusicdownloader.ui.SearchView;

import ru.johnlife.lifetoolsmp3.services.PlaybackService;

public class SearchFragment extends Fragment implements BaseMaterialFragment, Constants {

	private SearchView searchView;
	public SearchFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		searchView = new SearchView(inflater);
		return searchView.getView();
	}
	
	@Override
	public void onPause() {
		searchView.saveState();
		searchView.onPause();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		MainActivity activity = (MainActivity) getActivity();
		activity.setSelectedItem(SEARCH_FRAGMENT);
		activity.setTitle(getDrawerTitle());
		activity.setDrawerEnabled(true);
		searchView.onResume();
		if (PlaybackService.hasInstance() && PlaybackService.get(getActivity()).isPrepared()) {
			searchView.notifyAdapter();
		}
		super.onResume();
	}

	@Override
	public int getDrawerIcon() {
		return R.string.font_search;
	}

	@Override
	public int getDrawerTitle() {
		return R.string.tab_search;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}
}
