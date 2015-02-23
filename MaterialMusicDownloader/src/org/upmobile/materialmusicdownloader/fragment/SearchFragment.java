package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.ui.SearchView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class SearchFragment extends Fragment implements BaseMaterialFragment {

	private SearchView searchView;
	private String query;

	public SearchFragment(String query) {
		this.query = query;
	}

	public SearchFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		searchView = new SearchView(getActivity().getLayoutInflater());
		return searchView.getView();
	}
	
	@Override
	public void onPause() {
		searchView.saveState();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		query = ((UIMainActivity) getActivity()).getQuery();
		if (null != query) {
			searchView.setExtraSearch(query);
			searchView.setSearchField(query);
			searchView.trySearch();
			query = null;
		}
		((UIMainActivity) getActivity()).setSelectedItem(0);
		((UIMainActivity) getActivity()).setTitle(getDrawerTitle());
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
