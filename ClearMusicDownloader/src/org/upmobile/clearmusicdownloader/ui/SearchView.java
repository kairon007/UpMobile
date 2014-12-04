package org.upmobile.clearmusicdownloader.ui;

import org.upmobile.clearmusicdownloader.Advertisement;
import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.Settings;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class SearchView extends OnlineSearchView {

	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	protected void click(final View view, int position) {
		final RemoteSong song  = (RemoteSong) getResultAdapter().getItem(position);
		 Bundle bundle = new Bundle();
		 bundle.putParcelable(Constants.KEY_SELECTED_SONG, song);
		 PlayerFragment playerFragment = new PlayerFragment();
		 playerFragment.setArguments(bundle);
	     ((MainActivity) view.getContext()).changeFragment(playerFragment);
		super.click(view, position);
	}

	@Override
	protected BaseSettings getSettings() {
		return new Settings();
	}

	@Override
	protected Advertisment getAdvertisment() {
		return new Advertisement();
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshLibrary() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isWhiteTheme(Context context) {
		return false;
	}
	
	@Override
	protected boolean showFullElement() {
		return false;
	}
}
