package ru.johnlife.lifetoolsmp3.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.database.Cursor;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;

public class HelperService extends IntentService implements Constants {

	private ArrayList<RemoteSong> songs;

	public HelperService() {
		this("HelperService");
	}

	public HelperService(String name) {
		super(name);
	}
	
	@Override
	public void onCreate() {
		startForeground(7958453, new Notification());
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		songs = intent.getParcelableArrayListExtra(EXTRA_DATA);
		DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		int count = songs.size();
		while (count > 0) {
			for (int i = 0; i < count; i++) {
				RemoteSong song = songs.get(i);
				Cursor c = manager.query(new DownloadManager.Query().setFilterById(song.getId()));
				if (c.moveToFirst()) {
					int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
					if (DownloadManager.STATUS_SUCCESSFUL == status) {
						BaseDownloadSongTask.writeDownloadSong(song, this);
						songs.remove(song);
						count--;
					}
				}
			}
			try {
				Thread.sleep(3500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		stopSelf();
	}
	
}