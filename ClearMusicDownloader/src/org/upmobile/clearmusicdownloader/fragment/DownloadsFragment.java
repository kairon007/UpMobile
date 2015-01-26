package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.ui.DownloadsView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DownloadsFragment extends Fragment {

	private DownloadsView downloadsView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		downloadsView = new DownloadsView(getActivity().getLayoutInflater());
		((MainActivity) getActivity()).showTopFrame();
		return downloadsView.getView();
	}

	@Override
	public void onPause() {
		downloadsView.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		downloadsView.onResume();
		super.onResume();
	}

}