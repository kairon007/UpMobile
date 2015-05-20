package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.ui.SearchView;

import ru.johnlife.lifetoolsmp3.PlaybackService;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class SearchFragment extends Fragment implements BaseMaterialFragment, Constants {

	private SearchView searchView;
	private String query;

	public SearchFragment(String query) {
		this.query = query;
	}

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
		activity.setVisibleSearchView(false);
		query = activity.getQuery();
		if (null != query) {
			searchView.setExtraSearch(query);
			searchView.setSearchField(query);
			searchView.trySearch();
			query = null;
		}
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
