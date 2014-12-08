package org.upmobile.clearmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Advertisement;
import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.Settings;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class SearchView extends OnlineSearchView {

	private ImageView load;

	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	protected void click(final View view, int position) {
		ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getResultAdapter().getAll());
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.KEY_SELECTED_POSITION, position);
		bundle.putParcelableArrayList(Constants.KEY_SELECTED_SONG, list);
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
	
	@Override
	protected boolean showDownloadButton() {
		return false;
	}
	
	@Override
	public void specialInit(View v) {
		load = (ImageView) v.findViewById(R.id.loader_image);
	}
	
	@Override
	public void showBaseProgress() {
		load.setVisibility(View.VISIBLE);
		load.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
	}
	
	@Override
	public void hideBaseProgress() {
		load.clearAnimation();
		load.setVisibility(View.GONE);
	}
}
