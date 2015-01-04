package org.upmobile.clearmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.Nulldroid_Settings;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;

public class SearchView extends OnlineSearchView {

	private PlayerService service;
	private ImageView baseProgress;
	private ImageView refreshProgress;
    private String PACKAGE = "IDENTIFY";
    
	public SearchView(LayoutInflater inflater) {
		super(inflater);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				service = PlayerService.get(getContext());
			}
		}).start();
	}

	@Override
	public View getView() {
		View v = super.getView();
		listView.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_OVERLAY);
		return v;
	}
	
	@Override
	protected void click(final View view, int position) {
		if (!service.isCorrectlyState(Song.class, getResultAdapter().getCount())) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getResultAdapter().getAll());
			service.setArrayPlayback(list);
		} 
		Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_SELECTED_SONG, getResultAdapter().getItem(position));
		bundle.putInt(Constants.KEY_SELECTED_POSITION, position);
        int[] screen_location = new int[2];
        View v = view.findViewById(R.id.cover);
        v.getLocationOnScreen(screen_location);
        bundle.putInt(PACKAGE + ".left", screen_location[0]);
        bundle.putInt(PACKAGE + ".top", screen_location[1]);
        bundle.putInt(PACKAGE + ".width", v.getWidth());
        bundle.putInt(PACKAGE + ".height", v.getHeight());
		PlayerFragment playerFragment = new PlayerFragment();
		playerFragment.setArguments(bundle);
		((MainActivity) view.getContext()).changeFragment(playerFragment);
		((MainActivity) getContext()).overridePendingTransition(0, 0);
		super.click(view, position);
	}

	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Nulldroid_Advertisement getAdvertisment() {
		return new Nulldroid_Advertisement();
	}

	@Override
	protected void stopSystemPlayer(Context context) {

	}

	@Override
	public void refreshLibrary() {

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
		baseProgress = (ImageView) v.findViewById(R.id.loader_image);
	}

	@Override
	public void showBaseProgress() {
		showAnimation(baseProgress);
	}

	@Override
	public void hideBaseProgress() {
		hideAnimation(baseProgress);
	}

	@Override
	public Object initRefreshProgress() {
		refreshProgress = new ImageView(getContext());
		refreshProgress.setImageResource(com.special.R.drawable.loader);
		return refreshProgress;
	}

	@Override
	public void showRefreshProgress() {
		showAnimation(refreshProgress);
	}

	@Override
	public void hideRefreshProgress() {
		hideAnimation(refreshProgress);
	}

	private void hideAnimation(ImageView image) {
		image.clearAnimation();
		image.setVisibility(View.GONE);
	}

	private void showAnimation(ImageView image) {
		image.setVisibility(View.VISIBLE);
		image.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
	}

	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
	}
	
	@Override
	public int defaultCover() {
		return R.drawable.def_cover_circle;
	}
}
