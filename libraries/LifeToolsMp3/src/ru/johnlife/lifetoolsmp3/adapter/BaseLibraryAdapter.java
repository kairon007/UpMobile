package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public abstract class BaseLibraryAdapter extends BaseAbstractAdapter<MusicData> {
	
	private final String PROJECT_PRIFICS = getDirectory().replace(Environment.getExternalStorageDirectory().toString(), "");
	private final static String EXTERNAL = "external";
	public final String[] PROJECTION_PLAYLIST = { 
			MediaStore.Audio.Playlists._ID, 
			MediaStore.Audio.Playlists.NAME, };
	
	private AlertDialog dialog;

	protected PlaybackService service;
	
	protected abstract String getDirectory();
	protected abstract int getDefaultCover();
	protected abstract boolean showDeleteItemMenu();
	protected Bitmap getDefaultBitmap() { return null; }
	protected void remove() {};
	protected void setListener(ViewGroup parent, View view, final int position){ }
	
	protected OnStatePlayerListener stateListener = new OnStatePlayerListener() {
		
		@Override
		public void start(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			if (data != null) {
				data.turnOn(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}

		@Override
		public void play(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			if (data != null) {
				data.turnOn(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}

		@Override
		public void pause(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			if (data != null) {
				data.turnOff(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}

		@Override
		public void stop(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			if (data != null) {
				data.turnOff(MusicData.MODE_PLAYING);
			}
			notifyDataSetChanged();
		}

		@Override
		public void error() {
			
		}

		@Override
		public void update(AbstractSong song) {
			
		}
		
	};
	
	public BaseLibraryAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public BaseLibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup p) {
		View view = super.getView(position, convertView, p);
		if (isSetListener()) setListener(p, view, position);
		return view;
	}

	public MusicData get(AbstractSong data) {
		if (data == null) return null;
		for (int i = 0; i < getCount(); i++) {
			MusicData buf = (MusicData) getItem(i);
			if (buf.equals(data)) {
				return (MusicData) getItem(i);
			}
		}
		return null;
	}
	
	protected abstract class BaseLibraryViewHolder extends ViewHolder<MusicData> {
		
		protected ImageView cover;
		protected TextView title;
		protected TextView artist;
		protected TextView duration;
		protected View threeDot;
		
		@Override
		protected void hold(MusicData item, int position) {
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			threeDot.setTag(item);
			threeDot.setOnClickListener(new android.view.View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showMenu(v);
				}
			});
			Bitmap bitmap = item.getCover(getContext());
			if (null != bitmap) {
				cover.setImageBitmap(bitmap);
			} else {
				if (getDefaultCover() > 0) {
					cover.setImageResource(getDefaultCover());
				} else {
					cover.setImageBitmap(getDefaultBitmap());
				}
			}
			bitmap = null;
		}
	}
	
	protected void initService() {
		service = PlaybackService.get(getContext());
		service.addStatePlayerListener(stateListener);
	}
	
	@SuppressLint("NewApi")
	public void showMenu(final View v) {
		PopupMenu menu = new PopupMenu(getContext(), v);
		menu.getMenuInflater().inflate(R.menu.library_menu, menu.getMenu());
		menu.getMenu().getItem(2).setVisible(showDeleteItemMenu());
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem paramMenuItem) {
				if (paramMenuItem.getItemId() == R.id.library_menu_play) {
					if (!service.isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						service.setArrayPlayback(list);
					} 
					service.play((AbstractSong) v.getTag());
				}
				if (paramMenuItem.getItemId() == R.id.library_menu_add_to_playlist) {
					showPlaylistsDialog(v);
				}
				if (paramMenuItem.getItemId() == R.id.library_menu_delete) {
					remove((MusicData) v.getTag());
					service.remove((AbstractSong) v.getTag());
					((MusicData) v.getTag()).reset(getContext());
					remove();
				}
				return false;
			}
		});
		menu.show();
	}
	
	private void showPlaylistsDialog(final View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final View dialoglayout = LayoutInflater.from(getContext()).inflate(R.layout.playlist_select_dialog, null);
		ListView listView = (ListView) dialoglayout.findViewById(R.id.listView);
		final ArrayList<PlaylistData> playlistDatas = getPlaylists();
		String[] data = new String[playlistDatas.size()];
		for (int i = 0; i < playlistDatas.size(); i++) {
			data[i] = playlistDatas.get(i).getName().replace(PROJECT_PRIFICS, "");
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.playlist_select_dialog_item, data);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
				addToPlaylist(paramView.getContext().getContentResolver(), ((MusicData) v.getTag()).getId(), playlistDatas.get(paramInt).getId());
				dialog.dismiss();
			}
		});
		builder.setView(dialoglayout);
		dialog = builder.create();
		dialog.show();
	}
	
	private ArrayList<PlaylistData> getPlaylists() {
		ArrayList<PlaylistData> playlistDatas = new ArrayList<PlaylistData>();
		Cursor playlistCursor = myQuery(getContext(), MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, PROJECTION_PLAYLIST, null, null, MediaStore.Audio.Playlists.NAME);
		PlaylistData playlistData = new PlaylistData();
		playlistCursor.moveToFirst();
		if (playlistCursor.getString(1).contains(PROJECT_PRIFICS)) {
			playlistData.populate(playlistCursor);
			playlistDatas.add(playlistData);
		}
		while (playlistCursor.moveToNext()) {
			if (playlistCursor.getString(1).contains(PROJECT_PRIFICS)) {
				PlaylistData playlist = new PlaylistData();
				playlist.populate(playlistCursor);
				playlistDatas.add(playlist);
			}
		}
		return playlistDatas;
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
	
	public void addToPlaylist(ContentResolver resolver, long audioId, long playlistId) {
		try {
			String[] cols = new String[] { "count(*)" };
			Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, playlistId);
			Cursor cur = resolver.query(uri, cols, null, null, null);
			cur.moveToFirst();
			final int base = cur.getInt(0);
			cur.close();
			ContentValues values = new ContentValues();
			values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Long.valueOf(base + audioId));
			values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
			resolver.insert(uri, values);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
