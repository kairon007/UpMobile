package org.upmobile.newmaterialmusicdownloader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class PlaylistAdapter extends BasePlaylistsAdapter implements UndoAdapter {

	private int color;
	private Drawable arrowDown;
	private Drawable arrowUp;

	public PlaylistAdapter(Context context, int resource) {
		super(context, resource);
		color = context.getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimary));
		arrowDown = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_18dp);
		arrowUp = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_18dp);
	}

	@NonNull
    @Override
	public View getUndoView(int paramInt, View paramView, @NonNull ViewGroup paramViewGroup) {
		View view = paramView;
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_undo_view, paramViewGroup, false);
		}
		return view;
	}

	@NonNull
    @Override
	public View getUndoClickView(@NonNull View paramView) {
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
			cover = (ImageView) v.findViewById(R.id.item_cover);
			duration = (TextView) v.findViewById(R.id.textDuration);
            groupTitle = (TextView) v.findViewById(R.id.textTitle);
			playAll = v.findViewById(R.id.playAll);
			customGroupIndicator = v.findViewById(R.id.customGroupIndicator);
		}

		@Override
		protected void hold(AbstractSong data, int position) {
			super.hold(data, position);
			if (data.getClass() == PlaylistData.class) {
				((ImageView) customGroupIndicator).setColorFilter(color);
                ((ImageView) customGroupIndicator).setImageDrawable(((PlaylistData) data).isExpanded() ? arrowUp : arrowDown);
			}
		}
		
	}

	@Override
	protected int getFirstLayout() {
		return R.layout.playlist_group_item;
	}

}
