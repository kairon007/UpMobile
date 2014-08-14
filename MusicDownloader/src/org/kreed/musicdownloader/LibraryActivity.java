package org.kreed.musicdownloader;

import java.util.ArrayList;

import org.kreed.musicdownloader.song.Song;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LibraryActivity extends Activity {
	private ListView listView;
	private ArrayAdapter adapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_library);
		adapter = new ArrayAdapter(this, R.layout.library_row_expandable);
		listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(adapter);
		
	}
	
	private final class LibrarySongAdapter extends ArrayAdapter<Song> {
		private LayoutInflater inflater;
		
		public LibrarySongAdapter(Context context, int resource) {
			super(context, resource);
			this.inflater = LayoutInflater.from(context);
		}

		@SuppressLint("NewApi") public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.library_row_expandable, parent, false);
				holder = new ViewHolder();
				holder.artist = (TextView) convertView
						.findViewById(R.id.text);
				holder.title = (TextView) convertView
						.findViewById(R.id.line2);
				holder.cover = (ImageView) convertView
						.findViewById(R.id.cover);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Song song = getItem(position);
			if (song != null) {
				holder.artist.setText(song.getArtist());
				holder.title.setText(song.getTitle());
				holder.cover.setImageAlpha(R.drawable.fallback_cover);
			}
			convertView.setTag(holder);
			return convertView;
		}

		private class ViewHolder {
			TextView title;
			TextView artist;
			ImageView cover;
		}
	}
	@SuppressLint("NewApi") public void insertResults(ArrayList<Song> results) {
		// TODO Auto-generated method stub
		adapter.clear();
		for (Song item : results) {
			adapter.addAll(item);
			// Log.d("-------------------------","---------------------------");
			// rssFeedList.setAdapter(adapter);
		}
		adapter.notifyDataSetChanged();
	}
}
