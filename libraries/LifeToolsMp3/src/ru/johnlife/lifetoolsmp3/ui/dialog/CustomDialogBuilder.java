package ru.johnlife.lifetoolsmp3.ui.dialog;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Build;

public class CustomDialogBuilder extends Builder {
	
	public CustomDialogBuilder(Context context) {
		super(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	public static Builder getBuilder(Context context, boolean isWhiteTheme) {
		boolean oldApi = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
		if (isWhiteTheme && !oldApi) {
			return new Builder(context, AlertDialog.THEME_HOLO_LIGHT);
		} else {
			return new Builder(context);
		}
	}
}
