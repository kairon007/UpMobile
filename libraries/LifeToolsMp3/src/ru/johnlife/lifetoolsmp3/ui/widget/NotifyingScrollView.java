package ru.johnlife.lifetoolsmp3.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

public class NotifyingScrollView extends ScrollView {
	
	private ImageView image;
	private OnScrollChangedListener onScrollChangedListener;

	public interface OnScrollChangedListener {
		void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt, int alpha);
	}

	public NotifyingScrollView(Context context) {
		super(context);
	}

	public NotifyingScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NotifyingScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (onScrollChangedListener != null) {
			int alpha = 255;
			if (t < 0) {
				alpha = 0;
			} else if (t < image.getHeight()) {
				float alphaScale = (float)t / (float)image.getHeight();
				alpha =  (int)(alphaScale * alpha);
			}
			onScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt, alpha);
		}
		if (null != image) {
			image.scrollTo(0, t / 2);
		}
	}

	public void setOnScrollChangedListener(OnScrollChangedListener listener) {
		onScrollChangedListener = listener;
	}
	
	public void setImageResource(int resource) {
		image = (ImageView)((View)getParent()).findViewById(resource);
	}
	
	public void setImageBitmap(Bitmap bitmap) {
		if (null == image) {
			throw new IllegalStateException(getClass().getSimpleName() + " - call setImageResource(int resource) first!");
		}
		image.setImageBitmap(bitmap);
	}
	
	public void setImageBitmap(int resource) {
		if (null == image) {
			throw new IllegalStateException(getClass().getSimpleName() + " - call setImageResource(int resource) first!");
		}
		image.setImageResource(resource);
	}
}
