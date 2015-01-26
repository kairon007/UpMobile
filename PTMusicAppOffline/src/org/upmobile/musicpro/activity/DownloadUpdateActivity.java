package org.upmobile.musicpro.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.upmobile.musicpro.R;
import org.upmobile.musicpro.util.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

public class DownloadUpdateActivity extends Activity {
	private final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private ProgressDialog mProgressDialog;
	private String urlNewVersion;
	private String fileName;

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		urlNewVersion = extras.getString("url_song");
		String file = extras.getString("file_name");
		String rootFolder = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/";

		File folder = new File(rootFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		fileName = rootFolder + file;
		Logger.e(fileName);
		Logger.e(urlNewVersion);

		new DownloadFileAsync().execute(urlNewVersion);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(getString(R.string.downloading));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {
		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;

			try {
				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				Logger.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(fileName);

				byte data[] = new byte[1024];
				long total = 0;
				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
			}
			return null;
		}

		protected void onProgressUpdate(String... progress) {
			Logger.d("ANDRO_ASYNC", progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String unused) {
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			Toast.makeText(getBaseContext(), R.string.downloadComplete, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

}