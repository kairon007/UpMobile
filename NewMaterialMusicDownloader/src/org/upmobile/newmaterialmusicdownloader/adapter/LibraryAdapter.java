package org.upmobile.newmaterialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

public class LibraryAdapter extends BaseLibraryAdapter implements UndoAdapter, Constants {
	
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

	private class LibraryViewHolder extends BaseLibraryViewHolder{

		private MusicData data;
		private ImageView indicator;

		public LibraryViewHolder(View v) {
			cover = (ImageView) v.findViewById(R.id.cover);
			title = (TextView) v.findViewById(R.id.titleLine);
			artist = (TextView) v.findViewById(R.id.artistLine);
			duration = (TextView) v.findViewById(R.id.chunkTime);
			threeDot = v.findViewById(R.id.threeDot);
			indicator = (ImageView) v.findViewById(R.id.playingIndicator);
			indicator.setColorFilter(getContext().getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimary)));
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
		return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_album_grey);
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
		return NewMaterialApp.getDirectoryPrefix();
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
	
	@Override
	protected void showPlaylistsDialog(final ArrayList<PlaylistData> playlistDatas, final View v, String[] data) {
		int position = ((MainActivity) getContext()).getLastCheckPosition();
		if (position > data.length - 1) {
			position = 0;
		}
		ArrayList<Integer> selectedPositions = new ArrayList<>();
		for (int i=0;i <  playlistDatas.size(); i++) {
			if (contains(playlistDatas.get(i), ((MusicData) v.getTag()))) {
				selectedPositions.add(i);
			}
		}
		new MaterialDialog.Builder(getContext())
			.title(R.string.select_playlist)
			.titleColorAttr(R.attr.colorTextPrimary)
			.positiveColorAttr(R.attr.colorPrimary)
			.items(data)
			.setSelectedItems(selectedPositions)
			.itemsCallbackSingleChoice(position, new MaterialDialog.ListCallback() {
				@Override
				public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
					((MainActivity) getContext()).setLastCheckPosition(which);
					boolean playlistIsPlaying = playlistDatas.get(which).getSongs().equals(PlaybackService.get(getContext()).getArrayPlayback());
					playlistDatas.get(which).addToPlaylist(getContext(), ((MusicData) v.getTag()).getId(), playlistDatas.get(which).getId());
					if (playlistIsPlaying) {
						ArrayList<MusicData> array = playlistDatas.get(which).getSongsFromPlaylist(getContext(), playlistDatas.get(which).getId());
						PlaybackService.get(getContext()).addArrayPlayback(array.get(array.size() - 1));
					}
					dialog.cancel();
				}
			})
			.positiveText(R.string.add_to_playlist)
			.show();
	}
	
}
