package org.upmobile.newmaterialmusicdownloader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.adapter.PlaylistAdapter;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BasePlaylistView;
import ru.johnlife.lifetoolsmp3.utils.Util;
import ru.johnlife.uilibrary.widget.buttons.fab.FloatingActionButton;
import ru.johnlife.uilibrary.widget.buttons.fab.ScrollDirectionListener;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.DialogAction;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog.ButtonCallback;
import ru.johnlife.uilibrary.widget.textviews.FloatingEditText;

public class PlaylistView extends BasePlaylistView {

	private MaterialDialog.ButtonCallback buttonCallback;
	private MaterialDialog.Builder builder;
	private MaterialDialog dialog;
	private CustomSwipeUndoAdapter swipeUndoAdapter;
	private ListView lView;
	private TextView message;

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
		FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.floatingButton);
        fab.attachToListView(listView, new ScrollDirectionListener() {
            @Override
            public void onScrollDown() { }
            @Override
            public void onScrollUp() { }
        }, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }
        });
        fab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog();
			}
		});
		buttonCallback = new ButtonCallback() {
			@Override
			public void onPositive(MaterialDialog dialog) {
				super.onPositive(dialog);
				EditText input = (EditText) dialog.findViewById(android.R.id.edit);
				String newTitle = input.getText().toString().trim();
				for (AbstractSong data : getAllItems()) {
					if (data.getClass() == PlaylistData.class && 
							((PlaylistData) data).getName().replace(getDirectory(), "").equals(newTitle)) {
						showMessage(getContext(), R.string.playlist_already_exists);
						return;
					}
				}
				PlaylistView.this.createPlaylist(getContext().getContentResolver(), newTitle);
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
		return ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_album_grey)).getBitmap();
	}

	@Override
	protected String getDirectory() {
		return NewMaterialApp.getDirectoryPrefix();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.playlist_view;
	}

	@Override
	protected void showPlayerFragment(MusicData musicData) {
		((MainActivity) getContext()).startSong(musicData);
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

	@Override
	public void showMessage(Context context, int message) {
		showMessage(context, getResources().getString(message));
	}

	@Override
	public void showMessage(Context context, String message) {
		((MainActivity) context).showMessage(message);
	}
	
	@Override
	protected void showDialog() {
		final ArrayList<String> playlistNames = new ArrayList<>();
		for (AbstractSong abstractSong : getAllItems()) {
			if(abstractSong.getClass() == PlaylistData.class) {
				playlistNames.add(((PlaylistData) abstractSong).getName().replace(getDirectory(), ""));
			}
		}
		builder = new MaterialDialog.Builder(getContext())
									.title(R.string.create_new_playlist)
									.customView(R.layout.md_input_dialog, false)
									.titleColorAttr(R.attr.colorPrimary)
									.positiveColorAttr(R.attr.colorPrimary)
									.callback(buttonCallback)
									.autoDismiss(false)
									.cancelable(false)
									.positiveText(R.string.create)
									.negativeText(android.R.string.cancel);
		dialog = builder.build();
		dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
		final FloatingEditText editText = (FloatingEditText) dialog.getCustomView().findViewById(android.R.id.edit);
		editText.setHint(R.string.playlist_name);
		final int defColor = editText.getHighlightedColor();
		((FloatingEditText) dialog.getCustomView().findViewById(android.R.id.edit)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(playlistNames.contains(s.toString().trim())){
					((FloatingEditText) editText).setHighlightedColor(Color.RED);
					editText.setHint(R.string.playlist_already_exists);
					dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
				} else {
					((FloatingEditText) editText).setHighlightedColor(defColor);
					editText.setHint(R.string.playlist_name);
					dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
				}
				if (s.toString().trim().isEmpty()) {
					dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
				}
			}
		});
		dialog.show();
	}

	@Override
	public void closeDialog() {
		if (null != dialog && dialog.isShowing()) {
			dialog.cancel();
		}
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
				public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions, ArrayList<Object> removed) {
					for (int position : reverseSortedPositions) {
						try {
							AbstractSong data = (AbstractSong) adapter.getItem(position);
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
			swipeUndoAdapter.setAbsListView((DynamicListView) listView);
			((DynamicListView) listView).setAdapter(swipeUndoAdapter);
			((DynamicListView) listView).enableSimpleSwipeUndo();
		} catch (Throwable e) {
			Log.d(getClass().getSimpleName(), "Exception: " + e);
		}
	}

	@Override
	public void forceDelete() {
		swipeUndoAdapter.forceDelete();
	}
	
}
