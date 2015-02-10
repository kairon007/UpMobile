package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.ui.DownloadsView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DownloadsFragment extends Fragment {
	private DownloadsView downloadsView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		downloadsView = new DownloadsView(getActivity().getLayoutInflater());
		return downloadsView.getView();
	}

	@Override
	public void onPause() {
		downloadsView.onPause();
		((MainActivity) getActivity()).setSelectedItem(1);
		super.onPause();
	}

	@Override
	public void onResume() {
		downloadsView.onResume();
		super.onResume();
	}
}