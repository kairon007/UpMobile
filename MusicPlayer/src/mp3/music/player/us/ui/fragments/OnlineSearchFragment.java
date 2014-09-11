package mp3.music.player.us.ui.fragments;

import mp3.music.player.us.Constants;
import mp3.music.player.us.ui.OnlineSearchView;
import mp3.music.player.us.ui.activities.HomeActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class OnlineSearchFragment extends SherlockFragment {
	
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View searchView = OnlineSearchView.getInstanceView(inflater, (HomeActivity)getActivity());
		getSherlockActivity().getIntent();
		Intent intent = getSherlockActivity().getIntent();
		String str = intent.getStringExtra(Constants.EXTRA_SEARCH);
		if(str != null && !str.isEmpty()){
			getActivity().getIntent().removeExtra(Constants.EXTRA_SEARCH);
			OnlineSearchView.getInstance(inflater, (HomeActivity)getActivity()).trySearch();
		}		
		return searchView;
	}	
}