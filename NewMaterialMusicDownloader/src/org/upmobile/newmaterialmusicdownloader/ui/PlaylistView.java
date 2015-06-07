package org.upmobile.newmaterialmusicdownloader.ui;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.adapter.PlaylistAdapter;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BasePlaylistsAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSwipeUndoAdapter;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.PlaylistData;
import ru.johnlife.lifetoolsmp3.ui.button.fab.FloatingActionButton;
import ru.johnlife.lifetoolsmp3.ui.button.fab.ScrollDirectionListener;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog.ButtonCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

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
				Util.hideKeyboard(getContext(), dialog.getCustomView());
				EditText input = (EditText) dialog.findViewById(android.R.id.edit);
				String newTitle = input.getText().toString().trim();
				if (newTitle.isEmpty()) {
					dialog.cancel();
					showMessage(getContext(), R.string.playlist_cannot_be_empty);
					return;
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
		builder = new MaterialDialog.Builder(getContext())
									.title(R.string.create_new_playlist)
									.customView(R.layout.md_input_dialog, false)
									.titleColorAttr(R.attr.colorPrimary)
									.positiveColorAttr(R.attr.colorPrimary)
									.callback(buttonCallback)
									.autoDismiss(false)
									.positiveText(R.string.create)
									.negativeText(android.R.string.cancel);
		dialog = builder.build();
		((EditText)dialog.getCustomView().findViewById(android.R.id.edit)).setHint(R.string.playlist_name);
		dialog.show();
	}

	@Override
	public void closeDialog() {
		if (null != dialog && dialog.isShowing()) {
			dialog.cancel();
		}
	}

	@Override
	protected Object[] groupItems() {
		int color = getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimary));
		Drawable arrowDown = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_18dp);
		Drawable arrowUp = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_18dp);
		return new Object[] { arrowDown, arrowUp, color };
	}

	@Override
	protected BasePlaylistsAdapter getAdapter(Context context) {
		return new PlaylistAdapter(context, R.layout.playlist_group_item);
	}
	
	@Override
	protected void animateListView(ListView listView, final BasePlaylistsAdapter adapter) {
		swipeUndoAdapter = new CustomSwipeUndoAdapter(adapter, getContext(), new OnDismissCallback() {
			
	        @Override
	        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
	        	for (int position : reverseSortedPositions) {
	        		AbstractSong data = (AbstractSong) adapter.getItem(position);
	        		if (data.getClass() == MusicData.class) {
	        			removeData(getViewByPosition((ListView) listView, position), getPlaylistBySong((MusicData) data), (MusicData) data);
	        		} else {
	        			removeData(getViewByPosition((ListView) listView, position), (PlaylistData) data, null);
	        		}
	            	if (adapter.isEmpty()) {
	        			lView.setEmptyView(message);
	        		}
	            }
	        }
	    });
		swipeUndoAdapter.setAbsListView((DynamicListView)listView);
		((DynamicListView)listView).setAdapter(swipeUndoAdapter);
		((DynamicListView)listView).enableSimpleSwipeUndo();
	}
	
}
