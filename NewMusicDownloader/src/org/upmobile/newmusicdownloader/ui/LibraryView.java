package org.upmobile.newmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.adapter.LibraryAdapter;

import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseLibraryView;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LibraryView extends BaseLibraryView implements Handler.Callback, Constants {
	
	private ListView listView;
	private LibraryAdapter adapter;
	private TextView emptyMessage;

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected ArrayAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), R.layout.library_item, querySong());
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(R.id.listView);
	}

	@Override
	protected String getFolderPath() {
		return Environment.getExternalStorageDirectory() + DIRECTORY_PREFIX;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_FILL_ADAPTER) {
			ArrayList<MusicData> array = (ArrayList<MusicData>) msg.obj;
			if (!array.isEmpty() && emptyMessage.getVisibility() == View.VISIBLE) {
				emptyMessage.setVisibility(View.GONE);
			}
			if (adapter.isEmpty()) {
				adapter = new LibraryAdapter(getContext(), R.layout.library_item, array);
				listView.setAdapter(adapter);
			} else {
				adapter.changeArray(array);
			}
		}
		return true;
	}

}
