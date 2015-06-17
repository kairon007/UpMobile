package org.upmobile.materialmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.adapter.PlaylistAdapter;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.DialogAction;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog.ButtonCallback;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.Theme;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.csform.android.uiapptemplate.R;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

public class PlaylistView extends BasePlaylistView{
	
	private MaterialDialog.ButtonCallback buttonCallback;
	private MaterialDialog.Builder builder;
	private MaterialDialog dialog;
	private ListView lView;
	private TextView message;
	private CustomSwipeUndoAdapter swipeUndoAdapter;

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
		buttonCallback = new ButtonCallback() {
			@Override
			public void onPositive(MaterialDialog dialog) {
				super.onPositive(dialog);
				EditText input = (EditText) dialog.findViewById(android.R.id.edit);
				String newTitle =  input.getText().toString().trim();
				if (newTitle.isEmpty()) {
					dialog.cancel();
					showMessage(getContext(), R.string.playlist_cannot_be_empty);
					return;
				}
				for (AbstractSong data : getAllItems()) {
					if (data.getClass() == PlaylistData.class && ((PlaylistData) data).getName().replace(getDirectory(), "").equals(newTitle)) {
						showMessage(getContext(), R.string.playlist_already_exists);
						return;
					}
				}
				PlaylistView.this.createPlaylist(getContext().getContentResolver(), input.getText().toString());
				Util.hideKeyboard(getContext(), input);
				dialog.cancel();
			}
			@Override
			public void onNegative(MaterialDialog dialog) {
				super.onNegative(dialog);
				Util.hideKeyboard(getContext(), dialog.getCustomView());
				dialog.cancel();
			}
		};
	}

	@Override
	protected Bitmap getDeafultCover() {
		String cover =  getContext().getResources().getString(org.upmobile.materialmusicdownloader.R.string.font_musics);
		return ((MainActivity) getContext()).getDefaultBitmapCover(64, 62, 60,cover);
	}

	@Override
	protected String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectoryPrefix();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.playlist_view;
	}

	@Override
	protected void showPlayerFragment(MusicData data) {
		((MainActivity) getContext()).startSong(data);
	}

	@Override
	protected ListView getListView(View view) {
		lView = (ListView) view.findViewById(R.id.list);
		return lView;
	}

	@Override
	public TextView getMessageView(View view) {
		message = (TextView) view.findViewById(R.id.emptyText);
		return message;
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void showDialog() {
		final ArrayList<String> playlistNames = new ArrayList<>();
		for (AbstractSong abstractSong : getAllItems()) {
			if(abstractSong.getClass() == PlaylistData.class) {
				playlistNames.add(((PlaylistData) abstractSong).getName().replace(getDirectory(), ""));
			}
		}
		builder = new MaterialDialog.Builder(getContext());
		builder.theme(Theme.LIGHT)
		.title(R.string.create_new_playlist)
		.backgroundColor(getResources().getColor(R.color.main_color_grey_100))
		.customView(R.layout.md_input_dialog, false)
		.dividerColorRes(R.color.md_divider_white)
		.titleColorRes(R.color.main_color_500)
		.neutralColorRes(R.color.material_indigo_500)
		.positiveColorRes(R.color.material_indigo_500)
		.negativeColorRes(R.color.material_red_500)
		.callback(buttonCallback)
		.autoDismiss(false)
		.positiveText(R.string.create)
		.negativeText(android.R.string.cancel);
		dialog = builder.build();
		dialog.show();
		EditText input = (EditText) dialog.findViewById(android.R.id.edit);
		final TextView errorView = (TextView) dialog.findViewById(org.upmobile.materialmusicdownloader.R.id.errorMessage);
		input.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(playlistNames.contains(s.toString().trim())){
					errorView.setText(R.string.playlist_already_exists);
					dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
				} else {
					errorView.setText("");
					dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
				}
			}
		});
	}
	
	@Override
	public void closeDialog() {
		if (null != dialog && dialog.isShowing()) {
			dialog.cancel();
		}
	}
	
	@Override
	public void showMessage(Context context, int message) {
		showMessage(context, getResources().getString(message));
	}
	
	@Override
	public void showMessage(Context context, String message) {
		((MainActivity) context).showMessage(message);
	}

	@Override
	protected BasePlaylistsAdapter getAdapter(Context context) {
		return new PlaylistAdapter(context, R.layout.playlist_group_item);
	}
	
	@Override
	protected void animateListView(ListView listView, final BasePlaylistsAdapter adapter) {
		swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {
			
	        @Override
	        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, ArrayList<Object> removed) {
	        	for (int position : reverseSortedPositions) {
	        		AbstractSong data = (AbstractSong) adapter.getItem(position);
	        		if (null == data) return;
	        		if (data.equals(swipeUndoAdapter.getSongs().get(position))) {
		        		if (data.getClass() == MusicData.class) {
		        			removeData(getPlaylistBySong((MusicData) data), (MusicData) data);
		        		} else {
		        			removeData((PlaylistData) data, null);
		        		}
	        		} else {
	        			data = swipeUndoAdapter.getSongs().get(position);
		        		if (data.getClass() == MusicData.class) {
		        			PlaylistData playlistBySong = getPlaylistBySong((MusicData) data);
							playlistBySong.removeFromPlaylist(getContext(), playlistBySong.getId(), data.getId());
		        		} else {
		        			((PlaylistData) data).deletePlaylist(getContext(), data.getId());
		        		}
	        			swipeUndoAdapter.getSongs().remove(position);
	        		}
	            }
	        	updatePlaylist();
            	if (adapter.isEmpty()) {
        			lView.setEmptyView(message);
        		}
	        }
	    });
		swipeUndoAdapter.setAbsListView((DynamicListView)listView);
		((DynamicListView)listView).setAdapter(swipeUndoAdapter);
		((DynamicListView)listView).enableSimpleSwipeUndo();
	}
	
	public void forceDelete () {
		swipeUndoAdapter.forceDelete();
	}
	
}
