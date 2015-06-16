package ru.johnlife.lifetoolsmp3.ui.widget;

import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Display;
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

	public int getToolbarAlpha() {
		int alpha = 255;
		float alphaScale = (float)getScrollY() / (float)image.getHeight();
		return (int)(alphaScale * alpha);
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
	
	public void recalculateCover(int res, int visId) {
		Display display = ((BaseMiniPlayerActivity) getContext()).getWindowManager().getDefaultDisplay(); 
		int height = (int)(display.getHeight()/2);
		setMinImageSize(height, height);
		((View)getParent()).findViewById(visId).getLayoutParams().height = height;
		((View)getParent()).requestLayout();
	}
	
	public void setMinImageSize(int minWidth, int minHeight) {
		image.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		image.getLayoutParams().height = minHeight;
	}
}
