package ru.johnlife.lifetoolsmp3.ui.baseviews;

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
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.HashSet;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.utils.Util;

public abstract class BasePlaylistView extends View {

	private static final String PREF_DIRECTORY_PREFIX = "pref.directory.prefix";
	private static final String PREF_LAST_OPENED = "pref.last.opened";
	private static final String PLYLIST_TAG = "playlist";
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

	protected abstract Bitmap getDefaultCover();
	
	protected abstract String getDirectory();

	protected abstract int getLayoutId();
	
	protected abstract BasePlaylistsAdapter getAdapter(Context context);

	protected abstract void showPlayerFragment(MusicData musicData);

	protected abstract ListView getListView(View view);

	protected abstract void collapseSearchView();

	public abstract TextView getMessageView(View view);
	
	protected void animateListView(ListView listView, BasePlaylistsAdapter adapter) {
		//Animate ListView in childs, if need
	}
	
	protected abstract void forceDelete();
	
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
		BaseMiniPlayerActivity activity = (BaseMiniPlayerActivity) getContext();
		if (activity.getFragmentManager().getBackStackEntryAt(activity.getFragmentManager().getBackStackEntryCount() - 1).getName().toLowerCase().contains(PLYLIST_TAG.toLowerCase())) {
			setOpened();
		}
		MusicApp.getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
	}
	
	public void onPause () {
		MusicApp.getSharedPreferences().edit().putStringSet(PREF_LAST_OPENED, getOpenedPlaylists()).apply();
		MusicApp.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sharedPreferenceListener);
	}
	
	private void setOpened() {
		ArrayList<AbstractSong> allItems = getAllItems();
		HashSet<String> myHashSet = (HashSet<String>) MusicApp.getSharedPreferences().getStringSet(PREF_LAST_OPENED, new HashSet<String>());
		for (AbstractSong data : getAllItems()) {
			if (data.getClass() == PlaylistData.class && myHashSet.contains(String.valueOf(data.getId()))) {
				if (((PlaylistData) data).getSongs().size() == 0 || ((PlaylistData) data).isExpanded()) continue;
				allItems.addAll(allItems.indexOf(data) + 1, ((PlaylistData) data).getSongs());
				((PlaylistData) data).setExpanded(true);
			}
		}
		updateAdapter(allItems);
	}
	
	private HashSet getOpenedPlaylists() {
		HashSet<String> myHashSet = new HashSet<>();
		for (AbstractSong data : getAllItems()) {
			if (data.getClass() == PlaylistData.class && ((PlaylistData) data).isExpanded()) {
				myHashSet.add(String.valueOf(data.getId()));
			}
		}
		return myHashSet;
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
		ArrayList<MusicData> music = new ArrayList<>();
		ArrayList<AbstractSong> playlists = getAllItems();
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
	
	public void updatePlaylist() {
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
				forceDelete();
				AbstractSong data = (AbstractSong) adapter.getItem(position);
				showMenu(view, data.getClass() == MusicData.class ? getPlaylistBySong((MusicData) data) : (PlaylistData)data, data.getClass() == MusicData.class ? (MusicData)data : null);
				return false;
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				forceDelete();
				ArrayList<AbstractSong> playlists = getAllItems();
				AbstractSong abstractSong = playlists.get(position);
				if (abstractSong.getClass() == PlaylistData.class) {
					ArrayList<MusicData> songs = ((PlaylistData) abstractSong).getSongs();
					if (null == songs || songs.size() == 0) {
						showMessage(getContext(), R.string.playlist_is_empty);
	            		return;
					}
                    if (((PlaylistData) abstractSong).isExpanded()) {
                        playlists.removeAll(songs);
                        ((PlaylistData) abstractSong).setExpanded(false);
                    } else {
                        playlists.addAll(position + 1, songs);
                        ((PlaylistData) abstractSong).setExpanded(true);
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
					for (AbstractSong data : getAllItems()) {
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
		final ArrayList<String> playlistNames = new ArrayList<>();
		for (AbstractSong abstractSong : getAllItems()) {
			if(abstractSong.getClass() == PlaylistData.class) {
				playlistNames.add(((PlaylistData) abstractSong).getName().replace(getDirectory(), ""));
			}
		}
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
					showMessage(getContext(), R.string.playlist_cannot_be_empty);
					return;
				}
				for (AbstractSong data : getAllItems()) {
					if (data.getClass() == PlaylistData.class && ((PlaylistData) data).getName().replace(getDirectory(), "").equals(newTitle)) {
						showMessage(getContext(), R.string.playlist_already_exists);
						return;
					}
				}
				createPlaylist(getContext().getContentResolver(), newTitle);
				Util.hideKeyboard(getContext(), dialoglayout);
				collapseSearchView();
				dialog.cancel();
			}
		});
		dialog = newPlaylistDialog.create();
		dialog.setCancelable(false);
		EditText editText = (EditText) dialoglayout.findViewById(R.id.newPlaylistET);
		final TextView errorView = (TextView) dialoglayout.findViewById(R.id.errorView);
		editText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(playlistNames.contains(s.toString().trim())){
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
					errorView.setText(R.string.playlist_already_exists);
					errorView.setVisibility(View.VISIBLE);
				} else {
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
					errorView.setText("");
					errorView.setVisibility(View.GONE);
				}
			}
		});
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

	public ArrayList<AbstractSong> getAllItems() {
		return adapter.getAll();
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
		ArrayList<AbstractSong> playlists = getAllItems();
		if (musicData == null) {
			if (data.getSongs().equals(playbackService.getArrayPlayback())) {
				playbackService.stopPressed();						
			}
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
		if (null != musicData && ((PlaylistData) playlists.get(playlists.indexOf(data))).getSongs().size() == 0) {
			((PlaylistData) playlists.get(playlists.indexOf(data))).setExpanded(false);
		}
        adapter.remove(data);
        updateAdapter(playlists);
	}

	private ArrayList<AbstractSong> getPlaylists() {
		ArrayList<AbstractSong> playlistDatas = new ArrayList<>();
		try {
			Cursor playlistCursor = myQuery(getContext(), MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, PROJECTION_PLAYLIST, null, null, MediaStore.Audio.Playlists.NAME);
			PlaylistData playlistData = new PlaylistData();
			if (null == playlistCursor || playlistCursor.getCount() == 0 || !playlistCursor.moveToFirst()) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
        return playlistDatas;
	}
	
	public PlaylistData getPlaylistBySong(MusicData song) {
		for (AbstractSong playlistData : getAllItems()) {
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
