package org.kreed.musicdownloader.data;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.jaudiotagger.audio.AudioFileIO;
import org.kreed.musicdownloader.DBHelper;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.graphics.Bitmap;

public class MusicData {

	private Bitmap songBitmap;
	private String songArtist;
	private String songTitle;
	private String songDuration;
	private double downloadProgress = -1.0;
	private String songGenre;
	private String songAlbum;
	private String fileUri;
	private long downloadId = -1;
	private boolean useCover = true;

	public MusicData() {
	}

	public MusicData(String songArtist, String songTitle, String songDuration, Bitmap songBitmap) {
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.songDuration = songDuration;
		this.songBitmap = songBitmap;
	}

	public MusicData(String songArtist, String songTitle, String songDuration, Double downloadProgress, Bitmap cover) {
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
		fileUri = musicFile.getAbsolutePath();
		try {
			MusicMetadataSet src_set = new MyID3().read(musicFile);
			if (src_set != null) {
				MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
				if (metadata.isEmpty()) {
					String nameFile = musicFile.getName().split(".mp3")[0];
					String[] nameFileArray;
					if (!nameFile.contains("-<")) {
						nameFileArray = nameFile.split(" - ");
					} else {
						nameFileArray = nameFile.split("-<")[0].split(" - ");
					}
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
					songDuration = Util.formatTimeSimple(seconds * 1000);
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
						android.util.Log.d("log", "don't write music metadata from file. " + e);
					} finally {
						if (dst.exists())
							dst.delete();
					}
				} else {
					songArtist = metadata.getArtist();
					songTitle = metadata.getSongTitle();
					songBitmap = DBHelper.getArtworkImage(2, metadata);
					int seconds = 0;
					try {
						seconds = AudioFileIO.read(musicFile).getAudioHeader().getTrackLength();
					} catch (Exception e) {
						e.printStackTrace();
					}
					songDuration = Util.formatTimeSimple(seconds * 1000);
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
				temp.setSongTitle(metadata.getSongTitle());
				int seconds = 0;
				try {
					seconds = AudioFileIO.read(file).getAudioHeader().getTrackLength();
				} catch (Exception e) {
					e.printStackTrace();
				}
				temp.setSongDuration(Util.formatTimeSimple(seconds * 1000));
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

	public void update(MusicData newTag) {
		update(newTag, false);
	}

	public void rename(MusicData newTag) {
		update(newTag, false);
	}

	private void update(MusicData newTag, boolean flag) {
		boolean switcher = false;
		if (null != newTag.fileUri) {
			fileUri = newTag.fileUri;
		}
		if (!useCover) {
			if (null != songBitmap) {
				switcher = true;
			}
			songBitmap = null;
		}
		if (null != newTag.songGenre && !newTag.songGenre.equals(songGenre)) {
			songGenre = newTag.songGenre;
		}
		if (!newTag.songArtist.equals(songArtist)) {
			songArtist = newTag.songArtist;
			flag = true;
		}
		if (!newTag.songTitle.equals(songTitle)) {
			songTitle = newTag.songTitle;
			flag = true;
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
			metadata.setSongTitle(m.getSongTitle());
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
		String strCompare = songArtist + " - "  +songTitle;
		int index = Util.existFile(file.getParent(), strCompare);
		File newFile = null;
		MusicMetadataSet src_set = null;
		try {
			src_set = new MyID3().read(file);
			String newName = "";
			MusicMetadata metadata = src_set.merged;
			if (null != songAlbum && !songAlbum.equals("")) {
				metadata.setAlbum(songAlbum);
			}
			metadata.setSongTitle(songTitle);
			metadata.setArtist(songArtist);
			if (index < 1) {
				newName = songArtist+ " - " + songTitle + ".mp3";
			} else {
				newName = songArtist + " - " + songTitle + "-<" + (index) + ">.mp3";
			}
			newFile = new File(file.getParentFile(), newName);
			fileUri = newFile.getAbsolutePath();
			new MyID3().write(file, newFile, src_set, metadata);
		} catch (Exception e) {
		} finally {
			file.delete();
		}
	}

	public boolean isDownloaded() {
		if (null != fileUri || downloadProgress == -1.0 || downloadProgress > 99.0) {
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

	public Double getDownloadProgress() {
		return downloadProgress;
	}

	public void setDownloadProgress(Double downloadProgress) {
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
		if (this.songArtist != another.songArtist) {
			return false;
		}
		if (this.songTitle != another.songTitle) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
