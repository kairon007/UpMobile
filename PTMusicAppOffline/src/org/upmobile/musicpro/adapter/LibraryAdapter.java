package org.upmobile.musicpro.adapter;

import java.util.ArrayList;

import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter.ViewHolder;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class LibraryAdapter extends BaseAbstractAdapter<MusicData> {

	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void start(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			data.turnOn(MusicData.MODE_PLAYING);
			notifyDataSetChanged();
		}

		@Override
		public void play(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			data.turnOn(MusicData.MODE_PLAYING);
			notifyDataSetChanged();
		}

		@Override
		public void pause(AbstractSong song) {
			if (song.getClass() != MusicData.class) return;
			MusicData data = get(song);
			data.turnOff(MusicData.MODE_PLAYING);
			notifyDataSetChanged();
		}

		@Override
		public void stop(AbstractSong song) {
			MusicData data = get(song);
			if (song.getClass() != MusicData.class || data == null) return;
			data.turnOff(MusicData.MODE_PLAYING);
			notifyDataSetChanged();
		}
		
		@Override
		public void error() {
		}

		@Override
		public void update(AbstractSong song) {
			
		}

		@Override
		public void stopPressed() {
			
		}
		
	};

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
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

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}

	private class LibraryViewHolder extends ViewHolder<MusicData> implements OnLongClickListener{
		
		private MusicData data;
		private ViewGroup info;
		private ImageView cover;
		private TextView tvTitle;
		private TextView tvArtist;
		private TextView tvDuration;
		private ImageButton btnPlayback;

		private LibraryViewHolder(View v) {
			tvTitle = (TextView) v.findViewById(R.id.lib_title);
			info = (ViewGroup) v.findViewById(R.id.item_box_info);
			cover = (ImageView) v.findViewById(R.id.lib_cover);
			tvArtist = (TextView) v.findViewById(R.id.lib_artist);
			tvDuration = (TextView) v.findViewById(R.id.lib_duration);
			btnPlayback = (ImageButton) v.findViewById(R.id.lib_play);
		}

		@Override
		protected void hold(final MusicData item, int position) {
			data = item;
			tvTitle.setText(item.getTitle());
			tvArtist.setText(item.getArtist());
			tvDuration.setText(Util.getFormatedStrDuration(item.getDuration()));
			info.setOnLongClickListener(this);
			cover.setOnLongClickListener(this);
			if (item.check(MusicData.MODE_PLAYING)) {
				btnPlayback.setImageResource(R.drawable.btn_pause);
			} else {
				btnPlayback.setImageResource(R.drawable.btn_play);
			}
			btnPlayback.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((MainActivity) getContext()).cancelNotification();
					if (null != ((MainActivity) getContext()).getService(false)) {
						((MainActivity) getContext()).getService(false).reset();
						((MainActivity) getContext()).setButtonPlay();
					}
					if (PlaybackService.hasInstance()) {
						PlaybackService.get(getContext()).addStatePlayerListener(stateListener);
					}
					if (!PlaybackService.get(getContext()).isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						PlaybackService.get(getContext()).setArrayPlayback(list);
					} 
					PlaybackService.get(getContext()).play(item);
					((MainActivity) getContext()).initPlayback();
				}
			});
		}
		
		@Override
		public boolean onLongClick(View view) {
			if (view.getId() != btnPlayback.getId() ) {
				PopupMenu menu = new PopupMenu(getContext(), view);
				menu.getMenuInflater().inflate(R.menu.deletemenu, menu.getMenu());
				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						remove(data);
						PlaybackService.get(getContext()).remove(data);
						data.reset(getContext());
						return false;
					}
				});
				menu.show();
			}
			return true;
		}

	}

}
