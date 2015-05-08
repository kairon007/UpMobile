package org.upmobile.newmaterialmusicdownloader.adapter;

import java.util.Collections;
import java.util.List;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.data.NavDrawerItem;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.ui.widget.RippleView;
import android.content.Context;
import android.graphics.Typeface;
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

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		NavDrawerItem current = data.get(position);
		if (current.getType() == NavDrawerItem.Type.Secondary) {
			holder.ripple.setClickable(false);
			holder.ripple.getLayoutParams().height = Util.dpToPx(inflater.getContext(), 36);
			int padding = Util.dpToPx(inflater.getContext(), 16);
			holder.ripple.setPadding(padding, 0, padding, 0);
			holder.icon.setVisibility(View.GONE);
			holder.line.setVisibility(View.VISIBLE);
			holder.title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
			holder.title.setTextSize(14);
		} else {
			holder.ripple.setClickable(true);
			holder.icon.setVisibility(View.VISIBLE);
			holder.line.setVisibility(View.GONE);
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