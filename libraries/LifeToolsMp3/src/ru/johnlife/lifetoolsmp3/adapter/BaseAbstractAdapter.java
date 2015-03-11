package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;

public abstract class BaseAbstractAdapter<T extends AbstractSong> extends BaseAdapter {

	private ArrayList<T> items = new ArrayList<T>();
	private ArrayList<T> originalItems;
	
	private Context context;
	private LayoutInflater inflater;
	protected ViewGroup parent;
	private Filter filter;
	
	private int layoutId;
	private boolean doNotifyData = true;

	public BaseAbstractAdapter(Context context, int resource) {
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
		this.context = context;
	}

	public BaseAbstractAdapter(Context context, int resource, ArrayList<T> array) {
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
		this.context = context;
		items = array;
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
		if (null != originalItems) {
			originalItems.add(item);
		}
		items.add(item);
		if (doNotifyData) {
			notifyDataSetChanged();
		}
	}
	
	public void add(ArrayList<T> array) {
		if (null != originalItems) {
			originalItems.addAll(array);
		}
		items.addAll(array);
		if (doNotifyData) {
			notifyDataSetChanged();
		}
	}
	
	public void changeData(ArrayList<T> array) {
		if (null != originalItems) {
			originalItems = array;
		}
		items = array;
		if (doNotifyData) {
			notifyDataSetChanged();
		}
	}
	
	public void clear() {
		if (null != originalItems) {
			originalItems.clear();
		}
		items.clear();
		if (doNotifyData) {
			notifyDataSetChanged();
		}
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
		if (null != originalItems) {
			originalItems.remove(item);
		}
		items.remove(item);
		if (doNotifyData) {
			notifyDataSetChanged();
		}
	}
	
	public void remove(int index) {
		if (null != originalItems) {
			originalItems.remove(index);
		}
		items.remove(index);
		if (doNotifyData) {
			notifyDataSetChanged();
		}
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
	
	public Filter getFilter() {
		if (filter == null) {
			filter = new ResultFilter();
		}
		return filter;
	}
	
	public void clearFilter() {
		if (null == filter || null == originalItems) {
			return;
		}
		changeData(originalItems);
		notifyDataSetChanged();
		originalItems = null;
	}
	
	public void setDoNotifyData(boolean doNotifyData) {
		this.doNotifyData = doNotifyData;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		doNotifyData = true;
	}
	
	public static abstract class ViewHolder<T extends AbstractSong> {
		protected abstract void hold(T item, int position);
	}
	
	private class ResultFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			String prefix = constraint.toString().toLowerCase();
			if (originalItems == null) {
				originalItems = new ArrayList<T>(items);
			}
			if (prefix == null || prefix.length() == 0) {
				ArrayList<T> list = new ArrayList<T>(originalItems);
				results.values = list;
				results.count = list.size();
			} else {
				ArrayList<T> list = new ArrayList<T>(originalItems);
				ArrayList<T> nlist = new ArrayList<T>();
				int count = list.size();
				for (int i = 0; i < count; i++) {
					T data = list.get(i);
					String value = data.toString().toLowerCase();
					if (value.contains(prefix)) {
						nlist.add(data);
					}
					results.values = nlist;
					results.count = nlist.size();
				}
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, final FilterResults results) {
			items = (ArrayList<T>) results.values;
			if (results.count == 0) {
            	notifyDataSetInvalidated();
            } else {
            	notifyDataSetChanged();
            }
		}
	}
}
