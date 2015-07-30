package org.upmobile.newmaterialmusicdownloader.fragment;

import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.ui.DownloadsView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DownloadsFragment extends Fragment{
	
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
		act.setCurrentFragmentId(ManagerFragmentId.downloadFragment());
		act.setDrawerEnabled(true);
		act.setTitle(R.string.tab_downloads);
		act.invalidateOptionsMenu();
		act.showToolbarShadow(true);
		super.onResume();
	}

}
