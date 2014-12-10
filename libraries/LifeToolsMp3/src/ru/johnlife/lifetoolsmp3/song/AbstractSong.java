package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.os.Parcelable;

public interface AbstractSong extends Parcelable{
	
	public String getPath();

	public String getTitle();

	public String getArtist();
	
	public String getAlbum();

	public long getId();

	public long getDuration();
	
	public boolean getDownloadUrl(DownloadUrlListener listener);
	
	public void setNewArtist(String artist);
	
	public void setNewTitle(String title);
	
	public void setAlbum(String album);
	
	public void setPath(String path);

}
