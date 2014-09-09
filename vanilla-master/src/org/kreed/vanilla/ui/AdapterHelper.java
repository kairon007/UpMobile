package org.kreed.vanilla.ui;

import java.io.File;
import java.util.Vector;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.vanilla.LibraryActivity;
import org.kreed.vanilla.PlaybackService;
import org.kreed.vanilla.R;
import org.kreed.vanilla.app.VanillaApp;

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
		private TextView text;
		private TextView number;
		private TextView line2;
		private ImageView arrow;
		private ImageView cover;
		private View view;
		private View left;
		private TextView caption;
		private AsyncTask<Void, Void, Bitmap> loadCoverTask;
		
		private ViewBuilder(View view) {
			view.setTag(this);
			this.view = view; 
			view.setLongClickable(true);
			text = (TextView)view.findViewById(R.id.text);
			text.setTypeface(VanillaApp.FONT_LIGHT);
			line2 = (TextView)view.findViewById(R.id.line2);
			line2.setTypeface(VanillaApp.FONT_REGULAR);
			number = (TextView)view.findViewById(R.id.number);
			number.setTypeface(VanillaApp.FONT_LIGHT);
			caption = (TextView)view.findViewById(R.id.caption);
			caption.setTypeface(VanillaApp.FONT_LIGHT);
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
			setClickRedirect();
			return this;
		}
		
		public void setClickRedirect() {
			view.findViewById(R.id.main_layout).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					view.performClick();
				}
			});
		}
		
		public ViewBuilder setId(long value) {
			this.id = value;
			return this;
		}
		
		public ViewBuilder setLine1(String value) {
			text.setText(value);
			title = value;
			return this;
		}
		
		public ViewBuilder setLine2(String value) {
			line2.setText(value);
			setVisibility(line2, value);
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
		
		public void startLoadCover(final int maxWidth, final int type, final long id,
				final LibraryActivity activity) {
			if (null != loadCoverTask) {
				loadCoverTask.cancel(true);
			}
			loadCoverTask = new AsyncTask<Void, Void, Bitmap> () {
				
				@Override
				protected Bitmap doInBackground(Void... params) {
					File file = PlaybackService.get(activity).getFilePath(type, id);
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
