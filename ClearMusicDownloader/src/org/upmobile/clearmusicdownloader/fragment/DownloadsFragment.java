package org.upmobile.clearmusicdownloader.fragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.DownloadsAdapter;
import org.upmobile.clearmusicdownloader.data.MusicData;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.BaseClearActivity;
import com.special.R;
import com.special.menu.ResideMenu;
import com.special.utils.UISwipableList;

public class DownloadsFragment extends Fragment {

	private View parentView;
	private UISwipableList listView;
	private DownloadsAdapter mAdapter;
	private ResideMenu resideMenu;
	private DownloadManager manager;
	private Timer timer;
	private Updater updater;
	private static final int DEFAULT_SONG = 7340032; // 7 Mb
	private MainActivity activity;
	private int progress;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_list_transition, container, false);
		listView = (UISwipableList) parentView.findViewById(R.id.listView);
		BaseClearActivity parentActivity = (BaseClearActivity) getActivity();
		resideMenu = parentActivity.getResideMenu();
		initView();
		return parentView;
	}

	private void initView() {
		activity = (MainActivity) getActivity();
		mAdapter = new DownloadsAdapter(getActivity(), org.upmobile.clearmusicdownloader.R.layout.downloads_item);
		listView.setActionLayout(R.id.hidden_view);
		listView.setItemLayout(R.id.front_layout);
		listView.setAdapter(mAdapter);
		listView.setIgnoredViewHandler(resideMenu);
		listView.getAdapter();
		manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
		timer = new Timer();
		updater = new Updater();
	}

	@Override
	public void onDestroy() {
		timer.cancel();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		checkDownloads();
		super.onResume();
	}

	private void checkDownloads() {
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
			e.printStackTrace();
		}
	}

	private void updateList(Cursor c) {
		while (c.moveToNext()) {
			MusicData song = new MusicData(c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)), c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)), c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)), 25252);
			if (null != c.getString(14) && c.getString(14).contains(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX)) {
				if (mAdapter.getCount() == 0) {
					addItem(song);
				}
				ArrayList<MusicData> chek = new ArrayList<MusicData>();
				for (int i = 0; i < mAdapter.getCount(); i++) {
					chek.add((MusicData) mAdapter.getItem(i));
				}
				if (!chek.contains(song)) {
					addItem(song);
				}
			}
		}
	}

	private void addItem(final MusicData song) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAdapter.add(song);
				mAdapter.notifyDataSetChanged();
			}
		});
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
					for (int i = 0; i < mAdapter.getCount(); i++) {
						if (((MusicData) mAdapter.getItem(i)).getId() == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID))) {
							((MusicData) mAdapter.getItem(i)).setProgress(progress);
						}
					}
				} catch (Exception e) {
					android.util.Log.d(getClass().getSimpleName(), e.getLocalizedMessage());
				}
			}
			c.close();
			checkCanceled();
			checkFinished();
			reDrawAdapter();
		}
		
		private void reDrawAdapter() {
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mAdapter.notifyDataSetChanged();
				}
			});
		}

		private void checkFinished() {
			Cursor c = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
			while (c.moveToNext()) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					if (((MusicData) mAdapter.getItem(i)).getId() == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID))) {
						removeItem(i);	
					}
				}
			}
			c.close();
		}

		private void checkCanceled() {
			Cursor c = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_FAILED));
			while (c.moveToNext()) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					if (((MusicData) mAdapter.getItem(i)).getId() == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID))) {
						removeItem(i);
					}
				}
			}
			c.close();
		}

		private void removeItem(final int position) {
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mAdapter.remove(mAdapter.getItem(position));
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}
}
