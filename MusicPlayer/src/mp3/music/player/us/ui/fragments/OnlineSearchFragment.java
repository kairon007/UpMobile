package mp3.music.player.us.ui.fragments;

import mp3.music.player.us.Constants;
import mp3.music.player.us.ui.SearchView;
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
		SearchView searchView = new SearchView(inflater);
		getSherlockActivity().getIntent();
		Intent intent = getSherlockActivity().getIntent();
		String str = intent.getStringExtra(Constants.EXTRA_SEARCH);
		if(str != null && !str.isEmpty()) {
			getActivity().getIntent().removeExtra(Constants.EXTRA_SEARCH);
			searchView.trySearch();
		}		
		return searchView.getView();
	}
}