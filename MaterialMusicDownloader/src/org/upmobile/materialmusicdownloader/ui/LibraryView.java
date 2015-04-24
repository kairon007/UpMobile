package org.upmobile.materialmusicdownloader.ui;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.adapter.LibraryAdapter;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

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

public class LibraryView extends BaseLibraryView implements Constants {

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
	protected String getFolderPath() {
		return MaterialMusicDownloaderApp.getDirectory();
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
	protected void animateListView(ListView listView, final BaseAbstractAdapter<MusicData> adapter) {
		CustomSwipeUndoAdapter swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {
			
	        @Override
	        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
	            for (int position : reverseSortedPositions) {
	            	MusicData data = ((MusicData) adapter.getItem(position));
	            	if (data == null) return;
	            	data.reset(getContext());
	            	isUserDeleted = true;
	            	PlaybackService.get(getContext()).remove(data);
	            	StateKeeper.getInstance().removeSongInfo(data.getComment());
	            	adapter.remove(data);
	            	if (adapter.isEmpty()) {
	        			((MainActivity) getContext()).showPlayerElement(false);
	        			TextView emptyMsg = (TextView) ((MainActivity) getContext()).findViewById(R.id.message_listview);
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
	
	@Override
	public void showMessage(String message) {
		((MainActivity)getContext()).showMessage(message);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
}
