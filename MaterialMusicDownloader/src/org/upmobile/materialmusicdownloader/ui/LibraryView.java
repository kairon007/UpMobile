package org.upmobile.materialmusicdownloader.ui;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.adapter.LibraryAdapter;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseLibraryView;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class LibraryView extends BaseLibraryView implements Constants {

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), R.layout.library_item);
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
}
