/*
 * Copyright (C) 2010, 2011 Christopher Eby <kreed@kreed.org>
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

package org.kreed.vanilla.adapter;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.kreed.vanilla.LibraryActivity;
import org.kreed.vanilla.Limiter;
import org.kreed.vanilla.MediaUtils;
import org.kreed.vanilla.MusicAlphabetIndexer;
import org.kreed.vanilla.QueryTask;
import org.kreed.vanilla.R;
import org.kreed.vanilla.adapter.AdapterHelper.ViewBuilder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.GenresColumns;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

/**
 * MediaAdapter provides an adapter backed by a MediaStore content provider. It
 * generates simple one- or two-line text views to display each media element.
 * 
 * Filtering is supported, as is a more specific type of filtering referred to
 * as limiting. Limiting is separate from filtering; a new filter will not erase
 * an active filter. Limiting is intended to allow only media belonging to a
 * specific group to be displayed, e.g. only songs from a certain artist. See
 * getLimiter and setLimiter for details.
 */
@SuppressLint("DefaultLocale")
public class MediaAdapter extends SortAdapter implements SectionIndexer, LibraryAdapter, View.OnClickListener {
	
	private static final Pattern SPACE_SPLIT = Pattern.compile("\\s+");
	private static final SparseIntArray stringCaptions = new SparseIntArray() {
		{
			put(MediaUtils.TYPE_SEARCH, 0);
			put(MediaUtils.TYPE_ARTIST, R.array.songs);
			put(MediaUtils.TYPE_ALBUM, R.array.tracks);
			put(MediaUtils.TYPE_SONG, 0);
			put(MediaUtils.TYPE_PLAYLIST, 0);
			put(MediaUtils.TYPE_GENRE, R.array.tracks);
			put(MediaUtils.TYPE_FILE, 0);
		}
	};

	/**
	 * A context to use.
	 */
	private final LibraryActivity mActivity;
	/**
	 * A LayoutInflater to use.
	 */
	private final LayoutInflater mInflater;
	/**
	 * The current data.
	 */
	private Cursor mCursor;
	/**
	 * The type of media represented by this adapter. Must be one of the
	 * MediaUtils.FIELD_* constants. Determines which content provider to query
	 * for media and what fields to display.
	 */
	private final int mType;
	/**
	 * The URI of the content provider backing this adapter.
	 */
	private Uri mStore;
	/**
	 * The fields to use from the content provider. The last field will be
	 * displayed in the MediaView, as will the first field if there are multiple
	 * fields. Other fields will be used for searching.
	 */
	private String[] mFields;
	/**
	 * The collation keys corresponding to each field. If provided, these are
	 * used to speed up sorting and filtering.
	 */
	private String[] mFieldKeys;
	/**
	 * The columns to query from the content provider.
	 */
	private String[] mProjection;
	/**
	 * A limiter is used for filtering. The intention is to restrict items
	 * displayed in the list to only those of a specific artist or album, as
	 * selected through an expander arrow in a broader MediaAdapter list.
	 */
	private Limiter mLimiter;
	/**
	 * The constraint used for filtering, set by the search box.
	 */
	private String mConstraint;
	/**
	 * The section indexer, for the letter pop-up when scrolling.
	 */
	private final MusicAlphabetIndexer mIndexer;
	/**
	 * The sections used by the indexer.
	 */
	private Object[] mSections;
	/**
	 * The sort order for use with buildSongQuery().
	 */
	private String mSongSort;
	/**
	 * An array ORDER BY expressions for each sort mode. %1$s is replaced by ASC
	 * or DESC as appropriate before being passed to the query.
	 */
	private String[] mSortValues;
	/**
	 * If true, show the expander button on each row.
	 */
	private boolean mExpandable;

	private HashMap<String, String> arrayFilePath = new HashMap<String, String>();

	/**
	 * Construct a MediaAdapter representing the given <code>type</code> of
	 * media.
	 * 
	 * @param activity
	 *            The LibraryActivity that will contain this adapter.
	 * @param type
	 *            The type of media to represent. Must be one of the Song.TYPE_*
	 *            constants. This determines which content provider to query and
	 *            what fields to display in the views.
	 * @param limiter
	 *            An initial limiter to use
	 */

