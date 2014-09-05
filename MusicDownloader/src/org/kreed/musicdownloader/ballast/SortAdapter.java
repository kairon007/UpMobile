package org.kreed.musicdownloader.ballast;

import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.R.string;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SortAdapter extends BaseAdapter implements LibraryAdapter{
	
	/**
	 * The index of the current of the current sort mode in mSortValues, or
	 * the inverse of the index (in which case sort should be descending
	 * instead of ascending).
	 */
	protected int mSortMode = 0;
	/**
	 * The human-readable descriptions for each sort mode.
	 */
	protected int[] mSortEntries = new int[] { R.string.name };
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------
	
	public int getSortMode() {
		// TODO Auto-generated method stub
		return mSortMode;
	}

	/**
	 * Set the sorting mode. The adapter should be re-queried after changing
	 * this.
	 *
	 * @param i The index of the sort mode in the sort entries array. If this
	 * is negative, the inverse of the index will be used and sort order will
	 * be reversed.
	 */
	public void setSortMode(int i)
	{
		mSortMode = i;
//		mSections = null;
	}
	
	public int[] getSortEntries() {
		// TODO Auto-generated method stub
		return mSortEntries;
	}

	@Override
	public int getMediaType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLimiter(Limiter limiter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Limiter getLimiter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Limiter buildLimiter(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFilter(String filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object query() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commitQuery(Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Intent createData(View row) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getLimiterType() {
		return 0;
	}

//	from MediaAdapter
	/**
	 * Returns the sort mode that should be used if no preference is saved. This
	 * may very based on the active limiter.
	 */
	public int getDefaultSortMode()
	{
		return 0;// A to Z for FileSystermAdapter
	}
}
