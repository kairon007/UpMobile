package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.ui.DownloadsView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class DownloadsFragment extends Fragment implements BaseMaterialFragment {
	
	private DownloadsView downloadsView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		downloadsView = new DownloadsView(getActivity().getLayoutInflater());
		return downloadsView.getView();
	}

	@Override
	public void onPause() {
		downloadsView.onPause();
//		((MainActivity) getActivity()).setSelectedItem(1);
		((UIMainActivity) getActivity()).setTitle(getDrawerTitle());
		super.onPause();
	}

	@Override
	public void onResume() {
		downloadsView.onResume();
		((UIMainActivity) getActivity()).setSelectedItem(1);
		super.onResume();
	}

	@Override
	public int getDrawerIcon() {
		return R.string.font_download_pic;
	}

	@Override
	public int getDrawerTitle() {
		return R.string.tab_downloads;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}
}