package org.upmobile.newmaterialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog;
import android.content.Context;
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
			setListener();
			if (data.equals(StateKeeper.getInstance().getPlayingSong())) {
				StateKeeper.getInstance().setPlayingSong(data);
				data.getSpecial().setChecked(Boolean.TRUE);
			} else if (null != StateKeeper.getInstance().getPlayingSong() && data.getPath().equals(StateKeeper.getInstance().getPlayingSong().getPath())) {
				StateKeeper.getInstance().setPlayingSong(data);
				data.getSpecial().setChecked(Boolean.TRUE);
			}
			info.findViewById(R.id.playingIndicator).setVisibility(data.getSpecial().getIsChecked() ? View.VISIBLE : View.GONE);
			super.hold(data, position);
		}
		
		private void setListener() {
			info.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.boxInfoItem:
				Util.hideKeyboard(getContext(), view);
				if (!service.isCorrectlyState(MusicData.class, getCount())) {
					ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
					service.setArrayPlayback(list);
				}
				if (service.isPrepared() && service.getPlayingSong().equals(data)) return;
				StateKeeper.getInstance().setPlayingSong(data);
				data.getSpecial().setChecked(Boolean.TRUE);
				notifyDataSetChanged();
				MainActivity activity = (MainActivity) getContext();
				activity.showPlayerElement(Boolean.TRUE);
				activity.startSong(data);
				break;
			}
			
		}
	}
	
	@Override
	protected int getDefaultCover() {
		return R.drawable.ic_album_grey;
	}
	
	@Override
	public View getUndoView(int paramInt, View paramView, ViewGroup paramViewGroup) {
		View view = paramView;
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_undo_view, paramViewGroup, Boolean.FALSE);
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
		MainActivity activity = (MainActivity) getContext();
		activity.showPlayerElement(Boolean.TRUE);
		activity.startSong(abstractSong);
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
					addToPlaylist(getContext().getContentResolver(), ((MusicData) v.getTag()).getId(), playlistDatas.get(which).getId());
					dialog.cancel();
				}
			})
			.positiveText(R.string.add_to_playlist)
			.show();
	}
	
}
