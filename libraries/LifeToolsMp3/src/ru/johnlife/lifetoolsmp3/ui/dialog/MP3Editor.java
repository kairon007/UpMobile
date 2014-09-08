package ru.johnlife.lifetoolsmp3.ui.dialog;

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

	public MP3Editor(Context context){
		this.context = context;
	}

	public View getView() {
		return view;
	}
	
	public void initView() {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.view = inflater.inflate(R.layout.editor_dialog, null);
		init();
	}

	private void init() {
		etArtistName = (EditText) view.findViewById(R.id.editTextArtist);
		etSongTitle = (EditText) view.findViewById(R.id.editTextTitle);
		etAlbumTitle = (EditText) view.findViewById(R.id.editTextAlbum);
		checkBox = (CheckBox) view.findViewById(R.id.isShowCover);
		etArtistName.setText(newArtistName);
		etSongTitle.setText(newSongTitle);
		etAlbumTitle.setText(newAlbumTitle);
		checkBox.setChecked(showCover);
	}
	
	public void hideCheckBox(boolean isHide) {
		if (isHide) {
			checkBox.setVisibility(View.GONE);
		}
	}
	
	public boolean useAlbumCover() {
		return checkBox.isChecked();
	}
	
	public String getNewArtistName() {
		if (setArtistName()) {
			return newArtistName;
		}
		return "";
	}

	public String getNewSongTitle() {
		if (setSongTitle()) {
			return newSongTitle;
		}
		return "";
	}

	public String getNewAlbumTitle() {
		if (setAlbumTitle()) {
			return newAlbumTitle;
		}
		return "";
	}
	
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

	public boolean isShowCover() {
		return checkBox.isChecked();
	}

	public void setShowCover(boolean showCover) {
		this.showCover = showCover;
	}

	public void setStrings(String[] strings) {
		newArtistName = strings[0];
		newSongTitle = strings[1];
		newAlbumTitle = strings[2];
	}
}
