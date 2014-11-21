package ru.johnlife.lifetoolsmp3;

import java.io.File;
import java.text.MessageFormat;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class RenameTask extends AsyncTask<String, Void, Boolean> {
	private File file;
	private Context context;
	private ProgressDialog progress;
	private File newFile;
	private String artist;
	private String title;
	private String album;
	private boolean useCover;
	private RenameTaskSuccessListener listener;
	
	public RenameTask(Context context) {
		this.context = context;
		initProgress();
	}

	public RenameTask(File file, Context context, String artist, String title, String album, boolean useCover, RenameTaskSuccessListener listener) {
		this.file = file;
		this.context = context;
		this.artist = artist;
		this.title = title;
		this.album = album;
		this.useCover = useCover;
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

	@Override
	protected void onPreExecute() {
		showProgress();
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		boolean isChange = false;
		try {
			if (null != file && !file.exists()) {
				return false;
			}
			if (!useCover) {
				deleteCoverFromFile(file);
			}
			MusicMetadataSet src_set = new MyID3().read(file);
			if (src_set == null) {
				return false;
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
				return false;
			}
			newFile = new File(MessageFormat.format("{0}/{1} - {2}.mp3", file.getParentFile(), artist, title));
			if (file.renameTo(newFile)) {
				new MyID3().update(newFile, src_set, metadata);
				notifyMediaScanner(file);
				notifyMediaScanner(newFile);
				context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			} else {
				newFile.delete();
			}
		} catch (Exception e) {
			Log.d(getClass().getSimpleName(), e.getMessage());
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (!result) {
			Toast toast = Toast.makeText(context, R.string.bad_file, Toast.LENGTH_SHORT);
			toast.show();
			cancelProgress();
		}
		super.onPostExecute(result);
	}
	
	private void notifyMediaScanner(File file) {
		MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri) {
				if (newFile.getAbsolutePath().equals(path)) {
					if (null != listener)
						listener.success();
					cancelProgress();
				}
			}
		});
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
		}
	}
}