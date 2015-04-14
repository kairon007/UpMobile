package ru.johnlife.lifetoolsmp3.ui.views;

import java.io.File;
import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
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
			fillAdapter(querySong());
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
				fillAdapter(querySong());
			}
		}
	};
	
	private void fillAdapter(ArrayList<MusicData> list) {
		if (list.isEmpty()) return;
		Message msg = new Message();
		msg.what = MSG_FILL_ADAPTER;
		msg.obj = list;
		uiHandler.sendMessage(msg);
	};
	
	OnSharedPreferenceChangeListener sPrefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.contains(PREF_DIRECTORY_PREFIX)) {
				fillAdapter(querySong());
			}
		}
	};
	
	protected abstract BaseAbstractAdapter<MusicData> getAdapter();
	protected abstract ListView getListView(View view);
	public abstract TextView getMessageView(View view);
	protected abstract String getFolderPath();
	protected abstract int getLayoutId();
	
	public void onPause() {
		((BaseLibraryAdapter) adapter).resetListener();
		MusicApp.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sPrefListener);
		if (null != checkRemovedFiles) {
			checkRemovedFiles.cancel(true);
			checkRemovedFiles = null;
		}
	}
	
	public void onResume() {
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
		init(inflater);
		if (null != listView) {
			updateAdapter();
			listView.setAdapter(adapter);
			animateListView(listView, adapter);
		}
	}
	
	private void updateAdapter() {
		showProgress(view);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final ArrayList<MusicData> querySong = querySong();
				adapter.setDoNotifyData(false);
				adapter.add(querySong);
				uiHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						hideProgress(view);
						adapter.notifyDataSetChanged();
						listView.setEmptyView(emptyMessage);
					}

				}, 100);
			}
		}).start();
	}
	
	protected void showProgress(View v) {
		v.findViewById(R.id.progress).setVisibility(View.VISIBLE);
	}
	
	protected void hideProgress(View v) {
		v.findViewById(R.id.progress).setVisibility(View.GONE);
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
			ArrayList<MusicData> list = ((ArrayList<MusicData>) msg.obj);
			if (adapter.isEmpty()) {
				adapter = getAdapter();
				adapter.add(list);
				listView.setAdapter(adapter);
			} else {
				adapter.setDoNotifyData(false);
				adapter.clear();
				adapter.add(list);
				adapter.notifyDataSetChanged();
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