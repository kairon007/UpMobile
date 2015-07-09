package org.upmobile.newmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaylistAdapter extends BasePlaylistsAdapter {

	public PlaylistAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	@Override
	public void playAll(PlaylistData item, Context context) {
		if (item.getSongs().isEmpty()) {
			((MainActivity) context).showMessage(R.string.playlist_is_empty);
			return;
		}
		PlaybackService player = PlaybackService.get(context);
		MusicApp.getSharedPreferences().edit().putLong(Constants.PREF_LAST_PLAYLIST_ID, item.getId()).commit();
		player.setArrayPlayback(new ArrayList<AbstractSong>(item.getSongs()));
		((BaseMiniPlayerActivity) context).startSong((item.getSongs().get(0)));
	}

	@Override
	protected String getDirectory() {
		return NewMusicDownloaderApp.getDirectoryPrefix();
	}

	@Override
	protected Bitmap getDefaultCover() {
		return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.no_cover_art_light_big_dark);
	}
	
	@Override
	protected int getSecondaryLayout() {
		return R.layout.playlist_list_item;
	}

	@Override
	protected ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter.ViewHolder<AbstractSong> createViewHolder(View v) {
		return new PlaylistViewHolder(v);
	}
	
	private class PlaylistViewHolder extends BasePlaylistViewHolder {
		
		public PlaylistViewHolder(View v) {
			title = (TextView) v.findViewById(R.id.textTitle);
			artist = (TextView) v.findViewById(R.id.textHint);
			cover = v.findViewById(R.id.item_cover);
			duaration = (TextView) v.findViewById(R.id.textDuration);
			groupTitle = (TextView) v.findViewById(R.id.textTitle);
			playAll = (View) v.findViewById(R.id.playAll);
			customGroupIndicator = (ImageView) v.findViewById(R.id.customGroupIndicator);
		}

		@Override
		protected void hold(AbstractSong data, int position) {
			super.hold(data, position);
			if (data.getClass() == PlaylistData.class) {
				if (((PlaylistData) data).isExpanded()) {
					((ImageView) customGroupIndicator).setImageDrawable(getContext().getResources().getDrawable(
							Util.getResIdFromAttribute((Activity) getContext(), R.attr.icKeyboardArrowUp)));
				} else {
					((ImageView) customGroupIndicator).setImageDrawable(getContext().getResources().getDrawable(
							Util.getResIdFromAttribute((Activity) getContext(), R.attr.icKeyboardArrowDown)));
				}
			}
		}
	}

	@Override
	protected int getFirstLayout() {
		return R.layout.playlist_group_item;
	}

}
