package ru.johnlife.lifetoolsmp3.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class MP3Editor implements OnEditorActionListener {

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
	public static final String UNKNOWN = "unknown";
	private OnActionEndListener actionEndListener;
	
	public static interface OnActionEndListener {
		public void donePressed();
	}

	public MP3Editor(Context context) {
		this.context = context;
	}
	
	public MP3Editor(Context context, OnActionEndListener actionEndListener) {
		this.context = context;
		this.actionEndListener = actionEndListener;
	}
	
	public View getView() {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.view = inflater.inflate(Util.getResIdFromAttribute((Activity) context, R.attr.editorDialog), null);
		init();
		etArtistName.setText(newArtistName);
		etSongTitle.setText(newSongTitle);
		etAlbumTitle.setText(newAlbumTitle);
		CustomWatcher watcher = new CustomWatcher();
		etAlbumTitle.addTextChangedListener(watcher);
		etSongTitle.addTextChangedListener(watcher);
		etArtistName.addTextChangedListener(watcher);
		etAlbumTitle.setOnEditorActionListener(this);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				StateKeeper keeper = StateKeeper.getInstance();
				if (isChecked) keeper.setTempID3UseCover(1);
				else keeper.setTempID3UseCover(-1);
			}
		});
		clearFocus();
		return view;
	}
	
	public View getCustomView(int layout) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.view = inflater.inflate(layout, null);
		init();
		etArtistName.setText(newArtistName);
		etSongTitle.setText(newSongTitle);
		etAlbumTitle.setText(newAlbumTitle);
		CustomWatcher watcher = new CustomWatcher();
		etAlbumTitle.addTextChangedListener(watcher);
		etSongTitle.addTextChangedListener(watcher);
		etArtistName.addTextChangedListener(watcher);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				StateKeeper keeper = StateKeeper.getInstance();
				if (isChecked) keeper.setTempID3UseCover(1);
				else keeper.setTempID3UseCover(-1);
			}
		});
		clearFocus();
		return view;
	}

	public void clearFocus() {
		etAlbumTitle.clearFocus();
		etSongTitle.clearFocus();
		etArtistName.clearFocus();
	}

	private void init() {
		etArtistName = (EditText) view.findViewById(R.id.editTextArtist);
		etSongTitle = (EditText) view.findViewById(R.id.editTextTitle);
		etAlbumTitle = (EditText) view.findViewById(R.id.editTextAlbum);
		checkBox = (CheckBox) view.findViewById(R.id.isShowCover);
	}

	private class CustomWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			StateKeeper keeper = StateKeeper.getInstance();
			keeper.activateOptions(StateKeeper.MANIPULATE_TEXT_OPTION);
			newArtistName = etArtistName.getText().toString();
			newSongTitle = etSongTitle.getText().toString();
			newAlbumTitle = etAlbumTitle.getText().toString();
			keeper.setTempID3Fields( new String[] { newArtistName, newSongTitle, newAlbumTitle });
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	}

	public void hideCheckBox(boolean isHide) {
		if (isHide) {
			checkBox.setVisibility(View.GONE);
			view.findViewById(R.id.text_check_box).setVisibility(View.GONE);
		}
	}

	public boolean useAlbumCover() {
		return checkBox.isChecked();
	}

	public void setUseCover (boolean value) {
		checkBox.setChecked(value);
	}
	
	public String getNewArtistName() {
		if (setArtistName()) {
			return newArtistName;
		}
		return oldArtistName;
	}

	public String getNewSongTitle() {
		if (setSongTitle()) {
			return newSongTitle;
		}
		return oldSongTitle;
	}

	public String getNewAlbumTitle() {
		if (setAlbumTitle()) {
			return newAlbumTitle;
		}
		return oldAlbumTitle;
	}

	private boolean setArtistName() {
		String str = etArtistName.getText().toString();
		if (str.equals("")) {
			newArtistName = UNKNOWN;
			return true;
		} else if (!str.equals(oldArtistName)) {
			newArtistName = str;
			return true;
		}
		return false;
	}

	private boolean setSongTitle() {
		String str = etSongTitle.getText().toString();
		if (str.equals("")) {
			newSongTitle = UNKNOWN;
			return true;
		} else if (!str.equals(oldSongTitle)) {
			newSongTitle = str;
			return true;
		}
		return false;
	}

	private boolean setAlbumTitle() {
		String str = etAlbumTitle.getText().toString();
		if (str.equals("")) {
			newAlbumTitle = UNKNOWN;
			return true;
		} else if (!str.equals(oldAlbumTitle)) {
			newAlbumTitle = str;
			return true;
		}
		return false;
	}

	public boolean manipulateText() {
		if (!oldAlbumTitle.equals(newAlbumTitle)) {
			return true;
		}
		if (!oldArtistName.equals(newArtistName)) {
			return true;
		}
		if (!oldSongTitle.equals(newSongTitle)) {
			return true;
		}
		return false;
	}

	public void setStrings(String[] strings) {
		oldArtistName = newArtistName = strings[0];
		oldSongTitle = newSongTitle = strings[1];
		oldAlbumTitle = newAlbumTitle = strings[2];
	}

	public String[] getStrings() {
		return new String[] {newArtistName, newSongTitle, newAlbumTitle};
	}
	
	public void disableChekBox() {
		checkBox.setClickable(false);
		checkBox.setEnabled(false);
	}
	
	public void enableChekBox() {
		checkBox.setClickable(true);
		checkBox.setEnabled(true);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == EditorInfo.IME_ACTION_DONE) {
			if (null != actionEndListener) {
				actionEndListener.donePressed();
			}
        }
		return false;
	}

	public OnActionEndListener getActionEndListener() {
		return actionEndListener;
	}

	public void setActionEndListener(OnActionEndListener actionEndListener) {
		this.actionEndListener = actionEndListener;
	}
}
