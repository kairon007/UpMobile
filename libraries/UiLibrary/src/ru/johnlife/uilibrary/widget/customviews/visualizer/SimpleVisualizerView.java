package ru.johnlife.uilibrary.widget.customviews.visualizer;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.uilibrary.widget.Utils;
import android.annotation.TargetApi;
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
	private Paint primaryPaint;
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	private void init() {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		primaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		primaryPaint.setStyle(Style.STROKE);
		primaryPaint.setColor(getResources().getColor(android.R.color.white));
		primaryPaint.setAlpha(150);
		int widthFill = Utils.dpToPx(getContext(), 8);
		int widthDash = Utils.dpToPx(getContext(), 2);
		primaryPaint.setPathEffect(new DashPathEffect(new float[]{widthFill, widthDash}, 0));
	}

	public synchronized void updateVisualizer(byte[] bytes) {
		mBytes = bytes;
		invalidate();
	}
	
	public void setUpVizualizerColor (int primary, int accent) {
		this.primary = primary;
		this.accent = accent;
	}

	private void performDraw(Canvas canvas, Integer[] newValues) {
		values.add(newValues);
		if (values.size() > frameLength) {
			values.remove(0);
		}
		for (int i = 0; i < values.size(); i++) {
			if (i == values.size() - 1) {
				primaryPaint.setColor(primary != -1 ? primary : Color.WHITE);
				primaryPaint.setAlpha(150);
			} else {
				primaryPaint.setColor(accent != -1 ? accent : Color.WHITE);
				float alpha = ((float) i / (float) frameLength) * 100;
				primaryPaint.setAlpha((int) alpha);
			}
			Integer[] frameValues = values.get(i);
			for (int j = 0; j < frameValues.length; j++) {
				int currentX = mRect.width() / mLinesCount * j + mRect.width() / mLinesCount / 2;
				int height = mRect.height() - frameValues[j];
				canvas.drawLine(currentX, mRect.height() , currentX, height, primaryPaint);
			}
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mRect.set(0, 0, getWidth(), getHeight());
		mLinesStroke = getWidth() / mLinesCount;
		mLinesStroke = (int)(mLinesStroke * 0.45);
		primaryPaint.setStrokeWidth(mLinesStroke * 2);
	}
	
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