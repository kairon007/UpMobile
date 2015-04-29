package ru.johnlife.lifetoolsmp3.ui.widget;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.DecelerateInterpolator;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class PlayPauseView extends View {
	private static final Property<PlayPauseView, Integer> COLOR = new Property<PlayPauseView, Integer>(Integer.class, "color") {
		@Override
		public Integer get(PlayPauseView v) {
			return v.getColor();
		}

		@Override
		public void set(PlayPauseView v, Integer value) {
			v.setColor(value);
		}
	};
	
	private static final int DEFAULT_COLOR = Color.BLACK;
	private static final Integer DEFAULT_WIDTH = Integer.valueOf(10);
	private static final Integer DEFAULT_HEIGHT = Integer.valueOf(24);
	private static final Integer DEFAULT_DISTANCE = Integer.valueOf(8);
	private static final Integer DEFAULT_COLOR_BUTTON = Color.WHITE;
	private static final long PLAY_PAUSE_ANIMATION_DURATION = 200L;
	private final PlayPauseDrawable mDrawable;
	private final Paint mPaint = new Paint();
	private final int mColorButton;
	private final int mPauseBackgroundColor;
	private final int mPlayBackgroundColor;
	private float mPauseBarWidth;
	private float mPauseBarHeight;
	private float mPauseBarDistance;
	private AnimatorSet mAnimatorSet;
	private int mBackgroundColor;
	private int mWidth;
	private int mHeight;
	
	public PlayPauseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(Boolean.FALSE);
		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PlayPauseButton);
		mBackgroundColor = attributes.getColor(R.styleable.PlayPauseButton_mainColor, DEFAULT_COLOR);
		mPauseBarWidth = Util.dpToPx(context, (int) attributes.getInteger(R.styleable.PlayPauseButton_pauseBarWidth, DEFAULT_WIDTH));
		mPauseBarHeight = Util.dpToPx(context, (int) attributes.getInteger(R.styleable.PlayPauseButton_pauseBarHeight, DEFAULT_HEIGHT));
		mPauseBarDistance = Util.dpToPx(context, (int) attributes.getInteger(R.styleable.PlayPauseButton_pauseBarDistance, DEFAULT_DISTANCE));
		mColorButton = attributes.getColor(R.styleable.PlayPauseButton_colorButton, DEFAULT_COLOR_BUTTON);
		mPaint.setAntiAlias(Boolean.TRUE);
		mPaint.setStyle(Paint.Style.FILL);
		mDrawable = new PlayPauseDrawable(context, mColorButton, mPauseBarWidth, mPauseBarHeight, mPauseBarDistance);
		mDrawable.setCallback(this);
		mPauseBackgroundColor = attributes.getColor(R.styleable.PlayPauseButton_pauseBackground, DEFAULT_COLOR);
		mPlayBackgroundColor = attributes.getColor(R.styleable.PlayPauseButton_playBackground, DEFAULT_COLOR);
		attributes.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
		setMeasuredDimension(size, size);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mDrawable.setBounds(0, 0, w, h);
		mWidth = w;
		mHeight = h;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setOutlineProvider(new ViewOutlineProvider() {
				@TargetApi(Build.VERSION_CODES.LOLLIPOP)
				@Override
				public void getOutline(View view, Outline outline) {
					outline.setOval(0, 0, view.getWidth(), view.getHeight());
				}
			});
			setClipToOutline(Boolean.TRUE);
		}
	}

	private void setColor(int color) {
		mBackgroundColor = color;
		invalidate();
	}

	private int getColor() {
		return mBackgroundColor;
	}
	
	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mDrawable || super.verifyDrawable(who);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint.setColor(mBackgroundColor);
		final float radius = Math.min(mWidth, mHeight) / 2f;
		canvas.drawCircle(mWidth / 2f, mHeight / 2f, radius, mPaint);
		mDrawable.draw(canvas);
	}
	
	public boolean isPlay() {
		return mDrawable.isPlay();
	}
	
	@Override
	public void setClickable(final boolean clickable) {
		setColor(clickable);
		super.setClickable(clickable);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		setColor(enabled);
		super.setEnabled(enabled);
	}

	private void setColor(final boolean flag) {
		mDrawable.setColorFilter(flag ? mColorButton : Color.GRAY, PorterDuff.Mode.MULTIPLY);
	}

	public void toggle(final boolean isPlay) {
		if (null != mAnimatorSet) {
			mAnimatorSet.cancel();
		}
		mAnimatorSet = new AnimatorSet();
		mDrawable.setIsPlay(isPlay);
		final ObjectAnimator colorAnim = ObjectAnimator.ofInt(this, COLOR, mDrawable.isPlay() ? mPauseBackgroundColor : mPlayBackgroundColor);
		colorAnim.setEvaluator(new ArgbEvaluator());
		final Animator pausePlayAnim = mDrawable.getPausePlayAnimator();
		mAnimatorSet.setInterpolator(new DecelerateInterpolator());
		mAnimatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
		mAnimatorSet.playTogether(colorAnim, pausePlayAnim);
		mAnimatorSet.start();
	}
}
