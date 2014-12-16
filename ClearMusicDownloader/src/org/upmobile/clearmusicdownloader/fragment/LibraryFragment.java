package org.upmobile.clearmusicdownloader.fragment;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.LibraryAdapter;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.R;
import com.special.menu.ResideMenu;
import com.special.utils.UISwipableList;

public class LibraryFragment extends Fragment implements Handler.Callback{

	private static final int MSG_FILL_ADAPTER = 1;
	private View parentView;
	private UISwipableList listView;
	private LibraryAdapter adapter;
	private ResideMenu resideMenu;
	private Handler uiHandler;
	private String folderFilter;
	private ContentObserver observer = new ContentObserver(null) {
		
		public void onChange(boolean selfChange) {
				ArrayList<MusicData> list = querySong();
				Message msg = new Message();
				msg.what = MSG_FILL_ADAPTER;
				msg.obj = list;
				uiHandler.sendMessage(msg);
		};
		
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle state) {
		uiHandler = new Handler(this);
		getActivity().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, observer);
		parentView = inflater.inflate(R.layout.fragment_list_transition, container, false);
		MainActivity parentActivity = (MainActivity) getActivity();
		resideMenu = parentActivity.getResideMenu();
		((MainActivity) getActivity()).showTopFrame();
		init();
		settingListView();
		ArrayList<MusicData> srcList = querySong();
		if (!srcList.isEmpty()) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(srcList);
			PlayerService service = PlayerService.get(getActivity());
			if (service.isPlaying() && service.getPlayingSong().getClass() == MusicData.class) {
				int pos = service.getPlayingPosition();
				if (pos >= 0 && pos < list.size()) {
					((MusicData) list.get(pos)).turnOn(MusicData.MODE_PLAYING);
				}
			}
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
		folderFilter = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
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

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_FILL_ADAPTER:
			adapter.changeArray((ArrayList<MusicData>) msg.obj);
			break;
		}
		return true;
	}
	
}
