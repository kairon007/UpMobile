package org.upmobile.materialmusicdownloader.fragment;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.ui.PlaylistView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csform.android.uiapptemplate.Constants;
import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class PlaylistFragment extends Fragment implements BaseMaterialFragment{
	
	private PlaylistView playlistView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		playlistView = new PlaylistView(getActivity().getLayoutInflater());
		return playlistView.getView();
	}
	
	@Override
	public void onResume() {
		((UIMainActivity) getActivity()).setSelectedItem(Constants.PLYLIST_FRAGMENT);
		((UIMainActivity) getActivity()).setTitle(getDrawerTitle());
		((UIMainActivity)getActivity()).setDrawerEnabled(true);
		((UIMainActivity) getActivity()).invalidateOptionsMenu();
		super.onResume();
	}

	@Override
	public int getDrawerIcon() {
		return R.string.font_playlist;
	}

	@Override
	public int getDrawerTitle() {
		return R.string.tab_playlist;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}

}
