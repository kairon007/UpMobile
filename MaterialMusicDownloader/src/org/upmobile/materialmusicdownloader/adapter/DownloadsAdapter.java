package org.upmobile.materialmusicdownloader.adapter;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.adapter.BaseDownloadsAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

public class DownloadsAdapter extends BaseDownloadsAdapter implements UndoAdapter {

	private boolean isCanNotify = true;
	
	private class DownloadsViewHolder extends BaseDownloadsViewHolder {


		public DownloadsViewHolder(View v) {
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
			image = (ImageView) v.findViewById(R.id.item_image);
		}
	}

	public DownloadsAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new DownloadsViewHolder(v);
	}

	@Override
	public void removeItem(MusicData item) {
		super.removeItem(item);
		if (item.getId() == -1)	return;
		DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
		try {
			manager.remove(item.getId());
		} catch (UnsupportedOperationException e) {
			android.util.Log.d(getClass().getSimpleName(), e + "");
		}
	}

	@Override
	protected boolean isSetListener() {
		return false;
	}

	@Override
	protected int getDefaultCover() {
		return 0;
	}
	@Override
	protected Bitmap getDefaultBitmap() {
		return ((MainActivity) getContext()).getDeafultBitmapCover(64, 62, 60);
	}
	
	@Override
	public View getUndoView(int paramInt, View paramView, ViewGroup paramViewGroup) {
		View view = paramView;
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_undo_view, paramViewGroup, false);
		}
		return view;
	}

	@Override
	public View getUndoClickView(View paramView) {
		return paramView.findViewById(R.id.undo_button);
	}
	
	@Override
	public void notifyDataSetChanged() {
		if (isCanNotify) {
			super.notifyDataSetChanged();
		}
	}
	
	public void setCanNotify(boolean isCanNotify) {
		this.isCanNotify = isCanNotify;
	}
}
