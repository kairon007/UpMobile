package org.upmobile.clearmusicdownloader.adapters;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.special.utils.UICircularImage;
import com.special.utils.UISwipableList;
import com.special.utils.UISwipableList.OnSwipableListener;

public class LibraryAdapter extends BaseAdapter<MusicData> {
	
	private final Drawable BTN_PLAY;
	private final Drawable BTN_PAUSE;
	private int currentPlayPosition; 

	public LibraryAdapter(Context context, int resource) {
		super(context, resource);
		BTN_PAUSE = context.getResources().getDrawable(R.drawable.pause_white);
		BTN_PLAY = context.getResources().getDrawable(R.drawable.play_white);
	}

	@Override
	protected ViewHolder<MusicData> createViewHolder(View v) {
		return new LibraryViewHolder(v);
	}
	
	@Override
	protected void onItemSwipeVisible(int pos) {
		getItem(pos).turnOn(MusicData.MODE_VISIBLITY);
	}

	@Override
	protected void onItemSwipeGone(int pos) {
		getItem(pos).turnOff(MusicData.MODE_VISIBLITY);
	}
	
	private class LibraryViewHolder extends ViewHolder<MusicData> {
		
		private MusicData item;
		private View view;
		private View button;
		private TextView title;
		private TextView artist;
		private TextView duration;
		private TextView cancel;
		private UICircularImage image;
		private int onClickPosition;

		public LibraryViewHolder(View v) {
			view = v;
			button = (View) v.findViewById(R.id.item_play);
			title = (TextView) v.findViewById(R.id.item_title);
			artist = (TextView) v.findViewById(R.id.item_description);
			image = (UICircularImage) v.findViewById(R.id.item_image);
			duration = (TextView) v.findViewById(R.id.item_duration);
			cancel = (TextView) v.findViewById(R.id.hidden_view);
		}

		@Override
		protected void hold(MusicData item, int position) {
			this.item = item;
			onClickPosition = position;
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
			if (item.check(MusicData.MODE_PLAYING)) {
				setButtonBackground(BTN_PAUSE);
			} else {
				setButtonBackground(BTN_PLAY);
			}
			duration.setText(Util.getFormatedStrDuration(item.getDuration()));
			image.setImageResource(R.drawable.fallback_cover);
			setListener();
		}

		@SuppressLint("NewApi")
		private void setButtonBackground(Drawable drawable) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN){
				button.setBackgroundDrawable(drawable);
			} else {
				button.setBackground(drawable);
			}
		}

		private void setListener() {
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (item.check(MusicData.MODE_PLAYING)) {
						item.turnOff(MusicData.MODE_PLAYING);
						setButtonBackground(BTN_PLAY);
					} else {
						if (currentPlayPosition == onClickPosition) {
							item.turnOff(MusicData.MODE_PLAYING);
							setButtonBackground(BTN_PAUSE);
						} else {
							getItem(currentPlayPosition).turnOff(MusicData.MODE_PLAYING);
							notifyDataSetChanged();
						}
						currentPlayPosition = onClickPosition;
						item.turnOn(MusicData.MODE_PLAYING);
						setButtonBackground(BTN_PAUSE);
					}
				}
			});
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//<--it's template solution		item.reset(v.getContext()); 
					remove(item);
				}
			});
		}
	}
	
}
