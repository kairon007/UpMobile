package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;

public class CustomSwipeUndoAdapter extends SimpleSwipeUndoAdapter {
	
	private final int DELAY = 3000;
	private CanNotifyListener listener;
	private ArrayList<DissmissTimer> timers = new ArrayList<DissmissTimer>();
	
	public interface CanNotifyListener {
		
		void canNotify(boolean isCan);
	}
	
	public CustomSwipeUndoAdapter(BaseAdapter adapter, Context context, OnDismissCallback dismissCallback) {
		super(adapter, context, dismissCallback);
	}
	
	@Override
	public void onUndoShown(View view, int position) {
		if (null != listener) {
			listener.canNotify(false);
		}
		startTimer(position);
		super.onUndoShown(view, position);
	}
	
	@Override
	public void onDismiss(View view, int position) {
		if (null != listener && !hasUndoViews()) {
			listener.canNotify(true);
		}
		stopTimer(position);
		super.onDismiss(view, position);
	}
	
	@Override
	public void onDismiss(ViewGroup listView, int[] reverseSortedPositions) {
		if (null != listener && !hasUndoViews()) {
			listener.canNotify(true);
		}
		for (int position : reverseSortedPositions) {
			stopTimer(position);
		}
		super.onDismiss(listView, reverseSortedPositions);
	}
	
	@Override
	public void onUndo(View view, int position) {
		if (null != listener && !hasUndoViews()) {
			listener.canNotify(true);
		}
		stopTimer(position);
		super.onUndo(view, position);
	}
	
	public void setCanNotifyListener(CanNotifyListener listener) {
		this.listener = listener;
	}

	private void startTimer(int position) {
		if (null == getItem(position)) return;
		timers.add(new DissmissTimer(getItem(position), position).startTimer());
	}
	
	private void stopTimer(int position) {
		Object tag = getItem(position);
		for (DissmissTimer timer : timers) {
			if (null == tag) {
				notifyDataSetChanged();
				return;
			}
			if (timer.equals(tag)) {
				timer.cancel();
				timers.remove(timer);
				break;
			}
		}
	}
	
	private boolean hasUndoViews() {
		return timers.size() > 0;
	}
	
	private class DissmissTimer extends Timer {
		
		private Object tag;
		private int position;
		
		public DissmissTimer(Object tag, int position) {
			this.position = position;
			this.tag = tag;
		}
		
		public DissmissTimer startTimer() {
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
									dismiss(position);
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
			if (o instanceof DissmissTimer) {
				return ((DissmissTimer)o).getTag().equals(this.getTag());
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
		for (DissmissTimer timer : timers) {
			int position  = timer.getPosition();
			dismiss(position);
		}
	}
}
