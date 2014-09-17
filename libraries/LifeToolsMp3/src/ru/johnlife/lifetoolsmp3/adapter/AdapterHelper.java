package ru.johnlife.lifetoolsmp3.adapter;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.renderscript.Sampler.Value;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class AdapterHelper {
	private final static Bitmap[] cache = new Bitmap[2];

	public static class ViewBuilder {
		private long id;
		private String title;
		private TextView artistLine;
		private TextView number;
		private TextView titleLine;
		private TextView chunkTime;
		private ImageView arrow;
		private ImageView cover;
		private View view;
		private View left;
		private TextView caption;
		
		private ViewBuilder(View view) {
			view.setTag(this);
			this.view = view; 
			view.setLongClickable(true);
			init(view);
			titleLine.setTypeface(MusicApp.FONT_REGULAR);
			artistLine.setTypeface(MusicApp.FONT_LIGHT);
			chunkTime.setTypeface(MusicApp.FONT_REGULAR);
			number.setTypeface(MusicApp.FONT_LIGHT);
			caption.setTypeface(MusicApp.FONT_LIGHT);
		}

		private void init(View view) {
			artistLine = (TextView)view.findViewById(R.id.artistLine);
			titleLine = (TextView)view.findViewById(R.id.titleLine);
			chunkTime = (TextView) view.findViewById(R.id.chunkTime);
			number = (TextView)view.findViewById(R.id.number);
			caption = (TextView)view.findViewById(R.id.caption);
			arrow = (ImageView)view.findViewById(R.id.arrow);
			cover = (ImageView)view.findViewById(R.id.cover);
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
		
		public ViewBuilder setLine1(String valueTitle, String valueTime) {
			setVisibility(chunkTime, valueTime);
			setVisibility(titleLine, valueTitle);
			artistLine.setText(valueTitle);
			title = valueTitle;
			return this;
		}
		
		public ViewBuilder setLine2(String value) {
			titleLine.setText(value);
			setVisibility(titleLine, value);
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
			/**
			 * switch - case - will not work for this case because of the restriction of this operator
			**/
		
		public ViewBuilder setIcon(Object value) {
			if (null == value || value.equals(0)) {
				cover.setVisibility(View.GONE);
			} else {
				cover.setVisibility(View.VISIBLE);
				if (value.getClass().equals(Integer.class)) {
					cover.setVisibility(View.VISIBLE);
					cover.setImageResource((int) value);
				} else if (value.getClass().equals(Drawable.class)) {
					cover.setVisibility(View.VISIBLE);
					cover.setImageDrawable((Drawable) value);
					cover.setScaleType(ScaleType.CENTER_INSIDE);
				} else if  (value.getClass().equals(Bitmap.class)) {
					cover.setVisibility(View.VISIBLE);
					cover.setImageBitmap((Bitmap) value);
				}
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
	}

	public static ViewBuilder getViewBuilder(View convertView, LayoutInflater inflater) {
		View target = convertView;
		ViewBuilder builder;
		if (null == target) {
			target = inflater.inflate(R.layout.row_online_search, null);
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
