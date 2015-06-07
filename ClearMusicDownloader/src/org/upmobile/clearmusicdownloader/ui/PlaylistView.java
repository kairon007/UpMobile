package org.upmobile.clearmusicdownloader.ui;

import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.PlaylistAdapter;
import org.upmobile.clearmusicdownloader.app.ClearMusicDownloaderApp;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistView extends BasePlaylistView {

	private BaseAdapterDecorator swipeUndoAdapter;
	private ListView lView;
	private TextView message;

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected String getDirectory() {
		return ClearMusicDownloaderApp.getDirectoryPrefix();
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
	protected Bitmap getDeafultCover() {
		return BitmapFactory.decodeResource(getResources(), org.upmobile.clearmusicdownloader.R.drawable.def_cover_circle);
	}
	
	@Override
	protected Object[] groupItems() {
		return new Object[]{BitmapFactory.decodeResource(getResources(), R.drawable.ic_keyboard_arrow_down_black_18dp),BitmapFactory.decodeResource(getResources(), R.drawable.ic_keyboard_arrow_up_black_18dp)};
	}

	@Override
	protected BasePlaylistsAdapter getAdapter(Context context) {
		return new PlaylistAdapter(context, org.upmobile.clearmusicdownloader.R.layout.playlist_group_item);
	}
	
	@Override
	protected void animateListView(ListView listView, final BasePlaylistsAdapter adapter) {
		swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {
			
	        @Override
	        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
	        	for (int position : reverseSortedPositions) {
	        		AbstractSong data = (AbstractSong) adapter.getItem(position);
	        		if (data.getClass() == MusicData.class) {
	        			removeData(getViewByPosition((ListView) listView, position), getPlaylistBySong((MusicData) data), (MusicData) data);
	        		} else {
	        			removeData(getViewByPosition((ListView) listView, position), (PlaylistData) data, null);
	        		}
	            	if (adapter.isEmpty()) {
	        			lView.setEmptyView(message);
	        		}
	            }
	        }
	    });
		swipeUndoAdapter.setAbsListView((DynamicListView)listView);
		((DynamicListView)listView).setAdapter(swipeUndoAdapter);
		((DynamicListView)listView).enableSimpleSwipeUndo();
	}
}
