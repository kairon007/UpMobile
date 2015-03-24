package ru.johnlife.lifetoolsmp3.ui.widget.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class SimpleVisualizerView extends View {
	private byte[] mBytes;
	private Rect mRect = new Rect();
	protected float[] mPoints;
	protected float[] mFFTPoints;
	private Paint mForePaint = new Paint();
	private int mDivisions;

	public SimpleVisualizerView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mBytes = null;
		mForePaint.setStrokeWidth(1f);
		mForePaint.setAntiAlias(true);
		mForePaint.setColor(getResources().getColor(android.R.color.white));
	}

	public void updateVisualizer(byte[] bytes) {
		mBytes = bytes;
		invalidate();
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

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mDivisions = 10;
		mForePaint.setStrokeWidth(28f);
		mRect.set(0, 0, getWidth(), getHeight());
		if (mBytes == null) return;
		if (mFFTPoints == null || mFFTPoints.length < mBytes.length * 4) {
			mFFTPoints = new float[mBytes.length * 4];
		}
		for (int i = 0; i < mBytes.length / mDivisions; i++) {
			mFFTPoints[i * 4] = i * 4 * mDivisions;
			mFFTPoints[i * 4 + 2] = i * 4 * mDivisions;
			byte rfk = mBytes[mDivisions * i];
			byte ifk = mBytes[mDivisions * i + 1];
			float magnitude = (rfk * rfk + ifk * ifk);
			int dbValue = (int) (10 * Math.log10(magnitude));
			mFFTPoints[i * 4 + 1] = mRect.height();
			mFFTPoints[i * 4 + 3] = mRect.height() - (dbValue * 4);
		}
		canvas.drawLines(mFFTPoints, mForePaint);
	}
}