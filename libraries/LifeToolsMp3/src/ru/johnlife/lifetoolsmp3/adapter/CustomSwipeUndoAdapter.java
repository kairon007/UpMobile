package ru.johnlife.lifetoolsmp3.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;

public class CustomSwipeUndoAdapter extends SimpleSwipeUndoAdapter {
	
	private final int DELAY = 3000;
	private CanNotifyListener listener;
	private ArrayList<DismissTimers> timers = new ArrayList<>();
	private ArrayList<AbstractSong> songs = new ArrayList<>();
	private BaseAdapter adapter;
	
	public interface CanNotifyListener {
		
		void canNotify(boolean isCan);
	}
	
	public CustomSwipeUndoAdapter(BaseAdapter adapter, Context context, OnDismissCallback dismissCallback) {
		super(adapter, context, dismissCallback);
		this.adapter = adapter;
	}
	
	@Override
	public void onUndoShown(View view, int position, Object o) {
		if (null != listener) {
			listener.canNotify(false);
		}
		startTimer(position, o);
		AbstractSong song = (AbstractSong) adapter.getItem(position);
		if (!songs.contains(song)) songs.add(song);
		super.onUndoShown(view, position, o);
	}
	
	@Override
	public void onDismiss(View view, int position, Object o) {
		if (null != listener && !hasUndoViews()) {
			listener.canNotify(true);
		}
		stopTimer(position, o);
		super.onDismiss(view, position, o);
	}
	
	@Override
	public void onDismiss(ViewGroup listView, int[] reverseSortedPositions, HashSet<Object> removed) {
		if (null != listener && !hasUndoViews()) {
			listener.canNotify(true);
		}
		for (int position : reverseSortedPositions) {
			stopTimer(position, getItem(position));
		}
		super.onDismiss(listView, reverseSortedPositions, removed);
	}
	
	@Override
	public void onUndo(View view, int position, Object o) {
		if (null != listener && !hasUndoViews()) {
			listener.canNotify(true);
		}
		stopTimer(position, o);
		super.onUndo(view, position, o);
	}
	
	public void setCanNotifyListener(CanNotifyListener listener) {
		this.listener = listener;
	}

	private void startTimer(int position, Object o) {
		if (null == getItem(position)) return;
		timers.add(new DismissTimers(o, position).startTimer());
	}
	
	private void stopTimer(int position, Object o) {
		for (DismissTimers timer : timers) {
			if (null == o) {
				notifyDataSetChanged();
				return;
			}
			if (timer.equals(o)) {
				timer.cancel();
				timers.remove(timer);
				break;
			}
		}
	}
	
	private boolean hasUndoViews() {
		return timers.size() > 0;
	}
	
	private class DismissTimers extends Timer {
		
		private Object tag;
		private int position;
		
		public DismissTimers(Object tag, int position) {
			this.position = position;
			this.tag = tag;
		}
		
		public DismissTimers startTimer() {
			schedule(new TimerTask() {
				
				@Override
				public void run() {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						
						@Override
						public void run() {
							int position = -1;
							for (int i = 0; i < getCount(); i++) {
								if (getItem(i).equals(getTag())) {
									position = i;
									break;
								}
							}
							if (position != -1) {
								try {
									dismiss(position, tag);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							timers.remove(this);
						}
					});
				}
			}, DELAY);
			return this;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DismissTimers) {
				return ((DismissTimers)o).getTag().equals(this.getTag());
			} else {
				return o.equals(this.getTag());
			}
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

		public Object getTag() {
			return tag;
		}

		public int getPosition() {
			return position;
		}
	}
	
	public void forceDelete() {
		try {
			ArrayList<DismissTimers> removed = new ArrayList<>();
			for (DismissTimers timer : timers) {
				int position = timer.getPosition();
				dismiss(position, timer.getTag());
				removed.add(timer);
				try {
					timer.cancel();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			timers.removeAll(removed);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e + "");
		}
	}

	public ArrayList<AbstractSong> getSongs() {
		return songs;
	}
}
