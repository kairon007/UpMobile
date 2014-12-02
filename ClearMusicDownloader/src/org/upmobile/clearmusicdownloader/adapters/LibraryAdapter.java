package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.special.utils.UICircularImage;

public class LibraryAdapter extends BaseAdapter<MusicData> {

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}

	private class LibraryViewHolder extends ViewHolder<MusicData> {
		TextView title;
		TextView artist;
		TextView duration;
		UICircularImage image;
		ItemOnClickListener itemClickListener;

		private View v;

		public LibraryViewHolder(View v) {
			this.v = v;
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
		}

		@Override
		protected void hold(MusicData item) {
			itemClickListener = new ItemOnClickListener();
			itemClickListener.setItem(item);
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			image.setImageResource(R.drawable.fallback_cover);
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
		}
		
		private class ItemOnClickListener extends ItemizedClickListener {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
			
		}
	}
}
