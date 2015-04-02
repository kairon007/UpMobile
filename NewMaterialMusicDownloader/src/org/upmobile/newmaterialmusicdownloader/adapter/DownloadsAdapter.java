package org.upmobile.newmaterialmusicdownloader.adapter;

import org.upmobile.newmaterialmusicdownloader.R;

import ru.johnlife.lifetoolsmp3.adapter.BaseDownloadsAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.DownloadManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

public class DownloadsAdapter extends BaseDownloadsAdapter implements UndoAdapter {

	private boolean isCanNotify = true;
	
	private class DownloadsViewHolder extends BaseDownloadsViewHolder {


		public DownloadsViewHolder(View v) {
			title = (TextView) v.findViewById(R.id.artistLine);
			artist = (TextView) v.findViewById(R.id.titleLine);
			progress = (ProgressBar) v.findViewById(R.id.item_progress);
			image = (ImageView) v.findViewById(R.id.cover);
			threeDot = (ImageView) v.findViewById(R.id.threeDot);
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
	protected int getDefaultCover() {
		return R.drawable.ic_album_grey;
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
	
	@Override
	protected void showMenu(final View view, final MusicData item) {
		PopupMenu menu = new PopupMenu(getContext(), view);
		menu.getMenuInflater().inflate(R.menu.downloads_menu, menu.getMenu());
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem paramMenuItem) {
				if (paramMenuItem.getItemId() == R.id.downloads_menu_cancel) {
					removeItem(item);
				}
				return false;
			}
		});
		menu.show();
	}
}
