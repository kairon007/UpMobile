package org.upmobile.newmusicdownloader.fragment;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.adapter.LibraryAdapter;
import org.upmobile.newmusicdownloader.data.MusicData;
import org.upmobile.newmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.app.Fragment;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class LibraryFragment extends Fragment implements Handler.Callback {

	private final int MSG_FILL_ADAPTER = 1;
	private PlayerService service;
	private LibraryAdapter adapter;
	private Handler uiHandler;
	private View parentView;
	private ListView listView;
	private TextView emptyMessage;
	private String folderFilter;
	private ContentObserver observer = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			ArrayList<MusicData> list = querySong();
			customList(list);
			Message msg = new Message();
			msg.what = MSG_FILL_ADAPTER;
			msg.obj = list;
			uiHandler.sendMessage(msg);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
				ArrayList<MusicData> list = querySong();
				customList(list);
				Message msg = new Message();
				msg.what = MSG_FILL_ADAPTER;
				msg.obj = list;
				uiHandler.sendMessage(msg);
			}
		};

		private void customList(ArrayList<MusicData> list) {
			if (service.getPlayingPosition() >= 0 && service.isPlaying() && service.getPlayingSong().getClass() == MusicData.class) {
				int i = service.getPlayingPosition();
				list.get(i).setPlaying(true);
			}
		};

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				service = PlayerService.get(getActivity());
			}

		}).start();
		uiHandler = new Handler(this);
		getActivity().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, observer);
		parentView = inflater.inflate(R.layout.fragment_list_transition, container, false);
		init();
		ArrayList<MusicData> srcList = querySong();
		ArrayList<AbstractSong> bufList = new ArrayList<AbstractSong>(srcList);
		service.setArrayPlayback(bufList);
		if (!srcList.isEmpty()) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(srcList);
			if (service.isPlaying() && service.getPlayingSong().getClass() == MusicData.class) {
				int pos = service.getPlayingPosition();
				if (pos >= 0 && pos < list.size()) {
					MusicData data = (MusicData) list.get(pos);
					data.setPlaying(true);
					service.setPlayingSong(data);
				}
			}
			adapter.addAll(srcList);
			listView.setAdapter(adapter);
		} else {
			emptyMessage.setVisibility(View.VISIBLE);
			emptyMessage.setText(R.string.library_empty);
		}
		return parentView;
	}

	private void init() {
		folderFilter = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
		listView = (ListView) parentView.findViewById(R.id.listView);
		emptyMessage = (TextView) parentView.findViewById(R.id.message_listview);
		ArrayList<MusicData> initArray = new ArrayList<MusicData>();
		adapter = new LibraryAdapter(getActivity(), R.layout.library_item, initArray);
	}

	private ArrayList<MusicData> querySong() {
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		Cursor cursor = buildQuery(getActivity().getContentResolver());
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			return result;
		}
		MusicData d = new MusicData();
		d.populate(cursor);
		result.add(d);
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);
			result.add(data);
		}
		cursor.close();
		return result;
	}

	private Cursor buildQuery(ContentResolver resolver) {
		String selection = MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%'";
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		return cursor;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_FILL_ADAPTER) {
			ArrayList<MusicData> array = (ArrayList<MusicData>) msg.obj;
			if (!array.isEmpty() && emptyMessage.getVisibility() == View.VISIBLE) {
				emptyMessage.setVisibility(View.GONE);
			}
			if (adapter.isEmpty()) {
				adapter = new LibraryAdapter(getActivity(), R.layout.library_item, array);
				listView.setAdapter(adapter);
			} else {
				adapter.changeAll(array);
			}
		}
		return true;
	}
}
