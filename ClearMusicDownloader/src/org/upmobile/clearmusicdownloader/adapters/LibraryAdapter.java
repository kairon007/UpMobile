package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;

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
		TextView cancel;
		UICircularImage image;

		private View view;

		public LibraryViewHolder(View v) {
			view = v;
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
			cancel = (TextView) v.findViewById(R.id.hidden_view);
		}

		@Override
		protected void hold(final MusicData item) {
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			image.setImageResource(R.drawable.fallback_cover);
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					view.findViewById(R.id.hidden_view).setVisibility(View.GONE);
					int startPosition = 0 - view.getContext().getResources().getDimensionPixelSize(R.dimen.swipe_size);
					((UISwipableList)view.getParent()).slideOutView(view.findViewById(R.id.front_layout), startPosition, false);
					remove(item);
				}
			});
		}
	}

}
