package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;

import com.special.utils.UISwipableList;
import com.special.utils.UISwipableList.OnSwipableListener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class BaseAdapter<T> extends ArrayAdapter<T> {

	private LayoutInflater inflater;
	protected ViewGroup parent;
	private int layoutId;

	public BaseAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<T>());
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
	}

	public BaseAdapter(Context context, int resource, ArrayList<T> array) {
		super(context, resource, array);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
	}

	protected abstract ViewHolder<T> createViewHolder(final View v);

	protected void onItemSwipeVisible(Object selected, View v) {}

	protected void onItemSwipeGone(Object selected, View v) {}
	
	public void cancelTimer() {}
	
	protected abstract boolean isSetListener();
	
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
		if (isSetListener()) setListener(p, v, position);
		h.hold(item, position);
		return v;
	}

	private void setListener(ViewGroup p, View v, final int position) {
		if (null == parent) {
			parent = p;
		}
		((UISwipableList) parent).setOnSwipableListener(new OnSwipableListener() {

			@Override
			public void onSwipeVisible(Object selected, View v) {
				onItemSwipeVisible(selected, v);
			}

			@Override
			public void onSwipeGone(Object selected, View v) {
				onItemSwipeGone(selected, v);
			}
		});
	}

	public ArrayList<T> getAll() {
		ArrayList<T> result = new ArrayList<T>();
		for (int i = 0; i < getCount(); i++) {
			result.add(getItem(i));
		}
		return result;
	}

	public void changeArray(ArrayList<T> array) {
		setNotifyOnChange(false);
		clear();
		for (T t : array) {
			add(t);
		}
		notifyDataSetChanged();
	}

	public static abstract class ViewHolder<T> {
		protected abstract void hold(T item, int position);
	}
}
