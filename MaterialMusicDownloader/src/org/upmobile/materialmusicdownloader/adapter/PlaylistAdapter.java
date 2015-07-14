package org.upmobile.materialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

public class PlaylistAdapter extends BasePlaylistsAdapter implements UndoAdapter {

	public PlaylistAdapter(Context context, int resource) {
		super(context, resource);
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
		return MaterialMusicDownloaderApp.getDirectoryPrefix();
	}

	@Override
	protected Bitmap getDefaultCover() {
		String cover =  getContext().getResources().getString(R.string.font_musics);
		return ((MainActivity) getContext()).getDefaultBitmapCover(64, 62, 60,cover);
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
			duration = (TextView) v.findViewById(R.id.textDuration);
			groupTitle = (TextView) v.findViewById(R.id.textTitle);
			playAll = (TextView) v.findViewById(R.id.playAll);
			customGroupIndicator = (TextView) v.findViewById(R.id.customGroupIndicator);
		}

		@Override
		protected void hold(AbstractSong data, int position) {
			super.hold(data, position);
			if (data.getClass() == PlaylistData.class) {
				if (((PlaylistData) data).isExpanded()) {
					((TextView) customGroupIndicator).setText(getContext().getResources().getString(org.upmobile.materialmusicdownloader.R.string.font_arrow_up));
				} else {
					((TextView) customGroupIndicator).setText(getContext().getResources().getString(org.upmobile.materialmusicdownloader.R.string.font_arrow_down));
				}
			}
		}
		
	}

	@Override
	protected int getFirstLayout() {
		return R.layout.playlist_group_item;
	}

}
