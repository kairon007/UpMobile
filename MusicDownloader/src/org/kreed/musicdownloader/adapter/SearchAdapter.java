package org.kreed.musicdownloader.adapter;

import org.kreed.musicdownloader.Nulldroid_Settings;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.listeners.DownloadListener;
import org.kreed.musicdownloader.ui.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SearchAdapter extends BaseSearchAdapter {

	private DownloadListener downloadListener;

	public SearchAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Object initRefreshProgress() {
		return new ProgressBar(getContext());
	}

	@Override
	protected void download(final RemoteSong song, int position) {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		StringBuilder stringBuilder = new StringBuilder(song.getArtist().trim()).append(" - ").append(song.getTitle().trim());
		final String sb = Util.removeSpecialCharacters(stringBuilder.toString());
		boolean isCached = DownloadCache.getInstanse().contain(song.getArtist().trim(), song.getTitle().trim());
		String directory = Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX;
		int exist = Util.existFile(directory, sb);
		if (exist != 0 && !isCached) {
			Toast.makeText(getContext(), R.string.track_exist, Toast.LENGTH_SHORT).show();
		} else if (exist == 0 && !isCached) {
			downloadListener = new DownloadListener(getContext(), song, id);
			if (downloadListener.isBadInet()) return;
			song.setDownloaderListener(downloadListener.notifyStartDownload(id));
			song.getDownloadUrl(new DownloadUrlListener() {

				@Override
				public void success(String url) {
					song.setDownloadUrl(url);
					downloadListener.setUseAlbumCover(true);
					downloadListener.onClick(null);
				}

				@Override
				public void error(String error) {

				}
			});
		}
	}
	
	@Override
	protected ViewHolder<Song> createViewHolder(View view) {
		return new SearchViewHolder(view);
	}
	
	private class SearchViewHolder extends BaseSearchViewHolder implements OnClickListener  {
		
		private Song item;

		public SearchViewHolder(View view) {
			info = (ViewGroup) view.findViewById(R.id.boxInfoItem);
			cover = (ImageView) view.findViewById(R.id.cover);
			title = (TextView) view.findViewById(R.id.titleLine);
			artist = (TextView) view.findViewById(R.id.artistLine);
			duration = (TextView) view.findViewById(R.id.chunkTime);
			btnDownload = (ImageView) view.findViewById(R.id.btnDownload);
			btnDownload.setOnClickListener(this);
		}
		
		@Override
		protected void hold(Song item, int position) {
			this.item = item;
			//TODO white theme / black theme
			cover.setImageResource(R.drawable.fallback_cover);
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
			super.hold(item, position);
		}

		@Override
		public void onClick(View view) {
			switch(view.getId()) {
			case R.id.btnDownload:
				download((RemoteSong)item, getPosition(item));
				break;
			}
		}
	}
	
}
