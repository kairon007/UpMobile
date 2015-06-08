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
import org.jaudiotagger.audio.AudioFileIO;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.DownloadCache.DownloadCacheCallback;
import ru.johnlife.lifetoolsmp3.DownloadCache.Item;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.RenameTask;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.task.DownloadGrooveshark;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DownloadClickListener implements View.OnClickListener, OnBitmapReadyListener {

	private ArrayList<String[]> headers = new ArrayList<String[]>();

	private Context context;
	private InfoListener infolistener;

	private DownloadCache.Item cacheItem;
	protected RemoteSong downloadingSong;
	protected Bitmap cover;

	protected long currentDownloadId = 0;
	private int songId;
	private String downloadPath = null;

	private int id;
	private long progress = 0;
	private boolean useAlbumCover = true;
	protected boolean interrupted = false;

	private CanceledCallback cancelDownload = new CanceledCallback() {
		
		@Override
		public void cancel() {
			interrupted = true;
		}
		
	};

	private boolean earlierMsg = false;
	
	public interface CoverReadyListener {
		void onCoverReady(Bitmap cover);
	}

	public interface CanceledCallback {
		public void cancel();
	}
	
	public interface InfoListener {
		public void success(String str);
		public void erorr(String str);
	}
	
	private DownloadClickListener() {
	}

	public DownloadClickListener(final Context context, RemoteSong song, int id) {
		this.context = context;
		downloadingSong = song;
		this.id = id;
		songId = downloadingSong instanceof GrooveSong ? ((GrooveSong) downloadingSong).getSongId() : -1;
		headers = downloadingSong.getHeaders();
		downloadingSong.getCover(this);
	}
	
	public DownloadClickListener(final Context context, RemoteSong song, int id, boolean earlierMessage) {
		this.context = context;
		downloadingSong = song;
		this.id = id;
		earlierMsg = earlierMessage;
		songId = downloadingSong instanceof GrooveSong ? ((GrooveSong) downloadingSong).getSongId() : -1;
		headers = downloadingSong.getHeaders();
		downloadingSong.getCover(this);
		StringBuilder stringBuilder = new StringBuilder(song.getArtist()).append(" - ").append(song.getTitle());
		final String sb = Util.removeSpecialCharacters(stringBuilder.toString());
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showMessage(context, context.getString(R.string.download_started) + " " + sb);
			}
			
		});
	}
	
	public static void writeDownloadSong (RemoteSong song, Context context) {
		DownloadClickListener listener = new DownloadClickListener();
		String path =  song.getPath();
		listener.context = context;
		File src = new File(path);
		if (listener.setMetadataToFile(src, false, song)) {
			listener.insertToMediaStore(song, path);
		}
	}

	public void createUpdater(DownloadManager manager, long id) {
		currentDownloadId = id;
		UpdateTimerTask progressUpdateTask = new UpdateTimerTask(downloadingSong, manager, useAlbumCover, null);
		new Timer().schedule(progressUpdateTask, 2000, 3000);
	}

	// if change title or artist of song, then this method call before
	// downloadSong()
	public void setSong(RemoteSong song) {
		downloadingSong = song;
	}

	@SuppressLint("NewApi")
	public void downloadSong(boolean fromCallback) {
		String url = downloadingSong.getUrl();
		if (url == null || url.isEmpty()) {
			showMessage(context, R.string.download_error);
			return;
		}
		StateKeeper.getInstance().putSongInfo(downloadingSong.getComment(), AbstractSong.EMPTY_PATH, StateKeeper.DOWNLOADING);
		String songArtist = downloadingSong.getArtist().trim();
		String songTitle = downloadingSong.getTitle().trim();
		final File musicDir = new File(downloadPath == null ? getDirectory() : downloadPath);
		if (!musicDir.exists()) {
			musicDir.mkdirs();
		}
		boolean isCached = false;
		if (!fromCallback) {
			isCached = DownloadCache.getInstanse().put(songArtist, songTitle, downloadingSong.getComment(), new DownloadCacheCallback() {

				@Override
				public void callback(Item item) {
					cacheItem = item;
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
		setCanceledListener(id, cancelDownload);
		if (isCached) {
			showMessage(context, R.string.download_cached);
			return;
		}
		String format = (url.indexOf(".m4a?") > -1) ? ".m4a" : ".mp3";
		StringBuilder stringBuilder = new StringBuilder(songArtist).append(" - ").append(songTitle).append(format);
		final String sb = Util.removeSpecialCharacters(stringBuilder.toString());
		if (songId != -1) {
			DownloadGrooveshark manager = new DownloadGrooveshark(songId, musicDir.getAbsolutePath(), sb, context, new InfoListener() {

				@Override
				public void success(String str) {
					setMetadataToFile(new File(musicDir.getAbsolutePath() + "/" + sb), useAlbumCover, downloadingSong);
					insertToMediaStore(downloadingSong, musicDir.getAbsolutePath() + "/" + sb);
					showMessage(context, R.string.download_finished);
				}

				@Override
				public void erorr(String str) {
				}
			});
			manager.execute();
			return;
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && !isFullAction()) {
			// new download task for device with below 11
			String fileUri = musicDir.getAbsolutePath() + "/" + sb;
			DownloadSongTask task = new DownloadSongTask(downloadingSong, useAlbumCover, url, fileUri, id);
			task.start();
			return;
		}
		final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			url = url.replaceFirst("https", "http");
		}
		Uri uri = Uri.parse(url);
		Request request = new Request(uri).addRequestHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.3) Gecko/2008092814 (Debian-3.0.1-1)");
		if (headers != null && !headers.isEmpty()) {
			for (int i = 0; i < headers.size(); i++) {
				request.addRequestHeader(headers.get(i)[0], headers.get(i)[1]);
			}
		}
		request.setVisibleInDownloadsUi(true);
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(sb);
		try {
			request.setTitle(songArtist);
			request.setDestinationInExternalPublicDir(OnlineSearchView.getSimpleDownloadPath(musicDir.getAbsolutePath()), sb);
			request.setMimeType(downloadingSong.getComment());
			request.setDescription(songTitle);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
			showMessage(context, e.getMessage());
			return;
		}
		try {
			currentDownloadId = manager.enqueue(request);
		} catch (IllegalArgumentException e) {
			showMessage(context, R.string.turn_on_dm);
			return;
		}
		boolean isUpdated = continueDownload(id, currentDownloadId);
		if (!isUpdated) {
			downloadingSong.setDownloaderListener(notifyStartDownload(currentDownloadId));
		}
		if (!earlierMsg) {
			((Activity) context).runOnUiThread(new Runnable() {
	
				@Override
				public void run() {
					showMessage(context, context.getString(R.string.download_started) + " " + sb);
				}
				
			});
		}
		UpdateTimerTask progressUpdateTask = new UpdateTimerTask(downloadingSong, manager, useAlbumCover, cacheItem);
		new Timer().schedule(progressUpdateTask, 2000, 3000);
	}

	public void showMessage(final Context context, final String message) {
		((Activity) context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void showMessage(Context context, int message) {
		showMessage(context, context.getString(message));
	}

	@Override
	public void onClick(View v) {
		downloadSong(false);
	}

	@Override
	public void onBitmapReady(Bitmap bmp) {
		cover = bmp;
	}

	@SuppressLint("NewApi")
	public boolean isBadInet() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo == null)
			return true;
		NetworkInfo.DetailedState state = netInfo.getDetailedState();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			return state.equals(NetworkInfo.DetailedState.VERIFYING_POOR_LINK);
		}
		return state.equals(NetworkInfo.State.DISCONNECTING) || state.equals(NetworkInfo.State.DISCONNECTED);
	}
	
	public void setUseAlbumCover(boolean useAlbumCover) {
		this.useAlbumCover = useAlbumCover;
	}

	public void setDownloadPath(String path) {
		downloadPath = path;
	}

	public Integer getSongID() {
		return songId;
	}

	public void setInfolistener(InfoListener infolistener) {
		this.infolistener = infolistener;
	}

	public void deleteMP3FromMediaStore(String path) {
		Uri rootUri = MediaStore.Audio.Media.getContentUriForPath(path);
		context.getContentResolver().delete(rootUri, MediaStore.MediaColumns.DATA + "=?", new String[] { path });
	}

	public CoverReadyListener notifyStartDownload(long downloadId) {
		return null;
	}

	protected void setFileUri(long downloadId, String uri) {

	}

	protected void notifyDuringDownload(final long downloadId, final long currentProgress) {

	}

	protected void removeFromDownloads(final long downloadId) {

	}

	protected void setCanceledListener(long downloadId, CanceledCallback callback) {

	}

	protected boolean continueDownload(long lastID, long newId) {
		return false;
	}

	protected void notifyAboutFailed(long downloadId, final RemoteSong s) {
		StateKeeper.getInstance().removeSongInfo(s.getComment());
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String failedSong = context.getResources().getString(R.string.download_failed);
				String title = s.getArtist() + " - " + s.getTitle();
				showMessage(context, failedSong + " - " + title);
			}
		});
	}

	protected String getDirectory() {
		downloadPath = BaseConstants.DOWNLOAD_DIR;
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
	
	protected Context getContext() {
		return context;
	}

	protected void prepare(final File src, RemoteSong song, String path) {
	}

	protected boolean isFullAction() {
		return true;
	}

	private void insertToMediaStore(final RemoteSong song, final String pathToFile) {
		StateKeeper.getInstance().putSongInfo(song.getComment(), pathToFile, StateKeeper.DOWNLOADED);
		ContentResolver resolver = context.getContentResolver();
		int seconds = 0;
		long ms = 0;
		File musicFile = null;
		try {
			musicFile = new File(pathToFile);
			seconds = AudioFileIO.read(musicFile).getAudioHeader().getTrackLength();
			ms = seconds * 1000;
		} catch (Exception e) {
			e.printStackTrace();
		}
		ContentValues songValues = new ContentValues();
		songValues.put(MediaColumns.DATA, pathToFile);
		songValues.put(AudioColumns.ALBUM, song.getAlbum());
		songValues.put(AudioColumns.ARTIST, song.getArtist());
		songValues.put(MediaColumns.TITLE, song.getTitle());
		songValues.put(AudioColumns.DURATION, ms);
		songValues.put(AudioColumns.IS_MUSIC, 1);
		deleteMP3FromMediaStore(pathToFile);
		Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songValues);
		if (null == uri) {
			Log.d(getClass().getSimpleName(), "Insert into MediaStore was failed");
			return;
		}
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(musicFile)));
	}

	private boolean setMetadataToFile(File src, boolean useCover, RemoteSong song) {
		MusicMetadataSet src_set = null;
		try {
			src_set = new MyID3().read(src);
		} catch (Exception exception) {
			Log.d(getClass().getSimpleName(), "Unable to read music metadata from file. " + exception);
		}
		if (null == src_set) {
			insertToMediaStore(song, src.getAbsolutePath());
			return false;
		}

		// try {
		// MP3File mp3File = (MP3File) AudioFileIO.read(src);
		// if (mp3File.hasID3v1Tag()) {
		// mp3File.delete(mp3File.getID3v1Tag());
		// }
		// if (mp3File.hasID3v2Tag()) {
		// mp3File.delete(mp3File.getID3v2Tag());
		// mp3File.delete(mp3File.getID3v2TagAsv24());
		// }
		// mp3File = (MP3File) AudioFileIO.read(src);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
		metadata.clear();
		metadata.setSongTitle(song.getTitle().trim());
		metadata.setArtist(song.getArtist().trim());
		metadata.setComment(song.getComment());
		if (null != cover && useCover) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
			cover.compress(CompressFormat.JPEG, 85, out);
			metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
		} else if (!useCover) {
			RenameTask.deleteCoverFromFile(src);
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

	private final class UpdateTimerTask extends TimerTask {

		private static final int DEFAULT_SONG = 7340032; // 7 Mb
		private static final int BAD_SONG = 1024;
		private RemoteSong song;
		private Item item;
		private File src;
		private DownloadManager manager;
		private int counter = 0;

		public UpdateTimerTask(RemoteSong song, DownloadManager manager, boolean useCover, Item item) {
			this.song = song;
			this.manager = manager;
			this.item = item;
		}

		@Override
		public void run() {
			Cursor c = manager.query(new DownloadManager.Query().setFilterById(currentDownloadId));
			final String artist = song.getArtist().trim();
			final String title = song.getTitle().trim();
			if (c.moveToFirst()) {
				int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
				switch (status) {
				case DownloadManager.ERROR_CANNOT_RESUME:
				case DownloadManager.ERROR_FILE_ERROR:
				case DownloadManager.ERROR_HTTP_DATA_ERROR:
				case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
				case DownloadManager.ERROR_UNKNOWN:
				case DownloadManager.STATUS_FAILED:
					failed(c, artist, title);
					return;
				case DownloadManager.STATUS_RUNNING:
					if (isFullAction()) {
						break;
					}
					int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
					int idIndex = c.getColumnIndex(DownloadManager.COLUMN_ID);
					int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
					int size = c.getInt(sizeIndex);
					long id = c.getLong(idIndex);
					if (null != item) {
						item.setId(id);
					}
					int downloaded = c.getInt(downloadedIndex);
					if (size != -1 && size != 0) {
						progress = downloaded * 100 / size;
					} else {
						progress = downloaded * 100 / DEFAULT_SONG;
					}
					notifyDuringDownload(currentDownloadId, progress);
					break;
				case DownloadManager.STATUS_SUCCESSFUL:
					if (c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR) > BAD_SONG) {
						new File(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).delete();
						notifyAboutFailed(currentDownloadId, song);
						removeFromDownloads(currentDownloadId);
						this.cancel();
					} else {
						progress = 100;
						notifyDuringDownload(currentDownloadId, progress);
						int columnIndex = 0;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							columnIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
						} else if (columnIndex != -1) {
							columnIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
						}
						if (columnIndex == -1)
							return;
						String path = c.getString(columnIndex);
						c.close();
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
							path = cutPath(path);
							if (path.contains("%20")) {
								path.replaceAll("%20", " ");
							}
						}
						src = new File(path);
						if (setMetadataToFile(src, useAlbumCover, song)) {
							insertToMediaStore(song, path);
						}
						setFileUri(currentDownloadId, src.getAbsolutePath());
						prepare(src, song, path);
						DownloadCache.getInstanse().remove(artist, title);
						if (null != infolistener) {
							infolistener.success(song.getUrl());
						}
						this.cancel();
					}
					return;
				default:
					break;
				}
			}
			c.close();
			if (isBadInet()) {
				counter++;
			} else {
				counter = 0;
			}
			if (counter > 100) {
				notifyAboutFailed(currentDownloadId, song);
				DownloadCache.getInstanse().remove(artist, title);
				manager.remove(currentDownloadId);
				this.cancel();
			}
		}

		private void failed(Cursor c, final String artist, final String title) {
			notifyAboutFailed(currentDownloadId, song);
			c.close();
			DownloadCache.getInstanse().remove(artist, title);
			if (null != infolistener) {
				infolistener.erorr("");
			}
			this.cancel();
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

		public DownloadSongTask(RemoteSong song, boolean useCover, String url, String filePath, int idDownload) {
			this.url = url;
			this.idDownload = idDownload;
			this.filePath = filePath;
			this.notificationTitle = Util.removeSpecialCharacters(song.artist) + " - " + Util.removeSpecialCharacters(song.title);
			this.song = song;
			this.useCover = useCover;
			sPref = context.getSharedPreferences(DOWNLOAD_ID_GINGERBREAD, Context.MODE_PRIVATE);
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
			final String artist = song.getArtist().trim();
			final String title = song.getTitle().trim();
			if (!isStop) {
				notification = new Notification(android.R.drawable.stat_sys_download, notificationTitle, Calendar.getInstance().getTimeInMillis());
			} else if (progress < 100 && isStop && !interrupted) {
				String message = context.getString(R.string.download_failed);
				DownloadCache.getInstanse().remove(artist, title);
				notification = new Notification(android.R.drawable.stat_notify_error, message, Calendar.getInstance().getTimeInMillis());
			} else if (progress == 0 && isStop && interrupted) {
				String message = context.getString(R.string.download_canceled);
				DownloadCache.getInstanse().remove(artist, title);
				notification = new Notification(android.R.drawable.stat_notify_error, message, Calendar.getInstance().getTimeInMillis());
			} else {
				String message = context.getString(R.string.download_finished);
				DownloadCache.getInstanse().remove(artist, title);
				notification = new Notification(android.R.drawable.stat_sys_download_done, message, Calendar.getInstance().getTimeInMillis());
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
				sendNotification(0, true);
				notifyAboutFailed(idDownload, song);
				return;
			}
			int size = 0;
			int index = 0;
			int current = 0;
			try {
				file = new File(filePath);
				if (!file.exists()) {
					file.createNewFile();
				}
				output = new FileOutputStream(file);
				input = connection.getInputStream();
				size = connection.getContentLength();
				buffer = new BufferedInputStream(input);
				byte[] bBuffer = new byte[10240];
				int i = 0;
				notifyDuringDownload(idDownload, 3);
				while ((current = buffer.read(bBuffer)) != -1) {
					if (interrupted) {
						output.close();
						input.close();
						buffer.close();
						if (file.delete()) {
							sendNotification(0, true);
						}
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
				if (!setMetadataToFile(file, useCover, song)) {
					notifyAboutFailed(idDownload, song);
					sendNotification(0, true);
					output.close();
					input.close();
					buffer.close();
					return;
				}
			} catch (Exception e) {
				notifyAboutFailed(idDownload, song);
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
			insertToMediaStore(song, file.getAbsolutePath());
			return;
		}

	}

}
