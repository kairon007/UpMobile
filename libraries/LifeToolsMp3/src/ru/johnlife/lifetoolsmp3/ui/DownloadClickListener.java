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
import org.jaudiotagger.audio.mp3.MP3File;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.DownloadCache.DownloadCacheCallback;
import ru.johnlife.lifetoolsmp3.DownloadCache.Item;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.RefreshListener;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DownloadClickListener implements View.OnClickListener, OnBitmapReadyListener {

	private ArrayList<String[]> headers = new ArrayList<String[]>();
	private DownloadCache.Item cacheItem;
	private Context context;
	protected RemoteSong song;
	protected Bitmap cover;
	protected Long currentDownloadId;
	public Integer songId;
	private int id;
	private long progress = 0;
	private boolean useAlbumCover = true;
	private RefreshListener listener;
	protected boolean interrupted = false;
	
	private CanceledCallback cancelDownload = new CanceledCallback() {
		@Override
		public void cancel() {
			interrupted = true;
		}
	};

	protected DownloadClickListener(Context context, RemoteSong song, RefreshListener listener, int id) {
		this.context = context;
		this.song = song;
		this.listener = listener;
		this.id = id;
		initSong();
	}
	
	private void initSong() {
		songId = song instanceof GrooveSong ? ((GrooveSong) song).getSongId() : -1;
		headers = song.getHeaders();
		song.getCover(this);
	}
	
	@SuppressLint("NewApi")
	public void downloadSong(boolean fromCallback) {
		String url = song.getUrl();
		if (url == null || url.isEmpty()) {
			Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
			return;
		}
		String songArtist = song.getArtist().trim();
		String songTitle = song.getTitle().trim();
		File musicDir = new File(getDirectory());
		if (!musicDir.exists()) {
			musicDir.mkdirs();
		}
		boolean isCached = false;
		if (!fromCallback) {
			isCached = DownloadCache.getInstanse().put(songArtist, songTitle, new DownloadCacheCallback() {
				
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
			Toast.makeText(context, context.getResources().getString(R.string.download_cached), Toast.LENGTH_SHORT).show();
			return;
		}
		StringBuilder stringBuilder = new StringBuilder(songArtist).append(" - ").append(songTitle).append(".mp3");
		final String sb = Util.removeSpecialCharacters(stringBuilder.toString());
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
			Uri uri = Uri.parse(url);
			DownloadManager.Request request = new DownloadManager.Request(uri).addRequestHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.3) Gecko/2008092814 (Debian-3.0.1-1)");
			if (headers != null && !headers.isEmpty()) {
				for (int i = 0; i < headers.size(); i++) {
					request.addRequestHeader(headers.get(i)[0], headers.get(i)[1]);
				}
			}
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(sb);
			try {
				request.setTitle(songArtist);
				request.setDescription(songTitle);
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
			((Activity) context).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(context, context.getString(R.string.download_started) +" "+sb, Toast.LENGTH_SHORT).show();
				}
			});
			UpdateTimerTask progressUpdateTask = new UpdateTimerTask(song, manager, useAlbumCover, cacheItem);
			new Timer().schedule(progressUpdateTask, 1000, 1000);
		}
	}

	@Override
	public void onClick(View v) {
		downloadSong(false);
	}

	protected void setFileUri(long downloadId, String uri) {
		
	}
	
	protected void notifyDuringDownload(final long downloadId, final long currentProgress) {
		
	}
	
	protected void removeFromDownloads(final long downloadId) {
		
	}
 
	public CoverReadyListener notifyStartDownload(long downloadId) {
		return null;
	}
	
	protected void setCanceledListener(long downloadId, CanceledCallback callback) {
		
	}
	
	protected boolean continueDownload(long lastID, long newId) {
		return false;
	}
	
	@SuppressLint("NewApi")
	public boolean isBadInet() {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo == null) return true;
	    NetworkInfo.DetailedState state = netInfo.getDetailedState();
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
	    	return state.equals(NetworkInfo.DetailedState.VERIFYING_POOR_LINK);
	    } else {
	    	return state.equals(NetworkInfo.State.DISCONNECTING) || state.equals(NetworkInfo.State.DISCONNECTED);
		}
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
	
	//if change title or artist of song, then this method call before downloadSong()
	public void setSong(RemoteSong song) {
		this.song = song;
	}

	private void insertToMediaStore(final RemoteSong song, final String pathToFile) {
			ContentResolver resolver = context.getContentResolver();
			int seconds = 0;
			long ms = 0;
			try {
				File musicFile = new File(pathToFile);
				seconds = AudioFileIO.read(musicFile).getAudioHeader().getTrackLength();
				ms = seconds * 1000;
			} catch (Exception e) {
				e.printStackTrace();
			}
			ContentValues songValues = new ContentValues();
			songValues.put(MediaStore.Audio.Media.DATA, pathToFile);
			songValues.put(MediaStore.Audio.Media.ALBUM, song.getAlbum());
			songValues.put(MediaStore.Audio.Media.ARTIST, song.getArtist());
			songValues.put(MediaStore.Audio.Media.TITLE, song.getTitle());
			songValues.put(MediaStore.Audio.Media.DURATION, ms);
			resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songValues);
			resolver.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
	}
	
	private boolean setMetadataToFile(String path, File src, boolean useCover) {
		MusicMetadataSet src_set = null;
		try {
			src_set = new MyID3().read(src);
		} catch (Exception exception) {
			Log.d(getClass().getSimpleName(), "Unable to read music metadata from file. " + exception);
		}
		if (null == src_set) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				insertToMediaStore(song, path);
			}
			return false;
		}
		
		//
		try {
			MP3File mp3File = (MP3File) AudioFileIO.read(src);
			android.util.Log.d("logd", "first v1 = " + mp3File.getID3v1Tag() + ", v2 = " 
					+ mp3File.getID3v2Tag() + ", v24 = " + mp3File.getID3v2TagAsv24());
			if (mp3File.hasID3v1Tag()) {
				mp3File.delete(mp3File.getID3v1Tag());
			}
			if (mp3File.hasID3v2Tag()) {
				mp3File.delete(mp3File.getID3v2Tag());
				mp3File.delete(mp3File.getID3v2TagAsv24());
			}
			mp3File = (MP3File) AudioFileIO.read(src);
			android.util.Log.d("logd", "second v1 = " + mp3File.getID3v1Tag() + ", v2 = " 
					+ mp3File.getID3v2Tag() + ", v24 = " + mp3File.getID3v2TagAsv24());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
		
		MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
		metadata.clear();
		metadata.setSongTitle(song.getTitle().trim());
		metadata.setArtist(song.getArtist().trim());
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
		private static final int BAD_SONG = 1024;
		private RemoteSong song;
		private Item item;
		private File src;
		private DownloadManager manager;
		private boolean useCover;
		private int counter = 0;  

		public UpdateTimerTask(RemoteSong song, DownloadManager manager, boolean useCover, Item item) {
			this.song = song;
			this.manager = manager;
			this.useCover = useCover;
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
				case DownloadManager.STATUS_FAILED:
					notifyAboutFailed(currentDownloadId);
					c.close();
					DownloadCache.getInstanse().remove(artist, title);
					this.cancel();
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
						notifyAboutFailed(currentDownloadId);
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
						if (setMetadataToFile(path, src, useCover) && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
							insertToMediaStore(song, path);
						}
						setFileUri(currentDownloadId, src.getAbsolutePath());
						prepare(src, song, path);
						DownloadCache.getInstanse().remove(artist, title);
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
				notifyAboutFailed(currentDownloadId);
				DownloadCache.getInstanse().remove(artist, title);
				manager.remove(currentDownloadId);
				this.cancel();
			}
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
			final String artist = song.getArtist().trim();
			final String title = song.getTitle().trim();
			if (!isStop) {
				notification = new Notification(android.R.drawable.stat_sys_download, notificationTitle, Calendar.getInstance().getTimeInMillis());
			} else if (progress < 100 && isStop && !interrupted){
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
				notifyAboutFailed(idDownload);
				return;
			}
			int size = 0;
			int index = 0;
			int current = 0;
			try {
				file = new File(filePath);
				if (!file.exists()){
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
						if (file.delete()){
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
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) insertToMediaStore(song, file.getAbsolutePath());
			return;
		}
		
	}

}
