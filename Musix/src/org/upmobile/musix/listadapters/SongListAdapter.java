package org.upmobile.musix.listadapters;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.upmobile.musix.R;
import org.upmobile.musix.utils.TypefaceHelper;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SongListAdapter extends BaseAdapter {

	private TypefaceHelper typefaceHelper;
	private ArrayList<MusicData> songArrayList;
    private Context mContext;

    public SongListAdapter(Context context, ArrayList<MusicData> songs) {
        this.mContext = context;
        this.songArrayList = songs;
        typefaceHelper = new TypefaceHelper(mContext);
    }

	public void add(MusicData song) {
		if (songArrayList != null) {
			songArrayList.add(song);
			reDraw();
		}
	}

	public void reDraw() {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}
	
	@Override
    public int getCount() {
        return songArrayList.size();
    }

    @Override
    public AbstractSong getItem(int position) {
        return songArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder viewHolder;
        AbstractSong song;
        if (row == null) {
            LayoutInflater mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = mLayoutInflater.inflate(R.layout.row_view_song_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.txtSongTitle = (TextView) row.findViewById(R.id.rowView_SongTitle);
            viewHolder.txtArtistName = (TextView) row.findViewById(R.id.rowView_SongArtistName);
            viewHolder.albumCover = (ImageView) row.findViewById(R.id.rowView_SongPicture);
            row.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) row.getTag();
        }
        song = songArrayList.get(position);
        viewHolder.txtSongTitle.setText(song.getTitle());
        viewHolder.txtArtistName.setText(song.getArtist());
        viewHolder.txtSongTitle.setTypeface(typefaceHelper.getRobotoLight());
        viewHolder.txtArtistName.setTypeface(typefaceHelper.getRobotoLight());
        viewHolder.albumCover.setImageResource(R.drawable.ic_launcher);
		WeakReference<Bitmap> bitmap = new WeakReference<Bitmap>(song.getCover(row.getContext()));
		if (null != bitmap && null != bitmap.get()) {
			viewHolder.albumCover.setImageBitmap(bitmap.get());
		} 
        return row;
    }

    private class ViewHolder {
        ImageView albumCover;
        TextView txtSongTitle;
        TextView txtArtistName;
    }

	public void clear() {
		songArrayList.clear();
	}
	
	public void addAll(ArrayList<MusicData> list) {
		songArrayList.addAll(list);
		notifyDataSetChanged();
	}
	
	public void remove(int position) {
		 if ((position < 0) || getCount() < position) {
			 return;
		 }
		 songArrayList.remove(position);
		 notifyDataSetChanged();
	}
	
	public ArrayList<MusicData> getList() {
		return songArrayList;
	}

}
