package org.kreed.musicdownloader.ui.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.ui.activity.MainActivity;

import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LibraryTab extends ArrayAdapter<MusicData> implements TextWatcher {

	private final Object lock = new Object();
	private ArrayList<MusicData> mObjects;
	private ArrayList<MusicData> mOriginalValues;
	private Filter filter;
	private MainActivity activity;
	private LayoutInflater inflater;
	private ListView lv;
	private EditText textFilter;
	private boolean isDeployFilter;
	private Runnable reDraw = new Runnable() {

		@Override
		public void run() {
			notifyDataSetChanged();
		}
	};
	

	public LibraryTab(int resource, MainActivity activity) {
		super(activity, resource);
		mObjects = new ArrayList<MusicData>();
		this.activity = activity;
		inflater = LayoutInflater.from(activity);
		textFilter = (EditText) activity.findViewById(R.id.filter_text);
		textFilter.addTextChangedListener(this);
		String str = activity.getTextFilterLibrary();
		if (!str.equals("")) {
			textFilter.setText(str);
		}
	}
	
	@Override
	public void add(MusicData object) {
		synchronized (lock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(object);
			}
			if (null == mObjects) {
				mObjects = new ArrayList<MusicData>();
			}
			mObjects.add(object);
		}
		activity.runOnUiThread(reDraw);
	}
	
	@Override
	public void addAll(Collection<? extends MusicData> collection) {
        synchronized (lock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
        activity.runOnUiThread(reDraw);
	}
	
	@Override
	public void addAll(MusicData... items) {
        synchronized (lock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
            }
        }
        activity.runOnUiThread(reDraw);
	}
	
	@Override
	public void remove(MusicData object) {
		synchronized (lock) {
			if (mOriginalValues != null) {
				mOriginalValues.remove(object);
			}
			mObjects.remove(object);
		}
		activity.runOnUiThread(reDraw);
	}
	
	public void removeByUri(String filePath) {
		synchronized (lock) {
			if (mOriginalValues != null) {
				for (int i = 0; i < mOriginalValues.size(); ++i) {
					if (mOriginalValues.get(i).getFileUri().endsWith(filePath)) {
						mOriginalValues.remove(i);
						break;
					}
							
				}
			}
			for (int i = 0; i < mObjects.size(); ++i) {
				if (mObjects.get(i).getFileUri().endsWith(filePath)) {
					mObjects.remove(i);
					break;
				}
						
			}
		}
		activity.runOnUiThread(reDraw);
	}
	
	public void updateItem(MusicData oldData, MusicData newData) {
		synchronized (lock) {
			getItem(getPosition(oldData)).update(newData);
		}
		activity.runOnUiThread(reDraw);
	}
	
	@Override
	public int getCount() {
		synchronized (lock) {
			if (null == mObjects) {
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
	public void clear() {
		synchronized (lock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
			}
			mObjects.clear();
		}
		activity.runOnUiThread(reDraw);
	}
	
	@Override
	public void insert(MusicData object, int index) {
        synchronized (lock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
            }
        }
        activity.runOnUiThread(reDraw);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderItem holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.library_item, null);
			holder = new ViewHolderItem();
			holder.hThreedot = (ImageButton) convertView.findViewById(R.id.threedotButton);
			holder.hButtonPlay = (ImageButton) convertView.findViewById(R.id.play_song);
			holder.hSongTitle = (TextView) convertView.findViewById(R.id.title_song);
			holder.hSongArtist = (TextView) convertView.findViewById(R.id.artist);
			holder.hCoverImage = (ImageView) convertView.findViewById(R.id.cover_song);
			holder.hSongDuration = (TextView) convertView.findViewById(R.id.duration_song);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderItem) convertView.getTag();
		}
		MusicData music = getItem(position);
		String strArtist = music.getSongArtist();
		String strTitle = music.getSongTitle();
		String strDuration = music.getSongDuration();
		Bitmap bitmap = music.getSongBitmap();
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			holder.hButtonPlay.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.selectable_item_bg_honeycomb));
		}
		if (null != bitmap) {
			holder.hCoverImage.setImageBitmap(bitmap);
		} else {
			holder.hCoverImage.setImageResource(R.drawable.fallback_cover);
		}
		holder.hSongTitle.setText(strTitle);
		holder.hSongDuration.setText(strDuration);
		holder.hSongArtist.setText(strArtist);
		final int pos = position;
		activity.registerForContextMenu(convertView);
		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				MusicData music = getItem(pos);
				activity.setDeletedItem(music);
				activity.setSelectedItem(music);
				return false;
			}
			
		});
		holder.hThreedot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MusicData music = getItem(pos);
				activity.setDeletedItem(music);
				activity.setSelectedItem(music);
				arg0.performLongClick();
			}
			
		});
		holder.hButtonPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MusicData musicData = getItem(pos);
				activity.play(null, musicData);
			}

		});
		return convertView;
	}

	private class ViewHolderItem {
		ImageButton hButtonPlay;
		ImageView hCoverImage;
		TextView hSongTitle;
		TextView hSongArtist;
		TextView hSongDuration;
		ImageButton hThreedot;
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
			activity.runOnUiThread(reDraw);
			if (lv != null && isDeployFilter) {
				lv.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		notifyFilter(null);
		isDeployFilter = (count != 0) ? true : false;
	}

	@Override
	public void afterTextChanged(Editable s) {

	}
	
	public void notifyFilter(ListView lv) {
		getFilter().filter(textFilter.getText().toString().toLowerCase(Locale.ENGLISH));
		this.lv = lv;
	}

	public boolean checkDeployFilter() {
		return isDeployFilter;
	}
	
}