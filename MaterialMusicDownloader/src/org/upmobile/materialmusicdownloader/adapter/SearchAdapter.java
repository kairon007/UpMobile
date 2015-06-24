package org.upmobile.materialmusicdownloader.adapter;

import org.upmobile.materialmusicdownloader.DownloadListener;
import org.upmobile.materialmusicdownloader.Nulldroid_Settings;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.csform.android.uiapptemplate.font.MusicTextView;

public class SearchAdapter extends BaseSearchAdapter {

	public SearchAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Object initRefreshProgress() {
		return LayoutInflater.from(getContext()).inflate(R.layout.progress, null);
	}

	@Override
	protected void download(RemoteSong song, int position) {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		DownloadListener downloadListener = new DownloadListener(getContext(), song, id);
		downloadListener.setDownloadPath(MaterialMusicDownloaderApp.getDirectory());
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
	}

	@Override
	protected ViewHolder<Song> createViewHolder(View view) {
		return new SearchViewHolder(view);
	}
	
	private class SearchViewHolder extends BaseSearchViewHolder implements OnClickListener  {
		
		public SearchViewHolder(View view) {
			info = (ViewGroup) view.findViewById(R.id.boxInfoItem);
			cover = (ImageView) view.findViewById(R.id.cover);
			title = (TextView) view.findViewById(R.id.titleLine);
			artist = (TextView) view.findViewById(R.id.artistLine);
			duration = (TextView) view.findViewById(R.id.chunkTime);
			threeDot = view.findViewById(R.id.threeDot);
			dowloadLabel = (TextView) view.findViewById(R.id.infoView);
			indicator = (MusicTextView) view.findViewById(R.id.playingIndicator);
			info.setOnClickListener(this);
			threeDot.setOnClickListener(this);
		}

		@Override
		protected void hold(Song item, int position) {
			String comment = item.getComment();
			int lableStatus = keeper.checkSongInfo(comment);
			if (lableStatus == StateKeeper.DOWNLOADED) {
				item.setPath(keeper.getSongPath(comment));
			}
			boolean hasPlayingSong = null != service && service.isEnqueueToStream() && (item.equals(service.getPlayingSong()) || 
					(null != item.getPath() && item.getPath().equals(service.getPlayingSong().getPath())));
			setDownloadLable(lableStatus);
			showPlayingIndicator(hasPlayingSong);
			String coverString =  context.getResources().getString(R.string.font_musics);
			cover.setImageBitmap(((MainActivity) getContext()).getDefaultBitmapCover(64, 62, 60, coverString));
			super.hold(item, position);
		}
		
		@Override
		public void onClick(View view) {
			switch(view.getId()) {
			case R.id.boxInfoItem:
				listView.performItemClick(view, (int) view.getTag(), view.getId());
				break;
			case R.id.threeDot:
				showMenu(view);
				break;
			}
		}
	}

}
