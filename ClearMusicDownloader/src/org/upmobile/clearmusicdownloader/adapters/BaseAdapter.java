package org.upmobile.clearmusicdownloader.adapters;

import java.util.ArrayList;

import com.special.utils.UISwipableList;

import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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

	protected abstract ViewHolder<T> createViewHolder(final View v, int position);
	
	@SuppressWarnings("unchecked")
	@Override
	public View getView(final int position, View convertView, ViewGroup p) {
		if (null == parent) parent = p;
		View v = convertView;
		BaseAdapter.ViewHolder<T> h;
		T item = getItem(position);
		if (v == null) {
			v = inflater.inflate(layoutId, p, false);
			h = createViewHolder(v, position);
			v.setTag(h);
		} else {
			h = (BaseAdapter.ViewHolder<T>) v.getTag();
		}
		h.hold(item);
		v.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				((UISwipableList)parent).setSelectedPosition(position);
  				return true;
			}
		});
		return v;
	}

	public static abstract class ViewHolder<T> {
		
		protected abstract void hold(T item);

	}
}
