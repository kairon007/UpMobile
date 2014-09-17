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

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.task.DownloadGrooveshark;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
	private String URL;
	protected String duration;
	private String currentDownloadingSongTitle;
	protected Long currentDownloadId;
	public Integer songId;
	private boolean waitingForCover = true;
	private double progress = 0.0;
	private String album;
	private boolean useAlbumCover;

	protected DownloadClickListener(Context context, RemoteSong song, Bitmap bitmap) {
		this.context = context;
		this.song = song;
		this.cover = bitmap != null ? bitmap : null;
		this.songId = song instanceof GrooveSong ? ((GrooveSong) song).getSongId() : -1;
		songTitle = song.getTitle();
		songArtist = song.getArtist();
		URL = song.getDownloadUrl();
		duration = Util.formatTimeIsoDate(song.getDuration());
		headers = song.getHeaders();
	}

	public void setSong(ArrayList<String> sFields) {
		songArtist = sFields.get(0);
		album = sFields.get(1);
		songTitle = sFields.get(2);
		useAlbumCover = (Boolean.getBoolean(sFields.get(3)));
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		if (URL == null) {
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
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL));
			final String fileName = sb.toString();
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(fileName);
			request.setDestinationInExternalPublicDir(OnlineSearchView.getSimpleDownloadPath(musicDir.getAbsolutePath()), sb.append(".mp3").toString());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
			}
			final long downloadId = manager.enqueue(request);
			notifyAboutDownload(downloadId);
			Toast.makeText(context, String.format(context.getString(R.string.download_started), fileName), Toast.LENGTH_SHORT).show();
			final TimerTask progresUpdateTask = new TimerTask() {

				private File src;

				@Override
				public void run() {
					if(isFullAction() && waitingForCover) {
						return;
					}
					Cursor cs = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_RUNNING));
					if (cs.moveToFirst()) {
						int sizeIndex = cs.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
						int downloadedIndex = cs.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
						long size = cs.getInt(sizeIndex);
						long downloaded = cs.getInt(downloadedIndex);
						currentDownloadId = downloadId;
						currentDownloadingSongTitle = cs.getString(cs.getColumnIndex(DownloadManager.COLUMN_TITLE));
						if (size != -1) {
							progress = downloaded * 100.0 / size;
						}
						notifyDuringDownload(currentDownloadId, currentDownloadingSongTitle, progress);
					}
					cs.close();
					Cursor completeCursor = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
					if (completeCursor.moveToFirst()) {
						if (currentDownloadingSongTitle.equalsIgnoreCase(completeCursor.getString(completeCursor.getColumnIndex(DownloadManager.COLUMN_TITLE)))) {
							progress = 100;
							notifyDuringDownload(currentDownloadId, currentDownloadingSongTitle, progress);
							int columnIndex = 0;
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								columnIndex = completeCursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
							} else if (columnIndex != -1) {
								columnIndex = completeCursor.getColumnIndex("local_uri");
							}
							if (columnIndex == -1)
								return;
							String path = completeCursor.getString(columnIndex);
							completeCursor.close();
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
							if (isFullAction()) {
								metadata.setSongTitle(songTitle);
							} else {
								metadata.setSongTitle(songTitle + '/' + duration);
							}
							metadata.setArtist(songArtist);
							if (null != cover) {
								ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
								cover.compress(CompressFormat.JPEG, 85, out);
								metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
							}
							File dst = new File(src.getParentFile(), src.getName() + "-1");
							try {
								new MyID3().write(src, dst, src_set, metadata);
								dst.renameTo(src);
							} catch (Exception e) {
								android.util.Log.d("log", "don't write music metadata from file. " + e);
							} finally {
								if (dst.exists())
									dst.delete();
							}
							notifyMediascanner(song);
							this.cancel();
						}
					}
					completeCursor.close();
					if (waitingForCover) {
						return;
					}
				}

				private String cutPath(String s) {
					int index = s.indexOf('m');
					return s.substring(index - 1);
				}

				private void notifyMediascanner(final RemoteSong song) {
					String path = song.path;
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

	protected void notifyDuringDownload(final long currentDownloadId, final String currentDownloadTitle, final double currentProgress) {
	}

	protected void notifyAboutDownload(long downloadId) {

	}

	protected String getDirectory() {
		return BaseConstants.DOWNLOAD_DIR;
	}

	protected void prepare(final File src, RemoteSong song) {
	}

	protected boolean isFullAction() {
		return true;
	}
	
	@Override
	public void onBitmapReady(Bitmap bmp) {
		this.cover = bmp;
		this.waitingForCover = false;
	}
}