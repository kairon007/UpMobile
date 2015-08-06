package org.upmobile.musicpro.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.upmobile.musicpro.R;
import org.upmobile.musicpro.adapter.LibraryAdapter;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseLibraryView;

public class LibraryView extends BaseLibraryView {

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), R.layout.library_item);
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(R.id.lsvSongLibrary);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_library;
	}

	@Override
	protected void forceDelete() {

	}

	@Override
	public TextView getMessageView(View view) {
		return (TextView) view.findViewById(R.id.message_listview);
	}

}
