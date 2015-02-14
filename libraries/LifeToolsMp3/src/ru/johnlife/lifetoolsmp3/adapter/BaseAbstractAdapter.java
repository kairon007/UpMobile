package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class BaseAbstractAdapter<T extends AbstractSong> extends BaseAdapter {

	private LayoutInflater inflater;
	protected ViewGroup parent;
	private int layoutId;
	private ArrayList<T> items = new ArrayList<T>();
	private Context context;

	public BaseAbstractAdapter(Context context, int resource) {
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
		this.context = context;
	}

	public BaseAbstractAdapter(Context context, int resource, ArrayList<T> array) {
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
		this.context = context;
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
		BaseAbstractAdapter.ViewHolder<T> h;
		T item = (T)getItem(position);
		if (v == null) {
			v = inflater.inflate(layoutId, p, false);
			h = createViewHolder(v);
			v.setTag(h);
		} else {
			h = (BaseAbstractAdapter.ViewHolder<T>) v.getTag();
		}
		h.hold(item, position);
		return v;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<T> getAll() {
		ArrayList<T> result = new ArrayList<T>();
		for (int i = 0; i < getCount(); i++) {
			result.add((T) getItem(i));
		}
		return result;
	}
	
	public void add(T item) {
		items.add(item);
		notifyDataSetChanged();
	}
	
	public void add(ArrayList<T> array) {
		items.clear();
		items.addAll(array);
		notifyDataSetChanged();
	}
	
	public void changeData(ArrayList<T> array) {
		this.items = array;
		notifyDataSetChanged();
	}
	
	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}
	
	public boolean contains(T data) {
		for (int i = 0; i < getCount(); i++) {
			if (getItem(i).equals(data)) {
				return true;
			}
		}
		return false;
	}

	public void remove(T item) {
		items.remove(item);
		notifyDataSetChanged();
	}
	
	public void remove(int index) {
		items.remove(index);
		notifyDataSetChanged();
	}
	
	public Context getContext() {
		return context;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}
	
	@Override
	public Object getItem(int paramInt) {
		return items.get(paramInt);
	}
	
	@Override
	public long getItemId(int paramInt) {
		return items.get(paramInt).getId();
	}
	
	public int getPosition(T item) {
		return items.indexOf(item);
	}
	
	public static abstract class ViewHolder<T extends AbstractSong> {
		protected abstract void hold(T item, int position);
	}
}
