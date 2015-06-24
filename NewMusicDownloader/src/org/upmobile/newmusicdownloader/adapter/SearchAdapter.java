package org.upmobile.newmusicdownloader.adapter;

import org.upmobile.newmusicdownloader.DownloadListener;
import org.upmobile.newmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchAdapter extends BaseSearchAdapter {

	public SearchAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected BaseSettings getSettings() { return new Nulldroid_Settings(); }

	@Override
	protected Object initRefreshProgress() { return new ProgressBar(getContext()); }

	@Override
	protected void download(RemoteSong song, int position) {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		DownloadListener downloadListener = new DownloadListener(getContext(), song, id);
		downloadListener.setDownloadPath(NewMusicDownloaderApp.getDirectory());
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
			indicator = (ImageView) info.findViewById(R.id.playingIndicator);
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
			setDownloadLable(lableStatus);
			cover.setImageResource(R.drawable.no_cover_art_light_big_dark);
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
