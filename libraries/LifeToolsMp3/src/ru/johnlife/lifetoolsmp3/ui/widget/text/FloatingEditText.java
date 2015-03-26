package ru.johnlife.lifetoolsmp3.ui.widget.text;

import ru.johnlife.lifetoolsmp3.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * @author Pallad!n
 *
 */
public class FloatingEditText extends EditText {
	
	private static final long ANIMATION_DURATION = 120;
	private static final int MAX_CHAR_COUNT = 17;
    private static final int StateHintNormal = 0;
    private static final int StateHintZoomIn = 1;
    private static final int StateHintZoomOut = 2;
    private static final float HINT_SCALE = 0.6f;
    
    private Paint hintPaint;
    private Paint countPaint;
    
    private String validateMessage;
    
    private int spacingBtmToUnderline = 16;// Btm = Bottom
    private int spacingBtmToText = 24;
    private int spacingTopToText = 16;
    
    //attr
    private int color;
    private int highlightedColor;
    private int errorColor;
    private int underlineHeight;
    private int underlineHighlightedHeight;
    private int leftPadding;
    private int rightPadding;
    private int maxCharCount = MAX_CHAR_COUNT;
    private int currentCharCount = 0;
    private float hintScale;
    private boolean showCharCount = false;
    
    
    
    private boolean isExcessiveCount = false;
    private int state = StateHintNormal;
    private long startTime;
    private boolean verified = true;
    private boolean textEmpty;
    
    public FloatingEditText(Context context) {
        this(context, null);
    }

