package ru.johnlife.lifetoolsmp3.ui.widget.visualizer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class SimpleVisualizerView extends View {
	
//	private static final Object LOCK = new Object();
	private byte[] mBytes;
	private Rect mRect = new Rect();
	protected float[] mPoints;
	private Paint mForePaint = new Paint();
	private int mLinesCount = 16;
	private int mLinesStroke;
	private int frameLength = 5;
	private List<Integer[]> values = new ArrayList<Integer[]>();
	
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

	private void init() {
		mForePaint.setAntiAlias(true);
		mForePaint.setColor(getResources().getColor(android.R.color.white));
		mForePaint.setAlpha(20);
		mForePaint.setStrokeWidth(10);
	}

	public synchronized void updateVisualizer(byte[] bytes) {
		mBytes = bytes;
//		synchronized (LOCK) {
//			try {
//				LOCK.wait();
				invalidate();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}

	public void setColor(int color) {
		mForePaint.setColor(color);
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

	private void performDraw(Canvas canvas, Integer[] newValues) {
		values.add(newValues);
		if (values.size() > frameLength) {
			values.remove(0);
		}
		for (int i = values.size(); i > 0; i--) {
			if (i == values.size()) {
				setAlpha(150);
			} else {
				float alpha = (float)i / (float)frameLength;
				setAlpha((int)(alpha * 75));
			}
			Integer[] frameValues = values.get(i - 1);
			for (int j = 0; j < frameValues.length; j++) {
				int currentX = mRect.width() / mLinesCount * j + mRect.width() / mLinesCount / 2;
				for (int k = 0; k < frameValues[j]; k += 12) {
					canvas.drawLine(currentX - mLinesStroke, mRect.height() - k, currentX + mLinesStroke, mRect.height() - k, mForePaint);
				}
			}
		}
//		LOCK.notifyAll();
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
				int position = i * mLinesCount + j;
				byte rfk = mBytes[position];
				byte ifk = mBytes[position + 1];
				float magnitude = (rfk * rfk + ifk * ifk);
				if (magnitude > 0) {
					value += (10 * Math.log10(magnitude));
				}
			}
			currentValues[i] = value / mLinesCount * 2;
		}
		performDraw(canvas, currentValues);
	}
}