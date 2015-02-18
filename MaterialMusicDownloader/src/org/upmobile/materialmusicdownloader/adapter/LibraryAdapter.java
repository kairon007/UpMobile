package org.upmobile.materialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.fragment.PlayerFragment;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class LibraryAdapter extends BaseLibraryAdapter implements UndoAdapter {
	
	private TextView textcover;

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
		private TextView button;

		public LibraryViewHolder(View v) {
			info = (ViewGroup) v.findViewById(R.id.item_box_info);
			button = (TextView) v.findViewById(R.id.item_play);
			cover = (ImageView) v.findViewById(R.id.item_cover);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_artist);
			duration = (TextView) v.findViewById(R.id.item_duration);
			textcover = (TextView) v.findViewById(R.id.item_cover_text);
		}

		@Override
		protected void hold(MusicData data, int position) {
			this.data = data;
			super.hold(data, position);
			if (data.check(MusicData.MODE_PLAYING)) {
				button.setText(getContext().getString(R.string.pause));
			} else {
				button.setText(getContext().getString(R.string.play));
			}
			setListener();
		}

		private void setListener() {
			info.setOnClickListener(this);
			cover.setOnClickListener(this);
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
	}

	@Override
	protected boolean isSetListener() {
		return false;
	}

	@Override
	protected int getDefaultCover() {
		textcover.setVisibility(View.VISIBLE);
		textcover.setText(getContext().getString(R.string.musics));
		return android.R.color.transparent;
	}
	
	public void deleteSong(MusicData song) {
		remove(song);
		service.remove(song);
		song.reset(getContext());
		if (isEmpty()) {
			((MainActivity) getContext()).showPlayerElement(false);
			TextView emptyMsg = (TextView) ((MainActivity) getContext()).findViewById(R.id.message_listview);
			emptyMsg.setVisibility(View.VISIBLE);
			emptyMsg.setText(R.string.library_empty);
		}
	}

	@Override
	public View getUndoView(int paramInt, View paramView, ViewGroup paramViewGroup) {
		View view = paramView;
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_undo_view, paramViewGroup, false);
		}
		return view;
	}

	@Override
	public View getUndoClickView(View paramView) {
		return paramView.findViewById(R.id.undo_button);
	}
}
