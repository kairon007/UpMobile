package org.kreed.musicdownloader;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;

import com.startapp.android.publish.model.AdPreferences;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsTab implements LoadPercentageInterface, MusicDataInterface {
	private ListView listView;
	private DownloadsAdapter adapter;
	private String progressString = "0";
	private static DownloadsTab instance;
	private View view;
	private Activity activity;
	private LayoutInflater inflater;
	private MusicData mData;

	private final class DownloadsAdapter extends ArrayAdapter<MusicData> {
		private LayoutInflater inflater;

		public DownloadsAdapter(Context context, int resource) {
			super(context, resource);
			this.inflater = LayoutInflater.from(context);
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
						adapter.remove(adapter.getItem(position));
					}
				});
			}
			MusicData song = getItem(position);
			if (song != null) {
				holder.artist.setText(song.getSongArtist());
				holder.title.setText(song.getSongTitle());
				// holder.cover.setImageAlpha(R.drawable.fallback_cover);
				if (progressString != null) {
					holder.downloadProgress.setProgress(Integer.valueOf(song.getDownloadProgress()));
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
	}

	@Override
	public void insertProgress(String progressString) {
		this.progressString = progressString;
		adapter.getItem(0).setDownloadProgress(progressString);
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
		adapter = new DownloadsAdapter(inflateView.getContext(), R.layout.downloads_row);
		listView = (ListView) inflateView.findViewById(R.id.list_downloads);
		listView.setAdapter(adapter);

	}

	public DownloadsTab() {
		// TODO Auto-generated constructor stub
	}
}
