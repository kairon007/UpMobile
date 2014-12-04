package org.upmobile.clearmusicdownloader.adapters;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.data.MusicData;

import com.special.utils.UISwipableList;
import com.special.utils.UISwipableList.OnSwipableListener;

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

	protected abstract ViewHolder<T> createViewHolder(final View v);
	protected abstract void onItemSwipeVisible(int position);
	protected abstract void onItemSwipeGone(int position);
	
	@SuppressWarnings("unchecked")
	@Override
	public View getView(final int position, View convertView, ViewGroup p) {
		if (null == parent) {
			parent = p;
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
		
		protected abstract void hold(T item, int position);

	}
}
