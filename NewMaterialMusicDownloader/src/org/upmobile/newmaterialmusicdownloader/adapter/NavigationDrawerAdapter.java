package org.upmobile.newmaterialmusicdownloader.adapter;

import java.util.Collections;
import java.util.List;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.data.NavDrawerItem;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.ui.widget.RippleView;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

	private List<NavDrawerItem> data = Collections.emptyList();
	private Context context;
	
	private int mSelectedPosition = -1;
    private int mTouchedPosition = -1;

	public NavigationDrawerAdapter(Context context, List<NavDrawerItem> data) {
		this.context = context;
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
		View view = LayoutInflater.from(context).inflate(R.layout.nav_drawer_row, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		NavDrawerItem current = data.get(position);
		if (current.getType() == NavDrawerItem.Type.Secondary) {
			holder.itemView.setClickable(false);
			holder.ripple.getLayoutParams().height = Util.dpToPx(context, 36);
			holder.title.setTextAppearance(context, R.style.boldText);
			setVisibility(holder, false);
		} else {
			holder.itemView.setClickable(true);
			holder.ripple.getLayoutParams().height = Util.dpToPx(context, 48);
			holder.title.setTextAppearance(context, R.style.normalText);
			setVisibility(holder, true);
			if (current.getIcon() != 0) {
				holder.icon.setImageResource(current.getIcon());
			}
		}
		holder.title.setText(current.getTitle());
		if (mSelectedPosition == position || mTouchedPosition == position) {
        	holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.selected_item));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
	}

	@Override
	public int getItemCount() {
		return data != null ? data.size() : 0;
	}
	
	@Override
	public long getItemId(int position) {
		return data.get(position).getId();
	}
	
	private void setVisibility(final ViewHolder holder, final boolean visible) {
		holder.icon.setVisibility(visible ? View.VISIBLE : View.GONE);
		holder.line.setVisibility(visible ? View.GONE : View.VISIBLE);
	}
	
	public void selectPosition(int position) {
        int lastPosition = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(lastPosition);
        notifyItemChanged(position);
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