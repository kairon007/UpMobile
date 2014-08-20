package org.kreed.musicdownloader;

import java.io.File;
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

	private ArrayList<MusicData> arrayMusic = new ArrayList<MusicData>();
	private ArrayList<File> files = new ArrayList<File>();
	private MainActivity activity;
	private LayoutInflater inflater;
	private ImageButton buttonPlay;
	private ImageView coverImage;
	private TextView songTitle;
	private TextView songGenre;
	private TextView songDuration;

	public LibraryPageAdapter(Context context, int resource, ArrayList<MusicData> arrayMusic, ArrayList<File> files, MainActivity activity) {
		super(context, resource, arrayMusic);
		this.activity  = activity;
		this.arrayMusic = arrayMusic;
		this.files = files;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView = inflater.inflate(R.layout.library_item, null);
		buttonPlay = (ImageButton) itemView.findViewById(R.id.play_song);
		songTitle = (TextView) itemView.findViewById(R.id.title_song);
		songGenre = (TextView) itemView.findViewById(R.id.genre_song);
		coverImage = (ImageView) itemView.findViewById(R.id.cover_song);
		songDuration = (TextView) itemView.findViewById(R.id.duration_song);
		MusicData music = arrayMusic.get(position);
		final String strArtist =music.getSongArtist();
		final String strTitle = music.getSongTitle();
		final String strDuration = music.getSongDuration();
		Bitmap bitmap = music.getSongBitmap();
		songTitle.setText(strArtist + " - " + strTitle);
		songDuration.setText(strDuration);
		coverImage.setImageBitmap(bitmap);
		songGenre.setText(music.getSongGenre());
		final File file = files.get(position);
		buttonPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.play(file.getAbsolutePath(), strArtist, strTitle, strDuration, PrefKeys.CALL_FROM_LIBRARY);
			}
			
		});
		return itemView; 
	}
	
	@Override
	public void add(MusicData object) {
		arrayMusic.add(object);
		File file = new File(object.getFileUri());
		files.add(file);
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	notifyDataSetChanged();
            }
        });
	}
	
}