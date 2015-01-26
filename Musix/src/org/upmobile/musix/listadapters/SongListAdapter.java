package org.upmobile.musix.listadapters;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import org.upmobile.musix.R;
import org.upmobile.musix.models.Song;
import org.upmobile.musix.utils.BitmapHelper;
import org.upmobile.musix.utils.TypefaceHelper;

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
    ArrayList<Song> songArrayList;
    private Context mContext;
    BitmapHelper bitmapHelper;
    Drawable defaultImage;
    ContentResolver musicResolver;

    GetAlbumBitmapAsync bitmapAsync;

    public SongListAdapter(Context context, ArrayList<Song> songs) {
        mContext = context;
        songArrayList = songs;
        bitmapHelper = new BitmapHelper(mContext);
        musicResolver = mContext.getContentResolver();
        typefaceHelper = new TypefaceHelper(mContext);
        defaultImage = mContext.getResources().getDrawable(R.drawable.ic_launcher);
    }

    public void add(Song song) {
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
    public Song getItem(int position) {
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
        Song song;

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
        viewHolder.txtArtistName.setText(song.getArtistName());

        viewHolder.txtSongTitle.setTypeface(typefaceHelper.getRobotoLight());
        viewHolder.txtArtistName.setTypeface(typefaceHelper.getRobotoLight());
        viewHolder.albumCover.setId(position);

        try {
            // album covert art (Async task)
            bitmapAsync = new GetAlbumBitmapAsync(viewHolder.albumCover, defaultImage);
            bitmapAsync.execute(song.getUri());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return row;
    }

    private class ViewHolder {
        ImageView albumCover;
        TextView txtSongTitle;
        TextView txtArtistName;
    }

    @SuppressLint("NewApi")
	private class GetAlbumBitmapAsync extends AsyncTask<Uri, Void, Bitmap> {
        int position;
        Bitmap bitmapAlbumArt = null;
        InputStream inputStream;
        ImageView imageView;

        public GetAlbumBitmapAsync(ImageView imageViewHolder, Drawable imageHolder) {
            imageView = imageViewHolder;
            imageView.setImageDrawable(imageHolder);
            position = imageView.getId();
        }

        @Override
        protected void onPostExecute(Bitmap bitmapResult) {
            bitmapAsync = null;

            if (bitmapResult != null) {

                if (imageView != null && position == imageView.getId()) {
                    imageView.setImageBitmap(bitmapResult);
                    songArrayList.get(position).setAlbumCoverArt(bitmapResult);
                }
            }
        }

        @Override
        protected Bitmap doInBackground(Uri... uris) {
            try {

                inputStream = musicResolver.openInputStream(uris[0]);
                bitmapAlbumArt = bitmapHelper.decodeSampledBitmapFromResourceMemOpt(inputStream, 130, 130);

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (Exception ex){
                ex.printStackTrace();
            }

            return bitmapAlbumArt;
        }
    }

}
