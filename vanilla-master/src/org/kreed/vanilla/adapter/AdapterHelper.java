package org.kreed.vanilla.adapter;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
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
		private TextView artistLine;
		private TextView number;
		private TextView titleLine;
		private TextView chunkTime;
		private ImageView btnArrowDot;
		private ImageView cover;
		private View view;
		private View left;
		private TextView caption;
		private boolean fullAction = true;

		private ViewBuilder(View view, boolean isCustomView) {
			this.view = view;
			this.view.setTag(this);
			this.view.setLongClickable(true);
			init(view);
			if (!isCustomView) {
				titleLine.setTypeface(MusicApp.FONT_REGULAR);
				artistLine.setTypeface(MusicApp.FONT_LIGHT);
				chunkTime.setTypeface(MusicApp.FONT_REGULAR);
				number.setTypeface(MusicApp.FONT_LIGHT);
				caption.setTypeface(MusicApp.FONT_LIGHT);
			}
		}

		private void init(final View view) {
			artistLine = (TextView) view.findViewById(R.id.artistLine);
			titleLine = (TextView) view.findViewById(R.id.titleLine);
			chunkTime = (TextView) view.findViewById(R.id.chunkTime);
			number = (TextView) view.findViewById(R.id.number);
			caption = (TextView) view.findViewById(R.id.caption);
			btnArrowDot = (ImageView) view.findViewById(R.id.btnDownload);
			cover = (ImageView) view.findViewById(R.id.cover);
			left = (View) number.getParent();
		}

		public ViewBuilder setExpandable(boolean value) {
			int idx = value ? 0 : 1;
			if (null == cache[idx]) {
				cache[idx] = BitmapFactory.decodeResource(view.getContext().getResources(), value ? R.drawable.arrow : R.drawable.threedot);
			}
			if (fullAction) {
				btnArrowDot.setImageBitmap(cache[idx]);
			}
			return this;
		}

		public ViewBuilder setButtonVisible(boolean value) {
			btnArrowDot.setVisibility(value ? View.VISIBLE : View.GONE);
			return this;
		}

		public ViewBuilder setLongClickable(boolean value) {
			view.setLongClickable(value);
			return this;
		}

		public ViewBuilder setArrowClickListener(OnClickListener listener) {
			btnArrowDot.setOnClickListener(listener);
			return this;
		}

		public ViewBuilder setMainClickListener(OnClickListener listener) {
			view.setOnClickListener(listener);
			setClickRedirect();
			return this;
		}

		private void setClickRedirect() {
			View v = view.findViewById(R.id.boxInfoItem);
			if (null != v) {
				v.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						view.performClick();
					}
				});
			}
		}

		public ViewBuilder setId(long value) {
			this.id = value;
			return this;
		}

		public ViewBuilder setLine1(String valueTitle, String valueTime) {
			artistLine.setText(valueTitle);
			setVisibility(titleLine, "");
			setVisibility(chunkTime, valueTime);
			if (null != valueTime) {
				fullAction = false;
				chunkTime.setText(valueTime);
			}
			return this;
		}

		public ViewBuilder setLine2(String value) {
			titleLine.setText(value);
			setVisibility(titleLine, value);
			titleLine.setText(value);
			return this;
		}

		public ViewBuilder setNumber(String value, int stringArrayResourceId) {
			number.setText(value);
			setVisibility(number, value);
			if (0 != stringArrayResourceId && number.getVisibility() == View.VISIBLE) {
				try {
					caption.setVisibility(View.VISIBLE);
					String[] strings = caption.getContext().getResources().getStringArray(stringArrayResourceId);
					int i = Math.max(0, Math.min(Integer.valueOf(value).intValue(), strings.length - 1));
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
		 * switch - case - will not work for this case because of the
		 * restriction of this operator
		 **/
		public ViewBuilder setIcon(Object obj) {
			if (null == obj || obj.equals(0)) {
				cover.setVisibility(View.GONE);
			} else {
				cover.setVisibility(View.VISIBLE);
				if (obj.getClass().equals(Integer.class)) {
					cover.setVisibility(View.VISIBLE);
					cover.setImageResource((Integer) obj);
					cover.setContentDescription(view.getContext().getResources().getString(R.string.default_cover));
				} else if (obj.getClass().equals(Drawable.class)) {
					cover.setVisibility(View.VISIBLE);
					cover.setImageDrawable((Drawable) obj);
					cover.setScaleType(ScaleType.CENTER_INSIDE);
					cover.setContentDescription("");
				} else if (obj.getClass().equals(Bitmap.class)) {
					cover.setVisibility(View.VISIBLE);
					cover.setImageBitmap((Bitmap) obj);
					cover.setContentDescription("");
				}
			}
			determineLeftVisibility();
			return this;
		}

		private void setVisibility(View view, String value) {
			boolean empty = value == null || value.isEmpty() || "0:00".equals(value);
			view.setVisibility(empty ? View.GONE : View.VISIBLE);
		}

		private void determineLeftVisibility() {
			left.setVisibility(number.getVisibility() == View.GONE && cover.getVisibility() == View.GONE ? View.INVISIBLE : View.VISIBLE);
		}

		public long getId() {
			return id;
		}

		public String getArtist() {
			return artistLine.getText().toString();
		}

		public String getTitle() {
			return title;
		}

		public View build() {
			return view;
		}
	}

	public static ViewBuilder getViewBuilder(View convertView, LayoutInflater inflater, int isCutomView) {
		ViewBuilder builder;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.row_online_search, null);
			builder = new ViewBuilder(convertView, isCutomView > 0);
		} else {
			try {
				builder = (ViewBuilder) convertView.getTag();
			} catch (Exception e) { // something wrong with the supplied view - create new one
				return getViewBuilder(null, inflater, isCutomView);
			}
		}
		return builder;
	}
}
