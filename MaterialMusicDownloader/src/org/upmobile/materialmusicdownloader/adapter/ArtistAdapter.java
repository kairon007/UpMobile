package org.upmobile.materialmusicdownloader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.adapter.BaseArtistAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.utils.Util;

/**
 * Created by Aleksandr on 06.08.2015.
 */
public class ArtistAdapter extends BaseArtistAdapter implements UndoAdapter {

    private Bitmap defaultCover;

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
        String cover =  getContext().getResources().getString(R.string.font_musics);
        if (null == defaultCover) defaultCover = ((MainActivity) getContext()).getDefaultBitmapCover(Util.dpToPx(getContext(), 64), Util.dpToPx(getContext(), 62), Util.dpToPx(getContext(), 60), cover);
        return defaultCover;
    }

    @Override
    protected ViewHolder<AbstractSong> createViewHolder(View v) {
        return new ArtistViewHolder(v);
    }
}