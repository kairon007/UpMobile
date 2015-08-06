package org.upmobile.newmusicdownloader.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.adapter.ArtistAdapter;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseArtistView;

/**
 * Created by Aleksandr on 06.08.2015.
 */
public class ArtistView extends BaseArtistView {

    private ListView lView;
    private TextView message;

    public ArtistView(LayoutInflater inflater) {
        super(inflater);
    }

    @Override
    protected BaseAbstractAdapter<AbstractSong> getAdapter() {
        return new ArtistAdapter(getContext(), R.layout.artist_row);
    }

    @Override
    protected ListView getListView(View view) {
        lView = (ListView) view.findViewById(R.id.listView);
        return lView;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_list_transition;
    }

    @Override
    public TextView getMessageView(View view) {
        message = (TextView) view.findViewById(R.id.message_listview);
        return message;
    }

    @Override
    protected void forceDelete() {
    }

    @Override
    protected void showPlayerFragment(MusicData musicData) {
        ((MainActivity) getContext()).startSong(musicData);
    }

    @Override
    protected void animateListView(ListView listView, final BaseAbstractAdapter<AbstractSong> adapter) {
    }
}