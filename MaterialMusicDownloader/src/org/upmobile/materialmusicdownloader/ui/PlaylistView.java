package org.upmobile.materialmusicdownloader.ui;

import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.adapter.ExpandableAdapterWrapper;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.MaterialDialog.ButtonCallback;
import ru.johnlife.lifetoolsmp3.ui.widget.materialdialog.Theme;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.csform.android.uiapptemplate.R;

public class PlaylistView extends BasePlaylistView{
	
	private MaterialDialog.ButtonCallback buttonCallback;
	private MaterialDialog.Builder builder;
	private MaterialDialog dialog;

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
		return 0;
	}

	@Override
	protected void showPlayerFragment(MusicData data) {
		((MainActivity) getContext()).startSong(data);
	}

	@Override
	protected ListView getListView(View view) {
		return null;
	}

	@Override
	public TextView getMessageView(View view) {
		return (TextView) view.findViewById(R.id.emptyText);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void showDialog() {
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
	protected Object[] groupItems() {
		return new Object[]{getResources().getString(org.upmobile.materialmusicdownloader.R.string.font_arrow_down),getResources().getString(org.upmobile.materialmusicdownloader.R.string.font_arrow_up)};
	}

	@Override
	protected BaseExpandableListAdapter getAdapter(Context context) {
		return new ExpandableAdapterWrapper(context);
	}
}
