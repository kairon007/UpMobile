package ru.johnlife.lifetoolsmp3.ui.widget;

import ru.johnlife.lifetoolsmp3.Util;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

public class NotifyingScrollView extends ScrollView {
	
	private ImageView image;
	private OnScrollChangedListener onScrollChangedListener;
	private final int maxOverscroll = Util.dpToPx(getContext(), 180);

	public interface OnScrollChangedListener {
		void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt);
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
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
			int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX,
				maxOverscroll, isTouchEvent);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (onScrollChangedListener != null) {
			onScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
		}
		if (null != image) {
			int offset = Util.dpToPx(getContext(), 90);
			image.scrollTo(0, t / 2 + offset);
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
	
	public void enableClickToScroll(int fakeViewID) {
		((View)getParent()).findViewById(fakeViewID).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				NotifyingScrollView.this.overScrollBy(0, 0 - maxOverscroll, 0, 0, 0, 0, 0, 0, true);
			}
		});
	}
}
