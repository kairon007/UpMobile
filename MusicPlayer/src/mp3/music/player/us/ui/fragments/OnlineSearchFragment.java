package mp3.music.player.us.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import mp3.music.player.us.Constants;
import mp3.music.player.us.ui.SearchView;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;

public class OnlineSearchFragment extends SherlockFragment {
	
	private SearchView searchView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Activity activity = getActivity();
		searchView = new SearchView(inflater);
		getSherlockActivity().getIntent();
		Intent intent = getSherlockActivity().getIntent();
		String str = intent.getStringExtra(Constants.EXTRA_SEARCH);
		if(str != null && !str.isEmpty()) {
			getActivity().getIntent().removeExtra(Constants.EXTRA_SEARCH);
			searchView.setExtraSearch(str);
		}
		return searchView.getView();
	}
	
	@Override
	public void onDestroyView() {
		StateKeeper.getInstance().saveStateAdapter(searchView);
		super.onDestroyView();
	}
}