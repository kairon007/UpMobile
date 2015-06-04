package org.upmobile.clearmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.clearmusicdownloader.Nulldroid_Settings;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.app.ClearMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

public class SearchView extends OnlineSearchView implements PlaybackService.OnErrorListener {

	private PlaybackService service;
	private ImageView baseProgress;
	private ImageView refreshProgress;
    
	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public View getView() {
		View v = super.getView();
		listView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		return v;
	}
	
	@Override
	public boolean isUseDefaultSpinner() {
		return true;
	}
	
	@Override
	protected void click(final View view, int position) {
		if (null == service) {
			service = PlaybackService.get(getContext());
			service.setOnErrorListener(this);
		}
		if (!service.isCorrectlyState(Song.class, getResultAdapter().getCount())) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>();
			for (AbstractSong abstractSong : getResultAdapter().getAll()) {
				try {
					list.add(abstractSong.cloneSong());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			service.setArrayPlayback(list);
		} 
		((MainActivity) getContext()).showPlayerElement();
		try {
			((MainActivity) getContext()).startSong(((Song)getResultAdapter().getItem(position)).cloneSong());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
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
	
	@Override
	protected String getDirectory() {
		return ClearMusicDownloaderApp.getDirectory();
	}

	@Override
	public void error(final String error) {
		((MainActivity) getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getContext(), R.string.error_getting_url_songs, Toast.LENGTH_SHORT).show();
				service.stopPressed();
			}
		});
	}
	
	@Override
	protected int getIdCustomView() {
		return R.layout.row_online_search;
	}
	
	@Override
	protected boolean showDownloadLabel() {
		return true;
	}
}
