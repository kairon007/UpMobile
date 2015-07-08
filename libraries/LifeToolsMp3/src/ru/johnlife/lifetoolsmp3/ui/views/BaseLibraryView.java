package ru.johnlife.lifetoolsmp3.ui.views;

import java.io.File;
import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
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
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
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
	private Object lock = new Object();
	private CheckRemovedFiles checkRemovedFiles;
	
	private ContentObserver observer = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (isUserDeleted) {
				isUserDeleted = false;
				return;
			}
			fillAdapter(querySong());
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			if (isUserDeleted) {
				isUserDeleted = false;
				return;
			}
			if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
				fillAdapter(querySong());
			}
		}
	};
	
	private void fillAdapter(ArrayList<MusicData> list) {
		if (list.isEmpty() && !adapter.isEmpty()) {
			adapter.clear();
			return;
		}
		Message msg = new Message();
		msg.what = MSG_FILL_ADAPTER;
		msg.obj = list;
		uiHandler.sendMessage(msg);
	};
	
	private OnSharedPreferenceChangeListener sPrefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.contains(PREF_DIRECTORY_PREFIX)) {
				if (View.VISIBLE == emptyMessage.getVisibility()) {
					emptyMessage.setVisibility(View.GONE);
				} else {
					adapter.clear();
				}
				showProgress(view);
				updateAdapter();
			}
		}
	};
	
	protected boolean isUserDeleted = false;
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
		StateKeeper.getInstance().setLibaryFirstPosition(listView.getFirstVisiblePosition());
	}
	
	public void onResume() {
		MusicApp.getSharedPreferences().registerOnSharedPreferenceChangeListener(sPrefListener);
		((BaseLibraryAdapter) adapter).setListener();
		checkRemovedFiles = new CheckRemovedFiles(adapter.getAll());
		if (checkRemovedFiles.getStatus() == Status.RUNNING) return;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			checkRemovedFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			checkRemovedFiles.execute();
		}
		updateAdapter();
	}
	
	private PlaybackService getService() {
		if (PlaybackService.hasInstance()) {
			return PlaybackService.get(getContext());
		}
		return null;
	}
	
	public BaseLibraryView(LayoutInflater inflater) {
		super(inflater.getContext());
		uiHandler = new Handler(this);
		getContext().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
		init(inflater);
		if (null != listView) {
			showProgress(view);
			listView.setAdapter(adapter);
			animateListView(listView, adapter);
		}
	}
	
	private void updateAdapter() {
		new Thread(new Runnable() {
			
			private ArrayList<MusicData> querySong;

			@Override
			public void run() {
				querySong = querySong();
				uiHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						fillAdapter(querySong);
						int firstPosition = StateKeeper.getInstance().getLibaryFirstPosition();
						if (firstPosition != 0 && firstPosition < adapter.getCount()) {
							listView.setSelection(firstPosition);
						}
						if (querySong.isEmpty()) {
							hideProgress(view);
							listView.setEmptyView(emptyMessage);
						}
					} 

				}, 1000);
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
		emptyMessage = getMessageView(view);
		adapter = getAdapter();
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				AbstractSong data = (AbstractSong) adapter.getItem(position);
				showMenu(view, (MusicData) data);
				return true;
			}
		});
	}
	
	private void showMenu(final View view, final MusicData musicData) {
		PopupMenu menu = new PopupMenu(getContext(), view);
		menu.getMenuInflater().inflate(R.menu.library_menu, menu.getMenu());
		menu.getMenu().getItem(0).setVisible(false);
		menu.getMenu().getItem(1).setVisible(false);
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem paramMenuItem) {
				if (paramMenuItem.getItemId() == R.id.library_menu_delete) {
					adapter.remove(musicData);
					musicData.reset(getContext());
				}
				return false;
			}
			
		});
		menu.show();
	}
	
	protected ArrayList<MusicData> querySong() {
		ArrayList<MusicData> result;
		synchronized (lock) {
			result = new ArrayList<MusicData>();
			Cursor cursor = buildQuery(getContext().getContentResolver(), Util.addQuotesForSqlQuery(getFolderPath()));
			if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
				cursor.close();
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
		}
		return result;
	}
	
	private Cursor buildQuery(ContentResolver resolver, String folderFilter) {
		Cursor cursor;
		synchronized (lock) {
//			String selection = MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%" + filterQuery + "%'";
			String selection = MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%" + "" + "%'";
			cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		}
		return cursor;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_FILL_ADAPTER) {
			ArrayList<MusicData> list = ((ArrayList<MusicData>) msg.obj);
			if (adapter.isEmpty()) {
				adapter.add(list);
			} else {
				android.util.Log.d("logd", "handleMessage: ");
				adapter.setDoNotifyData(false);
				adapter.clear();
				adapter.add(list);
				if(!filterQuery.isEmpty()) {
					applyFilter(filterQuery);
				} else {
					adapter.notifyDataSetChanged();
				}

			}
			hideProgress(view);
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
			if (srcList.isEmpty()) return null;
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
	
	public String getFilterQuery() {
		return filterQuery;
	}
}