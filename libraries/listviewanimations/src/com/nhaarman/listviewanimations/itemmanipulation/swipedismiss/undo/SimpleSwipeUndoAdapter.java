package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * An implementation of {@link SwipeUndoAdapter} which puts the primary and undo {@link android.view.View} in a {@link android.widget.FrameLayout},
 * and handles the undo click event.
 */
public class SimpleSwipeUndoAdapter extends SwipeUndoAdapter implements UndoCallback {

    @NonNull
    private final Context mContext;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback} that is notified of dismissed items.
     */
    @NonNull
    private final OnDismissCallback mOnDismissCallback;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter} that provides the undo {@link android.view.View}s.
     */
    @NonNull
    private final UndoAdapter mUndoAdapter;

    /**
     * The positions of the items currently in the undo state.
     */
    private final Collection<Integer> mUndoPositions = new ArrayList<>();
    private HashSet<Object> dismissObjects = new HashSet<>();

    /**
     * Create a new {@code SimpleSwipeUndoAdapterGen}, decorating given {@link android.widget.BaseAdapter}.
     *
     * @param adapter     the {@link android.widget.BaseAdapter} that is decorated. Must implement
     *                        {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter}.
     * @param context         the {@link android.content.Context}.
     * @param dismissCallback the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback} that is notified of dismissed items.
     */
    public SimpleSwipeUndoAdapter(@NonNull final BaseAdapter adapter, @NonNull final Context context,
                                  @NonNull final OnDismissCallback dismissCallback) {
        // We fix this right away
        // noinspection ConstantConditions
        super(adapter, null);
        setUndoCallback(this);

        BaseAdapter undoAdapter = adapter;
        while (undoAdapter instanceof BaseAdapterDecorator) {
            undoAdapter = ((BaseAdapterDecorator) undoAdapter).getDecoratedBaseAdapter();
        }

        if (!(undoAdapter instanceof UndoAdapter)) {
            throw new IllegalStateException("BaseAdapter must implement UndoAdapter!");
        }

        mUndoAdapter = (UndoAdapter) undoAdapter;
        mContext = context;
        mOnDismissCallback = dismissCallback;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        SwipeUndoView view = (SwipeUndoView) convertView;
        if (view == null) {
            view = new SwipeUndoView(mContext);
        }
        View primaryView = super.getView(position, view.getPrimaryView(), view);
        view.setPrimaryView(primaryView);

        View undoView = mUndoAdapter.getUndoView(position, view.getUndoView(), view);
        view.setUndoView(undoView);

        mUndoAdapter.getUndoClickView(undoView).setOnClickListener(new UndoClickListener(view, position, getListViewWrapper().getAdapter().getItem(position)));

        boolean isInUndoState = mUndoPositions.contains(position);
        primaryView.setVisibility(isInUndoState ? View.GONE : View.VISIBLE);
        undoView.setVisibility(isInUndoState ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    @NonNull
    public View getPrimaryView(@NonNull final View view) {
        View primaryView = ((SwipeUndoView) view).getPrimaryView();
        if (primaryView == null) {
            throw new IllegalStateException("primaryView == null");
        }
        return primaryView;
    }

    @Override
    @NonNull
    public View getUndoView(@NonNull final View view) {
        View undoView = ((SwipeUndoView) view).getUndoView();
        if (undoView == null) {
            throw new IllegalStateException("undoView == null");
        }
        return undoView;
    }

    @Override
    public void onUndoShown(@NonNull final View view, final int position, Object o) {
        mUndoPositions.add(position);
        dismissObjects.add(o);
    }

    @Override
    public void onUndo(@NonNull final View view, final int position, Object o) {
        mUndoPositions.remove(position);
        dismissObjects.remove(o);
    }

    @Override
    public void onDismiss(@NonNull final View view, final int position, Object o) {
        mUndoPositions.remove(position);
        dismissObjects.remove(o);
    }

    @Override
    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, HashSet<Object> removed) {
    	android.util.Log.d("logd", "onDismiss: ==========================================");
    	android.util.Log.d("logd", "onDismiss: removed" + removed);
    	for (Integer i : mUndoPositions) {
			android.util.Log.d("logd", "onDismiss: mUndoPos" + i.intValue());
		}
    	for (int i = 0; i < reverseSortedPositions.length; i++) {
			android.util.Log.d("logd", "onDismiss: reverse" + reverseSortedPositions[i]);
		}
    	android.util.Log.d("logd", "onDismiss: ===========================================");
    	mOnDismissCallback.onDismiss(listView, reverseSortedPositions, removed);
    		
        Collection<Integer> newUndoPositions = Util.processDeletions(mUndoPositions, reverseSortedPositions);
        mUndoPositions.clear();
        mUndoPositions.addAll(newUndoPositions);
        dismissObjects.removeAll(removed);
    }


    private class UndoClickListener implements View.OnClickListener {

        @NonNull
        private final SwipeUndoView mView;

        private final int mPosition;

		private Object o;

        UndoClickListener(@NonNull final SwipeUndoView view, final int position, Object o) {
            mView = view;
            mPosition = position;
			this.o = o;
        }

        @Override
        public void onClick(@NonNull final View v) {
            undo(mView, o);
        }
    }
}
