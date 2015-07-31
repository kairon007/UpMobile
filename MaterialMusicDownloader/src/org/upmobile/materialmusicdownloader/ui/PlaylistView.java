package org.upmobile.materialmusicdownloader.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.adapter.PlaylistAdapter;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import java.util.ArrayList;
import java.util.HashSet;

import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BasePlaylistView;
import ru.johnlife.lifetoolsmp3.utils.Util;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.DialogAction;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog.ButtonCallback;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.Theme;

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
				for (AbstractSong data : getAllItems()) {
					if (data.getClass() == PlaylistData.class && ((PlaylistData) data).getName().replace(getDirectory(), "").equals(newTitle)) {
						showMessage(getContext(), R.string.playlist_already_exists);
						return;
					}
				}
				PlaylistView.this.createPlaylist(getContext().getContentResolver(), newTitle);
				Util.hideKeyboard(getContext(), input);
				collapseSearchView();
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
	protected void collapseSearchView () {
		((MainActivity) getContext()).getSearchView().onActionViewCollapsed();
	}

	@Override
	protected Bitmap getDefaultCover() {
		String cover =  getContext().getResources().getString(org.upmobile.materialmusicdownloader.R.string.font_musics);
		return ((MainActivity) getContext()).getDefaultBitmapCover(Util.dpToPx(getContext(), 64),
				Util.dpToPx(getContext(),62), Util.dpToPx(getContext(),60), cover);
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
		.cancelable(false)
		.positiveText(R.string.create)
		.negativeText(android.R.string.cancel);
		dialog = builder.build();
		dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
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
				if (s.toString().trim().isEmpty()) {
					dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
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
		try {
			swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {

				@Override
				public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, HashSet<Object> removed) {
					for (Object data : removed) {
						try {
							if (null == data || !swipeUndoAdapter.getSongs().contains(data)) return;
							if (data.getClass() == MusicData.class) {
								removeData(getPlaylistBySong((MusicData) data), (MusicData) data);
							} else {
								removeData((PlaylistData) data, null);
							}
							swipeUndoAdapter.getSongs().remove(data);
						} catch (Exception e) {
							Log.e(getClass().getSimpleName(), e + "");
						}
					}
					if (adapter.isEmpty()) {
						lView.setEmptyView(message);
					}
				}
			});
			swipeUndoAdapter.setAbsListView(listView);
			listView.setAdapter(swipeUndoAdapter);
			((DynamicListView) listView).enableSimpleSwipeUndo();
		} catch (Throwable e) {
			Log.d(getClass().getSimpleName(), "Exception: " + e);
		}
	}
	
	public void forceDelete () {
		swipeUndoAdapter.forceDelete();
	}
	
}
