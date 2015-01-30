package org.upmobile.musicpro.widget;

import org.upmobile.musicpro.Constants;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.adapter.LibraryAdapter;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import ru.johnlife.lifetoolsmp3.adapter.BaseAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseLibraryView;

public class LibraryView extends BaseLibraryView {

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), R.layout.library_item);
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(R.id.lsvSongLibrary);
	}

	@Override
	protected String getFolderPath() {
		return Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
	}


	@Override
	protected int getLayoutId() {
		return R.layout.fragment_library;
	}

}
