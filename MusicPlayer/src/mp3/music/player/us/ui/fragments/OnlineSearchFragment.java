package mp3.music.player.us.ui.fragments;

import mp3.music.player.us.ui.OnlineSearchView;
import mp3.music.player.us.ui.activities.HomeActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class OnlineSearchFragment extends SherlockFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View searchView = OnlineSearchView.getInstanceView(inflater, (HomeActivity)getActivity());
		return searchView;
	}
	
}