package org.upmobile.newmaterialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;

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
		return NewMaterialApp.getDirectoryPrefix();
	}

	@Override
	protected Bitmap getDefaultCover() {
		return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_album_grey);
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
		}

		@Override
		protected void hold(AbstractSong data, int position) {
			super.hold(data, position);
		}
		
	}

	@Override
	protected int getFirstLayout() {
		return R.layout.playlist_group_item;
	}

}