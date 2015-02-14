package org.upmobile.materialmusicdownloader.ui;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.adapter.DownloadsAdapter;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseDownloadsView;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;

public class DownloadsView extends BaseDownloadsView implements Constants {

	public DownloadsView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	public View getView() {
		View v = super.getView();
		return v;
	}

	@Override
	protected String getDirectory() {
		return DIRECTORY_PREFIX;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}

	@Override
	protected void animateListView(final BaseAbstractAdapter<MusicData> adapter, ListView listView) {
//		AnimationAdapter animAdapter = new AlphaInAnimationAdapter(adapter);
//		animAdapter.setAbsListView((DynamicListView)listView);
//		((DynamicListView)listView).setAdapter(animAdapter);
		SimpleSwipeUndoAdapter swipeUndoAdapter = new SimpleSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {
			        @Override
			        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
			            for (int position : reverseSortedPositions) {
			            	adapter.remove(position);
			            }
			        }
			    }
			);
			swipeUndoAdapter.setAbsListView((DynamicListView)listView);
			((DynamicListView)listView).setAdapter(swipeUndoAdapter);
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
