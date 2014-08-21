package mp3.music.player.us.ui.fragments;

import mp3.music.player.us.ui.OnlineSearchView;
import mp3.music.player.us.ui.activities.HomeActivity;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
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
		String str = intent.getStringExtra("search");
		if(str != null && !str.isEmpty()){
			OnlineSearchView.getInstance(inflater, (HomeActivity)getActivity()).trySearch(str);
		}		
		return searchView;
	}	
}