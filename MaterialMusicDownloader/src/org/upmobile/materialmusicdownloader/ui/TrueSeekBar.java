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
		init(context);
	}

	public TrueSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TrueSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		int colorAccent = context.getResources().getColor(R.color.material_accent);
		Drawable drawable = context.getResources().getDrawable(R.drawable.progress_indeterminate_horizontal_holo);
		drawable.setColorFilter(new PorterDuffColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP));
		setIndeterminateDrawable(drawable);
		setProgressDrawable(context.getResources().getDrawable(R.drawable.seekbar));
	}

}
