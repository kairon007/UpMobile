package org.upmobile.materialmusicdownloader.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.adapter.DownloadsAdapter;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import java.util.HashSet;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter.CanNotifyListener;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseDownloadsView;

public class DownloadsView extends BaseDownloadsView implements Constants {

	public DownloadsView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectory();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}

	@Override
	protected void animateListView(final BaseAbstractAdapter<MusicData> adapter, ListView listView) {
		CustomSwipeUndoAdapter swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {
	        @Override
	        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, HashSet<Object> removed) {
	            for (int position : reverseSortedPositions) {
	            	((DownloadsAdapter)adapter).removeItem((MusicData)adapter.getItem(position));
	            }
	        }
	    });
		swipeUndoAdapter.setAbsListView(listView);
		swipeUndoAdapter.setCanNotifyListener(new CanNotifyListener() {
			
			@Override
			public void canNotify(boolean isCan) {
				((DownloadsAdapter)adapter).setCanNotify(isCan);
			}
		});
		listView.setAdapter(swipeUndoAdapter);
		((DynamicListView)listView).enableSimpleSwipeUndo();
	}
	
	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return new DownloadsAdapter(getContext(), R.layout.downloads_item);
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(R.id.list);
	}

	@Override
	protected TextView getMessageView(View view) {
		return (TextView) view.findViewById(R.id.message_listview);
	}
}
