package ru.johnlife.lifetoolsmp3.ui.views;

import java.io.File;
import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public abstract class BaseLibraryView extends View implements Handler.Callback {

	private static final String PREF_DIRECTORY_PREFIX = "pref.directory.prefix";

	public static final int MSG_FILL_ADAPTER = 1;
	public static final int MSG_EMPTY_ADAPTER = 2;
	
	private ViewGroup view;
	private BaseAbstractAdapter<MusicData> adapter;
	private ListView listView;
	private TextView emptyMessage;
	private Handler uiHandler;
	private String filterQuery = "";
	private CheckRemovedFiles checkRemovedFiles;
	private ContentObserver observer = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			querySong();
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
				querySong();
			}
		}
	};
	
	private void fillAdapter(ArrayList<MusicData> list) {
		Message msg = new Message();
		msg.what = MSG_FILL_ADAPTER;
		msg.obj = list;
		uiHandler.sendMessage(msg);
	};
	
	OnSharedPreferenceChangeListener sPrefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.contains(PREF_DIRECTORY_PREFIX)) {
				querySong();
			}
		}
	};
	
	protected abstract BaseAbstractAdapter<MusicData> getAdapter();
	protected abstract ListView getListView(View view);
	protected abstract TextView getMessageView(View view);
	protected abstract String getFolderPath();
	protected abstract int getLayoutId();
	
	protected void onPause() {
		MusicApp.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sPrefListener);
		if (null != checkRemovedFiles) {
			checkRemovedFiles.cancel(true);
			checkRemovedFiles = null;
		}
	}
	
	protected void onResume() {
		MusicApp.getSharedPreferences().registerOnSharedPreferenceChangeListener(sPrefListener);
		checkRemovedFiles = new CheckRemovedFiles(adapter.getAll());
		if (checkRemovedFiles.getStatus() == Status.RUNNING) return;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			checkRemovedFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			checkRemovedFiles.execute();
		}
	}
	
	protected void specialInit(View view) { }
	
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
		querySong();
		init(inflater);
		emptyMessage.setVisibility(View.VISIBLE);
		if (null != listView) {
			listView.setAdapter(adapter);
			animateListView(listView, adapter);
		}
	}

	public View getView() {
		return view;
	}
	
	public void applyFilter(String srcFilter) {
		if (adapter.isEmpty()) {
			String message =  getResources().getString(R.string.library_empty);
			showMessage(message);
		} else {
			adapter.getFilter().filter(srcFilter);
			filterQuery = srcFilter;
		}
	}
	
	public void clearFilter() {
		adapter.clearFilter();
		filterQuery = "";
	}
	
	public void showMessage(String message) {
		Toast.makeText(getContext(), message ,Toast.LENGTH_SHORT).show();
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
		specialInit(view);
	}
	
	protected void querySong() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<MusicData> result = new ArrayList<MusicData>();
				Cursor cursor = buildQuery(getContext().getContentResolver(), getFolderPath());
				if (cursor.getCount() != 0 | cursor.moveToFirst()) {
					MusicData d = new MusicData();
					d.populate(cursor);
					result.add(d);
					while (cursor.moveToNext()) {
						MusicData data = new MusicData();
						data.populate(cursor);
						result.add(data);
					}
					cursor.close();
				}
				cursor.close();
				fillAdapter(result);
			}

		}).start();
	}
	
	private Cursor buildQuery(ContentResolver resolver, String folderFilter) {
		String selection = MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%" + filterQuery + "%'";
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		return cursor;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_FILL_ADAPTER) {
			emptyMessage.setVisibility(View.GONE);
			if (adapter.isEmpty()) {
				adapter = getAdapter();
				adapter.add(((ArrayList<MusicData>)msg.obj));
				adapter.notifyDataSetChanged();
				listView.setAdapter(adapter);
			} else {
				adapter.setDoNotifyData(false);
				adapter.clear();
				adapter.add((ArrayList<MusicData>) msg.obj);
				adapter.notifyDataSetChanged();
			}
		}  else if (msg.what == MSG_EMPTY_ADAPTER) {
			if (emptyMessage.getVisibility() != View.VISIBLE) {
				emptyMessage.setVisibility(View.VISIBLE);
			}
		}
		return true;
	}

	protected void animateListView(ListView listView, BaseAbstractAdapter<MusicData> adapter) {
		//Animate ListView in childs, if need
	}
	
	private class CheckRemovedFiles extends AsyncTask<Void, Void, ArrayList<MusicData>> {
		

		private ArrayList<MusicData> srcList;

		public CheckRemovedFiles(ArrayList<MusicData> srcList) {
			this.srcList = srcList;
		}

		@Override
		protected ArrayList<MusicData> doInBackground(Void... params) {
			ArrayList<MusicData> badFiles = new ArrayList<MusicData>();
			for (MusicData data : srcList) {
				if (!new File(data.getPath()).exists()) {
					badFiles.add(data);
				}
			}
			for (MusicData musicData : badFiles) {
				if (isCancelled()) return null;
				srcList.remove(musicData);
				musicData.reset(getContext());
			}
			return srcList;
		}
		
		@Override
		protected void onPostExecute(ArrayList<MusicData> result) {
			if (null != result) {
				fillAdapter(result);
			}
			super.onPostExecute(result);
		}
		
	}
}