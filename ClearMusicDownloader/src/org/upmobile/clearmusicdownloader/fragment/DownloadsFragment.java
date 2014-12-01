package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.adapters.DownloadsAdapter;

import ru.johnlife.lifetoolsmp3.song.Song;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.special.BaseClearActivity;
import com.special.R;
import com.special.menu.ResideMenu;
import com.special.utils.UISwipableList;

public class DownloadsFragment extends Fragment {

	// Views & Widgets
	private View parentView;
	private UISwipableList listView;
	private DownloadsAdapter mAdapter;
	private ResideMenu resideMenu;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_list_transition, container, false);
		listView = (UISwipableList) parentView.findViewById(R.id.listView);
		BaseClearActivity parentActivity = (BaseClearActivity) getActivity();
		resideMenu = parentActivity.getResideMenu();
		initView();
		return parentView;
	}

	private void initView() {
		mAdapter = new DownloadsAdapter(getActivity(), org.upmobile.clearmusicdownloader.R.layout.downloads_item);
		listView.setActionLayout(R.id.hidden_view);
		listView.setItemLayout(R.id.front_layout);
		listView.setAdapter(mAdapter);
		listView.setIgnoredViewHandler(resideMenu);
		// Test item for test view. Will be removed after create serach view.
		Song song = new Song(0);
		song.artist = "artistname";
		song.title = "titlename";
		mAdapter.add(song);
		// end
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View viewa, int i, long l) {
			}
		});
	}
}
