package org.upmobile.newmaterialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog;
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
			info = (ViewGroup) v.findViewById(R.id.boxInfoItem);
			cover = (ImageView) v.findViewById(R.id.cover);
			title = (TextView) v.findViewById(R.id.titleLine);
			artist = (TextView) v.findViewById(R.id.artistLine);
			duration = (TextView) v.findViewById(R.id.chunkTime);
			threeDot = v.findViewById(R.id.threeDot);
			indicator = (ImageView) info.findViewById(R.id.playingIndicator);
			indicator.setColorFilter(getContext().getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimary)));
		}

		@Override
		protected void hold(MusicData md, int position) {
			data = md;
			info.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Util.hideKeyboard(getContext(), v);
					if (!service.isCorrectlyState(MusicData.class, getCount())) {
						ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
						service.setArrayPlayback(list);
					}
					if (!service.isPrepared() || !data.getPath().equals(service.getPlayingSong().getPath())) {
						((MainActivity) getContext()).startSong(data);
					}
				}
			});
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
		new MaterialDialog.Builder(getContext())
			.title(R.string.select_playlist)
			.titleColorAttr(R.attr.colorTextPrimary)
			.positiveColorAttr(R.attr.colorPrimary)
			.items(data)
			.itemsCallbackSingleChoice(position, new MaterialDialog.ListCallback() {
				@Override
				public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
					((MainActivity) getContext()).setLastCheckPosition(which);
					if (contains(playlistDatas.get(which), ((MusicData) v.getTag()))) {
						showMessage(getContext(), R.string.song_in_the_playlist);
						dialog.cancel();
						return;
					}
					playlistDatas.get(which).addToPlaylist(getContext(), ((MusicData) v.getTag()).getId(), playlistDatas.get(which).getId());
					dialog.cancel();
				}
			})
			.positiveText(R.string.add_to_playlist)
			.show();
	}
	
}
