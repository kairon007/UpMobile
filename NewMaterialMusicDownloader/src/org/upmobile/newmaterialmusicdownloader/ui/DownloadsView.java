package org.upmobile.newmaterialmusicdownloader.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseDownloadsView;

public class DownloadsView extends BaseDownloadsView {

	public DownloadsView(LayoutInflater inflater) {
		super(inflater);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getDirectory() {
		return null;
	}

	@Override
	protected int getLayoutId() {
		return 0;
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return null;
	}

	@Override
	protected ListView getListView(View view) {
		return null;
	}

	@Override
	protected TextView getMessageView(View view) {
		return null;
	}

}
