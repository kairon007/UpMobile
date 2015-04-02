package org.upmobile.newmaterialmusicdownloader.fragment;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.ui.DownloadsView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DownloadsFragment extends Fragment implements Constants{
	
	DownloadsView downloadsView;
	
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
		((MainActivity) getActivity()).setCurrentFragmentId(ManagerFragmentId.downloadFragment());
		((MainActivity) getActivity()).setDraverEnabled(true);
		((MainActivity) getActivity()).setTitle(R.string.tab_downloads);
		((MainActivity) getActivity()).invalidateOptionsMenu();
		((MainActivity) getActivity()).showToolbarShadow(true);
		super.onResume();
	}

}
