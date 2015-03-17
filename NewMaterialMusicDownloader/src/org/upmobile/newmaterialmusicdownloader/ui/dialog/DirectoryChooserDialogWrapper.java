package org.upmobile.newmaterialmusicdownloader.ui.dialog;

import android.content.Context;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;

public class DirectoryChooserDialogWrapper extends DirectoryChooserDialog {

	public DirectoryChooserDialogWrapper(Context context, boolean isWhiteTheme, ChosenDirectoryListener chosenDirectoryListener) {
		super(context, isWhiteTheme, chosenDirectoryListener);
	}
	
	@Override
	protected boolean getResourcefromTheme() {
		return true;
	}
	
}
