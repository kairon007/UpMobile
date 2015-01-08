package org.upmobile.clearmusicdownloader.data;

import java.io.File;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

public class MusicData implements Comparable<MusicData>, AbstractSong{

	public static final String[] FILLED_PROJECTION = {
		MediaStore.Audio.Media._ID,
		MediaStore.Audio.Media.DATA,
		MediaStore.Audio.Media.TITLE,
		MediaStore.Audio.Media.ARTIST,
		MediaStore.Audio.Media.DURATION,
		MediaStore.Audio.Media.ALBUM,
	};
	public static final int MODE_VISIBLITY = 0x00000001;
	public static final int MODE_PLAYING = 0x00000002;
	private Bitmap cover;
	private String path;
	private String title;
	private String artist;
	private String album;
	private int mode;
	private long id;
	private long duration;
	private int progress;
	
	public MusicData() {
		
	}
	
	public MusicData(String title, String artist, long id, long duration) {
		this.title = title;
		this.artist = artist;
		this.id = id;
		this.duration = duration;
	}

	private MusicData(Parcel parcel) {
		id = parcel.readLong();
		duration = parcel.readLong();
		path = parcel.readString();
		title = parcel.readString();
		artist = parcel.readString();
		progress = parcel.readInt();
		album = parcel.readString();
	}

	public void populate(Cursor cursor) {
		id = cursor.getLong(0);
		path = cursor.getString(1);
		title = cursor.getString(2);
		artist = cursor.getString(3);
		duration = cursor.getLong(4);
		album = cursor.getString(5);
	}
	
	public void reset(final Context context) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				ContentResolver resolver = context.getContentResolver();
				String where = MediaStore.Audio.Media._ID + "=" + id;
				resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null);
				File file = new File(path);
				if (file.exists()) file.delete();
				else android.util.Log.i(getClass().getCanonicalName(), "Attention! File "+ artist + " - " + title + ".mp3 " + " doesn't exist");
			}
		}).start();
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
			android.util.Log.d(getClass().getSimpleName(), "Exeption! Metadata is bad. " + e.getMessage());
			return null;
		}
		return cover;
	}
	
	@Override
	public boolean isHasCover() {
		return cover != null;
	}
	
	@Override
	public int compareTo(MusicData other) {
		if (id > other.id) return 1;
		else if (id < other.id) return -1;
		else return 0;
	}
	
	@Override
	public String toString() {
		return String.format("%d %s", id, path);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)	return true;
		if (null == object) return false;
		if (this.getClass() != object.getClass()) return false;
		MusicData another = (MusicData) object;
		if (this.id != another.id) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		parcel.writeLong(id);
		parcel.writeLong(duration);
		parcel.writeString(path);
		parcel.writeString(title);
		parcel.writeString(artist);
		parcel.writeInt(progress);
		parcel.writeString(album);
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
	
	public void turnOff(int flag) {
		setFlag(flag, false);
	}
	
	public void turnOn(int flag) {
		setFlag(flag, true);
	}
	
	public boolean check(int flag) {
		int buf =  mode;
		boolean result  = (buf & flag) == flag;
		return result;  
	}
	
	private void setFlag(int flag, boolean onOff) {
		if ((check(flag) && onOff) || (!check(flag) && !onOff)) return;
		if (onOff) {
			mode |= flag;
		} else {
			mode &= ~flag;
		}
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
		
	public void setPath(String path) {
		this.path = path;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	@Override
	public String getAlbum() {
		return album;
	}

	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		return false;
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

	@Override
	public MusicData cloneSong(){
		try {
			return (MusicData) clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}