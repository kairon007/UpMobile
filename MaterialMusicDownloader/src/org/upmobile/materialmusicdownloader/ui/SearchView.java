package org.upmobile.materialmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.materialmusicdownloader.Nulldroid_Settings;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.fragment.PlayerFragment;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.csform.android.uiapptemplate.font.MusicTextView;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

public class SearchView extends OnlineSearchView implements Constants {

	private PlaybackService service;
    
	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	protected void click(final View view, int position) {
		if (null == service) {
			service = PlaybackService.get(getContext());
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
		Bundle bundle = new Bundle();
		try {
			bundle.putParcelable(KEY_SELECTED_SONG, ((Song)getResultAdapter().getItem(position)).cloneSong());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		PlayerFragment playerFragment = new PlayerFragment();
		playerFragment.setArguments(bundle);
		((MainActivity) getContext()).showPlayerElement(true);
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

	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
	}
	
	@Override
	public int defaultCover() {
		return 0;
	}
	
	@Override
	public Bitmap getDeafultBitmapCover() {
		return ((MainActivity) getContext()).getDeafultBitmapCover(64, 64, 55);
	}
	
	@Override
	protected void animateListView() {
		AnimationAdapter animAdapter = new AlphaInAnimationAdapter(getResultAdapter());
		animAdapter.setAbsListView((DynamicListView)listView);
		((DynamicListView)listView).setAdapter(animAdapter);
	}
	
}
