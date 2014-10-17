package ru.johnlife.lifetoolsmp3.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.cmc.music.myid3.MyID3v1;
import org.cmc.music.myid3.MyID3v2Write;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.RefreshListener;
import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.task.DownloadGrooveshark;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class DownloadClickListener implements View.OnClickListener, OnBitmapReadyListener {

	private ArrayList<String[]> headers = new ArrayList<String[]>();
	private Context context;
	private RemoteSong song;
	protected Bitmap cover;
	protected String songTitle;
	protected String songArtist;
	private String url;
	protected String duration;
	private String currentDownloadingSongTitle;
	protected Long currentDownloadId;
	public Integer songId;
	private boolean waitingForCover = true;
	private double progress = 0.0;
	private boolean useAlbumCover = true;
	private RefreshListener listener;

	protected DownloadClickListener(Context context, RemoteSong song, RefreshListener listener) {
		this.context = context;
		this.song = song;
		this.songId = song instanceof GrooveSong ? ((GrooveSong) song).getSongId() : -1;
		this.listener = listener;
		songTitle = song.getTitle();
		songArtist = song.getArtist();
		url = song.getDownloadUrl();
		duration = Util.formatTimeIsoDate(song.getDuration());
		headers = song.getHeaders();
	}

	@SuppressLint("NewApi")
	public void downloadSond(String artist, String title, final boolean useCover) {
		SongArrayHolder.getInstance().setStreamDialogOpened(false, null, null);
		if (!this.songArtist.equals(artist)) {
			songArtist = artist;
		}
		if (!this.songTitle.equals(title)) {
			songTitle = title;
		}
		if (url == null) {
			Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
			return;
		}
		File musicDir = new File(getDirectory());
		if (!musicDir.exists()) {
			musicDir.mkdirs();
		}
		StringBuilder sb = new StringBuilder(songArtist).append(" - ").append(songTitle);
		if (songId != -1) {
			Log.d("GroovesharkClient", "Its GrooveSharkDownloader. SongID: " + songId);
			DownloadGrooveshark manager = new DownloadGrooveshark(songId, musicDir.getAbsolutePath(), sb.append(".mp3").toString(), context);
			manager.execute();
		} else {
			final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				url = url.replaceFirst("https", "http");
			}
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url)).addRequestHeader("User-Agent",
					"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.3) Gecko/2008092814 (Debian-3.0.1-1)");
			if (headers != null && headers.get(0) != null) {
				for (int i = 0; i < headers.size(); i++) {
					request.addRequestHeader(headers.get(i)[0], headers.get(i)[1]);
				}
			}
			final String fileName = sb.toString();
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(fileName);
			try {
				request.setDestinationInExternalPublicDir(OnlineSearchView.getSimpleDownloadPath(musicDir.getAbsolutePath()), sb.append(".mp3").toString());
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), e.getMessage());
				String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
				Log.e(getClass().getSimpleName(), "Something wrong. Set default directory: " + dir);
				request.setDestinationInExternalPublicDir(dir, sb.append(".mp3").toString());
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
			}
			final long downloadId = manager.enqueue(request);
			notifyAboutDownload(downloadId);
			Toast.makeText(context, String.format(context.getString(R.string.download_started), fileName), Toast.LENGTH_SHORT).show();
			UpdateTimerTask progressUpdateTask = new UpdateTimerTask(song, manager, downloadId, useCover);
			new Timer().schedule(progressUpdateTask, 1000, 1000);
		}
	}

	@Override
	public void onClick(View v) {
		downloadSond(songArtist, songTitle, useAlbumCover);
	}

	protected void notifyDuringDownload(final long downloadId, final String currentDownloadTitle, final double currentProgress) {

	}

	protected void notifyAboutDownload(long downloadId) {

	}

	protected void notifyAboutFailed(long downloadId, String title) {

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

	protected void prepare(final File src, RemoteSong song, String path) {

	}

	protected boolean isFullAction() {
		return true;
	}

	@Override
	public void onBitmapReady(Bitmap bmp) {
		this.cover = bmp;
		this.waitingForCover = false;
	}

	@SuppressLint("NewApi")
	private final class UpdateTimerTask extends TimerTask {

		private RemoteSong song;
		private File src;
		private DownloadManager manager;
		private long downloadId;
		private boolean useCover;
		String currentSongTitle = "";

		public UpdateTimerTask(RemoteSong song, DownloadManager manager, long downloadId, boolean useCover) {
			this.song = song;
			this.manager = manager;
			this.downloadId = downloadId;
			this.useCover = useCover;
		}

		@Override
		public void run() {
			if (waitingForCover) {
				return;
			}
			Cursor c = manager.query(new DownloadManager.Query().setFilterById(downloadId));
			if (c.moveToFirst()) {
				int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
				switch (status) {
				case DownloadManager.STATUS_FAILED:
					String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
					notifyAboutFailed(downloadId, title);
					c.close();
					this.cancel();
					return;
				case DownloadManager.STATUS_RUNNING:
					if (isFullAction()) {
						break;
					}
					int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
					int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
					long size = c.getInt(sizeIndex);
					long downloaded = c.getInt(downloadedIndex);
					currentSongTitle = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
					if (size != -1) {
						progress = downloaded * 100.0 / size;
					}
					notifyDuringDownload(downloadId, currentSongTitle, progress);
					break;
				case DownloadManager.STATUS_SUCCESSFUL:
					progress = 100;
					notifyDuringDownload(downloadId, currentDownloadingSongTitle, progress);
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
					MusicMetadataSet src_set = null;
					try {
						src_set = new MyID3().read(src);
					} catch (Exception exception) {
						Log.d(getClass().getSimpleName(), "Don't read music metadata from file. " + exception);
					}
					if (null == src_set) {
						notifyMediascanner(song, path);
						this.cancel();
						return;
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

	}
}