	public MediaAdapter(LibraryActivity activity, int type, Limiter limiter) {
		mActivity = activity;
		mType = type;
		mLimiter = limiter;
		mIndexer = new MusicAlphabetIndexer(1);
		mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		switch (type) {
		case MediaUtils.TYPE_ARTIST:
			mStore = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
			mFields = new String[] { ArtistColumns.ARTIST, "null", ArtistColumns.NUMBER_OF_TRACKS };
			mFieldKeys = new String[] { ArtistColumns.ARTIST_KEY };
			mSongSort = MediaUtils.DEFAULT_SORT;
			mSortEntries = new int[] { R.string.name, R.string.number_of_tracks };
			mSortValues = new String[] { "artist_key %1$s", "number_of_tracks %1$s,artist_key %1$s" };
			break;
		case MediaUtils.TYPE_ALBUM:
			mStore = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
			mFields = new String[] { AlbumColumns.ALBUM, AlbumColumns.ARTIST, AlbumColumns.NUMBER_OF_SONGS };
			// Why is there no artist_key column constant in the album
			// MediaStore? The column does seem to exist.
			mFieldKeys = new String[] { "artist_key", AlbumColumns.ALBUM_KEY };
			mSongSort = "album_key,track";
			mSortEntries = new int[] { R.string.name, R.string.artist_album, R.string.year, R.string.number_of_tracks };
			mSortValues = new String[] { "album_key %1$s", "artist_key %1$s,album_key %1$s", "minyear %1$s,album_key %1$s", "numsongs %1$s,album_key %1$s" };
			break;
		case MediaUtils.TYPE_SONG:
			mStore = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			mFields = new String[] { MediaColumns.TITLE, AudioColumns.ARTIST, MediaColumns.DATA };
			mFieldKeys = new String[] { AudioColumns.ARTIST_KEY, AudioColumns.ALBUM_KEY, AudioColumns.TITLE_KEY };
			mSortEntries = new int[] { R.string.name, R.string.artist_album_track, R.string.artist_album_title, R.string.artist_year, R.string.year };
			mSortValues = new String[] { "title_key %1$s", "artist_key %1$s,album_key %1$s,track %1$s", "artist_key %1$s,album_key %1$s,title_key %1$s", "artist_key %1$s,year %1$s,track %1$s",
					"year %1$s,title_key %1$s" };
			break;
		case MediaUtils.TYPE_PLAYLIST:
			mStore = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
			mFields = new String[] { PlaylistsColumns.NAME };
			mFieldKeys = null;
			mSortEntries = new int[] { R.string.name, R.string.date_added };
			mSortValues = new String[] { "name %1$s", "date_added %1$s" };
			mExpandable = true;
			break;
		case MediaUtils.TYPE_GENRE:
			mStore = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
			mFields = new String[] { GenresColumns.NAME, MediaStore.Audio.GenresColumns.NAME, MediaStore.Audio.GenresColumns.NAME };
			mFieldKeys = null;
			mSortEntries = new int[] { R.string.name };
			mSortValues = new String[] { "name %1$s" };
			break;
		default:
			throw new IllegalArgumentException("Invalid value for type: " + type);
		}
		mProjection = new String[mFields.length + 1];
		mProjection[0] = BaseColumns._ID;
		for (int i = 0; i < mFields.length; i++) {
			mProjection[i + 1] = mFields[i];
		}
	}

