package org.kreed.musicdownloader.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Vector;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.musicdownloader.Constans;
import org.kreed.musicdownloader.DBHelper;

import android.graphics.Bitmap;
import android.os.Environment;
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
				if (null != metadata.getAlbum()) {
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
				if (null != metadata.getAlbum()) {
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
		update(newTag, false);
	}

	public void rename(MusicData newTag) {
		update(newTag, true);
	}

	private void update(MusicData newTag, boolean flag) {
		boolean switcher = false;
		if (null != newTag.fileUri) {
			fileUri = newTag.fileUri;
		}
		if (!newTag.useCover || !useCover) {
			if (null != songBitmap) {
				switcher = true;
			}
			songBitmap = null;
		}
		if (null != newTag.songGenre && !newTag.songGenre.equals(songGenre)) {
			songGenre = newTag.songGenre;
		}
		int i = 0;
		if (!newTag.songArtist.equals(songArtist)) {
			songArtist = newTag.songArtist;
		} else {
			++i;
		}
		if (!newTag.songTitle.equals(songTitle)) {
			songTitle = newTag.songTitle;
		} else {
			++i;
		}
		if (i == 2) {
			flag = false;
		}

		if (switcher) {
			deleteCoverFromFile();
		}
		if (flag) {
			renameBoundFile();
		}
	}

	private void deleteCoverFromFile() {
		File file = new File(fileUri);
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if (null == src_set) {
				return;
			}
			MusicMetadata m = (MusicMetadata) src_set.merged;
			m.clearPictureList();
			MusicMetadata metadata = new MusicMetadata("");
			metadata.setArtist(m.getArtist());
			metadata.setSongTitle(m.getSongTitle() + "/" + songDuration);
			metadata.setGenre(m.getGenre());
			metadata.setAlbum(m.getAlbum());
			File temp = new File(file.getParent(), file.getName() + ".temp");
			new MyID3().removeTags(file, temp);
			temp.renameTo(file);
			MusicMetadataSet src = new MyID3().read(file);
			File temp1 = new File(file.getParent(), file.getName() + ".temp1");
			new MyID3().write(file, temp1, src, metadata);
			temp1.renameTo(file);
		} catch (Exception e) {
		}
	}

	private void renameBoundFile() {
		File file = new File(fileUri);
		File newFile = null;
		MusicMetadataSet src_set = null;
		try {
			src_set = new MyID3().read(file);
			String newName = "";
			MusicMetadata metadata = src_set.merged;
			if (null != songAlbum && !songAlbum.equals("")) {
				metadata.setAlbum(songAlbum);
			}
			metadata.setSongTitle(songTitle + "/" + songDuration);
			metadata.setArtist(songArtist);
			newName = songTitle + " - " + songArtist + ".mp3";
			newFile = new File(file.getParentFile(), newName);
			fileUri = newFile.getAbsolutePath();
			new MyID3().write(file, newFile, src_set, metadata);
		} catch (Exception e) {
		} finally {
			file.delete();
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
