package ru.johnlife.lifetoolsmp3.ui.views;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.adapter.BaseAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

@SuppressLint("NewApi")
public abstract class BaseLibraryView extends View implements Handler.Callback {

	protected static final int MSG_FILL_ADAPTER = 1;
	
	private ViewGroup view;
	private BaseAdapter<MusicData> adapter;
	private ListView listView;
	private PlaybackService service;
	private Handler uiHandler;
	private ContentObserver observer = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			ArrayList<MusicData> list = querySong();
			customList(list);
			Message msg = new Message();
			msg.what = MSG_FILL_ADAPTER;
			msg.obj = list;
			uiHandler.sendMessage(msg);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
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
				list.get(i).turnOn(MusicData.MODE_PLAYING);
			}
		};
	};
	
	protected abstract BaseAdapter<MusicData> getAdapter();
	protected abstract ListView getListView(View view);
	protected abstract String getFolderPath();
	protected abstract int getLayoutId();
	
	public BaseLibraryView(LayoutInflater inflater) {
		super(inflater.getContext());
		uiHandler = new Handler((Callback) this);
		service = PlaybackService.get(getContext());
		getContext().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
		ArrayList<MusicData> srcList = querySong();
		init(inflater);
		if (!srcList.isEmpty()) {
			if (null != service && service.isPlaying() && service.getPlayingSong().getClass() == MusicData.class) {
				int pos = service.getPlayingPosition();
				if (pos >= 0 && pos < srcList.size()) {
					((MusicData) srcList.get(pos)).turnOn(MusicData.MODE_PLAYING);
				}
			}
		}
		if (null != listView) {
			adapter.addAll(srcList);
			listView.setAdapter(adapter);
		}
	}
	
	public View getView() {
		return view;
	}
	
	protected void adapterCancelTimer() {
		adapter.cancelTimer();
	}
	
	protected void deleteAdapterItem(MusicData item) {
		adapter.remove(item);
	}
	
	protected void deleteServiceItem(MusicData item) {
		service.remove(item);
	}
	
	private void init(LayoutInflater inflater) {
		view = (ViewGroup) inflater.inflate(getLayoutId(), null);
		listView = getListView(view);
		adapter = getAdapter();
	}
	
	protected ArrayList<MusicData> querySong() {
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		Cursor cursor = buildQuery(getContext().getContentResolver(), getFolderPath());
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
	
	private Cursor buildQuery(ContentResolver resolver, String folderFilter) {
		String selection = MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%'";
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		return cursor;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_FILL_ADAPTER) {
			if (adapter.isEmpty()) {
				adapter = getAdapter();
				listView.setAdapter(adapter);
			} else {
				adapter.changeArray((ArrayList<MusicData>) msg.obj);
			}
		}
		return true;
	}

}