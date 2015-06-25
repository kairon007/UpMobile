package org.upmobile.musix.listadapters;

import org.upmobile.musix.Nulldroid_Settings;
import org.upmobile.musix.R;

import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import android.content.Context;
import android.os.Environment;
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
		DownloadClickListener downloadListener = new DownloadClickListener(getContext(), song, id);
		downloadListener.setDownloadPath(Environment.DIRECTORY_MUSIC);
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
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
			cover.setImageResource(R.drawable.def_player_cover);
			super.hold(item, position);
		}

		@Override
		public void onClick(View view) {
			switch(view.getId()) {
			case R.id.btnDownload:
				download((RemoteSong) item, getPosition(item));
				break;
			}
		}
		
	}

}
