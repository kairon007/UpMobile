package org.upmobile.newmaterialmusicdownloader.ui;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.adapter.LibraryAdapter;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseLibraryView;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

public class LibraryView extends BaseLibraryView {

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), R.layout.row_online_search);
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(R.id.list);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}

	@Override
	public TextView getMessageView(View view) {
		return (TextView) view.findViewById(R.id.message_listview);
	}

	@Override
	protected String getFolderPath() {
		return NewMaterialApp.getDirectory();
	}
	
	@Override
	protected void animateListView(ListView listView, final BaseAbstractAdapter<MusicData> adapter) {
		CustomSwipeUndoAdapter swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {
				
	        @Override
	        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
	        	for (int position : reverseSortedPositions) {
	            	MusicData data = ((MusicData) adapter.getItem(position));
	            	isUserDeleted = Boolean.TRUE;
	            	PlaybackService.get(getContext()).remove(data);
	            	StateKeeper.getInstance().removeSongInfo(data.getComment());
	            	adapter.remove(data);
	            	data.reset(getContext());
	            	if (adapter.isEmpty()) {
	        			TextView emptyMsg = (TextView) ((MainActivity) getContext()).findViewById(R.id.message_listview);
	        			if (null == emptyMsg) return;
	        			emptyMsg.setVisibility(View.VISIBLE);
	        			emptyMsg.setText(R.string.library_empty);
	        		}
	            }
	        }
	    });
		swipeUndoAdapter.setAbsListView((DynamicListView)listView);
		((DynamicListView)listView).setAdapter(swipeUndoAdapter);
		((DynamicListView)listView).enableSimpleSwipeUndo();
	}
	
}
