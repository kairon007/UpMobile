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

	private class LibraryViewHolder extends BaseLibraryViewHolder{

		private ViewGroup info;

		public LibraryViewHolder(View v) {
			info = (ViewGroup) v.findViewById(R.id.boxInfoItem);
			cover = (ImageView) v.findViewById(R.id.cover);
			title = (TextView) v.findViewById(R.id.artistLine);
			artist = (TextView) v.findViewById(R.id.titleLine);
			duration = (TextView) v.findViewById(R.id.chunkTime);
			threeDot = v.findViewById(R.id.threeDot);
		}

		@Override
		protected void hold(final MusicData data, int position) {
			super.hold(data, position);
			info.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!service.isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						service.setArrayPlayback(list);
					}
					if (service.isPrepared() && service.getPlayingSong().equals(data)) {
						return;
					}
					((MainActivity) getContext()).showPlayerElement(true);
					((MainActivity)getContext()).startSong(data);
				}
			});
		}

	}

	@Override
	protected int getDefaultCover() {
		return R.drawable.no_cover_art_light_big_dark;
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