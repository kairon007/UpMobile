package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class BaseAdapter<T> extends ArrayAdapter<T> {

	private LayoutInflater inflater;
	private int layoutId;
	
	public BaseAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<T>());
		inflater = LayoutInflater.from(context);
		layoutId = resource;
	}

	public BaseAdapter(Context context, int resource, ArrayList<T> array) {
		super(context, resource, array);
		inflater = LayoutInflater.from(context);
		layoutId = resource;
	}
	
	protected abstract ViewHolder<T> createViewHolder(final View v);
	
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
		h.hold(item, position);
		return v;
	}

	public ArrayList<T> getAll() {
		ArrayList<T> result = new ArrayList<T>();
		for (int i = 0; i < getCount(); i++) {
			result.add(getItem(i));
		}
		return result;
	}
	
	public void changeAll(ArrayList<T> array) {
		setNotifyOnChange(false);
		clear();
		addAll(array);
		notifyDataSetChanged();
	}
	
	public static abstract class ViewHolder<T> {
		
		protected abstract void hold(T item, int position);

	}
}
