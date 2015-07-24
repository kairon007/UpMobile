package ru.johnlife.lifetoolsmp3.ui.baseviews;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.utils.DownloadCache;
import ru.johnlife.lifetoolsmp3.utils.DownloadCache.Item;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;

public abstract class BaseDownloadsView extends View{

	private ListView listView;
	private ViewGroup view;
	private BaseAbstractAdapter<MusicData> adapter;
	private DownloadManager manager;
	private Timer timer;
	private Updater updater;
	private static final int DEFAULT_SONG = 7340032; // 7 Mb
    private final Object lock  = new Object();
	private TextView messageView;

	protected abstract String getDirectory();
	
	protected abstract int getLayoutId();
	
	protected abstract BaseAbstractAdapter<MusicData> getAdapter();
	
	protected abstract ListView getListView(View view);
	
	protected abstract TextView getMessageView(View view);
	
	public BaseDownloadsView(LayoutInflater inflater) {
		super(inflater.getContext());
		init(inflater);
		listView.setAdapter(adapter);
		animateListView(adapter, listView);
	}
	
	public View getView() {
		return view;
	}
	
	private void init(LayoutInflater inflater) {
		view = (ViewGroup) inflater.inflate(getLayoutId(), null);
		listView = getListView(view);
		messageView = getMessageView(view);
		adapter = getAdapter();
		manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		timer = new Timer();
		updater = new Updater();
	}
	
	public void onPause() {
		timer.cancel();
	}

	public void onResume() {
		try {
			timer.schedule(updater, 100, 1000);
		} catch (Exception e) {
			android.util.Log.d(getClass().getName(), " appear problem: " + e);
			timer = new Timer();
			updater = new Updater();
			timer.schedule(updater, 100, 1000);
		}
	}
	
	private ArrayList<MusicData> checkDownloads() {
		ArrayList<MusicData> list = new ArrayList<>();
		synchronized (lock) {
			try {
				if (null != manager) {
					Cursor pending = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING));
					if (pending!=null) {
						updateList(pending, list);
					}
					Cursor paused = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PAUSED));
					if (paused != null) {
						updateList(paused, list);
					}
					Cursor waitingNetwork = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.PAUSED_WAITING_FOR_NETWORK));
					if (waitingNetwork != null) {
						updateList(waitingNetwork, list);
					}
					Cursor unknown = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.PAUSED_UNKNOWN));
					if (unknown != null) {
						updateList(unknown, list);
					}
					Cursor running = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING));
					if (running != null) {
						updateList(running, list);
					}
				}
			} catch (Exception ignored) {
			}
		}
		return sortList(list);
	}
	
	private ArrayList<MusicData> sortList (ArrayList<MusicData> arrayList) {
		Collections.sort(arrayList);
		return arrayList;
	}

	private ArrayList<MusicData> updateList(Cursor c, ArrayList<MusicData> result) {
		while (c.moveToNext()) {
			MusicData song = new MusicData(c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).trim(), 
													   c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)).trim(), 
													   c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)), 
													   25252, 
													   c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
			String comment =  c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
			song.setComment(comment);
			if (c.getString(8).contains(getDirectory())) {
				if (!result.contains(song)){
					result.add(song);
				}
			}
		}
		ArrayList<DownloadCache.Item> list = DownloadCache.getInstanse().getCachedItems();
		for (Item item : list) {
			MusicData song = new MusicData(item.getTitle(), item.getArtist(), item.getId(), -1, "");
			if (item.isCached() && !result.contains(song)) {
				result.add(song);
			}
		}
		c.close();
		return result;
	}

	private void reDrawAdapter() {
		new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				adapter.notifyDataSetChanged();
				if (adapter.isEmpty()) {
					if (null == messageView) return;
					messageView.setVisibility(View.VISIBLE);
					messageView.setText(getContext().getString(R.string.downloads_empty));
				} else {
					if (null == messageView) return;
					messageView.setVisibility(View.GONE);
					messageView.setText("");
				}
			}
		});
	}

	private void checkFinished() {
		Cursor c = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
		while (c.moveToNext()) {
			for (int i = 0; i < adapter.getCount(); i++) {
				if (((MusicData) adapter.getItem(i)).getId() == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID))) {
					removeItem(new MusicData(c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).trim(), 
														 c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)).trim(), 
														 c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)), 
														 25252, 
														 c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))));
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
					removeItem(new MusicData(c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).trim(), 
														 c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)).trim(), 
														 c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)), 
														 25252, 
														 c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))));
					break;
				}
			}
		}
		c.close();
	}

	private void removeItem(final MusicData musicData) {
		synchronized (lock) {
			try {
				((Activity) getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						DownloadCache.getInstanse().remove(musicData);
						StateKeeper.getInstance().removeSongInfo(musicData.getComment());
						adapter.remove(musicData);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void animateListView(BaseAbstractAdapter<MusicData> adapter, ListView listView) {
		//Animate listview in childs, if need
	}
	
	private class Updater extends TimerTask {

		@Override
		public void run() {
			final ArrayList<MusicData> list = checkDownloads();
			Cursor c = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING));
			while (c.moveToNext()) {
                int progress = 0;
				int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
				int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
				int size = c.getInt(sizeIndex);
				int downloaded = c.getInt(downloadedIndex);
				if (size != -1 && size != 0) {
					progress = downloaded * 100 / size;
				} else {
					progress = downloaded * 100 / DEFAULT_SONG;
				}
				try {
					for (int i = 0; i < adapter.getCount(); i++) {
						if ((list.get(i)).getId() == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID))) {
							(list.get(i)).setProgress(progress);
						}
					}
				} catch (Exception e) {
					android.util.Log.d(getClass().getSimpleName(), e + "");
				}
			}
			c.close();
			((Activity)getContext()).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					adapter.setDoNotifyData(false);
					adapter.changeData(list);
				}
			});
			checkCanceled();
			checkFinished();
			reDrawAdapter();
		}
	}
}
