package org.kreed.musicdownloader;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LibraryPageAdapter extends ArrayAdapter<MusicData> {
	
	private LayoutInflater inflater;

	public LibraryPageAdapter(Context context, int resource, ArrayList<MusicData> musicFiles) {
		super(context, resource, musicFiles);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.downloads_row, parent, false);
				holder = new ViewHolder();
				holder.artist = (TextView) convertView.findViewById(R.id.songArtist);
				holder.title = (TextView) convertView.findViewById(R.id.songTitle);
				holder.cover = (ImageView) convertView.findViewById(R.id.cover);
				holder.duration = (TextView) convertView.findViewById(R.id.totalTime);
				holder.remove = (ImageView) convertView.findViewById(R.id.cancel);
				holder.remove.setVisibility(View.GONE);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			MusicData song = getItem(position);
			if (song != null) {
				holder.artist.setText(song.getSongArtist());
				holder.title.setText(song.getSongTitle());
			if (song.getSongDuration() != null) {
				// holder.cover.setImageAlpha(R.drawable.fallback_cover);
				int i = Integer.parseInt(song.getSongDuration());
				String str = formatTime(i);
				holder.duration.setText(str);
			}else{
				holder.duration.setText("0.00");
			}
			}
			convertView.setTag(holder);
		return convertView;
	}
	
	private String formatTime(int duration) {
		duration /= 1000;
		int min = duration / 60;
		int sec = duration % 60;
		return String.format("%d:%02d", min, sec);
	}
	

	private class ViewHolder {
		TextView title;
		TextView artist;
		TextView duration;
		ImageView cover;
		ImageView remove;
	}

}
