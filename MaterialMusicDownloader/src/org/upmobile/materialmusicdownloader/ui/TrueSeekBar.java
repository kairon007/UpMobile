package org.upmobile.materialmusicdownloader.ui;

import org.upmobile.materialmusicdownloader.R;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class TrueSeekBar extends SeekBar {

	public TrueSeekBar(Context context) {
		this(context, null);
	}

	public TrueSeekBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TrueSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		int colorAccent = context.getResources().getColor(R.color.material_accent);
		Drawable drawable = context.getResources().getDrawable(R.drawable.progress_indeterminate_horizontal_holo);
		drawable.setColorFilter(new PorterDuffColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP));
		setIndeterminateDrawable(drawable);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		boolean current = isIndeterminate();
		setIndeterminate(!current);
		super.onSizeChanged(w, h, oldw, oldh);
		setIndeterminate(current);
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
}
