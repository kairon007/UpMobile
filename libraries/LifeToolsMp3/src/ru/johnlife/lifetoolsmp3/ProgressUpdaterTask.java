package ru.johnlife.lifetoolsmp3;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

public class ProgressUpdaterTask extends AsyncTask<Long, Integer, String> {

	private ProgressUpdaterListener listener;
	private static final String FAILURE = "failure";
	private final int DEFAULT_SONG = 7340032; // 7 Mb
	private final int BAD_SONG = 2048; // 200kb
	private Context context;

	public ProgressUpdaterTask(ProgressUpdaterListener listener, Context context) {
		this.listener = listener;
		this.context = context;
	}

	@Override
	protected String doInBackground(Long... params) {
		if (null == context) return null;
		DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		int progress = 0;
		do {
			if (isCancelled())
				return null;
			if (params[0] != -1) {
				Cursor c = manager.query(new DownloadManager.Query().setFilterById(params[0]));
				if (null == c) return null;
				if (c.moveToNext()) {
					int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
					int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
					int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
					int size = c.getInt(sizeIndex);
					int downloaded = c.getInt(downloadedIndex);
					switch (status) {
					case DownloadManager.STATUS_FAILED:
					case DownloadManager.ERROR_CANNOT_RESUME:
					case DownloadManager.ERROR_FILE_ERROR:
					case DownloadManager.ERROR_HTTP_DATA_ERROR:
					case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
					case DownloadManager.ERROR_UNKNOWN:
						return FAILURE;
					case DownloadManager.STATUS_RUNNING:
						if (size != -1 && size != 0) {
							progress = downloaded * 100 / size;
						} else {
							progress = downloaded * 100 / DEFAULT_SONG;
						}
						publishProgress(progress);
						break;
					case DownloadManager.STATUS_SUCCESSFUL:
						File file = new File(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
						if (downloaded < BAD_SONG || downloaded != size) {
							file.delete();
							return FAILURE;
						}
						progress = 100;
						publishProgress(100);
						break;
					}
				}
				c.close();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (progress < 100);
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		listener.onProgressUpdate(values);
		super.onProgressUpdate(values);
	}

	@Override
	protected void onCancelled() {
		listener.onCancelled();
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		listener.onPostExecute(result);
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		listener.onPreExecute();
		super.onPreExecute();
	}

	public static interface ProgressUpdaterListener {

		public void onProgressUpdate(Integer... values);

		public void onCancelled();

		public void onPostExecute(String params);

		public void onPreExecute();
		
		public boolean canceled = false;

	}

}
