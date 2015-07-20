package org.upmobile.clearmusicdownloader.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;
import com.special.utils.UICircularImage;

import org.upmobile.clearmusicdownloader.R;

import ru.johnlife.lifetoolsmp3.adapter.BaseDownloadsAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.utils.DownloadCache;

public class DownloadsAdapter extends BaseDownloadsAdapter implements UndoAdapter {

	private boolean isCanNotify = true;

	private class DownloadsViewHolder extends BaseDownloadsViewHolder {

		public DownloadsViewHolder(View v) {
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
		}
	}

	public DownloadsAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new DownloadsViewHolder(v);
	}
	
	public void removeItem(MusicData item) {
		if (null == item) return;
		DownloadCache.getInstanse().remove(item.getArtist(), item.getTitle());
		remove(item);
		if (item.getId() == -1) return;
		cancelDownload(item.getId());
	}
	
	@Override
	public void notifyDataSetChanged() {
		if (isCanNotify) {
			super.notifyDataSetChanged();
		}
	}

	private void cancelDownload(long id) {
		DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		try {
			manager.remove(id);
		} catch (UnsupportedOperationException e) {
			android.util.Log.d(getClass().getSimpleName(), e + "");
		}
	}

	@Override
	protected int getDefaultCover() {
		return R.drawable.def_cover_circle;
	}

	@Override
	public View getUndoClickView(View arg0) {
		return arg0.findViewById(R.id.undo_button);
	}

	@Override
	public View getUndoView(int arg0, View arg1, ViewGroup arg2) {
		View view = arg1;
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_undo_view, arg2, false);
		}
		return view;
	}
	
	public void setCanNotify(boolean isCanNotify) {
		this.isCanNotify = isCanNotify;
	}
}
