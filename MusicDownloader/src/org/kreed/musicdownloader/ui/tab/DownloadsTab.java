package org.kreed.musicdownloader.ui.tab;

import java.util.ArrayList;
import java.util.Collection;

import org.kreed.musicdownloader.DBHelper;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.interfaces.LoadPercentageInterface;
import org.kreed.musicdownloader.interfaces.MusicDataInterface;
import org.kreed.musicdownloader.interfaces.TaskSuccessListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
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

public class DownloadsTab implements LoadPercentageInterface, MusicDataInterface {
	
	private final String SET_VIS = "set.vis";
	private ListView listView;
	private ProgressBar progress;
	private DownloadsAdapter adapter;
	private String progressString = "0";
	private static DownloadsTab instance;
	private View view;
	private Activity activity;
	private LayoutInflater inflater;
	private MusicData mData;
	private Bitmap cover;
	private long cancelledId;
	private String currentDownloadingSongTitle;
	private Long currentDownloadingID;
	private ImageButton clearAll;
	
	private final class DownloadsAdapter extends ArrayAdapter<MusicData> implements TaskSuccessListener	{

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

		@SuppressLint("NewApi")
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
				if (progressString != null && song.getDownloadProgress() != null) {
					if (song.getDownloadProgress().equals(SET_VIS)) {
						holder.downloadProgress.setVisibility(View.INVISIBLE);
						holder.remove.setImageResource(R.drawable.icon_ok);
					} else {
						holder.downloadProgress.setVisibility(View.VISIBLE);
						holder.remove.setImageResource(R.drawable.icon_cancel);
						holder.downloadProgress.setProgress((int) Double.parseDouble(song.getDownloadProgress()));
					}
				} else {
					holder.downloadProgress.setVisibility(View.INVISIBLE);
					holder.remove.setImageResource(R.drawable.icon_ok);
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
			if (mOriginalValues != null) {
				mOriginalValues.add(object);
			}
			mObjects.add(object);
			redraw();
		}

		@Override
		public void insert(MusicData object, int index) {
			if (mOriginalValues != null) {
				mOriginalValues.add(index, object);
			}
			mObjects.add(index, object);
			redraw();
		}

		@SuppressLint("NewApi")
		@Override
		public void addAll(Collection<? extends MusicData> collection) {
			if (mOriginalValues != null) {
				mOriginalValues.addAll(collection);
			}
			mObjects.addAll(collection);
			redraw();
		}

		@Override
		public void clear() {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
			}
			mObjects.clear();
			redraw();
		}
		
		@Override
		public void remove(MusicData object) {
			if (mOriginalValues != null) {
				mOriginalValues.remove(object);
			}
			mObjects.remove(object);
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

	@Override
	public void insertProgress(String progressString) {
		this.progressString = progressString;
		if (currentDownloadingSongTitle != null) {
			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getItem(i).getSongTitle().equalsIgnoreCase(currentDownloadingSongTitle) && adapter.getItem(i).getDownloadId() == currentDownloadingID) {
					adapter.getItem(i).setDownloadProgress(progressString);
					if (progressString.equals("100.0") || progressString.equals("100")) {
						adapter.getItem(i).setDownloadProgress(SET_VIS);
					}
				}
			}
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void insertCover(Bitmap cover) {
		this.cover = cover;
		adapter.getItem(0).setSongBitmap(cover);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void insertData(ArrayList<MusicData> result) {
		for (MusicData data : result) {
			mData = data;
			adapter.insert(data, 0);
		}
		adapter.notifyDataSetChanged();
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
		this.inflater = layoutInflater;
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
		this.currentDownloadingSongTitle = currentDownloadingSongTitle;
	}

	@Override
	public void currentDownloadingID(Long currentDownloadingID) {
		this.currentDownloadingID = currentDownloadingID;
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
		adapter.getFilter().filter(text);
	}
	
	public void recreateAdaper() {
		ArrayList<MusicData> dataMusic = new ArrayList<MusicData>();
		for (int i = 0; i < adapter.getCount(); i++) {
			MusicData data = adapter.getItem(i);
			if ((progressString != null && data.getDownloadProgress() != null && data.getDownloadProgress().equals(SET_VIS))  || data.getFileUri() != null) {
				adapter.remove(data);
			}
		}
		DBHelper.getInstance().deleteAll();
	}
	
}