	/**
	 * Set whether or not the expander button should be shown in each row.
	 * Defaults to true for playlist adapter and false for all others.
	 * 
	 * @param expandable
	 *            True to show expander, false to hide.
	 */
	public void setExpandable(boolean expandable) {
		if (expandable != mExpandable) {
			mExpandable = expandable;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setFilter(String filter) {
		mConstraint = filter;
	}

	/**
	 * Build the query to be run with runQuery().
	 * 
	 * @param projection
	 *            The columns to query.
	 * @param forceMusicCheck
	 *            Force the is_music check to be added to the selection.
	 */
	public QueryTask buildQuery(String[] projection, boolean forceMusicCheck) {
		String constraint = mConstraint;
		Limiter limiter = mLimiter;

		StringBuilder selection = new StringBuilder();
		String[] selectionArgs = null;

		int mode = mSortMode;
		String sortDir;
		if (mode < 0) {
			mode = ~mode;
			sortDir = "DESC";
		} else {
			sortDir = "ASC";
		}
		String sort = String.format(mSortValues[mode], sortDir);

		if (mType == MediaUtils.TYPE_SONG || forceMusicCheck) {
			selection.append("is_music!=0");
		}

		if (constraint != null && !constraint.isEmpty()) {
			String[] needles;
			String[] keySource;

			// If we are using sorting keys, we need to change our constraint
			// into a list of collation keys. Otherwise, just split the
			// constraint with no modification.
			if (mFieldKeys != null) {
				String colKey = MediaStore.Audio.keyFor(constraint);
				String spaceColKey = DatabaseUtils.getCollationKey(" ");
				needles = colKey.split(spaceColKey);
				keySource = mFieldKeys;
			} else {
				needles = SPACE_SPLIT.split(constraint);
				keySource = mFields;
			}

			int size = needles.length;
			selectionArgs = new String[size];

			StringBuilder keys = new StringBuilder(20);
			keys.append(keySource[0]);
			for (int j = 1; j != keySource.length; ++j) {
				keys.append("||");
				keys.append(keySource[j]);
			}

			for (int j = 0; j != needles.length; ++j) {
				selectionArgs[j] = '%' + needles[j] + '%';

				// If we have something in the selection args (i.e. j > 0), we
				// must have something in the selection, so we can skip the more
				// costly direct check of the selection length.
				if (j != 0 || selection.length() != 0)
					selection.append(" AND ");
				selection.append(keys);
				selection.append(" LIKE ?");
			}
		}

		if (limiter != null && limiter.type == MediaUtils.TYPE_GENRE) {
			// Genre is not standard metadata for MediaStore.Audio.Media.
			// We have to query it through a separate provider. : /
			return MediaUtils.buildGenreQuery((Long) limiter.data, projection, selection.toString(), selectionArgs, sort);
		}
		if (limiter != null) {
			if (selection.length() != 0)
				selection.append(" AND ");
			selection.append(limiter.data);
		}

		QueryTask queryTask = new QueryTask(mStore, projection, selection.toString(), selectionArgs, sort);
		queryTask.type = mType;
		return queryTask;
	}

	@Override
	public Object query() {
		return buildQuery(mProjection, false).runQuery(mActivity.getContentResolver());
	}

	@Override
	public void commitQuery(Object data) {
		changeCursor((Cursor) data);
	}

	/**
	 * Build a query for all the songs represented by this adapter, for adding
	 * to the timeline.
	 * 
	 * @param projection
	 *            The columns to query.
	 */
	public QueryTask buildSongQuery(String[] projection) {
		QueryTask query = buildQuery(projection, true);
		query.type = mType;
		if (mType != MediaUtils.TYPE_SONG) {
			query.uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			// Would be better to match the sort order in the adapter. This
			// is likely to require significantly more work though.
			query.sortOrder = mSongSort;
		}
		return query;
	}

	@Override
	public void clear() {
		changeCursor(null);
	}

	@Override
	public int getMediaType() {
		return mType;
	}

	@Override
	public void setLimiter(Limiter limiter) {
		mLimiter = limiter;
	}

	@Override
	public Limiter getLimiter() {
		return mLimiter;
	}

	@Override
	public Limiter buildLimiter(long id) {
		String[] fields;
		Object data;

		Cursor cursor = mCursor;
		if (cursor == null)
			return null;
		for (int i = 0, count = cursor.getCount(); i != count; ++i) {

			cursor.moveToPosition(i);
			if (cursor.getLong(0) == id)
				break;
		}

		switch (mType) {
		case MediaUtils.TYPE_ARTIST:
			fields = new String[] { cursor.getString(1) };
			data = String.format("%s=%d", AudioColumns.ARTIST_ID, id);
			break;
		case MediaUtils.TYPE_ALBUM:
			fields = new String[] { cursor.getString(2), cursor.getString(1) };
			data = String.format("%s=%d", AudioColumns.ALBUM_ID, id);
			break;
		case MediaUtils.TYPE_GENRE:
			fields = new String[] { cursor.getString(1) };
			data = id;
			break;
		default:
			throw new IllegalStateException("getLimiter() is not supported for media type: " + mType);
		}

		return new Limiter(mType, fields, data);
	}

	/**
	 * Set a new cursor for this adapter. The old cursor will be closed.
	 * 
	 * @param cursor
	 *            The new cursor.
	 */
	public void changeCursor(Cursor cursor) {
		Cursor old = mCursor;
		mCursor = cursor;
		if (cursor == null) {
			notifyDataSetInvalidated();
		} else {
			notifyDataSetChanged();
		}
		mIndexer.setCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	@Override
	public Object[] getSections() {
		// if (mSections == null) {
		// if (mSortMode == 0)
		// mSections = MusicAlphabetIndexer.getSections();
		// else
		mSections = new String[] { " " };
		// }
		return mSections;
	}

	@Override
	public int getPositionForSection(int section) {
		if (section == 0)
			return 0;
		if (section == getSections().length)
			return getCount();
		return mIndexer.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mSortMode != 0)
			return 0;
		return mIndexer.getSectionForPosition(position);
	}
	
	

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewBuilder builder = AdapterHelper.getViewBuilder(view, mInflater, 0);
		Cursor cursor = mCursor;
		cursor.moveToPosition(position);
		int count = cursor.getColumnCount();
		String data = null;
		String zaycevTag = "(zaycev.net)";
		String title = cursor.getString(1);
		if (mType == MediaUtils.TYPE_SONG) {
			data = cursor.getString(2);
			arrayFilePath.put(title, data);
		}
		if (title != null && title.toUpperCase().contains(zaycevTag.toUpperCase())) {
			title = title.replace(zaycevTag, "");
		}
		builder.setArrowClickListener(this)
				.setMainClickListener(this)
				.setId(cursor.getLong(0))
				.setExpandable(mExpandable)
				.setLine1(title, null)
				.setLine2((count > 2 && mType != MediaUtils.TYPE_GENRE) ? removeLastChar(cursor.getString(2)) : null)
				.setNumber(count > 3 && mType != MediaUtils.TYPE_SONG ? cursor.getString(3) : null, stringCaptions.get(mType, 0));
//		 if (mType == MediaUtils.TYPE_SONG && Settings.ENABLE_SHOW_ALBUM_COVERS_IN_LIBRARY_TAB) {
//		 long id = cursor.getLong(0);
//		 File file = PlaybackService.get(mActivity).getFilePath(mType, id);
//		 builder.startLoadCover(2, mActivity, file);
//		 }
		return builder.build();
	}

	private String removeLastChar (String artist) {
		if (null != artist && artist.endsWith("?")) {
			artist = artist.substring(0, artist.length() - 1);
		}
		return artist;
	}

	/**
	 * Returns the type of the current limiter.
	 * 
	 * @return One of MediaUtils.TYPE_, or MediaUtils.TYPE_INVALID if there is
	 *         no limiter set.
	 */
	public int getLimiterType() {
		Limiter limiter = mLimiter;
		if (limiter != null)
			return limiter.type;
		return MediaUtils.TYPE_INVALID;
	}

	/**
	 * Return the available sort modes for this adapter.
	 * 
	 * @return An array containing the resource ids of the sort mode strings.
	 */
	public int[] getSortEntries() {
		return mSortEntries;
	}

	/**
	 * Set the sorting mode. The adapter should be re-queried after changing
	 * this.
	 * 
	 * @param i
	 *            The index of the sort mode in the sort entries array. If this
	 *            is negative, the inverse of the index will be used and sort
	 *            order will be reversed.
	 */
	public void setSortMode(int i) {
		mSortMode = i;
		mSections = null;
	}

	/**
	 * Returns the sort mode that should be used if no preference is saved. This
	 * may very based on the active limiter.
	 */
	public int getDefaultSortMode() {
		int type = mType;
		if (type == MediaUtils.TYPE_ALBUM || type == MediaUtils.TYPE_SONG)
			return 1; // aritst,album,track
		return 0;
	}

	/**
	 * Return the current sort mode set on this adapter.
	 */
	public int getSortMode() {
		return mSortMode;
	}

	@Override
	public Intent createData(View view) {
		ViewBuilder holder = (ViewBuilder) view.getTag();
		String title = null == holder.getTitle() ? holder.getArtist() : holder.getTitle();
		String path = arrayFilePath.get(title);
		Intent intent = new Intent();
		intent.putExtra(LibraryAdapter.DATA_TYPE, mType);
		intent.putExtra(LibraryAdapter.DATA_ID, holder.getId());
		intent.putExtra(LibraryAdapter.DATA_TITLE, title);
		intent.putExtra(LibraryAdapter.DATA_FILE_PATH, path);
		intent.putExtra(LibraryAdapter.DATA_EXPANDABLE, mExpandable);
		return intent;
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		boolean arrowClicked = id == R.id.btnDownload;
		if (arrowClicked) {
			view = (View) view.getParent();
			if (!mExpandable) {
				mActivity.openContextMenu(view);
				return;
			}
		}
		Intent intent = createData(view);
		if (arrowClicked) {
			mActivity.onItemExpanded(intent);
		} else {
			mActivity.onItemClicked(intent);
		}
	}

	@Override
	public int getCount() {
		Cursor cursor = mCursor;
		if (cursor == null) return 0;
		return cursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		Cursor cursor = mCursor;
		if (cursor == null)
			return 0;
		cursor.moveToPosition(position);
		try {
			return cursor.getLong(0);
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	// @Override
	// public void shiftCurrentSong(int delta) {
	// setSong(PlaybackService.get(mActivity).shiftCurrentSong(delta));
	// }

	// @Override
	// public void upSwipe() {
	// }
	//
	// @Override
	// public void downSwipe() {
	// }
}