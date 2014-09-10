package org.kreed.musicdownloader.data;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.musicdownloader.DBHelper;

import android.graphics.Bitmap;
import android.util.Log;

public class MusicData {
	
	private Bitmap songBitmap;
	private String songArtist;
	private String songTitle;
	private String songDuration;
	private String downloadProgress;
	private String songGenre;
	private String songAlbum;
	private String fileUri;
	private long downloadId;
	private boolean useCover = true;
	
	public MusicData() {
	}

	public MusicData(String songArtist, String songTitle, String songDuration, Bitmap songBitmap) {
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.songDuration = songDuration;
		this.songBitmap = songBitmap;
	}

	public MusicData(String songArtist, String songTitle, String songDuration, String downloadProgress, Bitmap cover) {
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.songDuration = songDuration;
		this.downloadProgress = downloadProgress;
		this.songBitmap = cover;
	}

	public MusicData(String songArtist, String songTitle, String songDuration, Bitmap cover, String songGenre) {
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.songDuration = songDuration;
		this.songBitmap = cover;
		this.songGenre = songGenre;
	}

	public MusicData(File musicFile) {
		try {
			MusicMetadataSet src_set = new MyID3().read(musicFile);
			if (src_set != null) {
				MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
				if (metadata.isEmpty()) {
					return;
				}
				songArtist = metadata.getArtist();
				songBitmap = DBHelper.getArtworkImage(2, metadata);
				if(null != metadata.getAlbum()) {
					songAlbum = metadata.getAlbum();
				}
				if (metadata.getSongTitle() != null) {
					int index = metadata.getSongTitle().indexOf('/');
					fileUri = musicFile.getAbsolutePath();
					if (index != -1) {
						songTitle = metadata.getSongTitle().substring(0, index);
						songDuration = metadata.getSongTitle().substring(index + 1);
					} else {
						songTitle = metadata.getSongTitle();
					}
				}
				if (metadata.containsKey("genre_id")) {
					int genre_id = (Integer) metadata.get("genre_id");
					songGenre = ID3v1Genre.get(genre_id);
				} else {
					songGenre = "unknown";
				}
			} else {
			}
		} catch (IOException e) {
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
				Bitmap bitmap = DBHelper.getArtworkImage(2, metadata);
				temp.setSongBitmap(bitmap);
				if(null != metadata.getAlbum()) {
					temp.setSongAlbum(metadata.getAlbum());
				}
				if (metadata.getSongTitle() != null) {
					int index = metadata.getSongTitle().indexOf('/');
					temp.setFileUri(file.getAbsolutePath());
					if (index != -1) {
						String title = metadata.getSongTitle().substring(0, index);
						String duration = metadata.getSongTitle().substring(index + 1);
						temp.setSongTitle(title);
						temp.setSongDuration(duration);
					} else {
						temp.setSongTitle(metadata.getSongTitle());
					}
				}
				if (metadata.containsKey("genre_id")) {
					int genre_id = (Integer) metadata.get("genre_id");
					temp.setSongGenre(ID3v1Genre.get(genre_id));
				} else {
					temp.setSongGenre("unknown");
				}
			} else {
			}
		} catch (IOException e) {
		}
		return temp;
	}
	
	public void update(MusicData newTag) {
		rename(newTag, false);
	}
	
	public void rename(MusicData newTag) {
		rename(newTag, true);
	}
	
	public void deleteCover(MusicData newTag) {
		File file = new File(newTag.fileUri);
		deleteCover(file);
	}

	private void rename(MusicData newTag, boolean flag) {
		this.songArtist = newTag.getSongArtist().equals(this.songArtist) ? this.songArtist : newTag.getSongArtist();
		this.songTitle = newTag.getSongTitle().equals(this.songTitle) ? this.songTitle : newTag.getSongTitle();
		this.songAlbum = newTag.getSongAlbum().equals(this.songAlbum) ? this.songAlbum : newTag.getSongAlbum();
		if (flag) {
			renameBoundFile(this.fileUri);
		}
	}
	
	private void deleteCover(File file) {
		this.songBitmap = null;
		MusicMetadataSet src_set;
		try {
			src_set = new MyID3().read(file);
			if (src_set != null) {
				MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
				metadata.clearPictureList();
				new MyID3().update(file, src_set, metadata);
			}
		} catch (Exception e) {
		}
	}
	
	private void renameBoundFile(String path) {
		File file = new File(path);
		if (!useCover && null != this.songBitmap) {
			deleteCover(file);
		}
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if (src_set == null) {
				return;
			}
			String newName = "";
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			if (null != this.songAlbum && !this.songAlbum.equals("")) {
				metadata.setAlbum(this.songAlbum);
			}
			metadata.setSongTitle(this.songTitle);
			metadata.setArtist(this.songArtist);
			newName = this.songTitle + " - " + this.songArtist + ".mp3";
			File newFile = new File(file.getParentFile(), newName);
			fileUri = newFile.getAbsolutePath();
			new MyID3().update(newFile, src_set, metadata);
			file.renameTo(newFile);
		} catch (Exception e) {
		}
	}
	
	public boolean isUseCover() {
		return useCover;
	}

	public void setUseCover(boolean useCover) {
		this.useCover = useCover;
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
		return songDuration;
	}

	public void setSongDuration(String songDuration) {
		this.songDuration = songDuration;
	}

	public Bitmap getSongBitmap() {
		return songBitmap;
	}

	public void setSongBitmap(Bitmap songBitmap) {
		this.songBitmap = songBitmap;
	}

	public String getDownloadProgress() {
		return downloadProgress;
	}

	public void setDownloadProgress(String downloadProgress) {
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
		if (this.songArtist != another.songArtist) {
			return false;
		}
		if (this.songTitle != another.songTitle) {
			return false;
		}
		return true;
	}

}
