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
		super(context);
	}
	
	public TrueSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TrueSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public synchronized void setIndeterminate(boolean indeterminate) {
		if (isIndeterminate() == indeterminate) return;
//		if (indeterminate) {
			int colorAccent = getContext().getResources().getColor(R.color.material_accent);
			Drawable drawable = getContext().getResources().getDrawable(R.drawable.progress_indeterminate_horizontal_holo);
			drawable.setColorFilter(new PorterDuffColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP));
			setIndeterminateDrawable(drawable);
//		} else {
			setProgressDrawable(getContext().getResources().getDrawable(R.drawable.seekbar));
//		}
		super.setIndeterminate(indeterminate);
		android.util.Log.d("logd", "setIndeterminate() = " + indeterminate);
	}
}
