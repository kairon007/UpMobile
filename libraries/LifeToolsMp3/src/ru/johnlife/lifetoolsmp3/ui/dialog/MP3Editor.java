package ru.johnlife.lifetoolsmp3.ui.dialog;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import ru.johnlife.lifetoolsmp3.R;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class MP3Editor{
	
	private Context context;
	private View view;
	private EditText etArtistName;
	private EditText etSongTitle;
	private EditText etAlbumTitle;
	private CheckBox checkBox;
	private String newArtistName;
	private String newSongTitle;
	private String newAlbumTitle;
	private boolean showCover = true;
	
	public MP3Editor(){
	}
	
	/**s
	 * Use into AlertDialog
	 * @param context
	 */
	public MP3Editor(Context context){
		this.context = context;
	}

	public View getView() {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.view = inflater.inflate(R.layout.editor_dialog, null);
		init(view);
		return view;
	}

	private void init(View view) {
		etArtistName = (EditText) view.findViewById(R.id.editTextArtist);
		etSongTitle = (EditText) view.findViewById(R.id.editTextTitle);
		etAlbumTitle = (EditText) view.findViewById(R.id.editTextAlbum);
		checkBox = (CheckBox) view.findViewById(R.id.isShowCover);
	}
	
	void onClick(View view){
		if (checkBox.isChecked()){
			Log.d("log", "checkBox checked");
			showCover = true;
		} else {
			Log.d("log", "checkBox don't checked");
			showCover = false;
		} 
	}
	
//	/**
//	 * Change metadata in music file
//	 * 
//	 * @param src - file on cd-card which should be changed
//	 * @return true if file will be changed, else false
//	 * @throws IOException
//	 * @throws ID3WriteException
//	 */
//	public boolean changeFileMetaData(File src) throws IOException, ID3WriteException{
//		if (!src.canRead() || !src.canWrite() || !src.exists()) {
//			throw new IOException("File is damaged.");
//		}
//		MusicMetadataSet src_set = new MyID3().read(src);
//		MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
//		String artist = null, title = null;
//		if(setArtistName()){
//			metadata.clearArtist();
//			metadata.setArtist(newArtistName);
//			artist = newArtistName;
//		} 
//		if(setAlbumTitle()){
//			metadata.clearAlbum();
//			metadata.setAlbum(newAlbumTitle);
//		}
//		if(setSongTitle()){
//			metadata.clearSongTitle();
//			metadata.setSongTitle(newSongTitle);
//			title = newSongTitle;
//		}
//		if (modifyFile(src_set, metadata, src, artist, title)) {
//			return true;
//		} else {
//			File dst = new File(src.getParentFile(), src.getName()+ "-1");
//			new MyID3().write(src, dst, src_set, metadata);  // write updated metadata
//			dst.delete();
//		}
//		return false;
//	}
//	
//	public boolean modifyFile(MusicMetadataSet src_set, MusicMetadata metadata, File src, String artist, String title) throws UnsupportedEncodingException, IOException, ID3WriteException {
//		File dst ;
//		if (null == artist && null == title){
//			return false;
// 		} else if (null != artist && null == title) {
//			dst = new File(src.getParentFile(), artist + ".mp3");
//		} else if (null != title && null == artist) {
//			dst = new File(src.getParentFile(), title + ".mp3");
//		} else {
//			dst = new File(src.getParentFile(), artist + " - " + title + ".mp3");
//		}
//		new MyID3().write(src, dst, src_set, metadata);
//		src.renameTo(dst);
//		return true;
//	}
	
	private boolean setArtistName() {
		String str = etArtistName.getText().toString();
		Log.d("log", "artist edit text = " + str);
		if (!str.equals("")) {
			newArtistName = str;
			return true;
		}
		return false;
	}
	
	private boolean setSongTitle() {
		String str = etSongTitle.getText().toString();
		Log.d("log", "song edit text = " + str);
		if (!str.equals("")) {
			newSongTitle = str;
			return true;
		}
		return false;
	}
	
	private boolean setAlbumTitle() {
		String str = etAlbumTitle.getText().toString();
		Log.d("log", "album edit text = " + str);
		if (!str.equals("")) {
			newAlbumTitle = str;
			return true;
		}
		return false;
	}

	public String getNewArtistName() {
		if (setArtistName()) {
			return newArtistName;
		}
		return null;
	}

	public String getNewSongTitle() {
		if (setSongTitle()) {
			return newSongTitle;
		}
		return null;
	}

	public String getNewAlbumTitle() {
		if (setAlbumTitle()) {
			return newAlbumTitle;
		}
		return null;
	}
}
