/*
 * Copyright (C) 2011 Christopher Eby <kreed@kreed.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.kreed.musicdownloader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.util.Log;

/**
 * Represents a pending query.
 */
public class QueryTask {
	public Uri uri;
	public final String[] projection;
	public final String selection;
	public final String[] selectionArgs;
	public String sortOrder;

	/**
	 * Used for {@link SongTimeline#addSongs(android.content.Context, QueryTask)}.
	 * One of SongTimeline.MODE_*.
	 */
	public int mode;

	/**
	 * Type of the group being query. One of MediaUtils.TYPE_*.
	 */
	public int type;

	/**
	 * Data. Required value depends on value of mode. See individual mode
	 * documentation for details.
	 */
	public long data;

	/**
	 * Create the tasks. All arguments are passed directly to
	 * ContentResolver.query().
	 */
	public QueryTask(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		this.uri = uri;
		this.projection = projection;
		this.selection = selection;
		this.selectionArgs = selectionArgs;
		this.sortOrder = sortOrder;
	}

	/**
	 * Run the query. Should be called on a background thread.
	 *
	 * @param resolver The ContentResolver to query with.
	 */
	public Cursor runQuery(ContentResolver resolver)
	{
		final Cursor query = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
		Log.i(getClass().getSimpleName(), "runQuery()");
		if (type == MediaUtils.TYPE_GENRE && query != null && query.getCount() > 0) {
			Log.i(getClass().getSimpleName(), "... genreCursor returned");
			return new GenreCursorWrapper(query, resolver);
		}
		return query;
	}
	
	private final class GenreCursorWrapper extends MergeCursor {
		private static final int COUNT_COLUMN = 3;
		private final Cursor query;
		private int[] counts;
		
		private GenreCursorWrapper(Cursor query, ContentResolver resolver) {
			super(new Cursor[]{query});
			Log.i(getClass().getSimpleName(), "GenreCursor constructor");
			this.query = query;
			this.counts = new int[query.getCount()];
		    int i = 1;
			query.moveToFirst();
			while (query.moveToNext() && !query.isAfterLast()) {
			    Uri uri = Audio.Genres.Members.getContentUri("external", query.getLong(0)); 
			    Cursor c = resolver.query(uri, null, null, null, null);
			    if (c == null || c.getCount() == 0) {
			    	counts[i++] = 0;
			    	continue;
			    }
			    int num = c.getCount();
			    c.close();
				counts[i++] = num;
			}
		}

		@Override
		public String getString(int column) {
			Log.i(getClass().getSimpleName(), "getString("+column+")");
			if (COUNT_COLUMN == column) {
				return String.valueOf(counts[getPosition()]);
			} else {
				return query.getString(column);
			}
		}
	}

}
