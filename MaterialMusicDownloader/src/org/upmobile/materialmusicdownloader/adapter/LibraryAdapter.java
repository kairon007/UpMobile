package org.upmobile.materialmusicdownloader.adapter;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.csform.android.uiapptemplate.view.dlg.MaterialDialog;
import com.csform.android.uiapptemplate.view.dlg.MaterialDialog.ListCallback;
import com.csform.android.uiapptemplate.view.dlg.Theme;
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
		private ViewGroup info;

		public LibraryViewHolder(View v) {
			info = (ViewGroup) v.findViewById(R.id.boxInfoItem);
			cover = (ImageView) v.findViewById(R.id.cover);
			title = (TextView) v.findViewById(R.id.titleLine);
			artist = (TextView) v.findViewById(R.id.artistLine);
			duration = (TextView) v.findViewById(R.id.chunkTime);
			threeDot = v.findViewById(R.id.threeDot);
		}

		@Override
		protected void hold(MusicData data, int position) {
			this.data = data;
			super.hold(data, position);
			setListener();
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
				if (service.isPlaying() && service.getPlayingSong().equals(data)) return;
				((MainActivity) getContext()).showPlayerElement(true);
				((MainActivity) getContext()).startSong(data);
				((MainActivity)getContext()).setSelectedItem(LIBRARY_FRAGMENT);
				break;
			}
		}
	}

	@Override
	protected boolean isSetListener() {
		return false;
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
	
	public void deleteSong(MusicData song) {
		remove(song);
		service.remove(song);
		song.reset(getContext());
		if (isEmpty()) {
			((MainActivity) getContext()).showPlayerElement(false);
			TextView emptyMsg = (TextView) ((MainActivity) getContext()).findViewById(R.id.message_listview);
			emptyMsg.setVisibility(View.VISIBLE);
			emptyMsg.setText(R.string.library_empty);
		}
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
		showMessage(getContext(), getContext().getResources().getString(message));
	}
	
	@Override
	public void showMessage(Context context, String message) {
		((MainActivity) getContext()).showMessage(message);
	}

	@Override
	protected void startSong(AbstractSong abstractSong) {
		((MainActivity) getContext()).startSong(abstractSong);
	}
}
