package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;

public interface AbstractSong extends Parcelable, Cloneable {
	
	public static final String EMPTY_COMMENT = "in_data_comment_is_empty";
	public static final String EMPTY_PATH = "comment_does_not_get_path";
	
	public String getPath();

	public String getTitle();

	public String getArtist();
	
	public String getAlbum();
	
	public Bitmap getCover();

	public boolean isHasCover();
	
	public long getId();

	public long getDuration();
	
	public boolean getDownloadUrl(DownloadUrlListener listener);
	
	public String getDownloadUrl();
	
	public String getComment();
	
	public void setArtist(String artist);
	
	public void setTitle(String title);
	
	public void setAlbum(String album);
	
	public void setPath(String path);
	
	public AbstractSong cloneSong() throws CloneNotSupportedException;

	public AbstractSpecial getSpecial();
}
