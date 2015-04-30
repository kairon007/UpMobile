package ru.johnlife.lifetoolsmp3.ui.widget.dsb.internal;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.ui.widget.dsb.internal.compat.SeekBarCompat;
import ru.johnlife.lifetoolsmp3.ui.widget.dsb.internal.drawable.MarkerDrawable;
import ru.johnlife.lifetoolsmp3.ui.widget.dsb.internal.drawable.ThumbDrawable;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * {@link android.view.ViewGroup} to be used as the real indicator.
 * <p>
 * I've used this to be able to acomodate the TextView and the
 * {@link org.adw.library.widgets.discreteseekbar.internal.drawable.MarkerDrawable}
 * with the required positions and offsets
 * </p>
 *
 * @hide
 */
public class Marker extends FrameLayout implements MarkerDrawable.MarkerAnimationListener {
	private static final int PADDING_DP = 4;
	private static final int ELEVATION_DP = 8;
	// The TextView to show the info
	private TextView mNumber;
	private View indeterminateView;
	private View contentView;
	MarkerDrawable mMarkerDrawable;
	private boolean isIndeterminate = false;

	public Marker(Context context) {
		this(context, null);
	}

	public Marker(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.discreteSeekBarStyle);
	}

	public Marker(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, "0");
	}

	public Marker(Context context, AttributeSet attrs, int defStyleAttr, String maxValue) {
		super(context, attrs, defStyleAttr);
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiscreteSeekBar, R.attr.discreteSeekBarStyle, R.style.DefaultSeekBar);

		int padding = (int) (PADDING_DP * displayMetrics.density) * 2;
		contentView = inflate(context, R.layout.marker, this);
		mNumber = (TextView)contentView.findViewById(R.id.simple_text);
		mNumber.setGravity(Gravity.CENTER);
		mNumber.setText(maxValue);
		mNumber.setTextColor(Color.WHITE);
		mNumber.setMaxLines(1);
		mNumber.setPadding(padding, 0, padding, padding * 2);
		mNumber.setSingleLine(true);
		SeekBarCompat.setTextDirection(mNumber, TEXT_DIRECTION_LOCALE);
		// Add indeterminate indicator
		indeterminateView = contentView.findViewById(R.id.marker_indeterminate);
//		indeterminateView.setPadding(padding, 0, padding, padding * 8);
		indeterminateView.setVisibility(View.INVISIBLE);
		resetSizes(maxValue);
		int thumbSize = (int) (ThumbDrawable.DEFAULT_SIZE_DP * displayMetrics.density);
		ColorStateList color = a.getColorStateList(R.styleable.DiscreteSeekBar_dsb_indicatorColor);
		mMarkerDrawable = new MarkerDrawable(color, thumbSize);
		mMarkerDrawable.setCallback(this);
		mMarkerDrawable.setMarkerListener(this);
		// Elevation for anroid 5+
		float elevation = a.getDimension(R.styleable.DiscreteSeekBar_dsb_indicatorElevation, ELEVATION_DP * displayMetrics.density);
		ViewCompat.setElevation(this, elevation);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			SeekBarCompat.setOutlineProvider(this, mMarkerDrawable);
		}
		a.recycle();
	}

	public void resetSizes(String maxValue) {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		// Account for negative numbers... is there any proper way of getting
		// the biggest string between our range????
		mNumber.setText("-" + maxValue);
		// Do a first forced measure call for the TextView (with the biggest
		// text content),
		// to calculate the max width and use always the same.
		// this avoids the TextView from shrinking and growing when the text
		// content changes
		int wSpec = MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, MeasureSpec.AT_MOST);
		int hSpec = MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, MeasureSpec.AT_MOST);
		contentView.measure(wSpec, hSpec);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mMarkerDrawable.draw(canvas);
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int left = getPaddingLeft();
		int top = getPaddingTop();
		int right = getWidth() - getPaddingRight();
		int bottom = getHeight() - getPaddingBottom();
		mMarkerDrawable.setBounds(left, top, right, bottom);
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mMarkerDrawable || super.verifyDrawable(who);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		// HACK: Sometimes, the animateOpen() call is made before the View is
		// attached
		// so the drawable cannot schedule itself to run the animation
		// I think we can call it here safely.
		// I've seen it happen in android 2.3.7
		animateOpen();
	}

	public void setValue(CharSequence value) {
		mNumber.setText(value);
	}

	public CharSequence getValue() {
		return mNumber.getText();
	}

	public void animateOpen() {
		mMarkerDrawable.stop();
		mMarkerDrawable.animateToPressed();
	}

	public void animateClose() {
		mMarkerDrawable.stop();
		mNumber.setVisibility(View.INVISIBLE);
		indeterminateView.setVisibility(View.INVISIBLE);
		mMarkerDrawable.animateToNormal();
	}

	@Override
	public void onOpeningComplete() {
		if (!isIndeterminate) {
			mNumber.setVisibility(View.VISIBLE);
		} else {
			indeterminateView.setVisibility(View.VISIBLE);
		}
		if (getParent() instanceof MarkerDrawable.MarkerAnimationListener) {
			((MarkerDrawable.MarkerAnimationListener) getParent()).onOpeningComplete();
		}
	}

	@Override
	public void onClosingComplete() {
		if (getParent() instanceof MarkerDrawable.MarkerAnimationListener) {
			((MarkerDrawable.MarkerAnimationListener) getParent()).onClosingComplete();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mMarkerDrawable.stop();
	}

	public void setColors(int startColor, int endColor) {
		mMarkerDrawable.setColors(startColor, endColor);
	}
	
	public void setIndeterminate(boolean isIndeterminate) {
		this.isIndeterminate = isIndeterminate;
		if (isIndeterminate) {
			mNumber.setVisibility(View.INVISIBLE);
		} else {
			indeterminateView.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setIndeterminateColor(ColorStateList colorState) {
		int color = colorState.getDefaultColor();
		Drawable indeterminateDrawable = indeterminateView.getBackground();
		indeterminateDrawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
		indeterminateView.setBackgroundDrawable(indeterminateDrawable);
	}
}
