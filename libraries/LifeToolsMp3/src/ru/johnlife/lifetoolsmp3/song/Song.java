/*
 * Copyright (C) 2010, 2011 Christopher Eby <kreed@kreed.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ru.johnlife.lifetoolsmp3.song;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.util.Locale;

import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.utils.Util;

/**
 * Represents a Song backed by the MediaStore. Includes basic metadata and
 * utilities to retrieve songs from the MediaStore.
 */
public class Song implements Comparable<Song>, AbstractSong {
	/**
	 * Indicates that this song was randomly selected from all songs.
	 */
	public static final int FLAG_RANDOM = 0x1;
	/**
	 * If set, this song has no cover art. If not set, this song may or may not
	 * have cover art.
	 */
	public static final int FLAG_NO_COVER = 0x2;
	/**
	 * The number of flags.
	 */
	public static final int FLAG_COUNT = 2;

	public static final String[] EMPTY_PROJECTION = { BaseColumns._ID, };

	public static final String[] FILLED_PROJECTION = { 
							BaseColumns._ID, 
							MediaColumns.DATA, 
							MediaColumns.TITLE, 
							AudioColumns.ALBUM, 
							AudioColumns.ARTIST, 
							AudioColumns.ALBUM_ID, 
							AudioColumns.ARTIST_ID, 
							AudioColumns.DURATION, 
							AudioColumns.TRACK, 
	};

	public static final String[] EMPTY_PLAYLIST_PROJECTION = { MediaStore.Audio.Playlists.Members.AUDIO_ID, };

	public static final String[] FILLED_PLAYLIST_PROJECTION = { 
							MediaStore.Audio.Playlists.Members.AUDIO_ID, 
							MediaColumns.DATA, 
							MediaColumns.TITLE, 
							AudioColumns.ALBUM, 
							AudioColumns.ARTIST,
							AudioColumns.ALBUM_ID, 
							AudioColumns.ARTIST_ID, 
							AudioColumns.DURATION, 
							AudioColumns.TRACK, 
	};

	/**
	 * If true, will not attempt to load any cover art in getCover()
	 */
	public static boolean mDisableCoverArt = false;

	/**
	 * Id of this song in the MediaStore
	 */
	public long id;
	/**
	 * Id of this song's album in the MediaStore
	 */
	public long albumId;
	/**
	 * Id of this song's artist in the MediaStore
	 */
	public long artistId;

	/**
	 * Path to the data for this song
	 */
	public String path;

	/**
	 * Song title
	 */
	public String title;
	/**
	 * Album name
	 */
	public String album;
	/**
	 * Artist name
	 */
	public String artist;

	/**
	 * Length of the song in milliseconds.
	 */
	public long duration;
	/**
	 * The position of the song in its album.
	 */
	public int trackNumber;

	/**
	 * Song flags. Currently {@link #FLAG_RANDOM} or {@link #FLAG_NO_COVER}.
	 */
	public int flags;

	/**
	 * Initialize the song with the specified id. Call populate to fill fields
	 * in the song.
	 */
	
	protected String comment;

	protected String downloadUrl;
	private AbstractSpecial special;
	public Bitmap cover;

	private boolean hasCover = false;

	public Song(long id) {
		this.id = id;
	}

	/**
	 * Initialize the song with the specified id and flags. Call populate to
	 * fill fields in the song.
	 */
	public Song(long id, int flags) {
		this.id = id;
		this.flags = flags;
	}

	/**
	 * Return true if this song was retrieved from randomSong().
	 */
	public boolean isRandom() {
		return (flags & FLAG_RANDOM) != 0;
	}

	/**
	 * Populate fields with data from the supplied cursor.
	 * 
	 * @param cursor
	 *            Cursor queried with FILLED_PROJECTION projection
	 */
	public void populate(Cursor cursor) {
		id = cursor.getLong(0);
		path = cursor.getString(1);
		String zaycevTag = "(zaycev.net)";
		title = cursor.getString(2);
		if (title != null && title.toUpperCase(Locale.getDefault()).contains(zaycevTag.toUpperCase()))	title = title.replace(zaycevTag, "");
		album = cursor.getString(3);
		artist = cursor.getString(4);
		albumId = cursor.getLong(5);
		artistId = cursor.getLong(6);
		duration = cursor.getLong(7);
		trackNumber = cursor.getInt(8);
	}

	/**
	 * Get the id of the given song.
	 * 
	 * @param song
	 *            The Song to get the id from.
	 * @return The id, or 0 if the given song is null.
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * Query the album art for this song.
	 * 
	 * @param context
	 *            A context to use.
	 * @return The album art or null if no album art could be found
	 */
	@Override
	public Bitmap getCover() {
		if (cover != null) {
			return cover;
		}
		if (mDisableCoverArt || id == -1 || (flags & FLAG_NO_COVER) != 0 || null == path) {
			return null;
		}
		File file = new File(path);
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if  (null == src_set) {
				flags |= FLAG_NO_COVER;
				return cover;
			}
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			if  (null == metadata) {
				flags |= FLAG_NO_COVER;
				return cover;
			}
			cover = Util.getArtworkImage(2, metadata, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cover == null) flags |= FLAG_NO_COVER;
		return cover;
	}

	public boolean hasCover() {
		return cover != null;
	}

	@Override
	public String toString() {
		return String.format(Locale.getDefault(), "%d %d %s %s", id, albumId, path, artist + " - " + title);
	}

	/**
	 * Compares the album ids of the two songs; if equal, compares track order.
	 */
	@Override
	public int compareTo(Song other) {
		if (albumId == other.albumId)
			return trackNumber - other.trackNumber;
		if (albumId > other.albumId)
			return 1;
		return -1;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (null == object)
			return false;
		if (this.getClass() != object.getClass())
			return false;
		Song another = (Song) object;
		if (this.id != another.id)
			return false;
		return true;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		return false;
	}
	
	@Override
	public String getDownloadUrl() {
		return downloadUrl;
	}

	@Override
	public String getArtist() {
		return artist;
	}

	public long getDuration() {
		return duration;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getAlbum() {
		return album;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		parcel.writeLong(id);
		parcel.writeString(path);
		parcel.writeString(title);
		parcel.writeString(album);
		parcel.writeLong(albumId);
		parcel.writeString(artist);
		parcel.writeLong(artistId);
		parcel.writeLong(duration);
		parcel.writeInt(trackNumber);
		parcel.writeString(downloadUrl);
		parcel.writeString(comment);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public Song(Parcel parcel) {
		id = parcel.readLong();
		path = parcel.readString();
		title = parcel.readString();
		album = parcel.readString();
		albumId = parcel.readLong();
		artist = parcel.readString();
		artistId = parcel.readLong();
		duration = parcel.readLong();
		trackNumber = parcel.readInt();
		downloadUrl = parcel.readString();
		comment = parcel.readString();
	}

	@Override
	public void setAlbum(String album) {
		this.album = album;
	}

	public void setCover(Bitmap cover) {
		this.cover = cover;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
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
	public Song cloneSong() throws CloneNotSupportedException {
		return clone();
	}

	@Override
	protected Song clone() throws CloneNotSupportedException {
		return (Song) super.clone();
	}

	@Override
	public boolean isHasCover() {
		if (hasCover()) {
			return true;
		}
		return this.hasCover;
	}

	public void setHasCover(boolean hasCover) {
		this.hasCover = hasCover;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public AbstractSpecial getSpecial() {
		if (null == special) {
			special = new AbstractSpecial();
		}
		return special;
	}
}
