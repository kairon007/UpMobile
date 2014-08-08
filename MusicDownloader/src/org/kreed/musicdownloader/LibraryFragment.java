package org.kreed.musicdownloader;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class LibraryFragment extends Fragment{
	
	private Context context;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
//		context = inflater.getContext();
		View view =  inflater.inflate(R.layout.library, null);
//		ListView listViewSong = (ListView) view.findViewById(R.id.pager_library);
//		int item = R.layout.library_row_expandable;
//		ArrayList<Song> songs = new ArrayList<Song>();
//		LibraryAdapter adapterSong = new LibraryAdapter(context, item, songs);
//		listViewSong.setAdapter(adapterSong);
		return view;
	}
}
