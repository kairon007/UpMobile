package ru.johnlife.lifetoolsmp3;

import java.io.File;
import java.text.MessageFormat;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class RenameTask {
	private File file;
	private Context context;
	private ProgressDialog progress;
	private File newFile;
	private String artist;
	private String title;
	private String album;
	private RenameTaskSuccessListener listener;
	
	/**
	 * 
	 * @param file
	 * @param context
	 * @param listener
	 * @param strArray - first element must be name of artist, second element - title of song, third element - title of album  
	 */
	public RenameTask(File file, Context context, RenameTaskSuccessListener listener, String... strArray) {
		this.file = file;
		this.context = context;
		this.artist = null == strArray[0] ? "" : strArray[0];
		this.title = null == strArray[1] ? "" : strArray[1];
		this.album = null == strArray[2] ? "" : strArray[2];
		this.listener = listener;
		initProgress();
	}
		
	private void initProgress() {
		progress = new ProgressDialog(context);
		progress.setTitle(R.string.message_please_wait);
		progress.setMessage(context.getResources().getString(R.string.message_loading));
		progress.setCancelable(false);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}
	/**
	 * The method which must be called to start renaming
	 * @param deleteCover - The parameter defines whether you want to delete a picture from a file
	 * @param onlyCover - The parameter specifying the removal of only the image from a file
	 */
	public void start(boolean deleteCover, boolean onlyCover) {
		showProgress();
		boolean isChange = false;
		try {
			if (null != file && !file.exists()) {
				error();
				return;
			}
			newFile = file;
			if (!deleteCover) {
				deleteCoverFromFile(file);
				if (onlyCover) {
					success();
					return;
				}
			}
			MusicMetadataSet src_set = new MyID3().read(file);
			if (src_set == null) {
				error();
				return;
			}
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			if (!album.equals("")) {
				isChange = true;
				metadata.setAlbum(album);
			}
			if (!title.equals("")) {
				isChange = true;
				metadata.setSongTitle(title);
			}
			if (!artist.equals("")) {
				isChange = true;
				metadata.setArtist(artist);
			}
			if (!isChange) {
				error();
				return;
			}
			newFile = new File(MessageFormat.format("{0}/{1} - {2}.mp3", file.getParentFile(), artist, title));
			if (file.renameTo(newFile)) {
				new MyID3().update(newFile, src_set, metadata);
			} else {
				newFile.delete();
			}
			success();
		} catch (Exception e) {
			Log.d(getClass().getSimpleName(), e.getMessage());
		}
	}

	private void success() {
		listener.success(newFile.getPath());
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (!title.equals("") && !artist.equals("")) {
					ContentResolver resolver = context.getContentResolver();
					String[] projection = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE };
					Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
					c.moveToFirst();
					while (c.moveToNext()) {
						if (file.getPath().equals(c.getString(1))) {
							int id = c.getInt(0);
							ContentValues songValues = new ContentValues();
							songValues.put(MediaStore.Audio.Media.DATA, newFile.getPath());
							songValues.put(MediaStore.Audio.Media.ALBUM, album);
							songValues.put(MediaStore.Audio.Media.ARTIST, artist);
							songValues.put(MediaStore.Audio.Media.TITLE, title);
							String where = MediaStore.Audio.Media._ID + "=" + id;
							resolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songValues, where, null);
							break;
						}
					}
					c.close();
				}
			}
		}).start();
		cancelProgress();
	}

	private void error() {
		Toast toast = Toast.makeText(context, R.string.bad_file, Toast.LENGTH_SHORT);
		toast.show();
		cancelProgress();
		listener.error();
	}
	
	public void showProgress() {
		if (null != progress) {
			progress.show();
		}
	}
	
	public void cancelProgress() {
		if (null != progress && progress.isShowing()) {
			progress.cancel();
		}
	}
	
	public boolean isShow() {
		return progress.isShowing();
	}

	private void deleteCoverFromFile(File file) {
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if (null == src_set) {
				return;
			}
			MusicMetadata m = (MusicMetadata) src_set.merged;
			m.clearPictureList();
			MusicMetadata metadata = new MusicMetadata("");
			metadata.setArtist(m.getArtist());
			metadata.setSongTitle(m.getSongTitle());
			metadata.setGenre(m.getGenre());
			metadata.setAlbum(m.getAlbum());
			File temp = new File(file.getParent(), file.getName() + ".temp");
			new MyID3().removeTags(file, temp);
			temp.renameTo(file);
			MusicMetadataSet src = new MyID3().read(file);
			File temp1 = new File(file.getParent(), file.getName() + ".temp1");
			new MyID3().write(file, temp1, src, metadata);
			temp1.renameTo(file);
		} catch (Exception e) {
			android.util.Log.d(getClass().getSimpleName(), e.getMessage());
		}
	}
	
}
