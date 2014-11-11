package ru.johnlife.lifetoolsmp3.ui;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.DownloadCache.DownloadCacheCallback;
import ru.johnlife.lifetoolsmp3.DownloadCache.Item;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.RefreshListener;
import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.task.DownloadGrooveshark;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DownloadClickListener implements View.OnClickListener, OnBitmapReadyListener {

	private ArrayList<String[]> headers = new ArrayList<String[]>();
	private Context context;
	private RemoteSong song;
	protected Bitmap cover;
	protected String songTitle;
	protected String songArtist;
	protected String url;
	protected String duration;
	protected Long currentDownloadId;
	public Integer songId;
	private long progress = 0;
	private boolean useAlbumCover = true;
	private RefreshListener listener;

	protected DownloadClickListener(Context context, RemoteSong song, RefreshListener listener) {
		this.context = context;
		this.song = song;
		this.listener = listener;
	}
	
	@SuppressLint("NewApi")
	public void downloadSong(boolean fromCallback) {
		songId = song instanceof GrooveSong ? ((GrooveSong) song).getSongId() : -1;
		songTitle = Util.removeSpecialCharacters(song.getTitle());
		songArtist = Util.removeSpecialCharacters(song.getArtist());
		duration = Util.getFormatedStrDuration(song.getDuration());
		headers = song.getHeaders();
		url = song.getUrl();
		if (url == null || "".equals(url)) {
			Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
			return;
		}
		File musicDir = new File(getDirectory());
		if (!musicDir.exists()) {
			musicDir.mkdirs();
		}
		SongArrayHolder.getInstance().setStreamDialogOpened(false, null, null);
		final int id = songArtist.hashCode() + songTitle.hashCode();
		boolean isCached = false;
		if (!fromCallback) {
			song.setDownloaderListener(notifyStartDownload(id));
			isCached = DownloadCache.getInstanse().put(songArtist, songTitle, new DownloadCacheCallback() {
				
				@Override
				public void callback(final Item item) {
					Runnable callbackRun = new Runnable() {
						
						@Override
						public void run() {
							downloadSong(true);			
						}
					};
					new Handler(Looper.getMainLooper()).post(callbackRun);
				}
			});
		}
		if (isCached)  {
			return;
		}
		StringBuilder stringBuilder = new StringBuilder(songArtist).append(" - ").append(songTitle).append(".mp3");
		String sb = Util.removeSpecialCharacters(stringBuilder.toString());
		if (songId != -1) {
			Log.d("GroovesharkClient", "Its GrooveSharkDownloader. SongID: " + songId);
			DownloadGrooveshark manager = new DownloadGrooveshark(songId, musicDir.getAbsolutePath(), sb, context);
			manager.execute();
		} else {
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && !isFullAction()){
				//new download task for device with below 11 
				String fileUri = musicDir.getAbsolutePath() + "/" + sb;
				DownloadSongTask task = new DownloadSongTask(song, useAlbumCover, url, fileUri, id);
				task.start();
				return;
			}
			final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				url = url.replaceFirst("https", "http");
			}
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url)).addRequestHeader("User-Agent",
					"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.3) Gecko/2008092814 (Debian-3.0.1-1)");
			if (headers != null && !headers.isEmpty()) {
				for (int i = 0; i < headers.size(); i++) {
					request.addRequestHeader(headers.get(i)[0], headers.get(i)[1]);
				}
			}
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(sb);
			try {
				request.setDestinationInExternalPublicDir(OnlineSearchView.getSimpleDownloadPath(musicDir.getAbsolutePath()), sb);
			} catch (Exception e) { 
				Log.e(getClass().getSimpleName(), e.getMessage());
				String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
				request.setDestinationInExternalPublicDir(dir, sb);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
			} 
			try {
				currentDownloadId = manager.enqueue(request);
			} catch (IllegalArgumentException e) {
				Toast toast = Toast.makeText(context, R.string.turn_on_dm, Toast.LENGTH_LONG);
				toast.show();
				return;
			}
			boolean isUpdated = continueDownload(id, currentDownloadId);
			if (!isUpdated) {
				song.setDownloaderListener(notifyStartDownload(currentDownloadId));
			}
			Toast.makeText(context, context.getString(R.string.download_started) +" "+sb, Toast.LENGTH_SHORT).show();
			UpdateTimerTask progressUpdateTask = new UpdateTimerTask(song, manager, useAlbumCover);
			new Timer().schedule(progressUpdateTask, 1000, 1000);
		}
	}

	@Override
	public void onClick(View v) {
		downloadSong(false);
	}

	protected void setFileUri(long downloadId, String uri) {}
	
	protected void notifyDuringDownload(final long downloadId, final long currentProgress) {}
 
	public CoverReadyListener notifyStartDownload(long downloadId) {
		return null;
	}
	
	protected void setCanceledListener(long downloadId, CanceledCallback callback) {}
	
	protected boolean continueDownload(long lastID, long newId) {
		return false;
	}

	protected void notifyAboutFailed(long downloadId) {
		((Activity)context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String failedSong = context.getResources().getString(R.string.download_failed);
				String title = song.getArtist() + " - " + song.getTitle();
				Toast.makeText(context, failedSong + " - " + title, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void setUseAlbumCover(boolean useAlbumCover) {
		this.useAlbumCover = useAlbumCover;
	}

	protected String getDirectory() {
		String downloadPath = BaseConstants.DOWNLOAD_DIR;
		if (context != null) {
			SharedPreferences downloadDetails = context.getSharedPreferences(OnlineSearchView.getDOWNLOAD_DETAIL(), Context.MODE_PRIVATE);
			String sharedDownloadPath = downloadDetails.getString(OnlineSearchView.getDOWNLOAD_DIR(), "");
			if (sharedDownloadPath.equals("")) {
				Editor edit = downloadDetails.edit();
				edit.clear();
				edit.putString(OnlineSearchView.getDOWNLOAD_DIR(), downloadPath);
				edit.commit();
			} else
				return sharedDownloadPath;
		}
		return downloadPath;
	}

	protected void prepare(final File src, RemoteSong song, String path) {}

	protected boolean isFullAction() {
		return true;
	}

	@Override
	public void onBitmapReady(Bitmap bmp) {
		this.cover = bmp;
	}
	
	public void setSong(RemoteSong song) {
		this.song = song;
	}

	private void notifyMediascanner(final RemoteSong song, final String pathToFile) {
		final File file = new File(pathToFile);
		MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri) {
				prepare(file, song, pathToFile);
				if (null != listener) {
					listener.success();
				}
			}
		});
	}
	
	private boolean setMetadataToFile(String path, File src, boolean useCover) {
		MusicMetadataSet src_set = null;
		try {
			src_set = new MyID3().read(src);
		} catch (Exception exception) {
			Log.d(getClass().getSimpleName(), "Unable to read music metadata from file. " + exception);
		}
		if (null == src_set) {
			notifyMediascanner(song, path);
			return false;
		}
		MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
		metadata.clearPictureList();
		metadata.setSongTitle(songTitle);
		metadata.setArtist(songArtist);
		if (null != cover && useCover) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
			cover.compress(CompressFormat.JPEG, 85, out);
			metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
		}
		File dst = new File(src.getParentFile(), src.getName() + "-1");
		boolean isRename = false;
		try {
			new MyID3().write(src, dst, src_set, metadata);
			isRename = dst.renameTo(src);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Unable to write music metadata from file. " + e);
		} finally {
			if (!isRename) {
				dst.delete();
			}
		}
		return true;
	}
	
	public interface CoverReadyListener {
		
		void onCoverReady(Bitmap cover);
	}
	
	public interface CanceledCallback {
			
		public void cancel();	
	}
	
	private final class UpdateTimerTask extends TimerTask {

		private static final int DEFAULT_SONG = 7340032; // 7 Mb
		private RemoteSong song;
		private File src;
		private DownloadManager manager;
		private boolean useCover;

		public UpdateTimerTask(RemoteSong song, DownloadManager manager, boolean useCover) {
			this.song = song;
			this.manager = manager;
			this.useCover = useCover;
		}

		@Override
		public void run() {
			Cursor c = manager.query(new DownloadManager.Query().setFilterById(currentDownloadId));
			if (c.moveToFirst()) {
				int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
				switch (status) {
				case DownloadManager.STATUS_FAILED:
					notifyAboutFailed(currentDownloadId);
					c.close();
					DownloadCache.getInstanse().remove(song.getArtist(), song.getTitle());
					this.cancel();
					return;
				case DownloadManager.STATUS_RUNNING:
					if (isFullAction()) {
						break;
					}
					int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
					int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
					int size = c.getInt(sizeIndex);
					int downloaded = c.getInt(downloadedIndex);
					if (size != -1) {
						progress = downloaded * 100 / size;
					} else {
						progress = downloaded * 100 / DEFAULT_SONG;
					}
					notifyDuringDownload(currentDownloadId, progress);
					break;
				case DownloadManager.STATUS_SUCCESSFUL:
					progress = 100;
					notifyDuringDownload(currentDownloadId, progress);
					int columnIndex = 0;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						columnIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
					} else if (columnIndex != -1) {
						columnIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
					}
					if (columnIndex == -1) return;
					String path = c.getString(columnIndex);
					c.close();
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
						path = cutPath(path);
						if (path.contains("%20")) {
							path.replaceAll("%20", " ");
						}
					}
					src = new File(path);
					if (!setMetadataToFile(path, src, useCover)) {
						setFileUri(currentDownloadId, src.getAbsolutePath());
						DownloadCache.getInstanse().remove(song.getArtist(), song.getTitle());
						this.cancel();
					}
					setFileUri(currentDownloadId, src.getAbsolutePath());
					DownloadCache.getInstanse().remove(songArtist, songTitle);
					notifyMediascanner(song, path);
					this.cancel();
					return;
				default:
					break;
				}
			}
			c.close();
		}

		private String cutPath(String s) {
			int index = s.indexOf('m');
			return s.substring(index - 1);
		}
	}
	
	private class DownloadSongTask extends Thread {

		private final String DOWNLOAD_ID_GINGERBREAD = "downloads_id_gingerbread";
		private final String DOWNLOAD_KEY_GINGERBREAD = "downloads_key_gingerbread";
		private NotificationManager notificationManager;
		private SharedPreferences sPref;
		private RemoteSong song;
		private String notificationTitle;
		private boolean useCover;
		private final int idNotification;
		private final int idDownload;
		private String url;
		private String filePath;
		private boolean interrupted = false;
		private CanceledCallback cancelDownload = new CanceledCallback() {
			
			@Override
			public void cancel() {
				interrupted = true;
			}
		};

		public DownloadSongTask(RemoteSong song, boolean useCover, String url, String filePath, int idDownload) {
			this.url = url;
			this.idDownload = idDownload;
			this.filePath = filePath;
			this.notificationTitle = Util.removeSpecialCharacters(song.artist) + " - " + Util.removeSpecialCharacters(song.title);
			this.song = song;
			this.useCover = useCover;
			sPref = context.getSharedPreferences(DOWNLOAD_ID_GINGERBREAD, context.MODE_PRIVATE);
			idNotification = sPref.getInt(DOWNLOAD_KEY_GINGERBREAD, 1);
			SharedPreferences.Editor editor = sPref.edit();
			int i = idNotification + 1;
			editor.putInt(DOWNLOAD_KEY_GINGERBREAD, i);
			editor.commit();
		}
		
		private void sendNotification(int progress, boolean isStop) {
			RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = null;
			if (!isStop) {
				notification = new Notification(android.R.drawable.stat_sys_download, notificationTitle, Calendar.getInstance().getTimeInMillis());
			} else if (progress < 100 && isStop && !interrupted){
				String message = context.getString(R.string.download_failed);
				DownloadCache.getInstanse().remove(song.getArtist(), song.getTitle());
				notification = new Notification(android.R.drawable.stat_notify_error, message, Calendar.getInstance().getTimeInMillis());
			} else if (progress == 0 && isStop && interrupted) {
				String message = context.getString(R.string.download_canceled);
				DownloadCache.getInstanse().remove(song.getArtist(), song.getTitle());
				notification = new Notification(android.R.drawable.stat_notify_error, message, Calendar.getInstance().getTimeInMillis());
			} else {
				String message = context.getString(R.string.download_finished);
				DownloadCache.getInstanse().remove(song.getArtist(), song.getTitle());
				notification = new Notification(android.R.drawable.stat_notify_error, message, Calendar.getInstance().getTimeInMillis());
			}
			Intent intent = new Intent();
			PendingIntent pend = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.contentIntent = pend;
			notification.contentView = notificationView;
			notification.contentView.setTextViewText(R.id.tv_notification, notificationTitle);
			notification.contentView.setTextViewText(R.id.tv_notification_progress, progress + " %");
			notification.contentView.setProgressBar(R.id.progres_notification, 100, progress, false);
			notificationManager.notify(idNotification, notification);
			if (isStop) {
				notificationManager.cancel(idNotification);
			}
		}

		@Override
		public void run() {
			File file = null;
			FileOutputStream output = null;
			InputStream input = null;
			BufferedInputStream buffer = null;
			URLConnection connection = null;
			try {
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				URL u = new URL(url);
				connection = u.openConnection();
			} catch (Exception e) {
				notifyAboutFailed(idDownload);
				return;
			}
			int size = connection.getContentLength();
			int index = 0;
			int current = 0;
			try {
				file = new File(filePath);
				file.createNewFile();
				output = new FileOutputStream(file);
				input = connection.getInputStream();
				buffer = new BufferedInputStream(input);
				byte[] bBuffer = new byte[10240];
				int i = 0;
				setCanceledListener(idDownload, cancelDownload);
				notifyDuringDownload(idDownload, 3);
				while ((current = buffer.read(bBuffer)) != -1) {
					if (interrupted) {
						output.close();
						input.close();
						buffer.close();
						sendNotification(0, true);
						return;
					}
					output.write(bBuffer, 0, current);
					index += current;
					int p = index * 100 / size;
					if (p % 5 == 0) {
						i++;
						if (i == 1) {
							sendNotification(p, false);
							notifyDuringDownload(idDownload, p);
						}
						if (p == 100) {
							notifyDuringDownload(idDownload, p);
						}
					} else {
						i = 0;
					}
				}
				if (!setMetadataToFile(file.getAbsolutePath(), file, useCover)) {
					notifyAboutFailed(idDownload);
					sendNotification(0, true);
					output.close();
					input.close();
					buffer.close();
					return;
				}
			} catch (Exception e) {
				notifyAboutFailed(idDownload);
				sendNotification(0, true);
				return;
			} finally {
				try {
					output.close();
					input.close();
					buffer.close();
				} catch (Exception e) {
					android.util.Log.d("log", "Appear problem: " + e);
				}
			}
			sendNotification(100, true);
			setFileUri(idDownload, file.getAbsolutePath());
			notifyMediascanner(song, file.getAbsolutePath());
			return;
		}
		
	}

}
