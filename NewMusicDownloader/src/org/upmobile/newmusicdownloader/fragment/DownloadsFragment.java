package org.upmobile.newmusicdownloader.fragment;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.ui.DownloadsView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DownloadsFragment extends Fragment {
	private DownloadsView downloadsView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		downloadsView = new DownloadsView(inflater);
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
		MainActivity act = (MainActivity) getActivity();
		act.invalidateOptionsMenu();
		act.setSelectedItem(Constants.DOWNLOADS_FRAGMENT);
		act.setTitle(R.string.tab_downloads);
		act.setDrawerEnabled(true);
		super.onResume();
	}
}