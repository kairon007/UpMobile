package org.kreed.musicdownloader.ui.tab;

import java.util.ArrayList;
import java.util.Collection;

import org.kreed.musicdownloader.DBHelper;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.interfaces.LoadPercentageInterface;
import org.kreed.musicdownloader.interfaces.TaskSuccessListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadsTab implements LoadPercentageInterface {

	private final Object lock = new Object();
	private ListView listView;
	private ProgressBar progress;
	private DownloadsAdapter adapter;
	private static DownloadsTab instance;
	private View view;
	private Activity activity;
	private long cancelledId;
	private final static double DOWNLOAD_FINISHED = -1;
	private ImageButton clearAll;

	public final class DownloadsAdapter extends ArrayAdapter<MusicData> implements TaskSuccessListener {

		private ArrayList<MusicData> mObjects;
		private ArrayList<MusicData> mOriginalValues;
		private LayoutInflater inflater;
		private Filter filter;

		public DownloadsAdapter(Context context, int resource) {
			super(context, resource);
			this.inflater = LayoutInflater.from(context);
			mObjects = new ArrayList<MusicData>();
			DBHelper.getInstance(getContext()).getAll(this);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.downloads_row, parent, false);
				holder = new ViewHolder();
				holder.artist = (TextView) convertView.findViewById(R.id.songArtist);
				holder.title = (TextView) convertView.findViewById(R.id.songTitle);
				holder.cover = (ImageView) convertView.findViewById(R.id.cover);
				holder.duration = (TextView) convertView.findViewById(R.id.totalTime);
				holder.downloadProgress = (ProgressBar) convertView.findViewById(R.id.progressBar);
				holder.remove = (ImageButton) convertView.findViewById(R.id.cancel);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (holder.remove != null) {
				holder.remove.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
						cancelledId = adapter.getItem(position).getDownloadId();
						manager.remove(cancelledId);
						if (getItem(position).getFileUri() != null) {
							DBHelper.getInstance(getContext()).delete(getItem(position));
						}
						remove(getItem(position));
					}
				});
			}
			MusicData song = getItem(position);
			if (song != null) {
				holder.artist.setText(song.getSongArtist());
				holder.title.setText(song.getSongTitle());
				if (song.getSongBitmap() != null) {
					holder.cover.setImageBitmap(song.getSongBitmap());
				} else {
					holder.cover.setImageResource(R.drawable.fallback_cover);
				}
				if (song.isDownloaded()) {
					holder.downloadProgress.setVisibility(View.GONE);
					holder.remove.setImageResource(R.drawable.icon_ok);
				} else {
					holder.downloadProgress.setVisibility(View.VISIBLE);
					double progress = song.getDownloadProgress();
					holder.downloadProgress.setProgress((int) progress);
					holder.remove.setImageResource(R.drawable.icon_cancel);
				}
				holder.duration.setText(song.getSongDuration());
			}
			convertView.setTag(holder);
			return convertView;
		}

		private class ViewHolder {
			TextView title;
			TextView artist;
			TextView duration;
			ImageView cover;
			ImageButton remove;
			ProgressBar downloadProgress;
		}

		@Override
		public void success(ArrayList<MusicData> result) {
			progress.setVisibility(View.GONE);
			insertData(result);
		}

		private void redraw() {
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					notifyDataSetChanged();
				}

			});
		}

		@Override
		public void add(MusicData object) {
			synchronized (lock) {
				if (mOriginalValues != null) {
					mOriginalValues.add(object);
				}
				mObjects.add(object);
			}
			redraw();
		}

		@Override
		public void insert(MusicData object, int index) {
			synchronized (lock) {
				if (mOriginalValues != null) {
					mOriginalValues.add(index, object);
				}
				if (mObjects == null) {
					mObjects = new ArrayList<MusicData>();
				}
				mObjects.add(index, object);
			}
			redraw();
		}

		@SuppressLint("NewApi")
		@Override
		public void addAll(Collection<? extends MusicData> collection) {
			synchronized (lock) {
				if (mOriginalValues != null) {
					mOriginalValues.addAll(collection);
				}
				mObjects.addAll(collection);
			}
			redraw();
		}

		@Override
		public void clear() {
			synchronized (lock) {
				if (mOriginalValues != null) {
					mOriginalValues.clear();
				}
				mObjects.clear();
			}
			redraw();
		}

		@Override
		public void remove(MusicData object) {
			synchronized (lock) {
				if (mOriginalValues != null) {
					mOriginalValues.remove(object);
				}
				mObjects.remove(object);
			}
			redraw();
		}

		@Override
		public int getCount() {
			if (mObjects == null) {
				return 0;
			}
			return mObjects.size();
		}

		@Override
		public MusicData getItem(int position) {
			return mObjects.get(position);
		}

		@Override
		public int getPosition(MusicData item) {
			return mObjects.indexOf(item);
		}

		@Override
		public Filter getFilter() {
			if (filter == null) {
				filter = new ResultFilter();
			}
			return filter;
		}

		private class ResultFilter extends Filter {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				String prefix = constraint.toString().toLowerCase();
				if (mOriginalValues == null) {
					mOriginalValues = new ArrayList<MusicData>(mObjects);
				}
				if (prefix == null || prefix.length() == 0) {
					ArrayList<MusicData> list = new ArrayList<MusicData>(mOriginalValues);
					results.values = list;
					results.count = list.size();
				} else {
					ArrayList<MusicData> list = new ArrayList<MusicData>(mOriginalValues);
					ArrayList<MusicData> nlist = new ArrayList<MusicData>();
					int count = list.size();
					for (int i = 0; i < count; i++) {
						MusicData data = list.get(i);
						String value = data.toString();
						if (value.contains(prefix)) {
							nlist.add(data);
						}
						results.values = nlist;
						results.count = nlist.size();
					}
				}
				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, final FilterResults results) {
				mObjects = (ArrayList<MusicData>) results.values;
				redraw();
			}
		}
	}

	public void deleteItem(final long downloadId, final String title) {
		for (int i = 0; i < adapter.getCount(); i++) {
			MusicData buf = adapter.getItem(i);
			if (buf.getDownloadId() == downloadId) {
				adapter.remove(buf);
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						String failedSong = activity.getResources().getString(R.string.downloads_failed);
						Toast.makeText(activity, failedSong + " - " + title, Toast.LENGTH_SHORT).show();
					}
				});
				return;
			}
		}
	}

	@Override
	public void insertProgress(double progress, long downloadId) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItem(i).getDownloadId() == downloadId) {
				adapter.getItem(i).setDownloadProgress(progress);
				if (progress > 99) {
					adapter.getItem(i).setDownloadProgress(DOWNLOAD_FINISHED);
				}
			}
		}
		adapter.redraw();
	}

	public void insertData(ArrayList<MusicData> result) {
		for (MusicData data : result) {
			insertData(data);
		}
	}
	
	public void insertData(MusicData data) {
		adapter.insert(data, 0);
	}

	public static View getInstanceView(LayoutInflater layoutInflater, Activity activity) {
		View instanceView = getInstance(layoutInflater, activity).view;
		ViewGroup parent = (ViewGroup) instanceView.getParent();
		if (null != parent) {
			parent.removeView(instanceView);
		}
		return instanceView;
	}

	private static DownloadsTab getInstance(LayoutInflater layoutInflater, Activity activity) {
		if (null == instance) {
			instance = new DownloadsTab(layoutInflater.inflate(R.layout.layout_download, null), layoutInflater, activity);
		} else {
			instance.activity = activity;
		}
		return instance;
	}

	public static DownloadsTab getInstance() {
		if (null == instance) {
			instance = new DownloadsTab();
		}
		return instance;
	}

	private DownloadsTab(final View inflateView, final LayoutInflater layoutInflater, Activity activity) {
		this.view = inflateView;
		this.activity = activity;
		clearAll = (ImageButton) activity.findViewById(R.id.clear_all_button);
		adapter = new DownloadsAdapter(inflateView.getContext(), R.layout.downloads_row);
		listView = (ListView) inflateView.findViewById(R.id.list_downloads);
		listView.setAdapter(adapter);
		progress = (ProgressBar) inflateView.findViewById(R.id.progress);
		clearAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				recreateAdaper();
			}

		});
	}

	public DownloadsTab() {
	}

	public long getCancelledId() {
		return cancelledId;
	}

	@Override
	public void currentDownloadingSongTitle(String currentDownloadingSongTitle) {
	}

	@Override
	public void setFileUri(String uri, long downloadId) {
		for (int i = 0; i < adapter.getCount(); i++) {
			MusicData data = adapter.getItem(i);
			if (data.getDownloadId() == downloadId) {
				data.setFileUri(uri);
			}
		}
	}

	public void setFilter(String text) {
		try {
			adapter.getFilter().filter(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void recreateAdaper() {
		ArrayList<MusicData> dataMusic = new ArrayList<MusicData>();
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItem(i).isDownloaded()) {
				dataMusic.add(adapter.getItem(i));
			}
		}
		if (!dataMusic.isEmpty()) {
			DBHelper.getInstance().deleteAll();
		}
		for (MusicData musicData : dataMusic) {
			adapter.remove(musicData);
		}
	}
}
