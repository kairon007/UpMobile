package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public abstract class BaseLibraryAdapter extends BaseAbstractAdapter<MusicData> {

	protected PlaybackService service;
	
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
			threeDot.setOnClickListener(new OnClickListener() {
				
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
					//TODO: 
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
}
