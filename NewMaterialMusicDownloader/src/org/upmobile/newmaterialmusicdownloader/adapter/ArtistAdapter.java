package org.upmobile.newmaterialmusicdownloader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import org.upmobile.newmaterialmusicdownloader.R;

import ru.johnlife.lifetoolsmp3.adapter.BaseArtistAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;

/**
 * Created by Aleksandr on 04.08.2015.
 */
public class ArtistAdapter extends BaseArtistAdapter implements UndoAdapter{

    public ArtistAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getUndoView(int paramInt, View paramView, ViewGroup paramViewGroup) {
        View view = paramView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_undo_view, paramViewGroup, false);
        }
        return view;
    }

    @Override
    public View getUndoClickView(View paramView) {
        return paramView.findViewById(R.id.undo_button);
    }

    private class ArtistViewHolder extends BaseArtistAdapter.BaseArtistViewHolder {


        public ArtistViewHolder(View v) {
            cover = (ImageView) v.findViewById(R.id.cover);
            title = (TextView) v.findViewById(R.id.titleLine);
            artist = (TextView) v.findViewById(R.id.artistLine);
            duration = (TextView) v.findViewById(R.id.chunkTime);
//            threeDot = v.findViewById(R.id.threeDot);
            artistRowName = (TextView) v.findViewById(R.id.artistRowName);
            numberOfSongs = (TextView) v.findViewById(R.id.numberOfSongs);
            textSongs = (TextView) v.findViewById(R.id.textSongs);
        }
    }

    @Override
    protected Bitmap getDefaultCover() {
        return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_album_grey);
    }

    @Override
    protected ViewHolder<AbstractSong> createViewHolder(View v) {
        return new ArtistViewHolder(v);
    }
}
