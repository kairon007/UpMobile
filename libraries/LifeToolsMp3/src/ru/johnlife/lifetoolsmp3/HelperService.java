package ru.johnlife.lifetoolsmp3;

import java.util.ArrayList;
import java.util.Collection;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.database.Cursor;

public class HelperService extends IntentService implements Constants {

	private ArrayList<RemoteSong> songs;

	public HelperService() {
		this("HelperService");
	}

	public HelperService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		startForeground(7958, new Notification());
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
						DownloadClickListener.writeDownloadSong(song, this);
						songs.remove(song);
						count--;
					}
				}
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				android.util.Log.d("logks", "EXEPTION - " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		stopSelf();
	}
	
}
