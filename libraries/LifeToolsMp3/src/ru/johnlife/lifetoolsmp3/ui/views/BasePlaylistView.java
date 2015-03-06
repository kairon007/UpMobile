package ru.johnlife.lifetoolsmp3.ui.views;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BasePlaylistView extends View {

	private final static String EXTERNAL = "external";
	private ViewGroup view;
	private ListView listView;
	private ExpandableAdapter expandableAdapter;
	private View playLastPlaylist;
	private View createNewPlayList;
	private AlertDialog.Builder newPlaylistDialog;

	protected abstract Bitmap getDeafultCover();
	
	protected abstract String getDirectory();

	protected abstract int getLayoutId();

	protected abstract void showPlayerFragment(MusicData musicData);

	protected abstract ListView getListView(View view);

	protected abstract TextView getMessageView(View view);

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

	public BasePlaylistView(LayoutInflater inflater) {
		super(inflater.getContext());
		init(inflater);
	}

	private void init(LayoutInflater inflater) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				playbackService = PlaybackService.get(getContext());
			}
		}).start();
		view = (ViewGroup) inflater.inflate(getLayoutId() > 0 ? getLayoutId() : ru.johnlife.lifetoolsmp3.R.layout.playlist_view, null);
		listView = getListView(view) != null ? getListView(view) : (AnimatedExpandableListView) view.findViewById(R.id.expandableListView);
		playLastPlaylist = (View) view.findViewById(R.id.lastPlayedPlaylist);
		createNewPlayList = (View) view.findViewById(R.id.createNewPlaylist);
		initListeners();
		expandableAdapter = new ExpandableAdapter(view.getContext());
		expandableAdapter.setProjectPrefics(getDirectory());
		expandableAdapter.setDeafultBmp(getDeafultCover());
		updatePlaylist();
		((AnimatedExpandableListView) listView).setAdapter(expandableAdapter);
		((AnimatedExpandableListView) listView).setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int groupPosition = ExpandableListView.getPackedPositionGroup(arg3);
		        int childPosition = ExpandableListView.getPackedPositionChild(arg3);
				showMenu(arg1, playlists.get(groupPosition), childPosition == -1 ? null : playlists.get(groupPosition).getSongs().get(childPosition) );
				return true;
			}
		});
		((AnimatedExpandableListView) listView).setOnChildClickListener(new OnChildClickListener() {
			

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (null != playbackService) {
					MusicApp.getSharedPreferences().edit().putLong(Constants.PREF_LAST_PLAYLIST_ID, playlists.get(groupPosition).getId()).commit();
					playbackService.setArrayPlayback(new ArrayList<AbstractSong>(playlists.get(groupPosition).getSongs()));
					playbackService.play(playlists.get(groupPosition).getSongs().get(childPosition));
					showPlayerFragment(playlists.get(groupPosition).getSongs().get(childPosition));
					return true;
				}
				return false;
			}
		});
	}

	private void updatePlaylist() {
		if (null != playlists) {
			playlists.clear();
		}
		playlists = getPlaylists();
		for (PlaylistData playlistData : playlists) {
			playlistData.setSongs(getSongsFromPlaylist(playlistData.getId()));
		}
		expandableAdapter.setData(playlists);
	}

	private void initListeners() {
		createNewPlayList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				showDialog();
			}
		});
		playLastPlaylist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
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
				dialog.cancel();
			}

		});
		newPlaylistDialog.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				createPlaylist(getContext().getContentResolver(), ((EditText) dialoglayout.findViewById(R.id.newPlaylistET)).getText().toString());
				dialog.cancel();
			}
		});
		newPlaylistDialog.create().show();
	}

	public View getView() {
		return view;
	}

	@SuppressLint("NewApi")
	public void showMenu(final View v, final PlaylistData data, final MusicData musicData) {
		final PopupMenu menu = new PopupMenu(getContext(), v);
		menu.getMenuInflater().inflate(R.menu.library_menu, menu.getMenu());
		menu.getMenu().getItem(0).setVisible(false);
		menu.getMenu().getItem(1).setVisible(false);
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem paramMenuItem) {
				if (paramMenuItem.getItemId() == R.id.library_menu_delete) {
					if (musicData == null) {
						deletePlaylist(getContext().getContentResolver(), data.getId());
						playlists.remove(playlists.indexOf(data));
					} else {
						removeFromPlaylist(getContext().getContentResolver(), data.getId(), musicData.getId());
						playlists.get(playlists.indexOf(data)).getSongs().remove(musicData);
					}
					updateAdapter();
				}
				return false;
			}
		});
		menu.show();
	}

	private void deletePlaylist(ContentResolver resolver, long playlistId) {
		try {
			String playlistid = String.valueOf(playlistId);
			String where = MediaStore.Audio.Playlists._ID + "=?";
			String[] whereVal = { playlistid };
			resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addToPlaylist(ContentResolver resolver, int audioId, int playlistId) {
		try {
			String[] cols = new String[] { "count(*)" };
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, playlistId);
			Cursor cur = resolver.query(uri, cols, null, null, null);
			cur.moveToFirst();
			final int base = cur.getInt(0);
			cur.close();
			ContentValues values = new ContentValues();
			values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + audioId));
			values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
			resolver.insert(uri, values);
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removeFromPlaylist(ContentResolver resolver, long playlistId, long audioId) {
	    try {
	        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, playlistId);
	        String where = MediaStore.Audio.Playlists.Members._ID + "=?" ;
	        String audioId1 = Long.toString(audioId);
	        String[] whereVal = { audioId1 };
	        resolver.delete(uri, where,whereVal);      
	    } catch (Exception e) {
	        e.printStackTrace();
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
		ArrayList<PlaylistData> playlistDatas = new ArrayList<PlaylistData>();
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
	}

	private ArrayList<MusicData> getSongsFromPlaylist(long playlistID) {
		Cursor cursor = myQuery(getContext(), MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, Long.valueOf(playlistID)), PROJECTION_MUSIC, null, null, null);
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			return result;
		}
		MusicData d = new MusicData();
		d.populate(cursor);
		result.add(d);
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);
			result.add(data);
		}
		cursor.close();
		return result;
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
	
	public void showMessage(Context context, String message) {
		Toast.makeText(context, message ,Toast.LENGTH_SHORT).show();
	}
	
	public void showMessage(Context context, int message) {
		showMessage(context, context.getString(message));
	}
}
