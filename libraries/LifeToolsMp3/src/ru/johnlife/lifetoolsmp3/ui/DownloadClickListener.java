package ru.johnlife.lifetoolsmp3.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.task.DownloadGrooveshark;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class DownloadClickListener implements View.OnClickListener, OnBitmapReadyListener {

	private final DateFormat isoDateFormat = new SimpleDateFormat("mm:ss", Locale.US);
	private ArrayList<String[]> headers = new ArrayList<>();
	private Context context;
	private RemoteSong song;
	private Bitmap cover;
	private String songTitle = "";
	private String songArtist = "";
	private String URL = "";
	private String duration = "";
	public Integer songId;
	private boolean waitingForCover = true;

	protected DownloadClickListener(Context context, RemoteSong song, Bitmap bitmap) {
		this.context = context;
		this.song = song;
		this.cover = bitmap != null ? bitmap : null;
		this.songId = song instanceof GrooveSong ? ((GrooveSong) song).getSongId() : -1;
		songTitle = song.getTitle();
		songArtist = song.getArtist();
		URL = song.getDownloadUrl();
		duration = formatTime(song.getDuration());
		headers = song.getHeaders();
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		if (URL == null || URL.equals("")) {
			Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
			return;
		}
		File musicDir = new File(BaseConstants.DOWNLOAD_DIR);
		if (!musicDir.exists()) {
			musicDir.mkdirs();
		}
		StringBuilder sb = new StringBuilder(songArtist).append(" - ").append(songTitle);
		if (songId != -1) {
			Log.d("GroovesharkClient", "Its GrooveSharkDownloader. SongID: " + songId);
			DownloadGrooveshark manager = new DownloadGrooveshark(songId, OnlineSearchView.getDownloadPath(context), sb.append(".mp3").toString(), context);
			manager.execute();
		} else {
			final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL));
			final String fileName = sb.toString();
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(fileName);
			request.setDestinationInExternalPublicDir(OnlineSearchView.getSimpleDownloadPath(OnlineSearchView.getDownloadPath(context)), sb.append(".mp3").toString());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
			}
			final long downloadId = manager.enqueue(request);
			Toast.makeText(context, String.format(context.getString(R.string.download_started), fileName), Toast.LENGTH_SHORT).show();
			final TimerTask progresUpdateTask = new TimerTask() {

				private File src;

				@Override
				public void run() {
					if (waitingForCover)
						return;
					Cursor c = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
					if (c == null || !c.moveToFirst())
						return;
					int columnIndex = 0;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						columnIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
					} else if (columnIndex != -1) {
						columnIndex = c.getColumnIndex("local_uri");
					}
					if (columnIndex == -1)
						return;
					String path = c.getString(columnIndex);
					c.close();
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
						path = cutPath(path);
					}
					src = new File(path);
					song.path = path;
					MusicMetadataSet src_set = null;
					try {
						src_set = new MyID3().read(src);
					} catch (Exception exception) {
						android.util.Log.d("log", "don't read music metadata from file. " + exception);
					}
					MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
					metadata.clearPictureList();
					metadata.setSongTitle(songTitle + '/' + duration);
					metadata.setArtist(songArtist);
					if (null != cover) {
						ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
						cover.compress(CompressFormat.JPEG, 85, out);
						metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
					}
					File dst = new File(src.getParentFile(), src.getName() + "-1");
					try {
						new MyID3().write(src, dst, src_set, metadata);
					} catch (Exception e) {
						android.util.Log.d("log", "don't write music metadata from file. " + e);
					} finally {
						if(dst.delete()){
							android.util.Log.d("log", "1, delete" );
						} else {
							android.util.Log.d("log", "1, don't delete");
						}
					}
					notifyMediascanner(song, path);
				}

				private String cutPath(String s) {
					int index = s.indexOf('m');
					return s.substring(index - 1);
				}

				private void notifyMediascanner(final RemoteSong song, String path) {
					final File file = new File(path);
					MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

						public void onScanCompleted(String path, Uri uri) {
							prepare(file, song);
						}

					});
				}
			};
			new Timer().schedule(progresUpdateTask, 1000, 1000);

		}
	}
	
	
	
	protected void prepare(final File src, RemoteSong song) {
		final String title = song.getTitle();
		new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... params) {
				MusicMetadataSet src_set = null;
				try {
					src_set = new MyID3().read(src);
				} catch (Exception exception) {
					android.util.Log.d("log", "don't read music metadata from file. " + exception);
				}
				MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
				metadata.setSongTitle(title);
				File dst = new File(src.getParentFile(), src.getName() + "-1");
				try {
					new MyID3().write(src, dst, src_set, metadata);
				} catch (Exception e) {
					android.util.Log.d("log", "don't write music metadata from file. " + e);
				} finally {
					if(dst.delete()){
						android.util.Log.d("log", "2, delete" );
					} else {
						android.util.Log.d("log", "2, don't delete");
					}
				}
				return null;
			}
			
		}.execute();
	}

	private final String formatTime(long date) {
		return isoDateFormat.format(new Date(date));
	}

	@Override
	public void onBitmapReady(Bitmap bmp) {
		this.cover = bmp;
		this.waitingForCover = false;
	}

}