package ru.johnlife.lifetoolsmp3.ui.dialog;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Build;
import android.view.ContextThemeWrapper;

public class CustomDialogBuilder extends Builder {
	
	public CustomDialogBuilder(Context context) {
		super(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	public static Builder getBuilder(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			return new Builder(context);
		} else {
			return new Builder(new ContextThemeWrapper(context, Util.getResIdFromAttribute((Activity) context, R.attr.customAlertDialogStyle)));
		}
	}
}
