package org.upmobile.clearmusicdownloader.fragment;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.adapters.LibraryAdapter;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.BaseClearActivity;
import com.special.R;
import com.special.menu.ResideMenu;
import com.special.utils.UISwipableList;

public class LibraryFragment extends Fragment {

	private View parentView;
	private UISwipableList listView;
	private LibraryAdapter adapter;
	private ResideMenu resideMenu;
	// it is condition for query of array
	private String folderFilter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle state) {
		parentView = inflater.inflate(R.layout.fragment_list_transition, container, false);
		BaseClearActivity parentActivity = (BaseClearActivity) getActivity();
		resideMenu = parentActivity.getResideMenu();
		init();
		settingListView();
		ArrayList<MusicData> srcList = querySong();
		if (!srcList.isEmpty()) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(srcList);
			PlayerService.get(getActivity()).setQueue(list);
			adapter.addAll(srcList);
			listView.setAdapter(adapter);
		}
		return parentView;
	}

	private void settingListView() {
		listView.setActionLayout(R.id.hidden_view);
		listView.setItemLayout(R.id.front_layout);
		listView.setIgnoredViewHandler(resideMenu);
	}

	private void init() {
		folderFilter = Environment.getExternalStorageDirectory() +"/MusicDownloader/";//it's temporary solution
		adapter = new LibraryAdapter(getActivity(), org.upmobile.clearmusicdownloader.R.layout.library_item);
		listView = (UISwipableList) parentView.findViewById(R.id.listView);
	}
	
	private ArrayList<MusicData> querySong() {
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		Cursor cursor = buildQuery(getActivity().getContentResolver());
		if (!cursor.moveToFirst()) {
			return new ArrayList<MusicData>();
		}
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);	
			result.add(data);
		}
		cursor.close();
		return result;
	}
	
	private Cursor buildQuery(ContentResolver resolver) {
		String selection =  MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%'" ;
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		return cursor;
	}
	
}
