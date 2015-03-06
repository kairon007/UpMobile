package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class LibraryAdapter extends BaseLibraryAdapter {

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
		initService();
	}
	
	public LibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
		initService();
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}

	private class LibraryViewHolder extends BaseLibraryViewHolder implements OnClickListener {

		private MusicData data;
		private ViewGroup info;
		private ImageButton button;

		public LibraryViewHolder(View v) {
			info = (ViewGroup) v.findViewById(R.id.item_box_info);
			button = (ImageButton) v.findViewById(R.id.item_play);
			cover = (ImageView) v.findViewById(R.id.item_cover);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_artist);
			duration = (TextView) v.findViewById(R.id.item_duration);
			threeDot = v.findViewById(R.id.threeDot);
		}

		@Override
		protected void hold(MusicData data, int position) {
			this.data = data;
			super.hold(data, position);
			if (data.check(MusicData.MODE_PLAYING)) {
				button.setImageResource(R.drawable.pause_white);
			} else {
				button.setImageResource(R.drawable.play_white);
			}
			setListener();
		}

		private void setListener() {
			cover.setOnClickListener(this);
			info.setOnClickListener(this);
			button.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.item_cover:
			case R.id.item_box_info:
				startSong();
				break;
			case R.id.item_play:
				startSong();
				break;
			}
		}

		private void startSong() {
			if (!service.isCorrectlyState(MusicData.class, getCount())) {
				ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
				service.setArrayPlayback(list);
			}
			((MainActivity)getContext()).startSong(data);
		}
	}

	@Override
	protected boolean isSetListener() {
		return false;
	}

	@Override
	protected int getDefaultCover() {
		return R.drawable.no_cover_art_big;
	}

	@Override
	protected boolean showDeleteItemMenu() {
		return true;
	}
	
	@Override
	protected void remove() {
		if (isEmpty()) {
			((MainActivity) getContext()).showPlayerElement(false);
			TextView emptyMsg = (TextView) ((MainActivity) getContext()).findViewById(R.id.message_listview);
			emptyMsg.setVisibility(View.VISIBLE);
			emptyMsg.setText(R.string.library_empty);
		}
	}

	@Override
	protected String getDirectory() {
		return NewMusicDownloaderApp.getDirectory();
	}

	@Override
	protected void startSong(AbstractSong abstractSong) {
		((MainActivity) getContext()).startSong(abstractSong);
	}
}