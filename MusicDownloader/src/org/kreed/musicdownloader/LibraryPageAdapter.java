package org.kreed.musicdownloader;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
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

	public LibraryPageAdapter(Context context, int resource, ArrayList<MusicData> arrayMusic, MainActivity activity) {
		super(context, resource, arrayMusic);
		this.activity  = activity;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderItem holder;
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
		holder.hSongTitle.setText(strArtist + " - " + strTitle);
		holder.hSongDuration.setText(strDuration);
		holder.hCoverImage.setImageBitmap(bitmap);
		holder.hSongGenre.setText(music.getSongGenre());
		holder.hButtonPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.play(strPath, strArtist, strTitle, strDuration, PrefKeys.CALL_FROM_LIBRARY);
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