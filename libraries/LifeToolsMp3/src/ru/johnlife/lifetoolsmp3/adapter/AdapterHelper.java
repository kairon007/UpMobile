package ru.johnlife.lifetoolsmp3.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.util.Vector;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class AdapterHelper {

	private final static Bitmap[] cache = new Bitmap[2];

	public static class ViewBuilder {
		private long id;
		private String title;
		private TextView artistLine;
		private TextView number;
		private TextView titleLine;
		private TextView chunkTime;
		private TextView dowloadLabel;
		private ImageView btnDownload;
		private ImageView cover;
		private View view, playingIndicator;
		private View left;
		private TextView caption;
		private boolean fullAction = true;
		private AsyncTask<Void, Void, Bitmap> loadCoverTask;
		private boolean useIndicator;
		
		private ViewBuilder(View view, boolean isCustomView, boolean useIndicator) {
			this.useIndicator = useIndicator;
			view.setTag(this);
			this.view = view;
			view.setLongClickable(true);
			init(view);
			if (!isCustomView) {
				titleLine.setTypeface(MusicApp.FONT_REGULAR);
				artistLine.setTypeface(MusicApp.FONT_LIGHT);
				chunkTime.setTypeface(MusicApp.FONT_REGULAR);
				number.setTypeface(MusicApp.FONT_LIGHT);
				caption.setTypeface(MusicApp.FONT_LIGHT);
			}
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
			dowloadLabel = (TextView) view.findViewById(R.id.infoView);
			if (!useIndicator) return;
			playingIndicator = view.findViewById(R.id.playingIndicator);
		}
		
		/**
		 * 
		 * @param isDownloaded
		 * 0 - The song is downloaded
		 * 1 - The song also downloaded
		 * -1 - The song before downloading
		 * 
		 */
		public ViewBuilder setDownloadLable(int isDownloaded) {
			if (null == dowloadLabel) {
				return this;
			}
			if (isDownloaded == -1) {
				dowloadLabel.setVisibility(View.GONE);
				return this;
			}
			dowloadLabel.setVisibility(View.VISIBLE);
			dowloadLabel.setTextColor(isDownloaded == 1 ? Color.RED : view.getResources().getColor(R.color.dark_green));
			dowloadLabel.setText(isDownloaded == 1 ? R.string.downloading : R.string.downloaded);
			return this;
		}
		
		public ViewBuilder setCustomColor(int color) {
			((ImageView) playingIndicator).setColorFilter(color);
			return this;
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
		
		public ViewBuilder showPlayingIndicator(boolean show) {
			if (!useIndicator) return this;
			playingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
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
					cover.setContentDescription(view.getContext().getResources().getString(R.string.default_cover));
				} else if (value.getClass().equals(Drawable.class)) {
					cover.setVisibility(View.VISIBLE);
					cover.setImageDrawable((Drawable) value);
					cover.setScaleType(ScaleType.CENTER_INSIDE);
					cover.setContentDescription("");
				} else if  (value.getClass().equals(Bitmap.class)) {
					cover.setVisibility(View.VISIBLE);
					cover.setImageBitmap((Bitmap) value);
					cover.setContentDescription("");
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
//							return  Util.getThemeName(activity).equals("AppTheme.White") ? 
//									BitmapFactory.decodeResource(res, R.drawable.fallback_cover_white) :
//									BitmapFactory.decodeResource(res, R.drawable.fallback_cover);
							return BitmapFactory.decodeResource(res, 
									Util.getResIdFromAttribute(activity, R.attr.fallback_cover));
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
//					return Util.getThemeName(activity).equals("AppTheme.White") ? 
//							BitmapFactory.decodeResource(res, R.drawable.fallback_cover_white) :
//							BitmapFactory.decodeResource(res, R.drawable.fallback_cover);
					return BitmapFactory.decodeResource(res, 
							Util.getResIdFromAttribute(activity, R.attr.fallback_cover));
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

	public static ViewBuilder getViewBuilder(Context context, View convertView, LayoutInflater inflater, int idCustomView, boolean useIndicator) {
		ViewBuilder builder;
		if (null == convertView) {
			boolean isCustomView = idCustomView > 0;
			if (isCustomView) {
				convertView = inflater.inflate(idCustomView, null);
			} else {
				convertView = inflater.inflate(Util.getResIdFromAttribute((Activity)context, R.attr.row_online_search), null);
			}
			
			builder = new ViewBuilder(convertView, isCustomView, useIndicator);
		} else {
			try {
				builder = (ViewBuilder) convertView.getTag();
			} catch (Exception e) {
				return getViewBuilder(context, null, inflater, idCustomView, useIndicator); // something wrong with the supplied view - create new one
			}
		}
		return builder;
	}
}
