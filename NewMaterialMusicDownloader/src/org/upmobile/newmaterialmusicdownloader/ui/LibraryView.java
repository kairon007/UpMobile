package org.upmobile.newmaterialmusicdownloader.ui;

import org.upmobile.newmaterialmusicdownloader.adapter.LibraryAdapter;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

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

public class LibraryView extends BaseLibraryView {

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), org.upmobile.newmaterialmusicdownloader.R.layout.row_online_search);
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(org.upmobile.newmaterialmusicdownloader.R.id.list);
	}

	@Override
	protected int getLayoutId() {
		return org.upmobile.newmaterialmusicdownloader.R.layout.fragment_list_transition;
	}

	@Override
	public TextView getMessageView(View view) {
		return (TextView) view.findViewById(org.upmobile.newmaterialmusicdownloader.R.id.message_listview);
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
	            	((LibraryAdapter)adapter).deleteSong((MusicData)adapter.getItem(position)); 
	            }
	        }
	    });
		swipeUndoAdapter.setAbsListView((DynamicListView)listView);
		((DynamicListView)listView).setAdapter(swipeUndoAdapter);
		((DynamicListView)listView).enableSimpleSwipeUndo();
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
