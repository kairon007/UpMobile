package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlayerFragment  extends Fragment {

	private View parentView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.player, container, false);
		return parentView;
	}
	
}
