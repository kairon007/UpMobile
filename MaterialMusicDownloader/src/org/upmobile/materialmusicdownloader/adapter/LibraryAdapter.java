package org.upmobile.materialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog.ListCallback;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.Theme;
import android.content.Context;
import android.graphics.Bitmap;
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

	private class LibraryViewHolder extends BaseLibraryViewHolder implements OnClickListener {

		private MusicData data;

		public LibraryViewHolder(View v) {
			info = (ViewGroup) v.findViewById(R.id.boxInfoItem);
			cover = (ImageView) v.findViewById(R.id.cover);
			title = (TextView) v.findViewById(R.id.titleLine);
			artist = (TextView) v.findViewById(R.id.artistLine);
			duration = (TextView) v.findViewById(R.id.chunkTime);
			threeDot = v.findViewById(R.id.threeDot);
		}

		@Override
		protected void hold(MusicData md, int position) {
			data = md;
			setListener();
			if (data.equals(StateKeeper.getInstance().getPlayingSong())) {
				StateKeeper.getInstance().setPlayingSong(data);
				data.getSpecial().setChecked(true);
			} else if (null != StateKeeper.getInstance().getPlayingSong() && data.getPath().equals(StateKeeper.getInstance().getPlayingSong().getPath())) {
				StateKeeper.getInstance().setPlayingSong(data);
				data.getSpecial().setChecked(true);
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
				if (!service.isCorrectlyState(MusicData.class, getCount())) {
					ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
					service.setArrayPlayback(list);
				}
				if (service.isPrepared() && service.getPlayingSong().equals(data)) return;
				StateKeeper.getInstance().setPlayingSong(data);
				data.getSpecial().setChecked(true);
				notifyDataSetChanged();
				MainActivity activity = ((MainActivity) getContext());
				activity.showPlayerElement(true);
				activity.startSong(data);
				activity.setSelectedItem(LIBRARY_FRAGMENT);
				break;
			}
		}
	}
	
	@Override
	protected Bitmap getDefaultBitmap() {
		String cover =  getContext().getResources().getString(R.string.font_musics);
		return ((MainActivity) getContext()).getDefaultBitmapCover(64, 62, 60,cover);
	}
	
	@Override
	protected int getDefaultCover() {
		return 0;
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
		return Boolean.FALSE;
	}

	@Override
	protected String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectory();
	}
	
	@Override
	protected void showPlaylistsDialog(final ArrayList<PlaylistData> playlistDatas, final View v, String[] data) {
		new MaterialDialog.Builder(getContext())
		.theme(Theme.LIGHT)
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
				addToPlaylist(getContext().getContentResolver(), ((MusicData) v.getTag()).getId(), playlistDatas.get(which).getId());
				dialog.cancel();
			}
		})
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
