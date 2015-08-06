package ru.johnlife.lifetoolsmp3.song;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Aleksandr on 04.08.2015.
 */

public class ArtistData implements Comparable<ArtistData>, AbstractSong {

    private long id;
    private String artist;
    private int numberOfTracks;
    private ArrayList<MusicData> artistSongs;
    private boolean isExpanded = false;

    public static final String[] FILLED_PROJECTION = {
            BaseColumns._ID,
            MediaStore.Audio.ArtistColumns.ARTIST,
            MediaStore.Audio.ArtistColumns.ARTIST_KEY,
            MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
    };

    public static final Parcelable.Creator<ArtistData> CREATOR = new Parcelable.Creator<ArtistData>() {

        @Override
        public ArtistData createFromParcel(Parcel source) {
            return new ArtistData(source);
        }

        @Override
        public ArtistData[] newArray(int size) {
            return new ArtistData[size];
        }
    };

    public ArtistData(long id, String artist, String artistKey, int numberOfTracks) {
        this.id = id;
        this.artist = artist;
        this.numberOfTracks = numberOfTracks;
    }

    private ArtistData(Parcel parcel) {
        id = parcel.readLong();
        artist = parcel.readString();
        numberOfTracks = parcel.readInt();
    }

    public ArtistData() {

    }

    public void populate(Cursor cursor) {
        id = cursor.getLong(0);
        artist = cursor.getString(1);
        numberOfTracks = cursor.getInt(2);
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getTitle() {
        return artist;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    @Override
    public String getAlbum() {
        return artist;
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
    public long getId() {
        return id;
    }

    @Override
    public long getDuration() {
        return numberOfTracks;
    }

    @Override
    public boolean getDownloadUrl(RemoteSong.DownloadUrlListener listener) {
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
    public void setArtist(String artist) {
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public void setAlbum(String album) {
    }

    @Override
    public void setPath(String path) {
    }

    @Override
    public AbstractSong cloneSong() throws CloneNotSupportedException {
        return null;
    }

    @Override
    public AbstractSpecial getSpecial() {
        return null;
    }

    @Override
    public int compareTo(@NonNull ArtistData another) {
        if (id > another.id)
            return 1;
        else if (id < another.id)
            return -1;
        else
            return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    private Cursor makeArtistSongCursor(String[] projection, Context context) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.ARTIST_ID + "=" + id);
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(), null, MediaStore.Audio.AudioColumns.TITLE);
    }

    public ArrayList<MusicData> getSongsByArtist(Context context) {
        artistSongs = new ArrayList<>();
        Cursor cursor = makeArtistSongCursor(MusicData.FILLED_PROJECTION, context);
        if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
            cursor.close();
            return artistSongs;
        }
        MusicData d = new MusicData();
        d.populate(cursor);
        artistSongs.add(d);
        while (cursor.moveToNext()) {
            MusicData data = new MusicData();
            data.populate(cursor);
            artistSongs.add(data);
        }
        cursor.close();
        return artistSongs;
    }

    public ArrayList<MusicData> getArtistSongs() {
        return artistSongs;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public int getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setNumberOfTracks(int numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }

    @Override
    public String toString() {
        return id + " " + artist + " " + numberOfTracks;
    }
}
