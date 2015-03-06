package org.upmobile.materialmusicdownloader.ui;

import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.csform.android.uiapptemplate.R;
import com.csform.android.uiapptemplate.view.dlg.MaterialDialog;
import com.csform.android.uiapptemplate.view.dlg.MaterialDialog.ButtonCallback;
import com.csform.android.uiapptemplate.view.dlg.Theme;

public class PlaylistView extends BasePlaylistView{
	
	private MaterialDialog.ButtonCallback buttonCallback;
	private EditText editText;

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
		buttonCallback = new ButtonCallback() {
			@Override
			public void onPositive(MaterialDialog dialog) {
				super.onPositive(dialog);
				PlaylistView.this.createPlaylist(getContext().getContentResolver(), editText.getText().toString());
				dialog.cancel();
			}
			@Override
			public void onNegative(MaterialDialog dialog) {
				super.onNegative(dialog);
				dialog.cancel();
			}
		};
	}

	@Override
	protected Bitmap getDeafultCover() {
		return null;
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
	protected TextView getMessageView(View view) {
		return null;
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void showDialog() {
		editText = new EditText(getContext());
		editText.setBackground(getResources().getDrawable(R.drawable.inverse_underline_edit_text));
		new MaterialDialog.Builder(getContext())
		.theme(Theme.LIGHT)
		.title(R.string.create_new_playlist)
		.backgroundColor(getResources().getColor(R.color.main_color_grey_100))
		.customView(editText, false)
		.dividerColorRes(R.color.md_divider_white)
		.titleColorRes(R.color.main_color_500)
		.neutralColorRes(R.color.material_indigo_500)
		.positiveColorRes(R.color.material_indigo_500)
		.negativeColorRes(R.color.material_red_500)
		.callback(buttonCallback)
		.autoDismiss(false)
		.positiveText(R.string.create)
		.negativeText(android.R.string.cancel)
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
}
