package ru.johnlife.lifetoolsmp3.song;

import android.graphics.Bitmap;
import android.os.Parcelable;

import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;

public interface AbstractSong extends Parcelable, Cloneable {
	
	String EMPTY_COMMENT = "in_data_comment_is_empty";
	String EMPTY_PATH = "comment_does_not_get_path";
	
	String getPath();

	String getTitle();

	String getArtist();
	
	String getAlbum();
	
	Bitmap getCover();

	boolean isHasCover();
	
	long getId();

	long getDuration();
	
	boolean getDownloadUrl(DownloadUrlListener listener);
	
	String getDownloadUrl();
	
	String getComment();
	
	void setArtist(String artist);
	
	void setTitle(String title);
	
	void setAlbum(String album);
	
	void setPath(String path);
	
	AbstractSong cloneSong() throws CloneNotSupportedException;

	AbstractSpecial getSpecial();
}
