package org.upmobile.newmaterialmusicdownloader.ui;

import org.upmobile.newmaterialmusicdownloader.adapter.LibraryAdapter;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseLibraryView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class LibraryView extends BaseLibraryView {

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), org.upmobile.newmaterialmusicdownloader.R.layout.row_online_search);
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(org.upmobile.newmaterialmusicdownloader.R.id.list);
	}

	@Override
	protected int getLayoutId() {
		return org.upmobile.newmaterialmusicdownloader.R.layout.fragment_list_transition;
	}

	@Override
	protected TextView getMessageView(View view) {
		return (TextView) view.findViewById(org.upmobile.newmaterialmusicdownloader.R.id.message_listview);
	}

	@Override
	protected String getFolderPath() {
		return NewMaterialApp.getDirectory();
	}

}
