package ru.johnlife.lifetoolsmp3.adapter;

import java.io.File;
import java.util.Vector;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
		private ImageView btnDownload;
		private ImageView cover;
		private View view;
		private View left;
		private TextView caption;
		private boolean fullAction = true;
		private AsyncTask<Void, Void, Bitmap> loadCoverTask;
		
		private ViewBuilder(View view, boolean whiteTheme) {
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
			btnDownload = (ImageView)view.findViewById(R.id.btnDownload);
			cover = (ImageView)view.findViewById(R.id.cover);
			left = (View) number.getParent();
		}
		
		public ViewBuilder setExpandable(boolean value) {
			int idx = value ? 0 : 1;
			if (null == cache[idx]) {
				cache[idx] = BitmapFactory.decodeResource(view.getContext().getResources(), value ? R.drawable.arrow : R.drawable.threedot);
			}
			if (fullAction) {
				btnDownload.setImageBitmap(cache[idx]);
			}
			return this;
		}
		
		public ViewBuilder setButtonVisible(boolean value) {
			btnDownload.setVisibility(value ? View.VISIBLE : View.GONE);
			return this;
		}

		public ViewBuilder setLongClickable(boolean value) {
			view.setLongClickable(value);
			return this;
		}

		public ViewBuilder setArrowClickListener(OnClickListener listener) {
			btnDownload.setOnClickListener(listener);
			return this;
		}
		
		/**
		 * @param listener
		 * @return
		 */
		public ViewBuilder setMainClickListener(OnClickListener listener) {
			view.setOnClickListener(listener);
			setClickRedirect();
			return this;
		}
		
		public void setClickRedirect() {
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
					cover.setImageResource((Integer) value);
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
		
		public void startLoadCover(final int maxWidth, final Activity activity, final File file) {
			if (null != loadCoverTask) {
				loadCoverTask.cancel(true);
			}
			loadCoverTask = new AsyncTask<Void, Void, Bitmap> () {
				
				@Override
				protected Bitmap doInBackground(Void... params) {
					Resources res = activity.getResources();
					try {
						MusicMetadataSet src_set = new MyID3().read(file);
						MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
						Vector<ImageData> pictureList = metadata.getPictureList();
						if ((pictureList == null) || (pictureList.size() == 0)) {
							return  BitmapFactory.decodeResource(res, R.drawable.fallback_cover);
						}
						ImageData imageData = (ImageData) pictureList.get(0);
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inJustDecodeBounds = true;
						int scale = 1;
						if ((maxWidth != -1) && (opts.outWidth > maxWidth)) {
							int scaleWidth = opts.outWidth;
							while (scaleWidth > maxWidth) {
								scaleWidth /= 2;
								scale *= 2;
							}
						}
						opts = new BitmapFactory.Options();
						opts.inPurgeable = true;
						opts.inSampleSize = scale;
						Bitmap bitmap = BitmapFactory.decodeByteArray(imageData.imageData, 0, imageData.imageData.length, opts);
						return bitmap;
					}
					catch (Exception e) {
					}
					return BitmapFactory.decodeResource(res, R.drawable.fallback_cover);
				}
				
				@Override
				protected void onPostExecute(Bitmap result) {
					setIcon(result);
				}
			}.execute();
		}
		
		private void setVisibility(View view, String value) {
			boolean empty = value == null || "".equals(value) || "0:00".equals(value);
			view.setVisibility(empty ? View.GONE : View.VISIBLE);
		}

		private void determineLeftVisibility() {
			left.setVisibility(number.getVisibility() == View.GONE && cover.getVisibility() == View.GONE ?
					View.INVISIBLE : View.VISIBLE);
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

	public static ViewBuilder getViewBuilder(View convertView, LayoutInflater inflater, boolean whiteTheme) {
		View target = convertView;
		ViewBuilder builder;
		if (null == target) {
			if (whiteTheme) {
				target = inflater.inflate(R.layout.row_online_search_white, null);
			} else {
				target = inflater.inflate(R.layout.row_online_search, null);
			}
			builder = new ViewBuilder(target, whiteTheme);
		} else {
			try {
				builder = (ViewBuilder) target.getTag();
			} catch (Exception e) {
				return getViewBuilder(null, inflater, whiteTheme); //something wrong with the supplied view - create new one 
			}
		}
		return builder;
	}
}
