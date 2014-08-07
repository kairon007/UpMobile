package org.kreed.musicdownloader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DownloadsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View downloadView = DownloadsTab.getInstanceView(getLayoutInflater(savedInstanceState), getActivity());
		return downloadView;
	}
	
}
