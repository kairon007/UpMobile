package org.upmobile.musicpro.adapter;

import java.util.ArrayList;

import org.upmobile.musicpro.R;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class LibraryAdapter extends BaseAdapter<MusicData> {

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
		
	};

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public MusicData get(AbstractSong data) {
		if (data == null) return null;
		for (int i = 0; i < getCount(); i++) {
			MusicData buf = getItem(i);
			if (buf.equals(data)) {
				return getItem(i);
			}
		}
		return null;
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}

	@Override
	protected boolean isSetListener() {
		return false;
	}

	private class LibraryViewHolder extends ViewHolder<MusicData> {
		
		private TextView tvTitle;
		private TextView tvArtist;
		private TextView tvDuration;
		private ImageButton btnPlayback;

		private LibraryViewHolder(View v) {
			tvTitle = (TextView) v.findViewById(R.id.lib_title);
			tvArtist = (TextView) v.findViewById(R.id.lib_artist);
			tvDuration = (TextView) v.findViewById(R.id.lib_duration);
			btnPlayback = (ImageButton) v.findViewById(R.id.lib_play);
		}

		@Override
		protected void hold(final MusicData item, int position) {
			tvTitle.setText(item.getTitle());
			tvArtist.setText(item.getArtist());
			tvDuration.setText(Util.getFormatedStrDuration(item.getDuration()));
			if (item.check(MusicData.MODE_PLAYING)) {
				btnPlayback.setImageResource(R.drawable.btn_pause);
			} else {
				btnPlayback.setImageResource(R.drawable.btn_play);
			}
			btnPlayback.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (PlaybackService.hasInstance()) {
						PlaybackService.get(getContext()).setStatePlayerListener(stateListener);
					}
					if (!PlaybackService.get(getContext()).isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						PlaybackService.get(getContext()).setArrayPlayback(list);
					} 
					PlaybackService.get(getContext()).play(item);
				}
			});
		}

	}

}
