package org.upmobile.clearmusicdownloader.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class BaseAdapter<T> extends ArrayAdapter<T> {

	private LayoutInflater inflater;
	private int layoutId;

	public BaseAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<T>());
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
	}

	protected View createView(ViewGroup parent) {
		return inflater.inflate(layoutId, parent, false);
	}

	protected abstract ViewHolder<T> createViewHolder(final View v);

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		BaseAdapter.ViewHolder<T> h;
		if (v == null) {
			v = createView(parent);
			h = createViewHolder(v);
			v.setTag(h);
		} else {
			h = (BaseAdapter.ViewHolder<T>) v.getTag();
		}
		T item = getItem(position);
		h.hold(item);
		return v;
	}

	public static abstract class ViewHolder<T> {
		protected abstract class ItemizedClickListener implements OnClickListener {
			private T item;

			public T getItem() {
				return item;
			}

			public void setItem(T item) {
				this.item = item;
			}

		}

		protected abstract void hold(T item);
	}

}
