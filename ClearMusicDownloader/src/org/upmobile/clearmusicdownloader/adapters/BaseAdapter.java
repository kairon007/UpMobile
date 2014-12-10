package org.upmobile.clearmusicdownloader.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.special.utils.UISwipableList;
import com.special.utils.UISwipableList.OnSwipableListener;

public abstract class BaseAdapter<T> extends ArrayAdapter<T> {

	private LayoutInflater inflater;
	protected ViewGroup parent;
	private int layoutId;
	
	public BaseAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<T>());
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
	}

	protected abstract ViewHolder<T> createViewHolder(final View v);
	protected abstract void onItemSwipeVisible(int position);
	protected abstract void onItemSwipeGone(int position);
	
	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup p) {
		View v = convertView;
		BaseAdapter.ViewHolder<T> h;
		T item = getItem(position);
		if (v == null) {
			v = inflater.inflate(layoutId, p, false);
			h = createViewHolder(v);
			v.setTag(h);
		} else {
			h = (BaseAdapter.ViewHolder<T>) v.getTag();
		}
		setListener(p, v, position);
		h.hold(item, position);
		return v;
	}

	private void setListener(ViewGroup p, View v, final int position) {
		if (null == parent) {
			parent = p;
		}
		((UISwipableList) parent).setOnSwipableListener(new OnSwipableListener() {

			@Override
			public void onSwipeVisible(int pos) {
				onItemSwipeVisible(pos);
			}

			@Override
			public void onSwipeGone(int pos) {
				onItemSwipeGone(pos);
			}
		});
	}
	
	public static abstract class ViewHolder<T> {
		
		protected abstract void hold(T item, int position);

	}
}
