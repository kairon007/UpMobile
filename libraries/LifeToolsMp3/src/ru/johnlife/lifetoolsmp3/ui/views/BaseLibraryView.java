package ru.johnlife.lifetoolsmp3.ui.views;

import java.util.ArrayList;
import java.util.Locale;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
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
import android.widget.TextView;

@SuppressLint("NewApi")
public abstract class BaseLibraryView extends View implements Handler.Callback {

	public static final int MSG_FILL_ADAPTER = 1;
	
	private ViewGroup view;
	private BaseAbstractAdapter<MusicData> adapter;
	private ListView listView;
	private TextView emptyMessage;
	private Handler uiHandler;
	private String filterQuery = "";
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
			if (getService().getPlayingPosition() >= 0 && getService().isPlaying() && getService().getPlayingSong().getClass() == MusicData.class) {
				int i = getService().getPlayingPosition();
				list.get(i).turnOn(MusicData.MODE_PLAYING);
			}
		};
	};
	
	protected abstract BaseAbstractAdapter<MusicData> getAdapter();
	protected abstract ListView getListView(View view);
	protected abstract TextView getMessageView(View view);
	protected abstract String getFolderPath();
	protected abstract int getLayoutId();
	
	private PlaybackService getService() {
		if (PlaybackService.hasInstance()) {
			return PlaybackService.get(getContext());
		}
		return null;
	}
	
	public BaseLibraryView(LayoutInflater inflater) {
		super(inflater.getContext());
		uiHandler = new Handler((Callback) this);
		getContext().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
		ArrayList<MusicData> srcList = querySong();
		init(inflater);
		if (!srcList.isEmpty()) {
			if (null != getService() && getService().isPlaying() && getService().getPlayingSong().getClass() == MusicData.class) {
				int pos = getService().getPlayingPosition();
				if (pos >= 0 && pos < srcList.size()) {
					((MusicData) srcList.get(pos)).turnOn(MusicData.MODE_PLAYING);
				}
			}
			emptyMessage.setVisibility(View.GONE);
		} else {
			emptyMessage.setVisibility(View.VISIBLE);
		}
		if (null != listView) {
			adapter.add(srcList);
			listView.setAdapter(adapter);
			animateListView(listView, adapter);
		}
	}
	
	public View getView() {
		return view;
	}
	
	public void applyFilter(String srcFilter) {
		adapter.getFilter().filter(srcFilter);
		filterQuery = srcFilter;
	}
	
	public void clearFilter() {
		adapter.clearFilter();
		filterQuery = "";
	}
	
	protected void adapterCancelTimer() {
		adapter.cancelTimer();
	}
	
	protected void deleteAdapterItem(MusicData item) {
		adapter.remove(item);
	}
	
	protected void deleteServiceItem(MusicData item) {
		getService().remove(item);
	}
	
	private void init(LayoutInflater inflater) {
		view = (ViewGroup) inflater.inflate(getLayoutId(), null);
		listView = getListView(view);
		adapter = getAdapter();
		emptyMessage = getMessageView(view);
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
		String selection = MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%" + filterQuery + "%'";
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
				adapter.setDoNotifyData(false);
				adapter.clear();
				adapter.add((ArrayList<MusicData>) msg.obj);
				adapter.notifyDataSetChanged();
			}
		} 
		return true;
	}

	protected void animateListView(ListView listView, BaseAbstractAdapter<MusicData> adapter) {
		//Animate ListView in childs, if need
	}
}