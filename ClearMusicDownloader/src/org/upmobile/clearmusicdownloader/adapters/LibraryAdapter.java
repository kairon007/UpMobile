package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;
import com.special.utils.UISwipableList.OnSwipableListener;

public class LibraryAdapter extends BaseAdapter<MusicData> {

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v, int position) {
		return new LibraryViewHolder(v, position);
	}
	
	private class LibraryViewHolder extends ViewHolder<MusicData> {
		
		private View view;
		private ImageView button;
		private TextView title;
		private TextView artist;
		private TextView duration;
		private TextView cancel;
		private UICircularImage image;

		public LibraryViewHolder(View v, int position) {
			view = v;
			((UISwipableList) parent).setOnSwipableListener(new OnSwipableListener() {

				@Override
				public void onSwipeVisible(int pos) {
					getItem(pos).turnOn(MusicData.MODE_VISIBLITY);
				}

				@Override
				public void onSwipeGone(int pos) {
					getItem(pos).turnOff(MusicData.MODE_VISIBLITY);
				}
			});
			button = (ImageView) v.findViewById(R.id.item_play);
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
			if (!item.check(MusicData.MODE_VISIBLITY) && cancel.getVisibility() == View.VISIBLE) {
				cancel.setVisibility(View.GONE);
				ViewGroup box = (ViewGroup) view.findViewById(R.id.front_layout);
				box.setX(0);
			} else if (item.check(MusicData.MODE_VISIBLITY) && cancel.getVisibility() == View.GONE){
				ViewGroup box = (ViewGroup) view.findViewById(R.id.front_layout);
				int startPosition = 0 - parent.getContext().getResources().getDimensionPixelSize(R.dimen.swipe_size);
				box.setX(startPosition);
				cancel.setVisibility(View.VISIBLE);
			}
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			image.setImageResource(R.drawable.fallback_cover);
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					item.reset(v.getContext()); it's template solution
					remove(item);
				}
			});
		}
	}
	
}
