package ru.johnlife.lifetoolsmp3.ui.views;

import java.io.File;
import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.ExpandableAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.widget.AnimatedExpandableListView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BasePlaylistView extends View {

	private static final String PREF_DIRECTORY_PREFIX = "pref.directory.prefix";
	private ViewGroup view;
	protected ListView listView;
	private ExpandableAdapter expandableAdapter;
	private View playLastPlaylist;
	private View createNewPlayList;
	private View emptyView;
	private AlertDialog.Builder newPlaylistDialog;
	private PopupMenu menu;
	
	protected abstract Object[] groupItems();

	protected abstract Bitmap getDeafultCover();
	
	protected abstract String getDirectory();

	protected abstract int getLayoutId();
	
	protected abstract BaseExpandableListAdapter getAdapter(Context context);

	protected abstract void showPlayerFragment(MusicData musicData);

	protected abstract ListView getListView(View view);

	public abstract TextView getMessageView(View view);
	
	protected boolean isAnimateExpandCollapse() {return true;};

	public final String[] PROJECTION_PLAYLIST = { 
			MediaStore.Audio.Playlists._ID, 
			MediaStore.Audio.Playlists.NAME, };

	public final String[] PROJECTION_MUSIC = { 
			MediaStore.Audio.Media._ID, 
			MediaStore.Audio.Media.DATA, 
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST, 
			MediaStore.Audio.Media.DURATION, 
			MediaStore.Audio.Media.ALBUM, };
	
	private ArrayList<PlaylistData> playlists;
	private PlaybackService playbackService;
	private AlertDialog dialog;
	
	private OnSharedPreferenceChangeListener sharedPreferenceListener = new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.contains(PREF_DIRECTORY_PREFIX)) {
				expandableAdapter.setProjectPrefics(getDirectory());
				updatePlaylist();
			}
		}
	};

	public BasePlaylistView(LayoutInflater inflater) {
		super(inflater.getContext());
		init(inflater);
	}
	
	public void onResume() {
		MusicApp.getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
	}
	
	public void onPause () {
		MusicApp.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sharedPreferenceListener);
	}
	
	private void init(LayoutInflater inflater) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				playbackService = PlaybackService.get(getContext());
			}
		}).start();
		view = (ViewGroup) inflater.inflate(getLayoutId() > 0 ? getLayoutId() : R.layout.playlist_view, null);
		listView = getListView(view) != null ? getListView(view) : (AnimatedExpandableListView) view.findViewById(R.id.expandableListView);
		playLastPlaylist = view.findViewById(R.id.lastPlayedPlaylist);
		createNewPlayList = view.findViewById(R.id.createNewPlaylist);
		emptyView = getMessageView(view);
		initListeners();
		expandableAdapter = (ExpandableAdapter) (null != getAdapter(view.getContext()) ? getAdapter(view.getContext()) : new ExpandableAdapter(view.getContext()));
		expandableAdapter.setProjectPrefics(getDirectory());
		expandableAdapter.setDefaultBmp(getDeafultCover());
		updatePlaylist();
		if (null !=  groupItems() && groupItems().length > 1) {
			((AnimatedExpandableListView) listView).setGroupIndicator(null);
		}
		((AnimatedExpandableListView) listView).setAdapter(expandableAdapter);
		((AnimatedExpandableListView) listView).setEmptyView(emptyView);
		((AnimatedExpandableListView) listView).setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            	Util.hideKeyboard(getContext(), v);
            	if (playlists.get(groupPosition).getSongs().size() == 0) {
            		showMessage(getContext(), R.string.playlist_is_empty);
            		return false;
            	}
                if (((AnimatedExpandableListView) listView).isGroupExpanded(groupPosition)) {
                	if (isAnimateExpandCollapse()) {
                		((AnimatedExpandableListView) listView).collapseGroupWithAnimation(groupPosition);
                	} else {
                		((AnimatedExpandableListView) listView).collapseGroup(groupPosition);
                	}
                	if (null !=groupItems() && groupItems().length > 1) {
                		setGroupIndicator(v, 0);
                	}
                } else {
                	if (isAnimateExpandCollapse()) {
                		((AnimatedExpandableListView) listView).expandGroupWithAnimation(groupPosition); 
                	} else {
                		((AnimatedExpandableListView) listView).expandGroup(groupPosition);
                	}
                	if (null !=groupItems() && groupItems().length > 1) {
                		setGroupIndicator(v, 1);
                	}
                }
                return true;
            }
        });
		((AnimatedExpandableListView) listView).setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Util.hideKeyboard(getContext(), view);
				int groupPosition = ExpandableListView.getPackedPositionGroup(id);
		        int childPosition = ExpandableListView.getPackedPositionChild(id);
				showMenu(view, playlists.get(groupPosition), childPosition == -1 ? null : playlists.get(groupPosition).getSongs().get(childPosition) );
				return true;
			}
		});
		((AnimatedExpandableListView) listView).setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Util.hideKeyboard(getContext(), v);
				if (null != playbackService) {
					MusicApp.getSharedPreferences().edit().putLong(Constants.PREF_LAST_PLAYLIST_ID, playlists.get(groupPosition).getId()).commit();
					playbackService.setArrayPlayback(new ArrayList<AbstractSong>(playlists.get(groupPosition).getSongs()));
					showPlayerFragment(playlists.get(groupPosition).getSongs().get(childPosition));
					return true;
				}
				return false;
			}
		});
	}
	
	private void setGroupIndicator(View v, int i) {
		if (groupItems()[0].getClass() == String.class) {
			((TextView) v.findViewById(R.id.customGroupIndicator)).setText(groupItems()[i].toString());
		} else {
			if (groupItems()[0].getClass() == Bitmap.class) {
				((ImageView) v.findViewById(R.id.customGroupIndicator)).setImageBitmap((Bitmap) groupItems()[i]);
				if (groupItems().length > 2) {
					((ImageView) v.findViewById(R.id.customGroupIndicator)).setColorFilter((Integer) groupItems()[2]);
				}
			} else {
				((ImageView) v.findViewById(R.id.customGroupIndicator)).setImageDrawable((Drawable) groupItems()[i]);
				if (groupItems().length > 2) {
					((ImageView) v.findViewById(R.id.customGroupIndicator)).setColorFilter((Integer) groupItems()[2]);
				}
			}
		}
	}

	private void updatePlaylist() {
		if (null != playlists) {
			playlists.clear();
		}
		playlists = getPlaylists();
		for (PlaylistData playlistData : playlists) {
			playlistData.setSongs(playlistData.getSongsFromPlaylist(getContext(), playlistData.getId()));
		}
		expandableAdapter.setData(playlists);
		expandableAdapter.notifyDataSetChanged();
	}

	private void initListeners() {
		createNewPlayList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				closeDialog();
				showDialog();
			}
		});
		playLastPlaylist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				Util.hideKeyboard(getContext(), paramView);
				long id = MusicApp.getSharedPreferences().getLong(Constants.PREF_LAST_PLAYLIST_ID, -1);
				if (id == -1) {
					showMessage(getContext(), R.string.no_previous_playlists);
				} else {
					boolean isPlaylistExists = false;
					for (PlaylistData data : playlists) {
						if (data.getId() == id) {
							isPlaylistExists = true;
							if (data.getSongs().size() == 0) {
								showMessage(getContext(), R.string.no_songs_in_the_last_playlist);
							} else {
								playbackService.setArrayPlayback(new ArrayList<AbstractSong>(data.getSongs()));
								playbackService.play(data.getSongs().get(0));
								showPlayerFragment(data.getSongs().get(0));
								return;
							}
						}
					}
					if (!isPlaylistExists) {
						showMessage(getContext(), R.string.the_playlist_was_deleted);
					}
				}
			}
		});

	}

	protected void showDialog() {
		final View dialoglayout = LayoutInflater.from(getContext()).inflate(R.layout.playlist_create_new_dialog, null);
		newPlaylistDialog = new AlertDialog.Builder(getContext());
		newPlaylistDialog.setView(dialoglayout);
		newPlaylistDialog.setNegativeButton(R.string.edit_mp3_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Util.hideKeyboard(getContext(), dialoglayout);
				dialog.cancel();
			}

		});
		newPlaylistDialog.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newTitle =  ((EditText) dialoglayout.findViewById(R.id.newPlaylistET)).getText().toString().trim();
				if (newTitle.isEmpty()) {
					dialog.cancel();
					showMessage(getContext(), R.string.playlist_cannot_be_empty);
					return;
				}
				createPlaylist(getContext().getContentResolver(), ((EditText) dialoglayout.findViewById(R.id.newPlaylistET)).getText().toString());
				Util.hideKeyboard(getContext(), dialoglayout);
				dialog.cancel();
			}
		});
		dialog = newPlaylistDialog.create();
		dialog.show();
	}

	public View getView() {
		return view;
	}

	@SuppressLint("NewApi")
	public void showMenu(final View v, final PlaylistData data, final MusicData musicData) {
		menu = new PopupMenu(getContext(), v);
		menu.getMenuInflater().inflate(R.menu.library_menu, menu.getMenu());
		menu.getMenu().getItem(0).setVisible(false);
		menu.getMenu().getItem(1).setVisible(false);
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem paramMenuItem) {
				if (paramMenuItem.getItemId() == R.id.library_menu_delete) {
					if (musicData == null) {
						data.deletePlaylist(getContext(), data.getId());
						playlists.remove(playlists.indexOf(data));
					} else {
						data.removeFromPlaylist(getContext(), data.getId(), musicData.getId());
						playlists.get(playlists.indexOf(data)).getSongs().remove(musicData);
						playbackService.remove(musicData);
						if (playlists.get(playlists.indexOf(data)).getSongs().size() == 0 && null !=groupItems() && groupItems().length > 1) {
		                		setGroupIndicator((View) v.getParent(), 0);
						}
					}
					updateAdapter();
				}
				return false;
			}
			
		});
		menu.show();
	}
	
	@SuppressLint("NewApi")
	public void closeDialog() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) return;
		if (null != dialog && dialog.isShowing()) {
			dialog.cancel();
		}
	}
	
	public void createPlaylist(ContentResolver resolver, String pName) {
		pName = getDirectory() + pName; 
		try {
			Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
			ContentValues values = new ContentValues();
			values.put(MediaStore.Audio.Playlists.NAME, pName);
			resolver.insert(uri, values);
			updatePlaylist();
			updateAdapter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateAdapter() {
		((Activity) getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				expandableAdapter.notifyDataSetChanged();
			}
		});
	}

	private ArrayList<PlaylistData> getPlaylists() {
		ArrayList<PlaylistData> playlistDatas = null;
		try {
			playlistDatas = new ArrayList<PlaylistData>();
			Cursor playlistCursor = myQuery(getContext(), MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, PROJECTION_PLAYLIST, null, null, MediaStore.Audio.Playlists.NAME);
			PlaylistData playlistData = new PlaylistData();
			if (playlistCursor.getCount() == 0 || !playlistCursor.moveToFirst()) {
				return playlistDatas;
			}
			if (playlistCursor.getString(1).contains(getDirectory())) {
				playlistData.populate(playlistCursor);
				playlistDatas.add(playlistData);
			}
			while (playlistCursor.moveToNext()) {
				if (playlistCursor.getString(1).contains(getDirectory())) {
					PlaylistData playlist = new PlaylistData();
					playlist.populate(playlistCursor);
					playlistDatas.add(playlist);
				}
			}
			playlistCursor.close();
			return playlistDatas;
		} catch (Exception e) {
			e.printStackTrace();
			return playlistDatas;
		}
	}

	public Cursor myQuery(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		try {
			ContentResolver resolver = context.getContentResolver();
			if (resolver == null) {
				return null;
			}
			return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (UnsupportedOperationException ex) {
			return null;
		}
	}
	
	public void applyFilter(String srcFilter) {
		expandableAdapter.getFilter().filter(srcFilter);
	}
	
	public void clearFilter() {
		expandableAdapter.clearFilter();
	}
	
	public void showMessage(Context context, String message) {
		Toast.makeText(context, message ,Toast.LENGTH_SHORT).show();
	}
	
	public void showMessage(Context context, int message) {
		showMessage(context, context.getString(message));
	}
	
}
