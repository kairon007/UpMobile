package org.upmobile.clearmusicdownloader.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.adapters.LibraryAdapter;

import java.util.HashSet;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseLibraryView;

public class LibraryView extends BaseLibraryView implements Constants {

	private ListView lView;
	private TextView message;
	private CustomSwipeUndoAdapter swipeUndoAdapter;

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected void showShadow(boolean show) {

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
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
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
	        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, HashSet<Object> removed) {
				for (Object o : removed) {
					removeData((MusicData) o);
	            }
	        }
			
	    });
		swipeUndoAdapter.setAbsListView(listView);
		listView.setAdapter(swipeUndoAdapter);
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
	
	@Override
	protected void hideProgress(View v) {
		v.findViewById(R.id.progress).clearAnimation();
		v.findViewById(R.id.progress).setVisibility(View.GONE);
	}
	
	@Override
	protected void showProgress(View v) {
		v.findViewById(R.id.progress).setVisibility(View.VISIBLE);
		v.findViewById(R.id.progress).startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
	}

	@Override
	public void forceDelete() {
		swipeUndoAdapter.forceDelete();
	}
}
