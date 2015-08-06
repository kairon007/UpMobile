package org.upmobile.newmusicdownloader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.upmobile.newmusicdownloader.R;

import ru.johnlife.lifetoolsmp3.adapter.BaseArtistAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;

/**
 * Created by Aleksandr on 06.08.2015.
 */
public class ArtistAdapter extends BaseArtistAdapter {

    public ArtistAdapter(Context context, int resource) {
        super(context, resource);
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
        return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.no_cover_art_light_big_dark);
    }

    @Override
    protected ViewHolder<AbstractSong> createViewHolder(View v) {
        return new ArtistViewHolder(v);
    }
}