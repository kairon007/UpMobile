package org.upmobile.materialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.fragment.PlayerFragment;

import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.os.Bundle;
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

	private class LibraryViewHolder extends BaseLibraryViewHolder implements OnClickListener, OnLongClickListener {

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
			cover.setOnLongClickListener(this);
			info.setOnClickListener(this);
			info.setOnLongClickListener(this);
			button.setOnClickListener(this);
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
//				((MainActivity) view.getContext()).changeFragment(playerFragment);
				((MainActivity) getContext()).overridePendingTransition(0, 0);
				break;
			case R.id.item_play:
				if (!service.isCorrectlyState(MusicData.class, getCount())) {
					ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
					service.setArrayPlayback(list);
				} 
				service.play(data);
//				((MainActivity) getContext()).showPlayerElement(true);
				break;
			}
		}

		@Override
		public boolean onLongClick(View view) {
			if (view.getId() == cover.getId() || view.getId() == info.getId()) {
				PopupMenu menu = new PopupMenu(getContext(), view);
				menu.getMenuInflater().inflate(R.menu.deletemenu, menu.getMenu());
				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						remove(data);
						service.remove(data);
						data.reset(getContext());
						if (isEmpty()) {
//							((MainActivity) getContext()).showPlayerElement(false);
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

	@Override
	protected boolean isSetListener() {
		return false;
	}

	@Override
	protected int getDefaultCover() {
		return R.drawable.no_cover_art_big;
	}
}
