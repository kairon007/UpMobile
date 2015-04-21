package ru.johnlife.lifetoolsmp3.song;

import java.io.File;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

public class MusicData implements Comparable<MusicData>, AbstractSong {

	public static final String[] FILLED_PROJECTION = {
				BaseColumns._ID, 
				MediaColumns.DATA,
				MediaColumns.TITLE, 
				AudioColumns.ARTIST,
				AudioColumns.DURATION, 
				AudioColumns.ALBUM, };

	public static final int MODE_VISIBLITY = 0x00000001;
	public static final int MODE_PLAYING = 0x00000002;
	private Bitmap cover;
	private String path;
	private String title;
	private String artist;
	private String album;
	private String comment;
	private String downloadUrl;
	private int mode;
	private long id;
	private long duration;
	private int progress;
	private AbstractSpecial special;

	public MusicData() {

	}

	public MusicData(String title, String artist, long id, long duration, String downloadUrl) {
		this.title = title;
		this.artist = artist;
		this.id = id;
		this.duration = duration;
		this.setDownloadUrl(downloadUrl);
		
	}

	private MusicData(Parcel parcel) {
		id = parcel.readLong();
		duration = parcel.readLong();
		path = parcel.readString();
		title = parcel.readString();
		artist = parcel.readString();
		progress = parcel.readInt();
		album = parcel.readString();
		comment = parcel.readString();
	}

	public static final Parcelable.Creator<MusicData> CREATOR = new Parcelable.Creator<MusicData>() {

		@Override
		public MusicData createFromParcel(Parcel source) {
			return new MusicData(source);
		}

		@Override
		public MusicData[] newArray(int size) {
			return new MusicData[size];
		}
	};

	public void populate(Cursor cursor) {
		id = cursor.getLong(0);
		path = cursor.getString(1);
		title = cursor.getString(2);
		artist = cursor.getString(3);
		duration = cursor.getLong(4);
		album = cursor.getString(5);
		comment = getComment();
	}

	public void reset(final Context context) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				ContentResolver resolver = context.getContentResolver();
				String where = MediaStore.Audio.Media._ID + "=" + id;
				resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null);
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				} else {
					android.util.Log.i(getClass().getCanonicalName(), "Attention! File " + artist + " - " + title + ".mp3 " + " doesn't exist");	
				}
			}
		}).start();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(duration);
		dest.writeString(path);
		dest.writeString(title);
		dest.writeString(artist);
		dest.writeInt(progress);
		dest.writeString(album);
		dest.writeString(comment);
	}

	@Override
	public Bitmap getCover(Context context) {
		if (cover != null) return cover;
		File file = new File(path);
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			cover = Util.getArtworkImage(2, metadata);
		} catch (Exception e) {
			android.util.Log.d(getClass().getSimpleName(), "Exception! Metadata is bad. " + e.getMessage());
			return null;
		}
		return cover;
	}
	
	@Override
	public String getComment() {
		if (null != comment && !comment.isEmpty()) return comment;
		File file = new File(path);
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			comment = metadata.getComment();
		} catch (Exception e) {
			android.util.Log.d(getClass().getSimpleName(), "Exception! Metadata is bad. " + e.getMessage());
			return null;
		}
		return comment;
	}
	
	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean isHasCover() {
		return cover != null;
	}

	@Override
	public String getAlbum() {
		return album;
	}
	
	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		return false;
	}

	public String getPath() {
		return path;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public long getId() {
		return id;
	}

	public long getDuration() {
		return duration;
	}

	public int getProgress() {
		return progress;
	}
	
	@Override
	public void setArtist(String artist) {
		this.artist = artist;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setAlbum(String album) {
		this.album = album;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	@Override
	public MusicData cloneSong() throws CloneNotSupportedException {
		return clone();
	}

	@Override
	protected MusicData clone() throws CloneNotSupportedException {
		return (MusicData) super.clone();
	}

	@Override
	public int compareTo(MusicData another) {
		if (id > another.id)
			return 1;
		else if (id < another.id)
			return -1;
		else
			return 0;
	}

	@Override
	public String toString() {
		return String.format("%d %s", id, path);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (null == object)
			return false;
		if (this.getClass() != object.getClass())
			return false;
		MusicData another = (MusicData) object;
		if (this.id != another.id)
			return false;
		return true;
	}

	public void turnOff(int flag) {
		setFlag(flag, false);
	}

	public void turnOn(int flag) {
		setFlag(flag, true);
	}

	public boolean check(int flag) {
		int buf = mode;
		boolean result = (buf & flag) == flag;
		return result;
	}

	private void setFlag(int flag, boolean onOff) {
		if ((check(flag) && onOff) || (!check(flag) && !onOff))
			return;
		if (onOff) {
			mode |= flag;
		} else {
			mode &= ~flag;
		}
	}

	public void clearCover() {
		cover = null;
	}

	@Override
	public AbstractSpecial getSpecial() {
		if (null == special) {
			special = new AbstractSpecial();
		}
		return special;
	}
}
