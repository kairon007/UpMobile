package org.kreed.musicdownloader.ui.tabs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import org.kreed.musicdownloader.Nulldroid_Advertisement;
import org.kreed.musicdownloader.Nulldroid_Settings;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.adapter.SearchAdapter;
import org.kreed.musicdownloader.app.MusicDownloaderApp;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.ui.activity.MainActivity;
import org.kreed.musicdownloader.ui.viewpager.ViewPagerAdapter;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseSearchView;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class SearchViewTab  extends BaseSearchView {
	
	private MainActivity activity;
	private ViewPagerAdapter parentAdapter;
	private String path;
	private BaseSearchAdapter adapter;

	public SearchViewTab(LayoutInflater inflater, ViewPagerAdapter parentAdapter, MainActivity activity) {
		super(inflater);
		this.parentAdapter = parentAdapter;
		this.activity = activity;
		if (null == adapter) {
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
	}
	
	@Override
	protected boolean showFullElement() { return false; }

	@Override
	protected void click(final View view, int position) {
		final RemoteSong song = (RemoteSong) getAdapter().getItem(position);
		final MusicData data = new MusicData();
		data.setSongArtist(song.getArtist());
		data.setSongTitle(song.getTitle());
		data.setSongDuration(Util.getFormatedStrDuration(song.getDuration()));
			if (MusicDownloaderApp.getService().containsPlayer()) {
				MusicDownloaderApp.getService().getPlayer().setNewName(data.getSongArtist(), data.getSongTitle(), true, getCoverFromList(view));
			}
			showProgressDialog(view, song, position);
			((MainActivity) activity).stopPlayer();
			song.getDownloadUrl(new DownloadUrlListener() {
				
				@Override
				public void success(String url) {
					path = url;
					SearchViewTab.this.alertProgressDialog.dismiss();
					StateKeeper.getInstance().closeDialog(StateKeeper.PROGRESS_DIALOG);
					startPlay(song, data, getCoverFromList(view));
				}
				
				@Override
				public void error(String error) {}
			});
	}
	
	private Bitmap getCoverFromList(View v) {
		Bitmap bitmap = ((BitmapDrawable) ((ImageView) v.findViewById(R.id.cover)).getDrawable()).getBitmap();
		return Util.resizeBitmap(bitmap, Util.dpToPx(getContext(), 64), Util.dpToPx(getContext(), 64));
	}
	
	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}
	
	@Override
	protected void hideView() {
		activity.onBackPressed();
	}

	@Override
	protected Nulldroid_Advertisement getAdvertisment() {
		try {
			return Nulldroid_Advertisement.class.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void stopSystemPlayer(Context context) {}
	
	private void startPlay(final RemoteSong song, final MusicData data, final Bitmap bitmap) {
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				data.setFileUri(path);
				ArrayList<String[]> headers = song.getHeaders();
				((MainActivity) activity).play(headers, data);
				((MainActivity) activity).setCoverToPlayer(bitmap);
			}
		});
	}

	@Override
	public BaseSearchAdapter getAdapter() {
		if (null == adapter) {
			new NullPointerException("Adapter must not be null");
			return adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
		return adapter;
	}

	@Override
	protected ListView getListView(View v) {
		return (ListView) v.findViewById(R.id.list);
	}
}