package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.data.MusicData;
import org.upmobile.newmusicdownloader.service.PlayerService;
import org.upmobile.newmusicdownloader.service.PlayerService.OnStatePlayerListener;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LibraryAdapter extends BaseAdapter<MusicData> {
	
	private PlayerService service;
	private final Drawable BTN_PLAY;
	private final Drawable BTN_PAUSE;
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {
		
		@Override
		public void update(AbstractSong song) {
//			if (song.getClass() != MusicData.class) return;
//			if (position > 0) {
//				((MusicData) getItem(position - 1)).setPlaying(false);
//			} else if (position == 0) {
//				((MusicData) getItem(getCount() - 1)).setPlaying(false);
//			}
//			notifyDataSetChanged();
		}
		
		@Override
		public void start(AbstractSong song) {
//			if (song.getClass() != MusicData.class || position == -1) return;
//			if (position == getCount()) --position;
//			((MusicData) getItem(position)).setPlaying(true);
//			notifyDataSetChanged();
		}
		
		@Override
		public void play(AbstractSong song) {
			
		}
		
		@Override
		public void pause(AbstractSong song) {
			
		}
	};
	
	public LibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
		BTN_PAUSE = context.getResources().getDrawable(R.drawable.pause);
		BTN_PLAY = context.getResources().getDrawable(R.drawable.play);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				service = PlayerService.get(getContext());
				service.setStatePlayerListener(stateListener);
			}
		}).start();
	}
	
	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}

	private class LibraryViewHolder extends ViewHolder<MusicData> implements OnClickListener{
		
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
			Bitmap bitmap = data.getCover(getContext());
			if (null != bitmap) {
				cover.setImageBitmap(bitmap);
			}
			setListener();
		}

		private void setListener() {
			cover.setOnClickListener(this);
			info.setOnClickListener(this);
			button.setOnClickListener(this);
		}

		@SuppressLint("NewApi")
		private void setButtonBackground(Drawable drawable) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN){
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
				//TODO from this get into PlayerFragment
				break;
			case R.id.item_play:
				if (!service.isCorrectlyState(MusicData.class, getCount())) {
					ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
					service.setArrayPlayback(list);
				}
				service.play(getPosition(data));
				break;
			}
		}
	}

}
