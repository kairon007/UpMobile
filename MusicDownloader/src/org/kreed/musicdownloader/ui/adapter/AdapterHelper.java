package org.kreed.musicdownloader.ui.adapter;



import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.app.MusicDownloaderApp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class AdapterHelper {
	private final static Bitmap[] cache = new Bitmap[2];

	public static class ViewBuilder {
		private long id;
		private String title;
		private TextView titleLine;
		private TextView number;
		private TextView artistLine;
		private ImageView arrow;
		private ImageView cover;
		private View view;
		private View left;
		private TextView time;
		private TextView caption;
		private ImageView download;
		
		private ViewBuilder(View view) {
			view.setTag(this);
			this.view = view; 
			view.setLongClickable(true);
			time = (TextView)view.findViewById(R.id.time);
			titleLine = (TextView)view.findViewById(R.id.titleLine);
			titleLine.setTypeface(MusicDownloaderApp.FONT_LIGHT);
			artistLine = (TextView)view.findViewById(R.id.artistLine);
			artistLine.setTypeface(MusicDownloaderApp.FONT_REGULAR);
			number = (TextView)view.findViewById(R.id.number);
			number.setTypeface(MusicDownloaderApp.FONT_LIGHT);
			caption = (TextView)view.findViewById(R.id.caption);
			caption.setTypeface(MusicDownloaderApp.FONT_LIGHT);
			arrow = (ImageView)view.findViewById(R.id.arrow);
			cover = (ImageView)view.findViewById(R.id.cover);
			download = ((ImageView) view.findViewById(R.id.downloadIV));
			left = (View) number.getParent();
		}
		
		public ViewBuilder setExpandable(boolean value) {
			int idx = value ? 0 : 1;
			if (null == cache[idx]) {
				cache[idx] = BitmapFactory.decodeResource(view.getContext().getResources(), value ? R.drawable.arrow : R.drawable.threedot);
			}
			arrow.setImageBitmap(cache[idx]);
			return this;
		}
		
		public ViewBuilder setButtonVisible(boolean value) {
			arrow.setVisibility(value ? View.VISIBLE : View.GONE);
			return this;
		}

		public ViewBuilder setLongClickable(boolean value) {
			view.setLongClickable(value);
			return this;
		}

		public ViewBuilder setArrowClickListener(OnClickListener listener) {
			arrow.setOnClickListener(listener);
			return this;
		}
		
		public ViewBuilder setMainClickListener(OnClickListener listener) {
			view.setOnClickListener(listener);
			return this;
		}
		
		public ViewBuilder setId(long value) {
			this.id = value;
			return this;
		}
		
		public ViewBuilder setLine1(String value) {
			titleLine.setText(value);
			title = value;
			return this;
		}
		
		public ViewBuilder setLine2(String value) {
			artistLine.setText(value);
			setVisibility(artistLine, value);
			return this;
		}
		
		public ViewBuilder setTime(String value) {
				if (value == null) {
					time.setVisibility(View.GONE);
				} else {
					time.setText(value);
				}
			return this;
		}
		
		
		public ViewBuilder setNumber(String value, int stringArrayResourceId) {
			number.setText(value);
			setVisibility(number, value);
			if (0 != stringArrayResourceId && number.getVisibility() == View.VISIBLE) {
				try {
					caption.setVisibility(View.VISIBLE);
					String[] strings = caption.getContext().getResources().getStringArray(stringArrayResourceId);
					int i = Math.max(0, Math.min(Integer.valueOf(value).intValue(), strings.length-1));
					caption.setText(strings[i]);
				} catch (NumberFormatException e) {
					caption.setVisibility(View.GONE);
				}
			} else {
				caption.setVisibility(View.GONE);
			}
			determineLeftVisibility();
			return this;
		}
		
		public ViewBuilder setIcon(Bitmap value) {
			if (null == value) {
				cover.setVisibility(View.GONE);
			} else {
				cover.setVisibility(View.VISIBLE);
				cover.setImageBitmap(value);
			}
			determineLeftVisibility();
			return this;
		}
		
		public ViewBuilder setIcon(Drawable value) {
			if (null == value) {
				cover.setVisibility(View.GONE);
			} else {
				cover.setVisibility(View.VISIBLE);
				cover.setImageDrawable(value);
				cover.setScaleType(ScaleType.CENTER_INSIDE);
			}
			determineLeftVisibility();
			return this;
		}
		
		public ViewBuilder setIcon(int resourceId) {
			if (0 == resourceId) {
				cover.setVisibility(View.GONE);
			} else {
				cover.setVisibility(View.VISIBLE);
				cover.setImageResource(resourceId);
			}
			determineLeftVisibility();
			return this;
		}
		
		private void setVisibility(View view, String value) {
			boolean empty = value == null || "".equals(value);
			view.setVisibility(empty ? View.GONE : View.VISIBLE);
		}

		private void determineLeftVisibility() {
			left.setVisibility(
				number.getVisibility() == View.GONE &&
				cover.getVisibility() == View.GONE ?
					View.INVISIBLE : View.VISIBLE
			);
			
		}

		public long getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}
		
		public View build() {
			return view;
		}

		public ImageView getDownload() {
			return download;
		}
	}

	public static ViewBuilder getViewBuilder(View convertView, LayoutInflater inflater) {
		View target = convertView;
		ViewBuilder builder;
		if (null == target) {
			target = inflater.inflate(R.layout.library_row_expandable, null);
			builder = new ViewBuilder(target);
		} else {
			try {
				builder = (ViewBuilder) target.getTag();
			} catch (Exception e) {
				return getViewBuilder(null, inflater); //something wrong with the supplied view - create new one 
			}
		}
		return builder;
	}
 
}
