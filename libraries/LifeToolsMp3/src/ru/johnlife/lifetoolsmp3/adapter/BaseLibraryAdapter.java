package ru.johnlife.lifetoolsmp3.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.services.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.utils.Util;

public abstract class BaseLibraryAdapter extends BaseAbstractAdapter<MusicData> {
	
	public final String[] PROJECTION_PLAYLIST = { 
			MediaStore.Audio.Playlists._ID, 
			MediaStore.Audio.Playlists.NAME, };
	
	private AlertDialog dialog;

	protected PlaybackService service;
	
	protected abstract String getDirectory();
	protected abstract Bitmap getDefaultCover();
	protected abstract boolean showDeleteItemMenu();
	protected abstract void startSong(AbstractSong abstractSong);
	
	protected void remove() {};
	
	private final int CACHE_CAPACITY = 50;
	
	@SuppressWarnings("serial")
	private final HashMap<Integer, Bitmap> hardBitmapCache = 
			new LinkedHashMap<Integer, Bitmap>(CACHE_CAPACITY, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(LinkedHashMap.Entry<Integer, Bitmap> eldest) {
            return size() > CACHE_CAPACITY;
		}
	};
	
	public void putToCache(Integer url, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (hardBitmapCache) {
				hardBitmapCache.put(url, bitmap);
			}
		}
	}
	
	public Bitmap getFromCache(Integer id) {
		Bitmap bitmap = null;
		synchronized (hardBitmapCache) {
			bitmap = hardBitmapCache.get(id);
			if (bitmap != null) {
				hardBitmapCache.remove(id);
				hardBitmapCache.put(id, bitmap);
				return bitmap;
			}
		}
		return bitmap;
	}

	public void clearCache() {
		for (Map.Entry<Integer, Bitmap> entry : hardBitmapCache.entrySet()) {
			Bitmap b = entry.getValue();
			if (b != null) {
				b.recycle();
			}
			b = null;
		}
		hardBitmapCache.clear();
		System.gc();
	}

	public BaseLibraryAdapter(Context context, int resource) {
		super(context, resource);
		service = PlaybackService.get(getContext());
	}
	
	public BaseLibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
		service = PlaybackService.get(getContext());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup p) {
        return super.getView(position, convertView, p);
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
		protected void hold(final MusicData item, int position) {
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
			
			Bitmap bmp = getFromCache(item.hashCode());
			if (null != bmp) {
				cover.setImageBitmap(bmp);
			} else {
				cover.setImageBitmap(getDefaultCover());
				item.getCover(new OnBitmapReadyListener() {
					int tag = item.hashCode();

					@Override
					public void onBitmapReady(Bitmap bmp) {
						if (bmp != null) {
							bmp = Util.resizeBitmap(bmp, Util.dpToPx(getContext(), 72),	Util.dpToPx(getContext(), 72));
						}
						setCover(bmp, tag);
						putToCache(item.hashCode(), bmp);
					}
				});
			}
		}
		
		private void setCover(final Bitmap bmp, final int tag) {
			((BaseMiniPlayerActivity) getContext()).runOnUiThread(new Runnable() {						
				@Override
				public void run() {						
					if (tag == threeDot.getTag().hashCode()) {
						cover.setImageBitmap(bmp == null ? getDefaultCover() : bmp);
					}
				}
			});
		}
	}	
	
	public void setListener() {
		service.addStatePlayerListener(stateListener);
	}
	
	public void resetListener() {
		service.removeStatePlayerListener(stateListener);
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
					notifyDataSetChanged();
					startSong((AbstractSong) v.getTag());
				} else if (paramMenuItem.getItemId() == R.id.library_menu_add_to_playlist) {
					preparePlaylists(v);
				} else if (paramMenuItem.getItemId() == R.id.library_menu_delete) {
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
	
	private void preparePlaylists(View v) {
		ArrayList<PlaylistData> playlistDatas = getPlaylists();
		String[] data = new String[playlistDatas.size()];
		for (int i = 0; i < playlistDatas.size(); i++) {
			data[i] = playlistDatas.get(i).getName().replace(getDirectory(), "");
		}
		if (playlistDatas.size() == 0) {
			showMessage(getContext(), R.string.playlists_are_missing);
		} else {
			for (PlaylistData playlistData : playlistDatas) {
				playlistData.setSongs(playlistData.getSongsFromPlaylist(getContext(), playlistData.getId()));
			}
			showPlaylistsDialog(playlistDatas, v, data);
		} 
	}
	
	protected void showPlaylistsDialog(final ArrayList<PlaylistData> playlistDatas, final View v, String[] data) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final View dialoglayout = LayoutInflater.from(getContext()).inflate(R.layout.playlist_select_dialog, null);
		final ListView listView = (ListView) dialoglayout.findViewById(R.id.listView);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.playlist_select_dialog_item, data){

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				if (contains(playlistDatas.get(position), ((MusicData) v.getTag()))) {
					((TextView) view.findViewById(android.R.id.text1)).setBackgroundColor(Color.LTGRAY);
				} else {
					((TextView) view.findViewById(android.R.id.text1)).setBackgroundColor(Color.TRANSPARENT);
				}
				return view;
			}

			@Override
			public boolean isEnabled(int position) {
                return !contains(playlistDatas.get(position), ((MusicData) v.getTag()));
            }

		};
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
				boolean playlistIsPlaying = playlistDatas.get(paramInt).getSongs().equals(PlaybackService.get(getContext()).getArrayPlayback());
				playlistDatas.get(paramInt).addToPlaylist(paramView.getContext(), ((MusicData) v.getTag()).getId(), playlistDatas.get(paramInt).getId());
				if (playlistIsPlaying) {
					ArrayList<MusicData> array = playlistDatas.get(paramInt).getSongsFromPlaylist(getContext(), playlistDatas.get(paramInt).getId());
					PlaybackService.get(getContext()).addArrayPlayback(array.get(array.size() - 1));
				}
				dialog.dismiss();
			}
		});
		builder.setView(dialoglayout);
		dialog = builder.create();
		dialog.show();
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
		} catch (Exception e) {
			e.printStackTrace();
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
	
	public boolean contains(PlaylistData data, MusicData mData) {
		for (MusicData music : data.getSongs()) {
			if (music.getPath().equalsIgnoreCase(mData.getPath())) {
				return true;
			}
		}
		return false;
	}
	
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void start(AbstractSong song) {
			notifyDataSetChanged();
		}

		@Override
		public void play(AbstractSong song) {}

		@Override
		public void pause(AbstractSong song) {}

		@Override
		public void stop(AbstractSong song) {
		}

		@Override
		public void stopPressed() {
			notifyDataSetChanged();
		}

		@Override
		public void onTrackTimeChanged(int time, boolean isOverBuffer) {}

		@Override
		public void onBufferingUpdate(double percent) {}

		@Override
		public void update(AbstractSong song) {
			notifyDataSetChanged();
		}

		@Override
		public void error() {}
		
	};
	
	public void showMessage(Context context, String message) {
		Toast.makeText(context, message ,Toast.LENGTH_SHORT).show();
	}
	
	public void showMessage(Context context, int message) {
		showMessage(context, context.getString(message));
	}
	
}
