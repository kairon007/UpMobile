package org.upmobile.newmaterialmusicdownloader.adapter;

import java.util.Collections;
import java.util.List;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.data.NavDrawerItem;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.ui.widget.RippleView;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

	private List<NavDrawerItem> data = Collections.emptyList();
	private LayoutInflater inflater;

	public NavigationDrawerAdapter(Context context, List<NavDrawerItem> data) {
		inflater = LayoutInflater.from(context);
		this.data = data;
		setHasStableIds(true);
	}

	public void delete(int position) {
		data.remove(position);
		notifyItemRemoved(position);
	}
	
	public void add(int position, NavDrawerItem object) {
		data.add(position, object);
		notifyItemInserted(position);
	}

	public void updateList(List<NavDrawerItem> data) {
		this.data = data;
		notifyDataSetChanged();
	}
	
	public void updateItem(int position, NavDrawerItem object) {
		delete(position);
		add(position, object);
	}
	
	public void clear() {
		data.clear();
		notifyDataSetChanged();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		NavDrawerItem current = data.get(position);
		if (current.getType() == NavDrawerItem.Type.Secondary) {
			holder.ripple.setClickable(Boolean.FALSE);
			holder.ripple.getLayoutParams().height = Util.dpToPx(inflater.getContext(), 36);
			setVisibility(holder, Boolean.FALSE);
		} else {
			holder.ripple.setClickable(Boolean.TRUE);
			holder.ripple.getLayoutParams().height = Util.dpToPx(inflater.getContext(), 48);
			setVisibility(holder, Boolean.TRUE);
		}
		holder.title.setText(current.getTitle());
		if (current.getIcon() != 0) {
			holder.icon.setImageResource(current.getIcon());
		}
	}

	@Override
	public int getItemCount() {
		return data.size();
	}
	
	@Override
	public long getItemId(int position) {
		return data.get(position).getId();
	}
	
	private void setVisibility(final ViewHolder holder, final boolean visible) {
		holder.icon.setVisibility(visible ? View.VISIBLE : View.GONE);
		holder.line.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		RippleView ripple;
		View line;
		ImageView icon;
		TextView title;

		public ViewHolder(View itemView) {
			super(itemView);
			ripple = (RippleView) itemView.findViewById(R.id.ripple);
			line = itemView.findViewById(R.id.line);
			icon = (ImageView) itemView.findViewById(R.id.icon);
			title = (TextView) itemView.findViewById(R.id.title);
		}
	}
}