package org.upmobile.newmusicdownloader.fragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.adapter.DownloadsAdapter;
import org.upmobile.newmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.DownloadCache.DownloadCacheCallback;
import ru.johnlife.lifetoolsmp3.DownloadCache.Item;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class DownloadsFragment extends Fragment implements OnScrollListener {

	private DownloadsAdapter adapter;
	private DownloadManager manager;
	private Timer timer;
	private Updater updater;
	private static final int DEFAULT_SONG = 7340032; // 7 Mb
	private MainActivity activity;
	private int progress;
	private Object lock  = new Object();
	private Animation anim;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		initView();
		return new View(activity);
	}

	private void initView() {
		activity = (MainActivity) getActivity();
		adapter = new DownloadsAdapter(getActivity(), org.upmobile.sevenplayer.R.layout.downloads_item);
		manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
		timer = new Timer();
		updater = new Updater();
	}
	
	@Override
	public void onPause() {
		timer.cancel();
		super.onPause();
	}

	@Override
	public void onResume() {
		checkDownloads();
		super.onResume();
	}

	private void checkDownloads() {
		synchronized (lock) {
			try {
				if (null != manager) {
					Cursor running = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING));
					if (running!=null) {
						updateList(running);
						running.close();
					}
					Cursor pending = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING));
					if (pending!=null) {
						updateList(pending);
						pending.close();
					}
				}
				timer.schedule(updater, 100, 1000);
			} catch (Exception e) {
			}
		}
	}

	private void updateList(Cursor c) {
		synchronized (lock) {
			while (c.moveToNext()) {
				MusicData song = new MusicData(c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).trim(), c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)).trim(), c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)), 25252);
				if (c.getString(8).contains(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX)) {
					if (!adapter.contains(song)) {
						addItem(song);
					}
				}
			}
		}
	}

	private void addItem(final MusicData song) {
		synchronized (lock) {
			try {
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
					 adapter.insert(song, 0);
					 adapter.notifyDataSetChanged();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void addAllCached(final ArrayList<Item> cache) {
		synchronized (lock) {
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					for (Item item : cache) {
						final MusicData song = new MusicData(item.getTitle(), item.getArtist(), item.getId(), -1);
						if (!adapter.contains(song)) {
							adapter.add(song);
							item.setCustomCallback(new DownloadCacheCallback() {
								
								@Override
								public void callback(Item item) {
									removeItem(song);
								}
							});
						}
					}
				}
			});
		}
	}
	
	private void reDrawAdapter() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	private void checkFinished() {
		Cursor c = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
		while (c.moveToNext()) {
			for (int i = 0; i < adapter.getCount(); i++) {
				if (((MusicData) adapter.getItem(i)).getId() == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID))) {
					removeItem(new MusicData(c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).trim(), c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)).trim(), c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)), 25252));	
					break;
				}
			}
		}
		c.close();
	}

	private void checkCanceled() {
		Cursor c = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_FAILED));
		while (c.moveToNext()) {
			for (int i = 0; i < adapter.getCount(); i++) {
				if (((MusicData) adapter.getItem(i)).getId() == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID))) {
					removeItem(new MusicData(c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).trim(), c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)).trim(), c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)), 25252));
					break;
				}
			}
		}
		c.close();
	}

	private void removeItem(final MusicData musicData) {
		synchronized (lock) {
			try {
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						DownloadCache.getInstanse().remove(musicData.getArtist(), musicData.getTitle());
						adapter.remove(musicData);
						adapter.notifyDataSetChanged();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class Updater extends TimerTask {

		@Override
		public void run() {
			checkDownloads();
			Cursor c = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING));
			while (c.moveToNext()) {
				progress = 0;
				int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
				int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
				int size = c.getInt(sizeIndex);
				int downloaded = c.getInt(downloadedIndex);
				if (size != -1) {
					progress = downloaded * 100 / size;
				} else {
					progress = downloaded * 100 / DEFAULT_SONG;
				}
				try {
					for (int i = 0; i < adapter.getCount(); i++) {
						if (((MusicData) adapter.getItem(i)).getId() == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID))) {
							((MusicData) adapter.getItem(i)).setProgress(progress);
						}
					}
				} catch (Exception e) {
					android.util.Log.d(getClass().getSimpleName(), e + "");
				}
			}
			c.close();
			checkCanceled();
			checkFinished();
			reDrawAdapter();
			addAllCached(DownloadCache.getInstanse().getCachedItems());
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView paramAbsListView, int paramInt) {
		for (final MusicData item : adapter.getAll()) {
			if (item.check(MusicData.MODE_VISIBLITY)) {
				anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
				anim.setDuration(200);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation paramAnimation) {
					}

					@Override
					public void onAnimationRepeat(Animation paramAnimation) {
					}

					@Override
					public void onAnimationEnd(Animation paramAnimation) {
						adapter.cancelTimer();
						adapter.removeItem(item);
					}
				});
			}
		}
	}

	@Override
	public void onScroll(AbsListView paramAbsListView, int paramInt1, int paramInt2, int paramInt3) {
	}
}
