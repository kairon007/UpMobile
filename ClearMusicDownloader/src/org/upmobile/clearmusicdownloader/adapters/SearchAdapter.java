package org.upmobile.clearmusicdownloader.adapters;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.upmobile.clearmusicdownloader.Nulldroid_Settings;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;

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
		ImageView refreshProgress = new ImageView(getContext());
		refreshProgress.setImageResource(com.special.R.drawable.loader);
		refreshProgress.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
		return refreshProgress;
	}

	@Override
	protected void download(RemoteSong song, int position) {
		((MainActivity) getContext()).download(song);
	}

	@Override
	protected ViewHolder<Song> createViewHolder(View view) {
		return new SearchViewHolder(view);
	}

	private class SearchViewHolder extends BaseSearchViewHolder implements OnClickListener {

		public SearchViewHolder(View view) {
			info = (ViewGroup) view.findViewById(R.id.boxInfoItem);
			cover = (ImageView) view.findViewById(R.id.cover);
			title = (TextView) view.findViewById(R.id.titleLine);
			artist = (TextView) view.findViewById(R.id.artistLine);
			duration = (TextView) view.findViewById(R.id.chunkTime);
			threeDot = view.findViewById(R.id.threeDot);
			dowloadLabel = (TextView) view.findViewById(R.id.infoView);
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
			cover.setImageResource(R.drawable.def_cover_circle);
			super.hold(item, position);
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.threeDot:
				showMenu(view);
				break;
			}
		}
	}

}
