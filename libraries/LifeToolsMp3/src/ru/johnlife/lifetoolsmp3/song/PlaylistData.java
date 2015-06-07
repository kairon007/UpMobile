package ru.johnlife.lifetoolsmp3.song;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.provider.BaseColumns;
import android.provider.MediaStore;

public class PlaylistData implements AbstractSong{

	private final static String EXTERNAL = "external";

	private long id;

	private String name;

	private ArrayList<MusicData> songs;
	
	private boolean isExpanded = false;

	public final String[] PROJECTION_MUSIC = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM, };

	public void populate(Cursor cursor) {
		id = cursor.getLong(0);
		name = cursor.getString(1);
	}

	public PlaylistData() {
	}

	public PlaylistData(int id, String name, ArrayList<MusicData> songs) {
		this.setId(id);
		this.setName(name);
		this.setSongs(songs);
	}

	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<MusicData> getSongs() {
		return songs;
	}

	public void setSongs(ArrayList<MusicData> songs) {
		this.songs = songs;
	}

	public void deletePlaylist(Context context, long playlistId) {
		try {
			String playlistid = String.valueOf(playlistId);
			String where = BaseColumns._ID + "=?";
			String[] whereVal = { playlistid };
			context.getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addToPlaylist(Context context, long l, long m) {
		try {
			String[] cols = new String[] { "count(*)" };
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, m);
			Cursor cur = context.getContentResolver().query(uri, cols, null, null, null);
			cur.moveToFirst();
			final int base = cur.getInt(0);
			cur.close();
			ContentValues values = new ContentValues();
			values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf((int) (base + l)));
			values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, l);
			context.getContentResolver().insert(uri, values);
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removeFromPlaylist(Context context, long playlistId, long audioId) {
	    try {
	        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, playlistId);
	        String where = MediaStore.Audio.Playlists.Members._ID + "=?" ;
	        String audioId1 = Long.toString(audioId);
	        String[] whereVal = { audioId1 };
	        context.getContentResolver().delete(uri, where,whereVal);      
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	 }
	
	public ArrayList<MusicData> getSongsFromPlaylist(Context context, long playlistID) {
		Cursor cursor = myQuery(context, MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, Long.valueOf(playlistID)), PROJECTION_MUSIC, null, null, null);
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return result;
		}
		MusicData d = new MusicData();
		d.populate(cursor);
		result.add(d);
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);
			result.add(data);
		}
		cursor.close();
		return result;
	}

	public Cursor myQuery(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		try {
			ContentResolver resolver = context.getContentResolver();
			if (resolver == null) {
				return null;
			}
			return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (UnsupportedOperationException ex) {
			return null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public String getArtist() {
		return null;
	}

	@Override
	public String getAlbum() {
		return null;
	}

	@Override
	public Bitmap getCover() {
		return null;
	}

	@Override
	public boolean isHasCover() {
		return false;
	}

	@Override
	public long getDuration() {
		return 0;
	}

	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		return false;
	}

	@Override
	public String getDownloadUrl() {
		return null;
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public void setArtist(String artist) {}

	@Override
	public void setTitle(String title) {}

	@Override
	public void setAlbum(String album) {}

	@Override
	public void setPath(String path) {}

	@Override
	public AbstractSong cloneSong() throws CloneNotSupportedException {
		return null;
	}

	@Override
	public AbstractSpecial getSpecial() {
		return new AbstractSpecial();
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}
}
