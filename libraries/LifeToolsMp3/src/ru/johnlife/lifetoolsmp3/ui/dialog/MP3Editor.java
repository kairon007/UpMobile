package ru.johnlife.lifetoolsmp3.ui.dialog;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
	private String oldArtistName;
	private String oldSongTitle;
	private String oldAlbumTitle;
	private boolean showCover = true;

	public MP3Editor(Context context, boolean isEnableCover) {
		this.context = context;
		this.showCover = isEnableCover;
	}

	public View getView() {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.view = inflater.inflate(R.layout.editor_dialog, null);
		init();
		return view;
	}
	
	private void init() {
		etArtistName = (EditText) view.findViewById(R.id.editTextArtist);
		etSongTitle = (EditText) view.findViewById(R.id.editTextTitle);
		etAlbumTitle = (EditText) view.findViewById(R.id.editTextAlbum);
		checkBox = (CheckBox) view.findViewById(R.id.isShowCover);
		etArtistName.setText(newArtistName);
		etSongTitle.setText(newSongTitle);
		etAlbumTitle.setText(newAlbumTitle);
		CustomWatcher watcher = new CustomWatcher();
		etAlbumTitle.addTextChangedListener(watcher);
		etSongTitle.addTextChangedListener(watcher);
		etArtistName.addTextChangedListener(watcher);
		checkBox.setChecked(showCover);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SongArrayHolder.getInstance().setCoverEnabled(isChecked);
			}
		});
	}
	
	private class CustomWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			newArtistName = etArtistName.getText().toString();
			newSongTitle = etSongTitle.getText().toString();
			newAlbumTitle = etAlbumTitle.getText().toString();
			SongArrayHolder.getInstance().setID3DialogOpened(true, 
					new String[] {newArtistName, newSongTitle, newAlbumTitle}, checkBox.isChecked());
		}

		@Override
		public void afterTextChanged(Editable s) {
			
		}
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
		if (!str.equals("")) {
			newArtistName = str;
			return true;
		}
		return false;
	}
	
	private boolean setSongTitle() {
		String str = etSongTitle.getText().toString();
		if (!str.equals("")) {
			newSongTitle = str;
			return true;
		}
		return false;
	}
	
	public boolean manipulateText() {
		if (!oldAlbumTitle.equals(newAlbumTitle)) {
			return true;
		}
		if (!oldArtistName.equals(newArtistName)){
			return true;
		}
		if (!oldSongTitle.equals(newSongTitle)) {
			return true;
		}
		return false;
	}
	
	private boolean setAlbumTitle() {
		String str = etAlbumTitle.getText().toString();
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
		oldArtistName = newArtistName = strings[0];
		oldSongTitle = newSongTitle = strings[1];
		oldAlbumTitle = newAlbumTitle = strings[2];
	}
}
