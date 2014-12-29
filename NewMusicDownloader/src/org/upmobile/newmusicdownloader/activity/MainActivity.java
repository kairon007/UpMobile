package org.upmobile.newmusicdownloader.activity;

import java.io.File;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.service.PlayerService;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.view.Window;

public class MainActivity extends Activity {

	private final String folderPath = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;

	private FileObserver fileObserver = new FileObserver(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX) {

		@Override
		public void onEvent(int event, String path) {
			if (event == FileObserver.DELETE_SELF) {
				File file = new File(folderPath);
				file.mkdirs();
				getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + " LIKE '" + folderPath + "%'", null);
				getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				PlayerService.get(MainActivity.this);
			}
			
		}).start();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		File file = new File(folderPath);
		if (!file.exists()) file.mkdirs();
		fileObserver.startWatching();
		super.onCreate(savedInstanceState);
	}

}