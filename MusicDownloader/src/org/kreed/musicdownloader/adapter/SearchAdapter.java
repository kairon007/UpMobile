package org.kreed.musicdownloader.adapter;

import org.kreed.musicdownloader.Nulldroid_Settings;
import org.kreed.musicdownloader.R;

import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.graphics.Bitmap;
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
		//TODO
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
			btnDownload = (ImageView) view.findViewById(R.id.btnDownload);
			btnDownload.setOnClickListener(this);
			info.setOnClickListener(this);
		}
		
		@Override
		protected void hold(Song item, int position) {
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
			case R.id.boxInfoItem:
				listView.performItemClick(view, (int) view.getTag(), view.getId());
				break;
			case R.id.btnDownload:
				int position = (int) info.getTag();
				download((RemoteSong) getItem(position), position);
				break;
			}
		}
	}
	
}
