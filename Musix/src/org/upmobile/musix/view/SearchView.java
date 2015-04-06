package org.upmobile.musix.view;

import org.upmobile.musix.Nulldroid_Advertisement;
import org.upmobile.musix.Nulldroid_Settings;
import org.upmobile.musix.R;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.LayoutInflater;
import android.view.View;

public class SearchView extends OnlineSearchView {

	private ProgressDialog progressDialog;

	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Nulldroid_Advertisement getAdvertisment() {
		try {
			return Nulldroid_Advertisement.class.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected String getDirectory() {
		return EMPTY_DIRECTORY;
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		PlaybackService.get(context).stop();
	}

	@Override
	public void refreshLibrary() {
	}

	@Override
	public boolean isWhiteTheme(Context context) {
		return false;
	}

	@Override
	protected boolean showFullElement() {
		return true;
	}

	@Override
	public int defaultCover() {
		return R.drawable.def_player_cover;
	}

	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
	}

	@Override
	public boolean isUseDefaultSpinner() {
		return true;
	}

	@Override
	protected void showProgressDialog(View view, final RemoteSong downloadSong, int position) {
		StateKeeper.getInstance().openDialog(StateKeeper.PROGRESS_DIALOG);
		View dialoglayout = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
		progressDialog = new ProgressDialog(getContext());
		progressDialog.show();
		progressDialog.setContentView(dialoglayout);
		progressDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				downloadSong.cancelTasks();
				StateKeeper.getInstance().closeDialog(StateKeeper.PROGRESS_DIALOG);
			}
		});
	}

	@Override
	protected void dismissProgressDialog() {
		if (null != progressDialog && progressDialog.isShowing()) {
			progressDialog.cancel();
		}
	}

}
