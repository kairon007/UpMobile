package org.upmobile.musicpro.adapter;

import java.util.ArrayList;
import java.util.List;

import org.upmobile.musicpro.R;
import org.upmobile.musicpro.object.Song;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {
	
	private Context context;
	private List<Song> listSongs;
	private LayoutInflater layoutInflater;

	public SongAdapter(Context context, List<Song> listSongs) {
		this.context = context;
		this.listSongs = listSongs;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return listSongs.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.item_song, null);
		}

		View layoutSong = convertView.findViewById(R.id.layoutSong);
		TextView lblName = (TextView) convertView.findViewById(R.id.lblName);
		TextView lblArtist = (TextView) convertView.findViewById(R.id.lblArtist);

		Song item = listSongs.get(position);
		if (item != null) {
			lblName.setText(item.getName());
			lblArtist.setText(item.getArtist());
		}

		if (position % 2 == 0) {
			layoutSong.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_list_selector));
		} else {
			layoutSong.setBackgroundColor(Color.TRANSPARENT);
		}
		return convertView;
	}
	
	public void addAll(ArrayList<? extends Parcelable> arrayList) {
		if (arrayList.isEmpty()) return;
		listSongs.clear();
		for (int i=0; i<arrayList.size(); i++)
			listSongs.add((Song) arrayList.get(i));
	}
	
	public List<Song> getAll() {
		return listSongs;
	}
}
