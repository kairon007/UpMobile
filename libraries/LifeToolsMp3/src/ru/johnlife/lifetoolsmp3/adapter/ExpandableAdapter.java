package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;
import java.util.Locale;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.widget.AnimatedExpandableListView.AnimatedExpandableListAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableAdapter extends AnimatedExpandableListAdapter {

	private LayoutInflater inflater;

	private ArrayList<PlaylistData> items = new ArrayList<PlaylistData>();
	private ArrayList<PlaylistData> itemsOriginal;
	
	private ResultFilter filter;

	private String projectPrefics;

	private Bitmap defaultBmp;

	public ExpandableAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<PlaylistData> items) {
		this.items = items;
	}

	@Override
	public MusicData getChild(int groupPosition, int childPosition) {
		return items.get(groupPosition).getSongs().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ChildHolder holder;
		MusicData item = getChild(groupPosition, childPosition);
		if (convertView == null) {
			holder = new ChildHolder();
			convertView = inflater.inflate(R.layout.playlist_list_item, parent, false);
			holder.title = (TextView) convertView.findViewById(R.id.textTitle);
			holder.artist = (TextView) convertView.findViewById(R.id.textHint);
			holder.cover = convertView.findViewById(R.id.item_cover);
			holder.duaration = (TextView) convertView.findViewById(R.id.textDuration);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}
		holder.title.setText(item.getTitle());
		holder.artist.setText(item.getArtist());
		holder.duaration.setText(Util.getFormatedStrDuration(item.getDuration()));
		Bitmap bitmap = item.getCover(inflater.getContext());
		if (null != bitmap) {
			((ImageView) holder.cover).setImageBitmap(bitmap);
		} else {
			((ImageView) holder.cover).setImageBitmap(getDefaultCover());
		}
		bitmap = null;
		return convertView;
	}

	private Bitmap getDefaultCover() {
		return defaultBmp;
	}

	public void setDefaultBmp(Bitmap bmp) {
		defaultBmp = bmp;
	}

	@Override
	public int getRealChildrenCount(int groupPosition) {
		return items.get(groupPosition).getSongs().size();
	}

	@Override
	public PlaylistData getGroup(int groupPosition) {
		return items.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return items.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupHolder holder;
		PlaylistData item = getGroup(groupPosition);
		if (convertView == null) {
			holder = new GroupHolder();
			convertView = inflater.inflate(R.layout.playlist_group_item, parent, false);
			holder.title = (TextView) convertView.findViewById(R.id.textTitle);
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}
		holder.title.setText(item.getName().replace(projectPrefics, ""));
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}
	
	public void setProjectPrefics(String projectPrefics) {
		this.projectPrefics = projectPrefics;

	}
	
	public Filter getFilter() {
		if (filter == null) {
			filter = new ResultFilter();
		}
		return filter;
	}
	
	public void clearFilter() {
		if (null == filter || null == itemsOriginal) {
			return;
		}
		setData(itemsOriginal);
		notifyDataSetChanged();
		itemsOriginal = null;
	}

	private static class ChildHolder {
		public TextView title;
		public TextView artist;
		public TextView duaration;
		public View cover;
	}

	private static class GroupHolder {
		public TextView title;
	}
	
	private class ResultFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			String prefix = constraint.toString().toLowerCase();
			if (itemsOriginal == null) {
				itemsOriginal = new ArrayList<PlaylistData>(items);
			}
			if (prefix == null || prefix.length() == 0) {
				ArrayList<PlaylistData> list = new ArrayList<PlaylistData>(itemsOriginal);
				results.values = list;
				results.count = list.size();
			} else {
				ArrayList<PlaylistData> list = new ArrayList<PlaylistData>(itemsOriginal);
				ArrayList<PlaylistData> nlist = new ArrayList<PlaylistData>();
				int count = list.size();
				for (int i = 0; i < count; i++) {
					PlaylistData data = list.get(i);
					String[] tempArray = data.getName().split("/");
					String value = tempArray[tempArray.length - 1];
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
			items = (ArrayList<PlaylistData>) results.values;
			if (results.count == 0) {
            	notifyDataSetInvalidated();
            } else {
            	notifyDataSetChanged();
            }
		}
	}

}
