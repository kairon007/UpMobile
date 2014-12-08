package ru.johnlife.lifetoolsmp3.song;

import android.os.Parcelable;

public interface AbstractSong extends Parcelable{
	
	public String getPath();

	public String getTitle();

	public String getArtist();

	public long getId();

	public long getDuration();

}
