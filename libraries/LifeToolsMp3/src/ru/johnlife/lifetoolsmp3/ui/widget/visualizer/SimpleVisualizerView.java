package ru.johnlife.lifetoolsmp3.ui.widget.visualizer;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.Util;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class SimpleVisualizerView extends View {
	
	private byte[] mBytes;
	private Rect mRect = new Rect();
	protected float[] mPoints;
	private Paint mForePaint = new Paint();
	private int mLinesCount = 16;
	private int mLinesStroke;
	private int frameLength = 5;
	private List<Integer[]> values = new ArrayList<Integer[]>();
	private int primary  = -1;
	private int accent = -1;
	
	public SimpleVisualizerView(Context context) {
		super(context);
		init();
	}
	
    public SimpleVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleVisualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

	@SuppressLint("NewApi")
	private void init() {
		mForePaint.setAntiAlias(true);
		mForePaint.setColor(getResources().getColor(android.R.color.white));
		mForePaint.setAlpha(20);
		setLayerType(LAYER_TYPE_SOFTWARE, mForePaint);
		mForePaint.setStyle(Style.STROKE);
		int widthFill = Util.dpToPx(getContext(), 8);
		int widthDash = Util.dpToPx(getContext(), 2);
		mForePaint.setPathEffect(new DashPathEffect(new float[]{widthFill, widthDash}, 0));
	}

	public synchronized void updateVisualizer(byte[] bytes) {
		mBytes = bytes;
		invalidate();
	}

	public void setColor(int color) {
		mForePaint.setColor(color);
	}
	
	public void setUpVizualizerColor (int primary, int accent) {
		this.primary = primary;
		this.accent = accent;
	}
 	
    /**
     * Helper to setColor(), that only assigns the color's alpha value,
     * leaving its r,g,b values unchanged. Results are undefined if the alpha
     * value is outside of the range [0..255]
     *
     * @param a set the alpha component [0..255] of the paint's color.
     */
	public void setAlpha(int alpha) {
		mForePaint.setAlpha(alpha);
	}

	@SuppressLint("NewApi")
	private void performDraw(Canvas canvas, Integer[] newValues) {
		mForePaint.setStrokeWidth(mLinesStroke * 2);
		values.add(newValues);
		if (values.size() > frameLength) {
			values.remove(0);
		}
		for (int i = values.size(); i > 0; i--) {
			if (i == values.size()) {
				if (primary != -1) {
					mForePaint.setColor(primary);
				} else {
					mForePaint.setColor(Color.WHITE);
				}
				setAlpha(100);
			} else {
				if (accent != -1) {
					mForePaint.setColor(accent);
				} else {
					mForePaint.setColor(Color.WHITE);
				}
				float alpha = ((float) i / (float) frameLength) * 100;
				setAlpha((int) alpha);
			}
			Integer[] frameValues = values.get(i - 1);
			for (int j = 0; j < frameValues.length; j++) {
				int currentX = mRect.width() / mLinesCount * j + mRect.width() / mLinesCount / 2;
				int height = mRect.height() - frameValues[j];
				canvas.drawLine(currentX, mRect.height() , currentX, height, mForePaint);
			}
		}	
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mRect.set(0, 0, getWidth(), getHeight());
		mLinesStroke = getWidth() / mLinesCount;
		mLinesStroke = (int)(mLinesStroke * 0.45);	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mBytes == null) return;
		Integer[] currentValues = new Integer[mLinesCount];
		for (int i = 0; i < mLinesCount; i++) {
			int value = 0;
			for (int j = 0; j < mBytes.length /mLinesCount; j++) {
				int position = i * (mBytes.length / mLinesCount) + j;
				byte rfk = mBytes[position];
				if ((position + 1) == mBytes.length) {
					position--;
				}
				byte ifk = mBytes[position + 1];
				float magnitude = (rfk * rfk + ifk * ifk);
				if (magnitude > 0) {
					value += (10 * Math.log10(magnitude));
				}
			}
			currentValues[i] = value;
		}
		performDraw(canvas, currentValues);
	}
}