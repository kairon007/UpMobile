package org.upmobile.newmusicdownloader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;

public class LibraryAdapter extends BaseLibraryAdapter {

    public LibraryAdapter(Context context, int resource) {
        super(context, resource);
    }

    public LibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
        super(context, resource, array);
    }

    @Override
    protected ViewHolder<MusicData> createViewHolder(View v) {
        return new LibraryViewHolder(v);
    }

    private class LibraryViewHolder extends BaseLibraryViewHolder {

        public LibraryViewHolder(View v) {
            cover = (ImageView) v.findViewById(R.id.cover);
            title = (TextView) v.findViewById(R.id.artistLine);
            artist = (TextView) v.findViewById(R.id.titleLine);
            duration = (TextView) v.findViewById(R.id.chunkTime);
            threeDot = v.findViewById(R.id.threeDot);
        }

        @Override
        protected void hold(final MusicData data, int position) {
            super.hold(data, position);
        }

    }

    @Override
    protected Bitmap getDefaultCover() {
        return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.no_cover_art_light_big_dark);
    }

    @Override
    protected boolean showDeleteItemMenu() {
        return true;
    }

    @Override
    protected void remove() {
        if (isEmpty()) {
            TextView emptyMsg = (TextView) ((MainActivity) getContext()).findViewById(R.id.message_listview);
            emptyMsg.setVisibility(View.VISIBLE);
            emptyMsg.setText(R.string.library_empty);
        }
    }

    @Override
    protected void startSong(AbstractSong abstractSong) {
        ((MainActivity) getContext()).startSong(abstractSong);
    }
}