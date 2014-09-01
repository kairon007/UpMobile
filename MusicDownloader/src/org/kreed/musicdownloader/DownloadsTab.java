package org.kreed.musicdownloader;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsTab implements LoadPercentageInterface, MusicDataInterface {
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
	private final String SET_VIS = "set.vis";
	private long cancelledId;
	private String currentDownloadingSongTitle;
	private Long currentDownloadingID;
	private ImageButton clearAll;

	private final class DownloadsAdapter extends ArrayAdapter<MusicData> implements TaskSuccessListener {
		private LayoutInflater inflater;

		public DownloadsAdapter(Context context, int resource) {
			super(context, resource);
			this.inflater = LayoutInflater.from(context);
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
				holder.remove = (ImageView) convertView.findViewById(R.id.cancel);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (holder.remove != null) {
				holder.remove.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d("logd", "dismiss clicked");
						DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
						cancelledId = adapter.getItem(position).getDownloadId();
						manager.remove(cancelledId);
						if (getItem(position).getFileUri() != null) {
							DBHelper.getInstance(getContext()).delete(getItem(position));
						}
						adapter.remove(adapter.getItem(position));
						recreateAdaper();
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
						holder.remove.setImageResource(R.drawable.ok);
					} else {
						holder.downloadProgress.setVisibility(View.VISIBLE);
						holder.remove.setImageResource(R.drawable.cancel);
						holder.downloadProgress.setProgress((int)Double.parseDouble(song.getDownloadProgress()));
					}
				} else {
					holder.downloadProgress.setVisibility(View.INVISIBLE);
					holder.remove.setImageResource(R.drawable.ok);
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
			ImageView remove;
			ProgressBar downloadProgress;
		}

		@Override
		public void success(ArrayList<MusicData> result) {
			progress.setVisibility(View.GONE);
			insertData(result);
		}
	}

	@Override
	public void insertProgress(String progressString) {
		this.progressString = progressString;
		if (currentDownloadingSongTitle != null) {
			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getItem(i).getSongTitle().equalsIgnoreCase(currentDownloadingSongTitle) && adapter.getItem(i).getDownloadId() ==  currentDownloadingID) {
					adapter.getItem(i).setDownloadProgress(progressString);
					adapter.notifyDataSetChanged();
					if (progressString.equals("100.0") || progressString.equals("100")) {
						adapter.getItem(i).setDownloadProgress(SET_VIS);
						adapter.notifyDataSetChanged();
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
				adapter.clear();
				DBHelper.getInstance().deleteAll();
				recreateAdaper();
			}
		});
	}

	public DownloadsTab() {
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
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
	
	private void recreateAdaper() {
		adapter = new DownloadsAdapter(activity, R.layout.downloads_row);
		listView.setAdapter(adapter);
	}
}