    public FloatingEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    @SuppressLint("NewApi")
    public FloatingEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, android.R.attr.editTextStyle);
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingEditText);
        try {
			hintScale = attr.getFloat(R.styleable.FloatingEditText_floating_et_hint_scale, HINT_SCALE);
			color = attr.getColor(R.styleable.FloatingEditText_floating_et_color, getResources().getColor(android.R.color.holo_blue_dark));
			highlightedColor = attr.getColor(R.styleable.FloatingEditText_floating_et_highlighted_color, getResources().getColor(android.R.color.black));
			errorColor = attr.getColor(R.styleable.FloatingEditText_floating_et_error_color, getResources().getColor(R.color.material_primary_dark));
			underlineHeight = attr.getDimensionPixelSize(R.styleable.FloatingEditText_floating_et_underline_height, getResources().getDimensionPixelSize(R.dimen.floating_edit_text_underline_height));
			underlineHighlightedHeight = attr.getDimensionPixelSize(R.styleable.FloatingEditText_floating_et_underline_highlighted_height, getResources().getDimensionPixelSize(R.dimen.floating_edit_text_underline_highlighted_height));
			showCharCount = attr.getBoolean(R.styleable.FloatingEditText_floating_et_show_count, false);
			maxCharCount = attr.getInteger(R.styleable.FloatingEditText_floating_et_char_count, MAX_CHAR_COUNT);
			leftPadding = attr.getDimensionPixelSize(R.styleable.FloatingEditText_floating_et_left_padding, 0);
			rightPadding = attr.getDimensionPixelSize(R.styleable.FloatingEditText_floating_et_right_padding, 0);
        } finally {
			attr.recycle();
		}
        setHintTextColor(Color.TRANSPARENT);
        textEmpty = TextUtils.isEmpty(getText());
        hintPaint = new Paint();
        hintPaint.setAntiAlias(true);
        countPaint = new Paint();
        countPaint.setAntiAlias(true);

        Drawable underline = new Drawable() {
        	
            @Override
            public void draw(Canvas canvas) {
                if (verified && !isExcessiveCount) {
                    if (isFocused()) {
                        Rect rect = getThickLineRect(canvas);
                        hintPaint.setColor(highlightedColor);
                        canvas.drawRect(rect, hintPaint);
                    } else {
                        Rect rect = getThinLineRect(canvas);
                        hintPaint.setColor(color);
                        canvas.drawRect(rect, hintPaint);
                    }
                } else {
                    Rect rect = getThickLineRect(canvas);
                    hintPaint.setColor(errorColor);
                    canvas.drawRect(rect, hintPaint);

                    if (!verified) {
						hintPaint.setColor(errorColor);
						hintPaint.setTextSize(getTextSize() * 0.6f);
						float x = getCompoundPaddingLeft();
						float y = rect.bottom + (dpToPx(16) - hintPaint.getFontMetricsInt().top) / 2;
						canvas.drawText(validateMessage, x, y, hintPaint);
					}
                }
            }

            @Override
            public void setAlpha(int alpha) {
                hintPaint.setAlpha(alpha);
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
                hintPaint.setColorFilter(colorFilter);
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSPARENT;
            }
        };
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(underline);
        } else {
            setBackground(underline);
        }
		int paddingTop = dpToPx(spacingTopToText);
		int paddingBottom = dpToPx(spacingBtmToText);
        setPadding(leftPadding, paddingTop, rightPadding, paddingBottom);
        setSingleLine();
    }
    
    private Rect getThinLineRect(Canvas canvas) {
    	Rect lineRect = new Rect();
        lineRect.left = getPaddingLeft();
        lineRect.top = canvas.getHeight() - underlineHeight - dpToPx(spacingBtmToUnderline);
        lineRect.right = getWidth();
        lineRect.bottom = canvas.getHeight() - dpToPx(spacingBtmToUnderline);
        return lineRect;
    }

    private Rect getThickLineRect(Canvas canvas) {
    	Rect lineRect = new Rect();
        lineRect.left = getPaddingLeft();
        lineRect.top = canvas.getHeight() - underlineHighlightedHeight - dpToPx(spacingBtmToUnderline);
        lineRect.right = getWidth();
        lineRect.bottom = canvas.getHeight() - dpToPx(spacingBtmToUnderline);
        return lineRect;
    }

    public void setNormalColor(int color) {
        this.color = color;
        invalidate();
    }

    public void setHighlightedColor(int color) {
        this.highlightedColor = color;
        invalidate();
    }

    public void setValidateResult(boolean verified, String message) {
        if (!verified && message == null) {
            throw new IllegalStateException("Must have a validate result message.");
        }
        this.verified = verified;
        this.validateMessage = message;
        invalidate();
    }

    @SuppressLint("NewApi")
	@Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        currentCharCount = text.length();
        if (showCharCount) {
        	isExcessiveCount = maxCharCount <= currentCharCount;
		}
		this.verified = true;
        this.validateMessage = null;
        boolean isEmpty = TextUtils.isEmpty(getText());
        if (textEmpty != isEmpty) {
            this.textEmpty = isEmpty;
            if (isEmpty && isShown()) {
                startTime = System.currentTimeMillis();
                state = StateHintZoomIn;
            } else {
                startTime = System.currentTimeMillis();
                state = StateHintZoomOut;
            }
        }
    }

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);
		if (!TextUtils.isEmpty(getHint())) {
			hintPaint.set(getPaint());
			float maxTextSize = getTextSize();
			float minTextSize = getTextSize() * hintScale;
			float maxHintY = getBaseline();
			float minHintY = getBaseline() + getPaint().getFontMetricsInt().top + getScrollY();
			float textSize;
			float hintY;
			float hintX = getCompoundPaddingLeft() + getScrollX();
			long elapsed = System.currentTimeMillis() - startTime;
			switch (state) {
			case StateHintNormal: 
				textSize = maxTextSize;
				hintY = maxHintY;
				hintPaint.setColor(color);
				hintPaint.setTextSize(textSize);
				canvas.drawText(getHint().toString(), hintX, hintY, hintPaint);
				break;
			case StateHintZoomIn: 
				if (elapsed < ANIMATION_DURATION) {
					textSize = ((maxTextSize - minTextSize) * elapsed) / ANIMATION_DURATION + minTextSize;
					hintY = ((maxHintY - minHintY) * elapsed) / ANIMATION_DURATION + minHintY;
					hintPaint.setColor(highlightedColor);
					hintPaint.setTextSize(textSize);
					canvas.drawText(getHint().toString(), hintX, hintY, hintPaint);
					postInvalidate();
				} else {
					textSize = maxTextSize;
					hintY = maxHintY;
					hintPaint.setColor(color);
					hintPaint.setTextSize(textSize);
					canvas.drawText(getHint().toString(), hintX, hintY, hintPaint);
				}
				break;
			case StateHintZoomOut: {
				if (elapsed < ANIMATION_DURATION) {
					textSize = maxTextSize - ((maxTextSize - minTextSize) * elapsed) / ANIMATION_DURATION;
					hintY = maxHintY - ((maxHintY - minHintY) * elapsed) / ANIMATION_DURATION;
					hintPaint.setColor(highlightedColor);
					hintPaint.setTextSize(textSize);
					canvas.drawText(getHint().toString(), hintX, hintY, hintPaint);
					postInvalidate();
				} else {
					textSize = minTextSize;
					hintY = minHintY;
					if (isFocused()) {
						hintPaint.setColor(highlightedColor);
					} else {
						hintPaint.setColor(color);
					}
					hintPaint.setTextSize(textSize);
					canvas.drawText(getHint().toString(), hintX, hintY, hintPaint);
				}
			}
				break;
			}
		}
		if (showCharCount) {
			String str = currentCharCount + "/" + maxCharCount;
			float textSize = spToPx(12);
			int bottom = (int) (canvas.getHeight() - textSize);
			float y = bottom + (dpToPx(8) - countPaint.getFontMetricsInt().top) / 2;
			float x = getWidth() - (str.length() * getTextSize() / 2);
			countPaint.set(getPaint());
			countPaint.setColor(isExcessiveCount ? errorColor : highlightedColor);
			countPaint.setTextSize(textSize);
			canvas.drawText(str, x, y, countPaint);
		}
	}

    public static float spToPx(int value) {
		return (value * Resources.getSystem().getDisplayMetrics().scaledDensity);
	}
    
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    
}