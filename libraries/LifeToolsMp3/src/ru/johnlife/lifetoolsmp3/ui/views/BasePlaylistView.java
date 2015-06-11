package ru.johnlife.lifetoolsmp3.ui.views;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
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
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BasePlaylistView extends View {

	private static final String PREF_DIRECTORY_PREFIX = "pref.directory.prefix";
	private ViewGroup view;
	protected ListView listView;
	private View playLastPlaylist;
	private View createNewPlayList;
	private TextView emptyMessage;
	private BasePlaylistsAdapter adapter;
	private AlertDialog.Builder newPlaylistDialog;
	private PopupMenu menu;
	private PlaybackService playbackService;
	private AlertDialog dialog;
	
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
	
	protected abstract Bitmap getDeafultCover();
	
	protected abstract String getDirectory();

	protected abstract int getLayoutId();
	
	protected abstract BasePlaylistsAdapter getAdapter(Context context);

	protected abstract void showPlayerFragment(MusicData musicData);

	protected abstract ListView getListView(View view);

	public abstract TextView getMessageView(View view);
	
	protected void animateListView(ListView listView, BasePlaylistsAdapter adapter) {
		//Animate ListView in childs, if need
	}
	
	private OnSharedPreferenceChangeListener sharedPreferenceListener = new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.contains(PREF_DIRECTORY_PREFIX)) {
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
		view = (ViewGroup) inflater.inflate(getLayoutId(), null);
		listView = getListView(view);
		emptyMessage = getMessageView(view);
		adapter = getAdapter(inflater.getContext());
		listView.setAdapter(adapter);
		playLastPlaylist = view.findViewById(R.id.lastPlayedPlaylist);
		createNewPlayList = view.findViewById(R.id.createNewPlaylist);
		animateListView(listView, adapter);
		initListeners();
		updatePlaylist();
    	if (adapter.isEmpty()) {
    		listView.setEmptyView(emptyMessage);
		}
    	adapter.getCount();
	}
	
	public void collapseAll () {
		ArrayList<MusicData> music = new ArrayList<MusicData>();
		ArrayList<AbstractSong> playlists = adapter.getAll();
		for (AbstractSong data : playlists ) {
			if (data.getClass() == MusicData.class) {
				music.add((MusicData) data);
			} else {
				((PlaylistData) data).setExpanded(false);
			}
		}
		playlists.removeAll(music);
		updateAdapter(playlists);
	}
	
	private void updatePlaylist() {
		ArrayList<AbstractSong> playlists = getPlaylists();
		for (AbstractSong playlistData : playlists) {
			((PlaylistData) playlistData).setSongs(((PlaylistData) playlistData).getSongsFromPlaylist(getContext(), playlistData.getId()));
		}
		updateAdapter(playlists);
	}

	private void initListeners() {
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				AbstractSong data = (AbstractSong) adapter.getItem(position);
				showMenu(view, data.getClass() == MusicData.class ? getPlaylistBySong((MusicData) data) : (PlaylistData)data, data.getClass() == MusicData.class ? (MusicData)data : null);
				return false;
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ArrayList<AbstractSong> playlists = adapter.getAll();
				AbstractSong abstractSong = playlists .get(position);
				if (abstractSong.getClass() == PlaylistData.class) {
					ArrayList<MusicData> songs = ((PlaylistData) abstractSong).getSongs();
					if (null == songs || songs.size() == 0) {
						showMessage(getContext(), R.string.playlist_is_empty);
	            		return;
					}
					if (!((PlaylistData) abstractSong).isExpanded()) {
						playlists.addAll(position + 1, songs);
						((PlaylistData) abstractSong).setExpanded(true);
					} else {
						playlists.removeAll(songs);
						((PlaylistData) abstractSong).setExpanded(false);
					}
					updateAdapter(playlists);
				} else {
					Util.hideKeyboard(getContext(), view);
					if (null != playbackService) {
						MusicApp.getSharedPreferences().edit().putLong(Constants.PREF_LAST_PLAYLIST_ID, getPlaylistBySong((MusicData) playlists.get(position)).getId()).commit();
						playbackService.setArrayPlayback(new ArrayList<AbstractSong>(getPlaylistBySong((MusicData) playlists.get(position)).getSongs()));
						showPlayerFragment((MusicData) playlists.get(position));
					}
				}
			}
		});
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
					for (AbstractSong data : adapter.getAll()) {
						if (data.getId() == id) {
							isPlaylistExists = true;
							if (((PlaylistData) data).getSongs().size() == 0) {
								showMessage(getContext(), R.string.no_songs_in_the_last_playlist);
							} else {
								playbackService.setArrayPlayback(new ArrayList<AbstractSong>(((PlaylistData) data).getSongs()));
								playbackService.play(((PlaylistData) data).getSongs().get(0));
								showPlayerFragment(((PlaylistData) data).getSongs().get(0));
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
					removeData(data, musicData);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateAdapter(final ArrayList<AbstractSong> playlists) {
		((Activity) getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				adapter.setDoNotifyData(false);
				adapter.clear();
				adapter.changeData(playlists);
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	public void removeData(final PlaylistData data, final MusicData musicData) {
		ArrayList<AbstractSong> playlists = adapter.getAll();
		if (musicData == null) {
			if (data.isExpanded()) {
				playlists.removeAll(data.getSongs());
			}
			data.deletePlaylist(getContext(), data.getId());
			playlists.remove(playlists.indexOf(data));
		} else {
			data.removeFromPlaylist(getContext(), data.getId(), musicData.getId());
			((PlaylistData) playlists.get(playlists.indexOf(data))).getSongs().remove(musicData);
			playlists.remove(musicData);
			playbackService.remove(musicData);
		}
		if (((PlaylistData) playlists.get(playlists.indexOf(data))).getSongs().size() == 0) {
			((PlaylistData) playlists.get(playlists.indexOf(data))).setExpanded(false);
		}
		updateAdapter(playlists);
	}

	private ArrayList<AbstractSong> getPlaylists() {
		ArrayList<AbstractSong> playlistDatas = null;
		try {
			playlistDatas = new ArrayList<AbstractSong>();
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
	
	public PlaylistData getPlaylistBySong(MusicData song) {
		for (AbstractSong playlistData : adapter.getAll()) {
			if (playlistData.getClass() == PlaylistData.class && ((PlaylistData) playlistData).getSongs().contains(song)) {
				return ((PlaylistData) playlistData);
			}
		}
		return null;
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
		adapter.getFilter().filter(srcFilter);
	}
	
	public void clearFilter() {
		adapter.clearFilter();
	}
	
	public void showMessage(Context context, String message) {
		Toast.makeText(context, message ,Toast.LENGTH_SHORT).show();
	}
	
	public void showMessage(Context context, int message) {
		showMessage(context, context.getString(message));
	}
	
}
