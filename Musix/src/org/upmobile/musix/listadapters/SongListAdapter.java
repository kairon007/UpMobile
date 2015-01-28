package org.upmobile.musix.listadapters;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.upmobile.musix.R;
import org.upmobile.musix.utils.BitmapHelper;
import org.upmobile.musix.utils.TypefaceHelper;

import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Gustavo on 02/07/2014.
 */
public class SongListAdapter extends BaseAdapter {

    TypefaceHelper typefaceHelper;
    ArrayList<MusicData> songArrayList;
    private Context mContext;
    BitmapHelper bitmapHelper;
    Drawable defaultImage;
    ContentResolver musicResolver;

    public SongListAdapter(Context context, ArrayList<MusicData> songs) {
        mContext = context;
        songArrayList = songs;
        bitmapHelper = new BitmapHelper(mContext);
        musicResolver = mContext.getContentResolver();
        typefaceHelper = new TypefaceHelper(mContext);
        defaultImage = mContext.getResources().getDrawable(R.drawable.def_player_cover);
    }

    public void add(MusicData song) {
        if (songArrayList != null) {
            songArrayList.add(song);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return songArrayList.size();
    }

    @Override
    public MusicData getItem(int position) {
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
        MusicData song;

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

}
