package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.widget.AnimatedExpandableListView.AnimatedExpandableListAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ExpandableAdapter extends AnimatedExpandableListAdapter {

	private LayoutInflater inflater;

	private ArrayList<PlaylistData> items;

	private String projectPrefics;


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
			holder.hint = (TextView) convertView.findViewById(R.id.textHint);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}
		holder.title.setText(item.getTitle());
		holder.hint.setText(item.getArtist());

		return convertView;
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

	private static class ChildHolder {
		public TextView title;
		public TextView hint;
	}

	private static class GroupHolder {
		public TextView title;
	}

	public void setProjectPrefics(String projectPrefics) {
		this.projectPrefics = projectPrefics;
		
	}
}
