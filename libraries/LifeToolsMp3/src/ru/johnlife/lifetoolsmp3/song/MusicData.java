package ru.johnlife.lifetoolsmp3.song;

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
import android.support.annotation.NonNull;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.util.Comparator;

import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class MusicData implements Comparable<MusicData>, AbstractSong, Comparator<MusicData> {

	public static final String[] FILLED_PROJECTION = {
				BaseColumns._ID, 
				MediaColumns.DATA,
				MediaColumns.TITLE, 
				AudioColumns.ARTIST,
				AudioColumns.DURATION, 
				AudioColumns.ALBUM,
				AudioColumns.IS_MUSIC,
	};

	public static final int MODE_VISIBLITY = 0x00000001;
	public static final int MODE_PLAYING = 0x00000002;
	private Bitmap cover;
	private String path;
	private String title;
	private String artist;
	private String album;
	private String comment;
	private int mode;
	private long id;
	private long duration;
	private int progress;
	private AbstractSpecial special;

	public MusicData() {

	}

	public MusicData(String title, String artist, long id, long duration, String comment) {
		this.title = title;
		this.artist = artist;
		this.id = id;
		this.duration = duration;
		this.comment = comment;
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
	}

	public void reset(final Context context) {
		ContentResolver resolver = context.getContentResolver();
		String where = BaseColumns._ID + "=" + id;
		resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null);
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		} else {
			android.util.Log.i(getClass().getSimpleName(), "Attention! File " + artist + " - " + title + ".mp3 " + " doesn't exist");
		}
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
	public Bitmap getCover() {
		if (cover != null) return cover;
		File file = new File(path);
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			return Util.getArtworkImage(2, metadata, path);
		} catch (Exception e) {
			android.util.Log.d(getClass().getSimpleName(), "Exception! Metadata is bad. " + e);
			return null;
		}
	}

	public void getCover(final RemoteSong.OnBitmapReadyListener readyListener) {
		Util.getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				File file = new File(path);
				try {
					MusicMetadataSet src_set = new MyID3().read(file);
					MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
					readyListener.onBitmapReady(Util.getArtworkImage(2, metadata, path));
				} catch (Exception e) {
					android.util.Log.d(getClass().getSimpleName(), "Exception! Metadata is bad. " + e);
					readyListener.onBitmapReady(null);
				}
			}
		});
	}

	@Override
	public String getComment() {
		if (null != comment && !comment.isEmpty()) return comment;
		try {
			File file = new File(path);
			MusicMetadataSet src_set = new MyID3().read(file);
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			comment = metadata.getComment();
		} catch (Exception e) {
			android.util.Log.d(getClass().getSimpleName(), "Exception! Metadata is bad. " + e);
		}
		return null == comment || comment.isEmpty() ? EMPTY_COMMENT : comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
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
		return title.replace("?", "");
	}

	public String getArtist() {
		return artist.replace("?", "");
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
	
	@Override
	public MusicData cloneSong() throws CloneNotSupportedException {
		return clone();
	}

	@Override
	protected MusicData clone() throws CloneNotSupportedException {
		return (MusicData) super.clone();
	}

	@Override
	public int compareTo(@NonNull MusicData another) {
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
	public int compare(MusicData lhs, MusicData rhs) {
		String arg1 = lhs.getArtist() + lhs.getTitle();
		String arg2 = rhs.getArtist() + rhs.getTitle();
		return arg1.compareToIgnoreCase(arg2);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (null == object || this.getClass() != object.getClass()) return false;
		MusicData another = (MusicData) object;
        return this.id == another.id;
    }

	public void turnOff(int flag) {
		setFlag(flag, false);
	}

	public void turnOn(int flag) {
		setFlag(flag, true);
	}

	public boolean check(int flag) {
		int buf = mode;
        return (buf & flag) == flag;
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

	@Override
	public String getDownloadUrl() {
		return null;
	}
}
