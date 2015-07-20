package org.upmobile.materialmusicdownloader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog.ListCallback;

public class LibraryAdapter extends BaseLibraryAdapter implements UndoAdapter, Constants {

	private Bitmap defaultBitmap;

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	public LibraryAdapter(Context context, int resource, ArrayList<MusicData> array) {
		super(context, resource, array);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}

	private class LibraryViewHolder extends BaseLibraryViewHolder {

		private MusicData data;
		private View indicator;

		public LibraryViewHolder(View v) {
			cover = (ImageView) v.findViewById(R.id.cover);
			title = (TextView) v.findViewById(R.id.titleLine);
			artist = (TextView) v.findViewById(R.id.artistLine);
			duration = (TextView) v.findViewById(R.id.chunkTime);
			threeDot = v.findViewById(R.id.threeDot);
			indicator = v.findViewById(R.id.playingIndicator);
		}
		
		@Override
		protected void hold(MusicData md, int position) {
			data = md;
			boolean isPlaying = service.isPrepared() && md.getPath().equals(service.getPlayingSong().getPath());
			indicator.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
			super.hold(md, position);
		}

	}
	
	@Override
	protected Bitmap getDefaultCover() {
		if (null == defaultBitmap) {
			String cover = getContext().getResources().getString(R.string.font_musics);
			defaultBitmap = ((MainActivity) getContext()).getDefaultBitmapCover(64, 62, 60, cover);
		}
		return defaultBitmap;
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
	protected boolean showDeleteItemMenu() {
		return false;
	}

	@Override
	protected String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectoryPrefix();
	}
	
	@Override
	protected void showPlaylistsDialog(final ArrayList<PlaylistData> playlistDatas, final View v, String[] data) {
		ArrayList<Integer> selectedPositions = new ArrayList<>();
		for (int i=0;i <  playlistDatas.size(); i++) {
			if (contains(playlistDatas.get(i), ((MusicData) v.getTag()))) {
				selectedPositions.add(i);
			}
		}
		new MaterialDialog.Builder(getContext())
		.theme(ru.johnlife.uilibrary.widget.dialogs.materialdialog.Theme.LIGHT)
		.title(R.string.select_playlist)
		.backgroundColor(getContext().getResources().getColor(R.color.main_color_grey_100))
		.dividerColorRes(R.color.md_divider_white)
		.titleColorRes(R.color.main_color_500)
		.neutralColorRes(R.color.material_indigo_500)
		.positiveColorRes(R.color.material_indigo_500)
		.negativeColorRes(R.color.material_red_500)
		.itemColor(getContext().getResources().getColor(R.color.main_color_500))
		.items(data)
		.itemsCallback(new ListCallback() {
			
			@Override
			public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
				boolean playlistIsPlaying = playlistDatas.get(which).getSongs().equals(PlaybackService.get(getContext()).getArrayPlayback());
				playlistDatas.get(which).addToPlaylist(getContext(), ((MusicData) v.getTag()).getId(), playlistDatas.get(which).getId());
				if (playlistIsPlaying) {
					ArrayList<MusicData> array = playlistDatas.get(which).getSongsFromPlaylist(getContext(), playlistDatas.get(which).getId());
					PlaybackService.get(getContext()).addArrayPlayback(array.get(array.size() - 1));
				}
				dialog.cancel();
			}
		})
		.setSelectedItems(selectedPositions)
		.autoDismiss(false)
		.build()
		.show();
	}
	
	@Override
	public void showMessage(Context context, int message) {
		showMessage(context, context.getResources().getString(message));
	}
	
	@Override
	public void showMessage(Context context, String message) {
		((MainActivity) context).showMessage(message);
	}

	@Override
	protected void startSong(AbstractSong abstractSong) {
		((MainActivity) getContext()).startSong(abstractSong);
	}
}
