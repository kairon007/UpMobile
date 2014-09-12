package ru.johnlife.lifetoolsmp3.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.task.DownloadGrooveshark;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class DownloadClickListener implements View.OnClickListener, OnBitmapReadyListener {
	private final Context context;
	private String songTitle;
	private Player player;
	private String songArtist;
	
	private Bitmap cover;
	private boolean waitingForCover = true;

	DownloadClickListener(Context context, String songTitle, String songArtist, Player player) {
		this.context = context;
		this.songTitle = songTitle;
		this.songArtist = songArtist;
		this.player = player;
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		if (player == null)
			return;
		String downloadUrl = player.getDownloadUrl();
		Integer songId = player.getSongId();
		if (downloadUrl == null || downloadUrl.equals("")) {
			Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
			return;
		}
		player.cancel();

		final File musicDir = new File(OnlineSearchView.getDownloadPath(context));
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
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));

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
						Cursor c = manager
								.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
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
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
						String artist = prefs.getString(BaseConstants.EDIT_ARTIST_NAME, "");
						String album = prefs.getString(BaseConstants.EDIT_ALBUM_TITLE, "");
						String song = prefs.getString(BaseConstants.EDIT_SONG_TITLE, "");
						boolean useAlbumCover = prefs.getBoolean(BaseConstants.USE_ALBUM_COVER, true);
						Log.d("log", "useAlbumCover = "+useAlbumCover);
						boolean useNewPath = false;
						if (!song.equals("")) {
							songTitle = song;
							useNewPath = true;
						}
						if (!artist.equals("")) {
							songArtist = artist;
							useNewPath = true;
						}
						int i = path.lastIndexOf("/");
						String fileName = songArtist + " - " + songTitle + ".mp3";
						String folder_name = path.substring(0, i);
						final String newPath = folder_name + "/" + fileName;
						src = new File(path);
						MusicMetadataSet src_set = null;
						try {
							src_set = new MyID3().read(src);
						} catch (IOException e) {
							e.printStackTrace();
						} // read metadata
						if (src_set == null) {
							return;
						}
						MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
						if (!album.equals("")) {
							metadata.clearAlbum();
							metadata.setAlbum(album);
						}
						if (null != cover && useAlbumCover) {
							ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
							cover.compress(CompressFormat.JPEG, 85, out);
							metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
						}
						metadata.clearSongTitle();
						metadata.setSongTitle(songTitle);
						metadata.clearArtist();
						metadata.setArtist(songArtist);
						if (useNewPath) {
							File file = new File(newPath);
							try {
								copy(src, file);
								new MyID3().update(file, src_set, metadata);
							} catch (Exception e) {
								Log.w(getClass().getSimpleName(), "Error writing metadata", e);
							} finally {
								src.delete();
							}
							notifyMediascanner(newPath);
						} else {
							File dst = null ;
							try {
								dst = new File(src.getParentFile(), src.getName()+ "-1");
								new MyID3().write(src, dst, src_set, metadata);  // write updated metadata
							} catch (UnsupportedEncodingException e) {
								dst.renameTo(src);
							} catch (ID3WriteException e) {
								dst.renameTo(src);
							} catch (IOException e) {
								dst.renameTo(src);
							} finally {
								dst.renameTo(src);
							}
						}
				}
				
				private String cutPath(String s) {
					int index = s.indexOf('m');
					return s.substring(index - 1);
				}
				
				private void notifyMediascanner(String path) {
					File file = new File(path);
					MediaScannerConnection.scanFile( context, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

						public void onScanCompleted(String path, Uri uri) {
							SharedPreferences.Editor settingsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
							settingsEditor.putString(BaseConstants.EDIT_ARTIST_NAME, "");
							settingsEditor.putString(BaseConstants.EDIT_ALBUM_TITLE, "");
							settingsEditor.putString(BaseConstants.EDIT_SONG_TITLE, "");
							settingsEditor.putBoolean(BaseConstants.USE_ALBUM_COVER, true);
							settingsEditor.commit();
						}
						
					});
				}
			};
			new Timer().schedule(progresUpdateTask, 1000, 1000);
			
		}
	}
	
	private void copy(File source, File target) throws IOException {
	    InputStream in = new FileInputStream(source);
	    OutputStream out = new FileOutputStream(target);
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
	
	@Override
	public void onBitmapReady(Bitmap bmp) {
		this.cover = bmp;
		this.waitingForCover = false;
	}

}