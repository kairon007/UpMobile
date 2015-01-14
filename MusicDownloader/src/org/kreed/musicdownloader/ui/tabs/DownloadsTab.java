package org.kreed.musicdownloader.ui.tabs;

import java.util.ArrayList;
import java.util.Collection;

import org.kreed.musicdownloader.DBHelper;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.interfaces.LoadPercentageInterface;
import org.kreed.musicdownloader.interfaces.TaskSuccessListener;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener.CanceledCallback;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadsTab implements LoadPercentageInterface {

	private final Object lock = new Object();
	private ListView listView;
	private ProgressBar progress;
	private DownloadsAdapter adapter;
	private static DownloadsTab instance;
	private View view;
	private Activity activity;
	private final long DOWNLOAD_FINISHED = -1;
	private ImageButton clearAll;

	public final class DownloadsAdapter extends ArrayAdapter<MusicData> implements TaskSuccessListener {

		private ArrayList<MusicData> mObjects;
		private ArrayList<MusicData> mOriginalValues;
		private LayoutInflater inflater;
		private Filter filter;
		private final int ANIMATION_DURATION = 200;

		private DownloadsAdapter(Context context, int resource) {
			super(context, resource);
			this.inflater = LayoutInflater.from(context);
			mObjects = new ArrayList<MusicData>();
			DBHelper.getInstance(getContext()).getAll(this);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.downloads_row, parent, false);
				holder = createViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				if (holder.needInvalidate) {
					convertView = inflater.inflate(R.layout.downloads_row, parent, false);
					holder = createViewHolder(convertView);
					convertView.setTag(holder);
				}
			}
			if (holder.remove != null) {
				final int pos  = position;
				holder.remove.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(final View v) {
						v.setOnClickListener(null);
						final MusicData song = getItem(pos);
						collapse((View)v.getParent(), new AnimationListener() {
							
							@Override
							public void onAnimationStart(Animation animation) {
								((ViewHolder)((View)v.getParent()).getTag()).needInvalidate = true;
								if (song.isDownloaded()) removeByUri(song.getFileUri());
								else remove(song);
							}
							
							@Override
							public void onAnimationRepeat(Animation animation) {}
							
							@Override
							public void onAnimationEnd(Animation animation) {}
						});
						if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
							CanceledCallback callback = (CanceledCallback) song.getTag();
							if (null != callback) {
								callback.cancel();
							}
						} else if (!DownloadCache.getInstanse().remove(song.getSongArtist().trim(), song.getSongTitle().trim())) {
							if (song.isDownloaded()) {
								DBHelper.getInstance(getContext()).delete(song);
							} else {
								DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
								long cancelledId = song.getDownloadId();
								try {
									int i = manager.remove(cancelledId);
								} catch (Exception e) {
									android.util.Log.d(getClass().getName(), "" + e);
								}
							}
						}
					}
				});
			}
			MusicData song = getItem(position);
			if (song != null) {
				boolean isWhiteTheme = Util.getThemeName(getContext()).equals(Util.WHITE_THEME);
				holder.artist.setText(song.getSongArtist());
				holder.title.setText(song.getSongTitle());
				if (song.getSongBitmap() != null) {
					holder.cover.setImageBitmap(song.getSongBitmap());
				} else {
					holder.cover.setImageResource(R.drawable.fallback_cover);
				}
				if (song.isDownloaded()) {
					holder.downloadProgress.setVisibility(View.GONE);
					holder.remove.setImageResource(isWhiteTheme ? R.drawable.icon_ok_black : R.drawable.icon_ok);
				} else {
					holder.downloadProgress.setVisibility(View.VISIBLE);
					long progress = song.getDownloadProgress();
					holder.downloadProgress.setIndeterminate(progress == 0);
					holder.downloadProgress.setProgress((int) progress);
					holder.remove.setImageResource(isWhiteTheme ? R.drawable.icon_cancel_black : R.drawable.icon_cancel);
				}
				holder.duration.setText(song.getSongDuration());
			}
			convertView.setTag(holder);
			return convertView;
		}

		private ViewHolder createViewHolder(View convertView) {
			ViewHolder holder;
			holder = new ViewHolder();
			holder.artist = (TextView) convertView.findViewById(R.id.songArtist);
			holder.title = (TextView) convertView.findViewById(R.id.songTitle);
			holder.cover = (ImageView) convertView.findViewById(R.id.cover);
			holder.duration = (TextView) convertView.findViewById(R.id.totalTime);
			holder.downloadProgress = (ProgressBar) convertView.findViewById(R.id.progressBar);
			holder.remove = (ImageButton) convertView.findViewById(R.id.cancel);
			return holder;
		}
		
		private void collapse(final View v, AnimationListener al) {
			final int initialHeight = v.getMeasuredHeight();

			Animation anim = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					if (interpolatedTime == 1) {
						v.setVisibility(View.GONE);
					}
					else {
						v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
						v.requestLayout();
					}
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			if (al!=null) {
				anim.setAnimationListener(al);
			}
			anim.setDuration(ANIMATION_DURATION);
			v.startAnimation(anim);
		}
		
		private class ViewHolder {
			TextView title;
			TextView artist;
			TextView duration;
			ImageView cover;
			ImageButton remove;
			ProgressBar downloadProgress;
			boolean needInvalidate = false;
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
				redraw();
			}
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
				redraw();
			}
		}
		
		@SuppressLint("NewApi")
		@Override
		public void addAll(MusicData... items) {
			super.addAll(items);
		}

		@SuppressLint("NewApi")
		@Override
		public void addAll(Collection<? extends MusicData> collection) {
			synchronized (lock) {
				if (mOriginalValues != null) {
					mOriginalValues.addAll(collection);
				}
				mObjects.addAll(collection);
				redraw();
			}
		}

		@Override
		public void clear() {
			synchronized (lock) {
				if (mOriginalValues != null) {
					mOriginalValues.clear();
				}
				mObjects.clear();
				redraw();
			}
		}
		
		public void removeByUri(String filePath) {
			synchronized (lock) {
				if (mOriginalValues != null) {
					int index = 0;
					for (int i = 0; i < mOriginalValues.size(); ++i) {
						if (mOriginalValues.get(i).getFileUri() != null && mOriginalValues.get(i).getFileUri().equals(filePath)) {
							index = i;
							break;
						}
					}
					if (index != -1) {
						mOriginalValues.remove(index);
					}
				}
				int index = -1;
				for (int i = 0; i < mObjects.size(); ++i) {
					if (mObjects.get(i).getFileUri() != null && mObjects.get(i).getFileUri().equals(filePath)) {
						index = i;
						break;
					}
				}
				if (index != -1) {
					mObjects.remove(index);
				}
				redraw();
			}
		}
		
		@Override
		public void remove(MusicData object) {
			synchronized (lock) {
				if (mOriginalValues != null) {
					mOriginalValues.remove(object);
				}
				mObjects.remove(object);
				redraw();
			}
		}

		@Override
		public int getCount() {
			synchronized (lock) {
				if (mObjects == null) {
					return 0;
				}
				return mObjects.size();
			}
		}

		@Override
		public MusicData getItem(int position) {
			synchronized (lock) {
				return mObjects.get(position);
			}
		}

		@Override
		public int getPosition(MusicData item) {
			synchronized (lock) {
				return mObjects.indexOf(item);
			}
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

	public void deleteItem(long downloadId) {
		MusicData buf = null;
		for (int i = 0; i < adapter.getCount(); i++) {
			buf = adapter.getItem(i);
			if (buf.getDownloadId() == downloadId) {
				break;
			}
		}
		if (null == buf) return;
		adapter.remove(buf);
	}

	@Override
	public void insertProgress(long progress, long downloadId) {
		synchronized (lock) {
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
	}
	
	public void insertTag(CanceledCallback callback, long downloadId) {
		synchronized (lock) {
			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getItem(i).getDownloadId() == downloadId) {
					adapter.getItem(i).setTag(callback);
				}
			}
		}
	}

	public void insertData(ArrayList<MusicData> result) {
		for (MusicData data : result) {
			insertData(data);
		}
	}

	public void insertData(MusicData data) {
		adapter.insert(data, 0);
	}

	public boolean updateData(long lastID, long newID) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItem(i).getDownloadId() == lastID) {
				adapter.getItem(i).setDownloadId(newID);
				return true;
			}
		}
		return false;
	}

	public static View getInstanceView(LayoutInflater layoutInflater, Activity activity) {
		View instanceView = getInstance(layoutInflater, activity).view;
		ViewGroup parent = null;
		if (null != instanceView){
			parent = (ViewGroup) instanceView.getParent();
		}
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
		listView.setDivider(activity.getResources().getDrawable(R.drawable.layout_divider));
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

//	public long getCancelledId() {
//		return cancelledId;
//	}

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