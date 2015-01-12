package org.upmobile.newmusicdownloader.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.data.MusicData;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.service.PlayerService;
import org.upmobile.newmusicdownloader.service.PlayerService.OnStatePlayerListener;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class LibraryAdapter extends BaseAdapter<MusicData> {

	private PlayerService service;
	private final Drawable BTN_PLAY;
	private final Drawable BTN_PAUSE;
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void start(AbstractSong song) {
			if (song.getClass() != MusicData.class)
				return;
			MusicData data = get(song);
			data.setPlaying(true);
			notifyDataSetChanged();
		}

		@Override
		public void play(AbstractSong song) {
			if (song.getClass() != MusicData.class)
				return;
			MusicData data = get(song);
			data.setPlaying(true);
			notifyDataSetChanged();
		}

		@Override
		public void pause(AbstractSong song) {
			if (song.getClass() != MusicData.class)
				return;
			MusicData data = get(song);
			data.setPlaying(false);
			notifyDataSetChanged();
		}

		@Override
		public void update(AbstractSong song) {
			if (song.getClass() != MusicData.class)
				return;
			MusicData data = get(song);
			data.setPlaying(true);
			notifyDataSetChanged();
		}

		@Override
		public void stop(AbstractSong song) {
			MusicData data = get(song);
			if (song.getClass() != MusicData.class || data == null) return;
			data.setPlaying(false);
			notifyDataSetChanged();
		}
		
	};

	public LibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
		BTN_PAUSE = context.getResources().getDrawable(R.drawable.pause_white);
		BTN_PLAY = context.getResources().getDrawable(R.drawable.play_white);
		new Thread(new Runnable() {

			@Override
			public void run() {
				service = PlayerService.get(getContext());
				service.setStatePlayerListener(stateListener);
			}
		}).start();
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

	private class LibraryViewHolder extends ViewHolder<MusicData> implements OnClickListener, OnLongClickListener {

		private MusicData data;
		private ViewGroup info;
		private View button;
		private ImageView cover;
		private TextView title;
		private TextView artist;
		private TextView duration;

		public LibraryViewHolder(View v) {
			info = (ViewGroup) v.findViewById(R.id.item_box_info);
			button = v.findViewById(R.id.item_play);
			cover = (ImageView) v.findViewById(R.id.item_cover);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_artist);
			duration = (TextView) v.findViewById(R.id.item_duration);
		}

		@Override
		protected void hold(MusicData data, int position) {
			this.data = data;
			title.setText(data.getTitle());
			artist.setText(data.getArtist());
			duration.setText(Util.getFormatedStrDuration(data.getDuration()));
			if (data.isPlaying()) {
				setButtonBackground(BTN_PAUSE);
			} else {
				setButtonBackground(BTN_PLAY);
			}
			cover.setImageResource(R.drawable.no_cover_art_big);
			WeakReference<Bitmap> bitmap = new WeakReference<Bitmap>(data.getCover(getContext()));
			if (null != bitmap && null != bitmap.get()) {
				cover.setImageBitmap(bitmap.get());
			}
			setListener();
		}

		private void setListener() {
			cover.setOnClickListener(this);
			cover.setOnLongClickListener(this);
			info.setOnClickListener(this);
			info.setOnLongClickListener(this);
			button.setOnClickListener(this);
		}

		@SuppressLint("NewApi")
		private void setButtonBackground(Drawable drawable) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				button.setBackgroundDrawable(drawable);
			} else {
				button.setBackground(drawable);
			}
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.item_cover:
			case R.id.item_box_info:
				if (!service.isCorrectlyState(MusicData.class, getCount())) {
					ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
					service.setArrayPlayback(list);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable(Constants.KEY_SELECTED_SONG, data);
				PlayerFragment playerFragment = new PlayerFragment();
				playerFragment.setArguments(bundle);
				((MainActivity) view.getContext()).changeFragment(playerFragment);
				((MainActivity) getContext()).overridePendingTransition(0, 0);
				break;
			case R.id.item_play:
				if (!service.isCorrectlyState(MusicData.class, getCount())) {
					ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
					service.setArrayPlayback(list);
				}
				service.play(data);
				((MainActivity) getContext()).showPlayerElement(true);
				break;
			}
		}

		@Override
		public boolean onLongClick(View view) {
			if (view.getId() == cover.getId() || view.getId() == info.getId()) {
				PopupMenu menu = new PopupMenu(getContext(), view);
				menu.getMenuInflater().inflate(R.menu.menu, menu.getMenu());
				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						remove(data);
						service.remove(data);
						data.reset(getContext());
						if (isEmpty()) {
							((MainActivity) getContext()).showPlayerElement(false);
							TextView emptyMsg = (TextView) ((MainActivity) getContext()).findViewById(R.id.message_listview);
							emptyMsg.setVisibility(View.VISIBLE);
							emptyMsg.setText(R.string.library_empty);
						}
						return false;
					}
				});
				menu.show();
			}
			return true;
		}
	}

}
