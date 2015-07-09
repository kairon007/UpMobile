package org.upmobile.materialmusicdownloader.adapter;

import java.util.List;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.models.DrawerItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerAdapter extends BaseAdapter {
	
	private List<DrawerItem> mDrawerItems;
	private LayoutInflater mInflater;
	private final boolean mIsFirstType; //Choose between two types of list items
	
	public DrawerAdapter(Context context, List<DrawerItem> items, boolean isFirstType) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDrawerItems = items;
		mIsFirstType = isFirstType;
	}

	@Override
	public int getCount() {
		return mDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mDrawerItems.get(position).getTag();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
		DrawerItem itemMenu = mDrawerItems.get(position);
		if (convertView == null) {
			switch (itemMenu.getType()) {
			case TYPE_MENU:
				convertView = mIsFirstType ? mInflater.inflate(R.layout.list_view_item_navigation_drawer_1, parent, false) : mInflater.inflate(R.layout.list_view_item_navigation_drawer_2, parent, false);
				holder.icon = (TextView) convertView.findViewById(R.id.icon);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				break;
			case TYPE_SECTION:
				convertView = mInflater.inflate(R.layout.list_view_item_navigation_section, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.section);
				break;
			case TYPE_SETTING:
				convertView = mInflater.inflate(R.layout.list_view_item_navigation_settings, parent, false);
				holder.icon = (TextView) convertView.findViewById(R.id.icon);
				holder.title = (TextView) convertView.findViewById(R.id.path);
				break;
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (mIsFirstType && itemMenu.getIcon() != 0) {	//We chose to set icon that exists in list_view_item_navigation_drawer_1.xml
			holder.icon.setText(itemMenu.getIcon());
		}
		if (itemMenu.getTitle() != 0) {
			holder.title.setText(itemMenu.getTitle());
		} else {
			holder.title.setText(itemMenu.getTitleString());
		}
		return convertView;
	}
	
	private static class ViewHolder {
		public TextView icon;
		public /*Roboto*/TextView title;
	}
}
