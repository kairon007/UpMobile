package org.upmobile.newmaterialmusicdownloader.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;

import com.csform.android.uiapptemplate.view.dlg.MaterialDialog;
import com.csform.android.uiapptemplate.view.dlg.MaterialDialog.ButtonCallback;

public class FolderSelectorDialog extends DialogFragment implements	MaterialDialog.ListCallback {

	private static final String TAG = "FOLDER_SELECTOR";
	private File parentFolder;
	private File[] parentContents;
	private boolean canGoUp = true;
	private FolderSelectCallback mCallback;
	private final MaterialDialog.ButtonCallback mButtonCallback = new MaterialDialog.ButtonCallback() {
		@Override
		public void onPositive(MaterialDialog materialDialog) {
			materialDialog.dismiss();
			mCallback.onFolderSelection(parentFolder);
		}

		@Override
		public void onNeutral(MaterialDialog dialog) {
			MaterialDialog dlg = new MaterialDialog.Builder(getActivity())
			.title(R.string.add_new_folder)
			.titleColorAttr(R.attr.colorTextPrimaryApp)
			.positiveColorAttr(R.attr.colorPrimaryApp)
			.customView(R.layout.md_input_dialog, false)
			.callback(new ButtonCallback() {
				
				@Override
				public void onNegative(MaterialDialog dialog) {
					dialog.dismiss();
				};
				
				@Override
				public void onPositive(MaterialDialog dialog) {
					EditText input = (EditText) dialog.findViewById(android.R.id.edit);
					String newDirName = input.getText().toString();
					if (createSubDir(parentFolder.getAbsolutePath() + "/" + newDirName)) {
						parentFolder = new File(parentFolder.getAbsolutePath() + "/" + newDirName);
						updateList();
					} else {
						((MainActivity) getActivity()).showMessage("Failed to create '" + newDirName + "' folder");
					}
				};
				
				private boolean createSubDir(String newDir) {
					File newDirFile = new File(newDir);
					if (!newDirFile.exists()) {
						return newDirFile.mkdir();
					}
					return false;
				};
			})
			.positiveText(android.R.string.ok)
			.negativeText(android.R.string.cancel)
			.build();
			dlg.show();
		}
		
		@Override
		public void onNegative(MaterialDialog materialDialog) {
			materialDialog.dismiss();
		}
	};

	public static interface FolderSelectCallback {
		void onFolderSelection(File folder);
	}

	public FolderSelectorDialog() {
		parentFolder = Environment.getExternalStorageDirectory();
		parentContents = listFiles();
	}

	String[] getContentsArray() {
		String[] results = new String[parentContents.length + (canGoUp ? 1 : 0)];
		if (canGoUp)
			results[0] = "...";
		for (int i = 0; i < parentContents.length; i++)
			results[canGoUp ? i + 1 : i] = parentContents[i].getName();
		return results;
	}

	File[] listFiles() {
		File[] contents = parentFolder.listFiles();
		List<File> results = new ArrayList<File>();
		for (File fi : contents) {
			if (fi.isDirectory())
				results.add(fi);
		}
		Collections.sort(results, new FolderSorter());
		return results.toArray(new File[results.size()]);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new MaterialDialog.Builder(getActivity())
				.title(parentFolder.getAbsolutePath())
				.items(getContentsArray())
				.titleColorAttr(R.attr.colorTextPrimaryApp)
				.itemColorAttr(R.attr.colorTextSecondaryApp)
				.positiveColorAttr(R.attr.colorPrimaryApp)
				.neutralColorAttr(R.attr.colorPrimaryApp)
				.itemsCallback(this)
				.callback(mButtonCallback)
				.autoDismiss(false)
				.neutralText(R.string.add_folder)
				.positiveText(android.R.string.ok)
				.negativeText(android.R.string.cancel)
				.build();
		
		
		
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		int id = ((MainActivity) getActivity()).getCurrentFragmentId();
		((MainActivity) getActivity()).setCurrentFragmentId(id);
		super.onDismiss(dialog);
	}
	
	@Override
	public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
		if (canGoUp && i == 0) {
			parentFolder = parentFolder.getParentFile();
			canGoUp = parentFolder.getParent() != null;
		} else {
			parentFolder = parentContents[canGoUp ? i - 1 : i];
			canGoUp = true;
		}
		updateList();
	}
	
	private void updateList(){
		parentContents = listFiles();
		MaterialDialog dialog = (MaterialDialog) getDialog();
		dialog.setTitle(parentFolder.getAbsolutePath());
		dialog.setItems(getContentsArray());
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (FolderSelectCallback) activity;
	}

	public void show(Activity context) {
		show(context.getFragmentManager(), TAG);
	}

	private static class FolderSorter implements Comparator<File> {
		@Override
		public int compare(File lhs, File rhs) {
			return lhs.getName().compareTo(rhs.getName());
		}
	}
}
