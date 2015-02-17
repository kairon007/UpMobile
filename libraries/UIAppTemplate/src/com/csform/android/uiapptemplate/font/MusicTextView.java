package com.csform.android.uiapptemplate.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MusicTextView extends TextView {

	private static Typeface musicFont;

	public MusicTextView(Context context) {
		super(context);
		if (isInEditMode()) return; //Won't work in Eclipse graphical layout
		setTypeface();
	}

	public MusicTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) return;
		setTypeface();
	}

	public MusicTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		setTypeface();
	}
	
	private void setTypeface() {
		if (musicFont == null) {
			musicFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/MusicFont.otf");
		}
		setTypeface(musicFont);
	}
}