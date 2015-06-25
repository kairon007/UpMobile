package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseSearchAdapter extends BaseAbstractAdapter<Song>  {
	
	protected Context context;
	protected StateKeeper keeper = StateKeeper.getInstance();

	private View refreshProgress;
	private FrameLayout footer;
	protected PlaybackService service;

	protected abstract BaseSettings getSettings();
	protected abstract Object initRefreshProgress();
	protected abstract void download(RemoteSong song, final int position);
	protected boolean showDownloadLabel() { return false; }
	protected boolean usePlayingIndicator() { return false; }
	protected int getAdapterBackground() { return 0; }
	
	public BaseSearchAdapter(final Context context, int resource) {
		super(context, resource);
		this.context = context;
		refreshProgress = (View) initRefreshProgress();
		footer = new FrameLayout(context);
		int footerHeight = Util.dpToPx(getContext(), 72);
		int progressSize = Util.dpToPx(getContext(), 48);
		footer.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, footerHeight));
		footer.addView(refreshProgress, new FrameLayout.LayoutParams(progressSize, progressSize, Gravity.CENTER));
		setVisibilityProgress(false);
		Thread thread = new Thread() {
		    @Override
		    public void run() {
				service = PlaybackService.get(context);
		    }
		};
		thread.start();
	}
	
	public void showMenu(final View view) {
		PopupMenu menu = new PopupMenu(getContext(), view);
		final int position = (int) view.getTag();
		menu.getMenuInflater().inflate(R.menu.search_menu, menu.getMenu());
		boolean isDownloaded = StateKeeper.NOT_DOWNLOAD != keeper.checkSongInfo(((RemoteSong) getItem(position)).getComment());
		if (isDownloaded) {
			menu.getMenu().getItem(1).setVisible(false);
		}
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem paramMenuItem) {
				final RemoteSong song = (RemoteSong) getItem(position);
				if (paramMenuItem.getItemId() == R.id.search_menu_play) {
					ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getAll());
					service.setArrayPlayback(list);
					((BaseMiniPlayerActivity) getContext()).startSong(song);
					notifyDataSetChanged();
				}
				if (paramMenuItem.getItemId() == R.id.search_menu_download) {
					((BaseMiniPlayerActivity) getContext()).hideDownloadButton(true);
					song.getDownloadUrl(new DownloadUrlListener() {

								@Override
								public void success(String url) {
									song.setDownloadUrl(url);
									download(song, position);
								}

								@Override
								public void error(final String error) {
									showMessage(context, R.string.error_getting_url_songs);
								}
							});
				}
				return false;
			}
		});
		menu.show();
	}
	
	public View getProgressView() { return footer; }
	
	public void setVisibilityProgress(boolean visible) {
		if (visible) {
			refreshProgress.setVisibility(View.VISIBLE);
			footer.setVisibility(View.VISIBLE);	
		} else {
			refreshProgress.setVisibility(View.GONE);
			footer.setVisibility(View.GONE);	
		}
	}
	
	public void showMessage(Context context, String message) {
		Toast.makeText(context, message ,Toast.LENGTH_SHORT).show();
	}
	
	public void showMessage(Context context, int message) {
		showMessage(context, context.getString(message));
	}
	
	protected abstract class BaseSearchViewHolder extends ViewHolder<Song>{

		protected ImageView cover;
		protected ImageView btnDownload;
		protected TextView title;
		protected TextView artist;
		protected TextView duration;
		protected TextView dowloadLabel;
		protected View indicator;
		protected View threeDot;
		protected ViewGroup info;

		@Override
		protected void hold(final Song item, int position) {
			title.setText(item.getTitle().replace("&#039;", "'"));
			artist.setText(item.getArtist().replace("&#039;", "'"));
			showDurationd(item.getDuration() > 0);
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			if (null != btnDownload) {
				btnDownload.setTag(position);
			}
			if (null != threeDot) {
				threeDot.setTag(position);
			}
			if (getSettings().getIsCoversEnabled(getContext()) && ((RemoteSong) item).isHasCoverFromSearch()) {
				((RemoteSong) item).getSmallCover(false, new OnBitmapReadyListener() {
							@Override
							public void onBitmapReady(Bitmap bmp) {
								if (null != bmp) {
									cover.setImageBitmap(bmp);
								}
							}
						});
			}
		}
		
		public void setDownloadLable(int isDownloaded) {
			if (null == dowloadLabel) return;
			if (isDownloaded == -1) {
				dowloadLabel.setVisibility(View.GONE);
				return;
			}
			dowloadLabel.setVisibility(View.VISIBLE);
			dowloadLabel.setTextColor(isDownloaded == 1 ? Color.RED : context.getResources().getColor(R.color.dark_green));
			dowloadLabel.setText(isDownloaded == 1 ? R.string.downloading : R.string.downloaded);
		}
		
		public void showPlayingIndicator(boolean show) {
			indicator.setVisibility(show ? View.VISIBLE : View.GONE);
		}
		
		public void showDurationd(boolean show) {
			duration.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
}
