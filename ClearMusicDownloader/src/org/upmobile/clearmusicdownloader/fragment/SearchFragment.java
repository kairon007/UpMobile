package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.ui.SearchView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SearchFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return new SearchView(getActivity().getLayoutInflater()).getView();
	}
}
