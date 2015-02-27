package org.upmobile.newmusicdownloader.adapter;

import java.util.List;

import org.upmobile.newmusicdownloader.DrawerItem;
import org.upmobile.newmusicdownloader.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationAdapter extends BaseAdapter {

	private List<DrawerItem> drawerItems;
	private LayoutInflater inflater;

	public NavigationAdapter(Context context, List<DrawerItem> items) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		drawerItems = items;
	}

	@Override
	public int getCount() {
		return drawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		return drawerItems.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		DrawerItem menuItem = (DrawerItem) getItem(position);
		if (convertView == null) {
			holder = new ViewHolder();
			switch (menuItem.getType()) {
			case TYPE_MENU:
				convertView = inflater.inflate(R.layout.navigation_item, parent, false);
				holder.icon = (ImageView) convertView.findViewById(R.id.navigation_image);
				holder.title = (TextView) convertView.findViewById(R.id.navigation_name);
				break;
			case TYPE_SECTION:
				convertView = inflater.inflate(R.layout.navigation_section, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.section);
				break;
			case TYPE_SETTING:
				convertView = inflater.inflate(R.layout.navigation_settings, parent, false);
				holder.icon = (ImageView) convertView.findViewById(R.id.navigation_image);
				holder.title = (TextView) convertView.findViewById(R.id.navmenusection_path);
				break;
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.title.setText(menuItem.getTitle());
		if (menuItem.getIcon() != 0) {
			holder.icon.setImageResource(menuItem.getIcon());
		}
		return convertView;
	}

	private static class ViewHolder {
		public ImageView icon;
		public TextView title;
	}

	@Override
	public long getItemId(int paramInt) {
		return 0;
	}
}
