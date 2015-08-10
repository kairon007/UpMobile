package org.upmobile.materialmusicdownloader.ui;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.adapter.ArtistAdapter;

import java.util.HashSet;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseArtistView;

/**
 * Created by Aleksandr on 06.08.2015.
 */
public class ArtistView extends BaseArtistView {

    private CustomSwipeUndoAdapter swipeUndoAdapter;
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
        lView = (ListView) view.findViewById(R.id.list);
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
    public void forceDelete() {
        swipeUndoAdapter.forceDelete();
    }

    @Override
    protected void showPlayerFragment(MusicData musicData) {
        ((MainActivity) getContext()).startSong(musicData);
    }

    @Override
    protected void animateListView(ListView listView, final BaseAbstractAdapter<AbstractSong> adapter) {
        try {
            swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {

                @Override
                public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, HashSet<Object> removed) {
                    for (Object o : removed) {
                        removeData((AbstractSong) o);
                    }
                    if (adapter.isEmpty()) {
                        lView.setEmptyView(message);
                    }
                }
            });
            swipeUndoAdapter.setAbsListView(listView);
            listView.setAdapter(swipeUndoAdapter);
            ((DynamicListView) listView).enableSimpleSwipeUndo();
        } catch (Throwable e) {
            Log.d(getClass().getSimpleName(), "Exception: " + e);
        }
    }

    @Override
    protected void showShadow(boolean show) {

    }
}