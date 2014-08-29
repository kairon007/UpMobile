package org.kreed.musicdownloader;

import java.util.ArrayList;

import com.google.gson.annotations.Since;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LibraryPageAdapter extends ArrayAdapter<MusicData> {

	private MainActivity activity;
	private LayoutInflater inflater;
	private int size;

	public LibraryPageAdapter(Context context, int resource, ArrayList<MusicData> arrayMusic, MainActivity activity) {
		super(context, resource, arrayMusic);
		this.activity  = activity;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderItem holder;
		size = getCount();
		Log.d("log", "getView.getCount = "+size);
		if(convertView == null){
			convertView = inflater.inflate(R.layout.library_item, null);
			holder = new ViewHolderItem();
			holder.hButtonPlay = (ImageButton) convertView.findViewById(R.id.play_song);
			holder.hSongTitle = (TextView) convertView.findViewById(R.id.title_song);
			holder.hSongGenre = (TextView) convertView.findViewById(R.id.genre_song);
			holder.hCoverImage = (ImageView) convertView.findViewById(R.id.cover_song);
			holder.hSongDuration = (TextView) convertView.findViewById(R.id.duration_song);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderItem) convertView.getTag();
		}
		MusicData music = getItem(position);
		final String strArtist = music.getSongArtist();
		final String strTitle = music.getSongTitle();
		final String strDuration = music.getSongDuration();
		final String strPath = music.getFileUri();
		Bitmap bitmap = music.getSongBitmap();
		if(null != bitmap){
			holder.hCoverImage.setImageBitmap(bitmap);
		}
		holder.hSongTitle.setText(strArtist + " - " + strTitle);
		holder.hSongDuration.setText(strDuration);
		holder.hSongGenre.setText(music.getSongGenre());
		final int pos = position;
		holder.hButtonPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((MainActivity) activity).setActivatedPlayButton(false);
				activity.play(strPath, strArtist, strTitle, strDuration, PrefKeys.CALL_FROM_LIBRARY, pos);
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
	}
	
}