package ru.johnlife.lifetoolsmp3.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.utils.Util;


public abstract class BasePlaylistsAdapter extends BaseAbstractAdapter<AbstractSong> {

	private LayoutInflater inflater;
	
	protected abstract String getDirectory();
	protected abstract Bitmap getDefaultCover();
	protected abstract int getSecondaryLayout();
	protected abstract int getFirstLayout();

	public BasePlaylistsAdapter(Context context, int resource) {
		super(context, resource);
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup p) {
		View v = convertView;
		BasePlaylistViewHolder h;
		AbstractSong item = (AbstractSong)getItem(position);
		boolean isMusicData = item.getClass() == MusicData.class;
		if (isMusicData) {
			if (v == null || !(((BasePlaylistViewHolder) v.getTag()).isChild)) {
				v = inflater.inflate(getSecondaryLayout(), p, false);
				h = (BasePlaylistViewHolder) createViewHolder(v);
				v.setTag(h);
			} else {
				h = (BasePlaylistViewHolder) v.getTag();
			}
			h.hold(item, position);
		} else {
			if (v == null || (((BasePlaylistViewHolder) v.getTag()).isChild)) {
				v = inflater.inflate(getFirstLayout(), p, false);
				h = (BasePlaylistViewHolder) createViewHolder(v);
				v.setTag(h);
			} else {
				h = (BasePlaylistViewHolder) v.getTag();
			}
			h.hold(item, position);
		}
		return v;
	}
	
	protected abstract class BasePlaylistViewHolder extends ViewHolder<AbstractSong> {
		
		protected TextView title;
		protected TextView artist;
		protected TextView duration;
		protected ImageView cover;
		protected TextView groupTitle;
		protected View playAll;
		protected View customGroupIndicator;
		protected boolean isChild = false;
		
		@Override
		protected void hold(final AbstractSong data, int position) {
			if (data.getClass() == MusicData.class) {
				isChild = true;
				if (null != data) {
					title.setText(data.getTitle());
					artist.setText(data.getArtist());
					Bitmap image = data.getCover();
					cover.setImageBitmap(image == null ? getDefaultCover() : image);
					duration.setText(Util.getFormatedStrDuration(data.getDuration()));
				}
			} else {
				isChild = false;
				if (null != data) {
					groupTitle.setText(((PlaylistData) data).getName().replace(getDirectory(), ""));
					playAll.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							playAll((PlaylistData) data, v.getContext());
						}

					});
				}
			}
		}
	}
	
	public void playAll(PlaylistData data, Context context) {
	}
	
}
