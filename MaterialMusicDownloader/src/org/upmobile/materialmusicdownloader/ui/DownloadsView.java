package org.upmobile.materialmusicdownloader.ui;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.adapter.DownloadsAdapter;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseDownloadsView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class DownloadsView extends BaseDownloadsView implements Constants {

	public DownloadsView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	public View getView() {
		View v = super.getView();
		return v;
	}

	@Override
	protected String getDirectory() {
		return DIRECTORY_PREFIX;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return  new DownloadsAdapter(getContext(), R.layout.downloads_item);
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(R.id.listView);
	}

	@Override
	protected TextView getMessageView(View view) {
		return (TextView) view.findViewById(R.id.message_listview);
	}
	
}
