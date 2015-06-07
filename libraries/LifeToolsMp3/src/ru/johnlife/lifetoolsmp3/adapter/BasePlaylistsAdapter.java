package ru.johnlife.lifetoolsmp3.adapter;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public abstract class BasePlaylistsAdapter extends ArrayAdapter<AbstractSong> {

	private LayoutInflater inflater;
	
	protected abstract String getDirectory();
	protected abstract Bitmap getDefaultCover();

	public BasePlaylistsAdapter(Context context, int resource) {
		super(context, resource);
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		ViewHolderGroup holderGroup = null;
		final AbstractSong data = getItem(position);
		if (data.getClass() == MusicData.class) {
			if (convertView == null || !(convertView.getTag().getClass() == ViewHolder.class)) { 
				convertView = inflater.inflate(R.layout.playlist_list_item, parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.textTitle);
				holder.artist = (TextView) convertView.findViewById(R.id.textHint);
				holder.cover = convertView.findViewById(R.id.item_cover);
				holder.duaration = (TextView) convertView.findViewById(R.id.textDuration);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (null != data) {
				holder.title.setText(data.getTitle());
				holder.artist.setText(data.getArtist());
				Bitmap cover = data.getCover();
				((ImageView) holder.cover).setImageBitmap(cover == null ? getDefaultCover() : cover);
				holder.duaration.setText(Util.getFormatedStrDuration(data.getDuration()));
			}
			convertView.setTag(holder);
		} else {
			if (convertView == null  || !(convertView.getTag().getClass() == ViewHolderGroup.class)) { 
				convertView = inflater.inflate(R.layout.playlist_group_item, parent, false);
				holderGroup = new ViewHolderGroup();
				holderGroup.groupTitle = (TextView) convertView.findViewById(R.id.textTitle);
				holderGroup.playAll = (View) convertView.findViewById(R.id.playAll);
			} else {
				holderGroup = (ViewHolderGroup) convertView.getTag();
			}
			if (null != data) {
				holderGroup.groupTitle.setText(((PlaylistData) data).getName().replace(getDirectory(), ""));
				holderGroup.playAll.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						playAll((PlaylistData) data, v.getContext());
					}

				});
			}
			convertView.setTag(holderGroup);
		}
		return convertView;
		
	}
	
	static class ViewHolder {
		public TextView title;
		public TextView artist;
		public TextView duaration;
		public View cover;
	}
	
	static class ViewHolderGroup {
		public TextView groupTitle;
		public View playAll;
	}
	
	public void playAll(PlaylistData data, Context context) {
	}
	
}
