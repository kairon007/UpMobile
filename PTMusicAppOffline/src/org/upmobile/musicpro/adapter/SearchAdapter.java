package org.upmobile.musicpro.adapter;

import org.upmobile.musicpro.Nulldroid_Settings;
import org.upmobile.musicpro.R;

import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchAdapter extends BaseSearchAdapter {
	
	public SearchAdapter(Context context, int resource) {
		super(context, resource);
		System.out.println("!!! SearchAdapter");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parentView) {
		System.out.println("!!! getView listView="+listView);
		View view = super.getView(position, convertView, parentView);
		if (position % 2 == 0) {
			view.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.bg_list_selector));
		} else {
			view.setBackgroundColor(Color.TRANSPARENT);
		}
		return view;
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
	protected void download(RemoteSong song, int position) {}

	@Override
	protected ViewHolder<Song> createViewHolder(View view) {
		System.out.println("!!! createViewHolder listView="+listView);
		return new SearchViewHolder(view);
	}
	
	private class SearchViewHolder extends BaseSearchViewHolder implements OnClickListener {

		public SearchViewHolder(View view) {
			System.out.println("!!! listView="+listView);
			info = (ViewGroup) view.findViewById(R.id.boxInfoItem);
			cover = (ImageView) view.findViewById(R.id.cover);
			title = (TextView) view.findViewById(R.id.titleLine);
			artist = (TextView) view.findViewById(R.id.artistLine);
			duration = (TextView) view.findViewById(R.id.chunkTime);
			threeDot = view.findViewById(R.id.threeDot);
			info.setOnClickListener(this);			
		}
		
		@Override
		protected void hold(Song item, int position) {
			System.out.println("!!! hold listView="+listView);
			cover.setImageResource(R.drawable.ic_music_node_search);
			super.hold(item, position);
		}
		
		@Override
		public void onClick(View view) {
			System.out.println("!!! onClick listView="+listView);
			switch(view.getId()) {
			case R.id.boxInfoItem:
				listView.performItemClick(view, (int) view.getTag(), view.getId());
				break;
			}
		}
		
	}

}
