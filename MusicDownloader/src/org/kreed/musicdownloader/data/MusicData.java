package org.kreed.musicdownloader.data;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.jaudiotagger.audio.AudioFileIO;
import org.kreed.musicdownloader.DBHelper;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class MusicData {

	private Object tag;
	private Bitmap songBitmap;
	private String songArtist;
	private String songTitle;
	private String songDuration;
	private String songGenre;
	private String songAlbum;
	private String fileUri;
	private long downloadProgress = -1;
	private long downloadId = -1;
	private boolean useCover = true;

	public MusicData() {
	}
	
	public static final String[] FILLED_PROJECTION = {
		MediaStore.Audio.Media._ID,
		MediaStore.Audio.Media.DATA,
		MediaStore.Audio.Media.TITLE,
		MediaStore.Audio.Media.ARTIST,
		MediaStore.Audio.Media.DURATION,
		MediaStore.Audio.Media.ALBUM,
	};
	
	public MusicData(String songArtist, String songTitle, String songAlbum, String fileUri) {
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.songAlbum = songAlbum;
		this.fileUri = fileUri;
	}
	
	public void populate(Cursor cursor) {
		fileUri = cursor.getString(1);
		songTitle = cursor.getString(2);
		songArtist = cursor.getString(3);
		songAlbum = cursor.getString(5);
	}

	public MusicData(File musicFile) {trololo
		fileUri = musicFile.getAbsolutePath();
		try {
			MusicMetadataSet src_set = new MyID3().read(musicFile);
			if (src_set != null) {
				MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
				if (metadata.isEmpty()) {
					String nameFile = musicFile.getName().split(".mp3")[0];
					String[] nameFileArray;
					nameFileArray = nameFile.split(" - ");
					if (nameFileArray.length > 0) {
						songTitle = nameFileArray[1];
						songArtist = nameFileArray[0];
						
					} else {
						songTitle = MP3Editor.UNKNOWN;
						songArtist = MP3Editor.UNKNOWN;
					}
					metadata.setSongTitle(songTitle);
					metadata.setArtist(songArtist);
					int seconds = 0;
					try {
						seconds = AudioFileIO.read(musicFile).getAudioHeader().getTrackLength();
					} catch (Exception e) {
						e.printStackTrace();
					}
					songDuration = Util.getFormatedStrDuration(seconds * 1000);
					if (metadata.containsKey("genre_id")) {
						int genre_id = (Integer) metadata.get("genre_id");
						songGenre = ID3v1Genre.get(genre_id);
					} else {
						songGenre = MP3Editor.UNKNOWN;
					}
					File dst = new File(musicFile.getParentFile(), musicFile.getName() + "-1");
					try {
						new MyID3().write(musicFile, dst, src_set, metadata);
						dst.renameTo(musicFile);
					} catch (Exception e) {
					} finally {
						if (dst.exists())
							dst.delete();
					}
				} else {
					songArtist = metadata.getArtist();
					songTitle = metadata.getSongTitle();
					int seconds = 0;
					try {
						seconds = AudioFileIO.read(musicFile).getAudioHeader().getTrackLength();
					} catch (Exception e) {
						e.printStackTrace();
					}
					songDuration = Util.getFormatedStrDuration(seconds * 1000);
					if (null != metadata.getAlbum()) {
						songAlbum = metadata.getAlbum();
					}

					if (metadata.containsKey("genre_id")) {
						int genre_id = (Integer) metadata.get("genre_id");
						songGenre = ID3v1Genre.get(genre_id);
					} else {
						songGenre = MP3Editor.UNKNOWN;
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public static MusicData getFromFile(File file) {
		MusicData temp = new MusicData();
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if (src_set != null) {
				MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
				if (metadata.isEmpty()) {
					return temp;
				}
				temp.setSongArtist(metadata.getArtist());
				temp.setSongTitle(metadata.getSongTitle());
				int seconds = 0;
				try {
					seconds = AudioFileIO.read(file).getAudioHeader().getTrackLength();
				} catch (Exception e) {
					e.printStackTrace();
				}
				temp.setSongDuration(Util.getFormatedStrDuration(seconds * 1000));
				Bitmap bitmap = DBHelper.getArtworkImage(2, metadata);
				temp.setSongBitmap(bitmap);
				if (null != metadata.getAlbum()) {
					temp.setSongAlbum(metadata.getAlbum());
				}
				if (metadata.containsKey("genre_id")) {
					int genre_id = (Integer) metadata.get("genre_id");
					temp.setSongGenre(ID3v1Genre.get(genre_id));
				} else {
					temp.setSongGenre(MP3Editor.UNKNOWN);
				}
			} else {
			}
		} catch (IOException e) {
		}
		return temp;
	}
	
	public void update(MusicData newData) { 
		if (null != newData.fileUri) {
			fileUri = newData.fileUri;
		} 
		if (!newData.useCover) {
			songBitmap = null;
		}
		if (null != newData.songGenre && !newData.songGenre.equals(songGenre)) {
			songGenre = newData.songGenre;
		}
		if (newData.songArtist != null && !newData.songArtist.equals(songArtist)) {
			songArtist = newData.songArtist;
		}
		if (newData.songTitle != null && !newData.songTitle.equals(songTitle)) {
			songTitle = newData.songTitle;
		}
	}

	public boolean isDownloaded() {
		if (downloadProgress == -1.0 || downloadProgress > 99.0) {
			return true;
		}
		return false;
	}

	public boolean isUseCover() {
		return useCover;
	}

	public void setUseCover(boolean useCover) {
		this.useCover = useCover;
	}
	
	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public String getSongAlbum() {
		return songAlbum;
	}

	public void setSongAlbum(String songAlbum) {
		this.songAlbum = songAlbum;
	}

	public String getSongArtist() {
		return songArtist;
	}

	public void setSongArtist(String songArtist) {
		this.songArtist = songArtist;
	}

	public String getSongTitle() {
		return songTitle;
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public String getSongDuration() {
		if (null == songDuration || songDuration.isEmpty()) {
			try {
				return Util.getFormatedStrDuration(AudioFileIO.read(new File(fileUri)).getAudioHeader().getTrackLength() * 1000);
			} catch (Exception e) {
				return "0:00";
			}
		} else {
			return songDuration;
		}
	}

	public void setSongDuration(String songDuration) {
		this.songDuration = songDuration;
	}

	public Bitmap getSongBitmap() {
		try {
			MusicMetadataSet src_set = new MyID3().read(new File(fileUri));
			if (src_set != null) {
				MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
				Bitmap bitmap = DBHelper.getArtworkImage(2, metadata);
				if (bitmap != null) {
					return bitmap;
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e + "");
		}
		return songBitmap;
	}

	public void setSongBitmap(Bitmap songBitmap) {
		this.songBitmap = songBitmap;
	}

	public long getDownloadProgress() {
		return downloadProgress;
	}

	public void setDownloadProgress(long downloadProgress) {
		this.downloadProgress = downloadProgress;
	}

	public void setDownloadId(long downloadId) {
		this.downloadId = downloadId;
	}

	public long getDownloadId() {
		return downloadId;
	}

	public String getSongGenre() {
		return songGenre;
	}

	public void setSongGenre(String songGenre) {
		this.songGenre = songGenre;
	}

	public String getFileUri() {
		return fileUri;
	}

	public void setFileUri(String fileUri) {
		this.fileUri = fileUri;
	}

	@Override
	public String toString() {
		if (null == songArtist && null == songTitle)
			return "null";
		if (null != songArtist && null == songTitle)
			return getSongArtist().toLowerCase(Locale.ENGLISH);
		if (null == songArtist && null != songTitle)
			return getSongTitle().toLowerCase(Locale.ENGLISH);
		return getSongArtist().toLowerCase(Locale.ENGLISH) + " - " + getSongTitle().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (null == o) {
			return false;
		}
		if (this.getClass() != o.getClass()) {
			return false;
		}
		MusicData another = (MusicData) o;
		if (this.songArtist != null && another.songArtist != null)
			if (!this.songArtist.equals(another.songArtist)) {
				return false;
			}
		if (this.songTitle != null && another.songTitle != null)
			if (!this.songTitle.equals(another.songTitle)) {
				return false;
			}
		if (fileUri!=null && another.fileUri!=null) 
			if (!fileUri.equals(another.fileUri))
				return false;
		if (this.downloadId != another.downloadId)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		if (songArtist != null) {
			hash *= songArtist.hashCode();
		}
		if (songTitle != null) {
			hash *= songTitle.hashCode();
		}
		if (null != fileUri && !fileUri.equals("")) {
			hash *= fileUri.hashCode();
		}
		return hash;
	}

}
