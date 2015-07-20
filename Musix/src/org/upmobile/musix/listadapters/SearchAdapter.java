package org.upmobile.musix.listadapters;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.upmobile.musix.Nulldroid_Settings;
import org.upmobile.musix.R;

import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;

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
		return new ProgressBar(getContext());
	}

	@Override
	protected void download(RemoteSong song, int position) {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		BaseDownloadSongTask downloadListener = new BaseDownloadSongTask(getContext(), song, id);
		downloadListener.setDownloadPath(Environment.DIRECTORY_MUSIC);
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
	}

	@Override
	protected ViewHolder<Song> createViewHolder(View view) {
		return new SearchViewHolder(view);
	}
	
	
	private class SearchViewHolder extends BaseSearchViewHolder  {
		

		public SearchViewHolder(View view) {
			info = (ViewGroup) view.findViewById(R.id.boxInfoItem);
			cover = (ImageView) view.findViewById(R.id.cover);
			title = (TextView) view.findViewById(R.id.titleLine);
			artist = (TextView) view.findViewById(R.id.artistLine);
			duration = (TextView) view.findViewById(R.id.chunkTime);
		}
		
		@Override
		protected void hold(Song item, int position) {
			cover.setImageResource(R.drawable.def_player_cover);
			super.hold(item, position);
		}
	}

}
