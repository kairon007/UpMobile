package ru.johnlife.uilibrary.widget.textviews;

import android.content.Context;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoResizeTextView extends TextView {

	public static final float MIN_TEXT_SIZE = 20;

	// Interface for resize notifications
	public interface OnTextResizeListener {
		public void onTextResize(TextView textView, float oldSize, float newSize);
	}

	private static final String mEllipsis = "...";

	private OnTextResizeListener mTextResizeListener;
	private boolean mNeedsResize = false;
	private float mTextSize;
	private float mMaxTextSize = 0;
	private float mMinTextSize = MIN_TEXT_SIZE;
	private float mSpacingMult = 1.0f;
	private float mSpacingAdd = 0.0f;
	private boolean mAddEllipsis = true;

	public AutoResizeTextView(Context context) {
		this(context, null);
	}

	public AutoResizeTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTextSize = getTextSize();
	}

	/**
	 * When text changes, set the force resize flag to true and reset the text
	 * size.
	 */
	@Override
	protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
		mNeedsResize = true;
		// Since this view may be reused, it is good to reset the text size
		resetTextSize();
	}

	/**
	 * If the text view size changed, set the force resize flag to true
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w != oldw || h != oldh) {
			mNeedsResize = true;
		}
	}

	/**
	 * Register listener to receive resize notifications
	 * 
	 * @param listener
	 */
	public void setOnResizeListener(OnTextResizeListener listener) {
		mTextResizeListener = listener;
	}

	/**
	 * Override the set text size to update our internal reference values
	 */
	@Override
	public void setTextSize(float size) {
		super.setTextSize(size);
		mTextSize = getTextSize();
	}

	/**
	 * Override the set text size to update our internal reference values
	 */
	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		mTextSize = getTextSize();
	}

	/**
	 * Override the set line spacing to update our internal reference values
	 */
	@Override
	public void setLineSpacing(float add, float mult) {
		super.setLineSpacing(add, mult);
		mSpacingMult = mult;
		mSpacingAdd = add;
	}

	/**
	 * Set the upper text size limit and invalidate the view
	 * 
	 * @param maxTextSize
	 */
	public void setMaxTextSize(float maxTextSize) {
		mMaxTextSize = maxTextSize;
		requestLayout();
		invalidate();
	}

	/**
	 * Return upper text size limit
	 * 
	 * @return
	 */
	public float getMaxTextSize() {
		return mMaxTextSize;
	}

	/**
	 * Set the lower text size limit and invalidate the view
	 * 
	 * @param minTextSize
	 */
	public void setMinTextSize(float minTextSize) {
		mMinTextSize = minTextSize;
		requestLayout();
		invalidate();
	}

	/**
	 * Return lower text size limit
	 * 
	 * @return
	 */
	public float getMinTextSize() {
		return mMinTextSize;
	}

	/**
	 * Set flag to add ellipsis to text that overflows at the smallest text size
	 * 
	 * @param addEllipsis
	 */
	public void setAddEllipsis(boolean addEllipsis) {
		mAddEllipsis = addEllipsis;
	}

	/**
	 * Return flag to add ellipsis to text that overflows at the smallest text
	 * size
	 * 
	 * @return
	 */
	public boolean getAddEllipsis() {
		return mAddEllipsis;
	}

	/**
	 * Reset the text to the original size
	 */
	public void resetTextSize() {
		if (mTextSize > 0) {
			super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
			mMaxTextSize = mTextSize;
		}
	}

	/**
	 * Resize text after measuring
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed || mNeedsResize) {
			int widthLimit = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
			int heightLimit = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
			resizeText(widthLimit, heightLimit);
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	/**
	 * Resize the text size with default width and height
	 */
	public void resizeText() {
		int heightLimit = getHeight() - getPaddingBottom() - getPaddingTop();
		int widthLimit = getWidth() - getPaddingLeft() - getPaddingRight();
		resizeText(widthLimit, heightLimit);
	}

	/**
	 * Resize the text size with specified width and height
	 * 
	 * @param width
	 * @param height
	 */
	public void resizeText(int width, int height) {
		CharSequence text = getText();
		if (text == null || text.length() == 0 || height <= 0 || width <= 0 || mTextSize == 0) return;
		if (getTransformationMethod() != null)
			text = getTransformationMethod().getTransformation(text, this);
		TextPaint textPaint = getPaint();
		float oldTextSize = textPaint.getTextSize();
		float targetTextSize = mMaxTextSize > 0 ? Math.min(mTextSize, mMaxTextSize) : mTextSize;
		int textHeight = getTextHeight(text, textPaint, width, targetTextSize);
		while (textHeight > height && targetTextSize > mMinTextSize) {
			targetTextSize = Math.max(targetTextSize - 2, mMinTextSize);
			textHeight = getTextHeight(text, textPaint, width, targetTextSize);
		}
		if (mAddEllipsis && targetTextSize == mMinTextSize && textHeight > height) {
			TextPaint paint = new TextPaint(textPaint);
			StaticLayout layout = new StaticLayout(text, paint, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, false);
			if (layout.getLineCount() > 0) {
				int lastLine = layout.getLineForVertical(height) - 1;
				if (lastLine < 0) {
					setText("");
				} else {
					int start = layout.getLineStart(lastLine);
					int end = layout.getLineEnd(lastLine);
					float lineWidth = layout.getLineWidth(lastLine);
					float ellipseWidth = textPaint.measureText(mEllipsis);
					while (width < lineWidth + ellipseWidth) {
						lineWidth = textPaint.measureText(text.subSequence(start, --end + 1).toString());
					}
					setText(text.subSequence(0, end) + mEllipsis);
				}
			}
		}
		setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize);
		setLineSpacing(mSpacingAdd, mSpacingMult);
		if (mTextResizeListener != null)
			mTextResizeListener.onTextResize(this, oldTextSize, targetTextSize);
		mNeedsResize = false;
	}

	private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
		TextPaint paintCopy = new TextPaint(paint);
		paintCopy.setTextSize(textSize);
		StaticLayout layout = new StaticLayout(source, paintCopy, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, true);
		return layout.getHeight();
	}
}
