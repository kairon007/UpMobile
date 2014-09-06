package org.kreed.musicdownloader.ui.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.kreed.musicdownloader.Constans;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.ui.activity.MainActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LibraryTabAdapter extends ArrayAdapter<MusicData> implements TextWatcher {

	private MainActivity activity;
	private LayoutInflater inflater;
	private EditText filter;
	private int size;

	public LibraryTabAdapter(Context context, int resource, ArrayList<MusicData> arrayMusic, MainActivity activity) {
		super(context, resource, arrayMusic);
		this.activity = activity;
		inflater = LayoutInflater.from(context);
		filter = (EditText) activity.findViewById(R.id.filter_text);
		filter.addTextChangedListener(this);
		String str = activity.getTextFilterLibrary();
		if (!str.equals("")) {
			filter.setText(str);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderItem holder;
		size = getCount();
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.library_item, null);
			holder = new ViewHolderItem();
			holder.hThreedot = (ImageButton) convertView.findViewById(R.id.threedotButton);
			holder.hButtonPlay = (ImageButton) convertView.findViewById(R.id.play_song);
			holder.hSongTitle = (TextView) convertView.findViewById(R.id.title_song);
			holder.hSongGenre = (TextView) convertView.findViewById(R.id.genre_song);
			holder.hCoverImage = (ImageView) convertView.findViewById(R.id.cover_song);
			holder.hSongDuration = (TextView) convertView.findViewById(R.id.duration_song);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderItem) convertView.getTag();
		}
		final MusicData music = getItem(position);
		final String strArtist = music.getSongArtist();
		final String strTitle = music.getSongTitle();
		final String strDuration = music.getSongDuration();
		final String strPath = music.getFileUri();
		Bitmap bitmap = music.getSongBitmap();
		if (null != bitmap) {
			holder.hCoverImage.setImageBitmap(bitmap);
		}
		holder.hSongTitle.setText(strArtist + " - " + strTitle);
		holder.hSongDuration.setText(strDuration);
		holder.hSongGenre.setText(music.getSongGenre());
		final int pos = position;
		activity.registerForContextMenu(convertView);
		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				activity.setMusic(music);
				activity.setSelectedItem(pos);
				return false;
			}
		});
		holder.hThreedot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				activity.setMusic(music);
				activity.setSelectedItem(pos);
				arg0.performLongClick();
			}
		});
		holder.hButtonPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) activity).setActivatedPlayButton(false);
				activity.play(strPath, strArtist, strTitle, strDuration, Constans.CALL_FROM_LIBRARY, pos);
			}

		});
		return convertView;
	}

	private class ViewHolderItem {
		ImageButton hButtonPlay;
		ImageView hCoverImage;
		TextView hSongTitle;
		TextView hSongGenre;
		TextView hSongDuration;
		ImageButton hThreedot;
	}

	public void updateItem(int position, String artist, String title, Bitmap cover) {
		if (!artist.equals("")) {
			getItem(position).setSongArtist(artist);
		}
		if (!title.equals("")) {
			getItem(position).setSongTitle(title);
		}
		if (null != cover) {
			getItem(position).setSongBitmap(cover);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		getFilter().filter(filter.getText().toString().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public void afterTextChanged(Editable s) {

	}
}