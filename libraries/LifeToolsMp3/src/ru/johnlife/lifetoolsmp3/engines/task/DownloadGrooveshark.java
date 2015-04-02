package ru.johnlife.lifetoolsmp3.engines.task;

import java.io.File;
import java.io.FileOutputStream;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.engines.SearchGrooveshark;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener.InfoListener;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;

import com.scilor.grooveshark.API.Base.GroovesharkAudioStream;

public class DownloadGrooveshark extends AsyncTask<Void, Void, Integer> {
	private Integer songId;
	private File outputFile;
	private static int ID = 1;
	private NotificationManager notifyManager;
	private Notification.Builder builder;
	private InfoListener infoListener;
	private Context context;

	@SuppressLint("NewApi")
	public DownloadGrooveshark(int songId, String dir, String fileName, Context context, InfoListener infoListener) {
		this.context = context;
		this.infoListener = infoListener;
		File musicDir = new File(dir);
		if (!musicDir.exists()) {
			musicDir.mkdirs();
		}
		this.outputFile = new File(musicDir, fileName);
		this.songId = songId;
		this.notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.builder = new Notification.Builder(context);
		builder.setContentTitle("Download " + fileName).setContentText("Download in progress").setSmallIcon(R.drawable.icon).build();

	}

	@SuppressLint("NewApi")
	@Override
	protected Integer doInBackground(Void... params) {
		try {
			GroovesharkAudioStream stream = SearchGrooveshark.getClient().GetMusicStream(songId);
			FileOutputStream writer = new FileOutputStream(outputFile);
			int readBytes = 0;
			int pos = 0;
			int percentage = 0;
			int prevPercentage = 0;
			do {
				byte[] buffer = new byte[4096];
				readBytes = stream.Stream().read(buffer);
				pos += readBytes;
				if (readBytes > 0) {
					writer.write(buffer, 0, readBytes);
				}
				percentage = 100 * pos / (stream.Length() - 1);
				if (percentage > prevPercentage + 1) {
					builder.setProgress(100, percentage, false);
					notifyManager.notify(ID, builder.build());
					prevPercentage = percentage;
				}
			} while (readBytes > 0);
			stream.MarkSongAsDownloaded();

			writer.flush();
			writer.close();
			stream.Stream().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@SuppressLint("NewApi")
	@Override
	protected void onPostExecute(Integer result) {
		builder.setContentText(context.getText(R.string.download_finished)).setProgress(0, 0, false);
		notifyManager.notify(ID, builder.build());
		if (null != infoListener) {
			infoListener.success("");
		}
		super.onPostExecute(result);
	}
}
