package org.upmobile.clearmusicdownloader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.PlaylistAdapter;

import java.util.HashSet;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BasePlaylistView;

public class PlaylistView extends BasePlaylistView {

	private CustomSwipeUndoAdapter swipeUndoAdapter;
	private ListView lView;
	private TextView message;

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.playlist_view;
	}
	
	@Override
	protected ListView getListView(View view) {
		lView = (ListView) view.findViewById(R.id.list);
		return lView;
	}

	@Override
	public TextView getMessageView(View view) {
		message = (TextView) view.findViewById(R.id.emptyText);
		return message;
	}

	@Override
	protected void showPlayerFragment(MusicData data) {
		((MainActivity) getContext()).showPlayerElement();
		((MainActivity) getContext()).startSong(data);
	}

	@Override
	protected Bitmap getDefaultCover() {
		return BitmapFactory.decodeResource(getResources(), org.upmobile.clearmusicdownloader.R.drawable.def_cover_circle);
	}

	@Override
	protected BasePlaylistsAdapter getAdapter(Context context) {
		return new PlaylistAdapter(context, org.upmobile.clearmusicdownloader.R.layout.playlist_group_item);
	}
	
	@Override
	protected void animateListView(ListView listView, final BasePlaylistsAdapter adapter) {
		try {
			swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {

				@Override
				public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, HashSet<Object> removed) {
					for (Object data : removed) {
						try {
							if (null == data || !swipeUndoAdapter.getSongs().contains(data)) return;
							if (data.getClass() == MusicData.class) {
								removeData(getPlaylistBySong((MusicData) data), (MusicData) data);
							} else {
								removeData((PlaylistData) data, null);
							}
							swipeUndoAdapter.getSongs().remove(data);
						} catch (Exception e) {
							Log.e(getClass().getSimpleName(), e + "");
						}
					}
					if (adapter.isEmpty()) {
						lView.setEmptyView(message);
					}
				}
			});
			swipeUndoAdapter.setAbsListView((DynamicListView) listView);
			((DynamicListView) listView).setAdapter(swipeUndoAdapter);
			((DynamicListView) listView).enableSimpleSwipeUndo();
		} catch (Throwable e) {
			Log.d(getClass().getSimpleName(), "Exception: " + e);
		}
	}

	@Override
	public void forceDelete() {
		swipeUndoAdapter.forceDelete();
	}

}
