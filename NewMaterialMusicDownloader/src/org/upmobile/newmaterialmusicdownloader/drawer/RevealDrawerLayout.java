package org.upmobile.newmaterialmusicdownloader.drawer;

import org.upmobile.newmaterialmusicdownloader.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;

public class RevealDrawerLayout extends DrawerLayout implements DrawerLayout.DrawerListener {

	private AccelerateInterpolator mAccelerateInterpolator;
	private int mDefaultYOffset;
	private int mListOffset;
	private float mRevealY;
	private Path mRevealPath;

	private float mStartCenterX;
	private float mStartCenterY;
	private float mStartRadius;

	private float mEndCenterX;
	private float mEndCenterY;
	private float mEndRadius;

	private DrawerListener mDrawerListenerDelegate;
	private boolean mAnimationEnabled = true;

	public RevealDrawerLayout(Context context) {
		this(context, null);
	}

	public RevealDrawerLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RevealDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (Build.VERSION.SDK_INT < 18) {
			mAnimationEnabled = false;
		}
		super.setDrawerListener(this);
		mAccelerateInterpolator = new AccelerateInterpolator(1.2f);
		mListOffset = -scale(50);
		mDefaultYOffset = getActionBarSize(getContext()) / 2;
		mRevealPath = new Path();
		if (mAnimationEnabled) {
			getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
						@Override
						public boolean onPreDraw() {
							getViewTreeObserver().removeOnPreDrawListener(this);
							for (int i = 0; i < getChildCount(); i++) {
								final View child = getChildAt(i);
								final int gravity = getDrawerViewGravity(child);
								setRevealRadius(gravity, calcRevealRadius(0, getWidth(), getHeight()));
							}
							return true;
						}
					});
		}
	}

	@Override
	public void setDrawerListener(DrawerListener listener) {
		this.mDrawerListenerDelegate = listener;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updateRevealOnDrawerView(Gravity.LEFT);
		updateRevealOnDrawerView(Gravity.RIGHT);
	}

	private void updateRevealOnDrawerView(int gravity) {
		if (isDrawerOpen(gravity)) {
			final View leftDrawerView = findDrawerView(gravity);
			if (leftDrawerView != null) {
				setDrawerReveal(gravity, leftDrawerView, 1);
			}
		}
	}

	@Override
	public void onDrawerSlide(View drawerView, float slideOffset) {
		if (mAnimationEnabled) {
			final int gravity = getDrawerViewGravity(drawerView);
			setDrawerReveal(gravity, drawerView, slideOffset);
		}

		if (mDrawerListenerDelegate != null) {
			mDrawerListenerDelegate.onDrawerSlide(drawerView, slideOffset);
		}
	}

	@Override
	public void onDrawerOpened(View drawerView) {
		mRevealY = 0;
		if (mDrawerListenerDelegate != null) {
			mDrawerListenerDelegate.onDrawerOpened(drawerView);
		}
	}

	@Override
	public void onDrawerClosed(View drawerView) {
		mRevealY = 0;
		if (mDrawerListenerDelegate != null) {
			mDrawerListenerDelegate.onDrawerClosed(drawerView);
		}
	}

	@Override
	public void onDrawerStateChanged(int newState) {
		if (mDrawerListenerDelegate != null) {
			mDrawerListenerDelegate.onDrawerStateChanged(newState);
		}
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		if (!mAnimationEnabled) {
			return super.drawChild(canvas, child, drawingTime);
		}
		final int gravity = getDrawerViewGravity(child);
		if (gravity == Gravity.NO_GRAVITY) {
			return super.drawChild(canvas, child, drawingTime);
		}
		final int state = canvas.save();
		mRevealPath.reset();
		switch (gravity) {
		case Gravity.LEFT:
			mRevealPath.addCircle(mStartCenterX, mStartCenterY, mStartRadius, Path.Direction.CW);
			break;

		case Gravity.RIGHT:
			mRevealPath.addCircle(mEndCenterX, mEndCenterY, mEndRadius, Path.Direction.CW);
			break;
		}
		canvas.clipPath(mRevealPath);
		boolean isInvalided = super.drawChild(canvas, child, drawingTime);
		canvas.restoreToCount(state);
		return isInvalided;
	}

	private void setDrawerReveal(int gravity, View drawerView, float slideOffset) {
		drawerView.setTranslationX((((gravity == Gravity.LEFT) ? drawerView.getWidth() 
						: -drawerView.getWidth()) + ((gravity == Gravity.LEFT) ? mListOffset
						: -mListOffset))
						* (1 - slideOffset));
		drawerView.setTranslationY((mRevealY - getHeight() / 2) * 0.2f * (1 - slideOffset));
		final float centerX;
		if (mRevealY == 0) {
			centerX = (gravity == Gravity.LEFT) ? mDefaultYOffset * (1 - slideOffset) : getWidth() - mDefaultYOffset;
		} else {
			centerX = (gravity == Gravity.LEFT) ? 0 : getWidth();
		}
		final float centerY = (mRevealY == 0) ? mDefaultYOffset : mRevealY;
		setCenter(gravity, centerX, centerY);
		final float revealRadius = calcRevealRadius(mRevealY, drawerView.getWidth(), getHeight());
		final float interpolatedOffset = mAccelerateInterpolator.getInterpolation(slideOffset);
		setRevealRadius(gravity, revealRadius * interpolatedOffset);
	}

	private void setCenter(int gravity, float centerX, float centerY) {
		switch (gravity) {
		case Gravity.LEFT:
			mStartCenterX = centerX;
			mStartCenterY = centerY;
			break;
		case Gravity.RIGHT:
			mEndCenterX = centerX;
			mEndCenterY = centerY;
			break;
		}
	}

	private void setRevealRadius(int gravity, float revealRadius) {
		switch (gravity) {
		case Gravity.LEFT:
			mStartRadius = revealRadius;
			break;
		case Gravity.RIGHT:
			mEndRadius = revealRadius;
			break;
		}
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mRevealY = ev.getY();
		return super.onTouchEvent(ev);
	}

	private static float calcRevealRadius(float revealY, int width, int height) {
		return (float) Math.hypot(width, (revealY > height / 2) ? revealY : height - revealY);
	}

	private static int getDrawerViewGravity(View drawerView) {
		final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
		final int absGravity = GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(drawerView));
		return (absGravity & (Gravity.LEFT | Gravity.RIGHT));
	}

	private View findDrawerView(int gravity) {
		for (int i = 0; i < getChildCount(); i++) {
			final View childView = getChildAt(i);
			if (getDrawerViewGravity(childView) == gravity) {
				return childView;
			}
		}
		return null;
	}

	private static int getActionBarSize(Context context) {
		final int[] attribute = new int[] { R.attr.actionBarSize };
		final TypedValue typedValue = new TypedValue();
		final TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
		final int actionBarHeight = array.getDimensionPixelSize(0, -1);
		array.recycle();
		return actionBarHeight;
	}

	private static int scale(int value) {
		return (int) (value * Resources.getSystem().getDisplayMetrics().density + 0.5f);
	}
}
