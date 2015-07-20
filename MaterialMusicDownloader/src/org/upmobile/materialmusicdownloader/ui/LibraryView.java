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
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.adapter.LibraryAdapter;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseLibraryView;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;

public class LibraryView extends BaseLibraryView implements Constants {

	private TextView message;
	private ListView lView;
	private CustomSwipeUndoAdapter swipeUndoAdapter;

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), R.layout.row_online_search);
	}

	@Override
	protected ListView getListView(View view) {
		lView = (ListView) view.findViewById(R.id.list);
		return lView;
	}

	@Override
	protected String getFolderPath() {
		return MaterialMusicDownloaderApp.getDirectory();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}

	@Override
	public void forceDelete() {
		swipeUndoAdapter.forceDelete();
	}

	@Override
	public TextView getMessageView(View view) {
		message = (TextView) view.findViewById(R.id.message_listview);
		return message;
	}
	
	@Override
	protected void animateListView(ListView listView, final BaseAbstractAdapter<MusicData> adapter) {
		swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {
			
	        @Override
	        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, ArrayList<Object> removed) {
	            for (int position : reverseSortedPositions) {
	            	MusicData data = ((MusicData) adapter.getItem(position));
	            	data.reset(getContext());
	            	isUserDeleted = true;
	            	PlaybackService.get(getContext()).remove(data);
	            	StateKeeper.getInstance().removeSongInfo(data.getComment());
	            	adapter.remove(data);
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
	
	@Override
	public void showMessage(String message) {
		((MainActivity)getContext()).showMessage(message);
	}
	
}
