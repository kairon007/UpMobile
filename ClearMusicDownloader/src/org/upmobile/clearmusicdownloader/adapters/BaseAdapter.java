package org.upmobile.clearmusicdownloader.adapters;

import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class BaseAdapter extends ArrayAdapter<Song> {

	private LayoutInflater inflater;
	private int layoutId;

	public BaseAdapter(Context context, int resource) {
		super(context, resource);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutId = resource;
	}

	protected View createView(ViewGroup parent) {
		return inflater.inflate(layoutId, parent, false);
	}

	protected abstract ViewHolder<Song> createViewHolder(final View v);

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		BaseAdapter.ViewHolder<Song> h;
		if (v == null) {
			v = createView(parent);
			h = createViewHolder(v);
			v.setTag(h);
		} else {
			h = (BaseAdapter.ViewHolder<Song>) v.getTag();
		}
		Song item = getItem(position);
		h.hold(item);
		return v;
	}

	public static abstract class ViewHolder<Song> {
		protected abstract class ItemizedClickListener implements OnClickListener {
			private Song item;

			public Song getItem() {
				return item;
			}

			public void setItem(Song item) {
				this.item = item;
			}

		}

		protected abstract void hold(Song item);
	}

}
