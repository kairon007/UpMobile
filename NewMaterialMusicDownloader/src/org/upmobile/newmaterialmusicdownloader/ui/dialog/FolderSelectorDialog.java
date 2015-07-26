package org.upmobile.newmaterialmusicdownloader.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog;
import ru.johnlife.uilibrary.widget.dialogs.materialdialog.MaterialDialog.ButtonCallback;

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
            StateKeeper.getInstance().setLibraryAdapterItems(null);
			mCallback.onFolderSelection(parentFolder);
			StateKeeper.getInstance().setLibraryFirstPosition(0);
		}

		@Override
		public void onNeutral(MaterialDialog dialog) {
			MaterialDialog dlg = new MaterialDialog.Builder(getActivity())
			.title(R.string.add_new_folder)
			.titleColorAttr(R.attr.colorTextPrimary)
			.positiveColorAttr(R.attr.colorPrimary)
			.customView(R.layout.md_input_dialog, false)
			.callback(new ButtonCallback() {
				
				@Override
				public void onNegative(MaterialDialog dialog) {
					Util.hideKeyboard(getActivity(), dialog.getCustomView());
					dialog.dismiss();
				};
				
				@Override
				public void onPositive(MaterialDialog dialog) {
					StateKeeper.getInstance().setLibraryFirstPosition(0);
					Util.hideKeyboard(getActivity(), dialog.getCustomView());
					EditText input = (EditText) dialog.findViewById(android.R.id.edit);
					String temp = input.getText().toString().trim();
					String newDirName = temp.length() > 29 ? temp.substring(0, 30) : temp;
					if (newDirName.isEmpty()) {
						((MainActivity) getActivity()).showMessage("Folder name can not be empty");
						return;
					}
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
			((EditText)dlg.getCustomView().findViewById(android.R.id.edit)).setHint(R.string.folder_name);
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

	private String[] getContentsArray() {
		String[] results = new String[parentContents.length + (canGoUp ? 1 : 0)];
		if (canGoUp)
			results[0] = "...";
		for (int i = 0; i < parentContents.length; i++)
			results[canGoUp ? i + 1 : i] = parentContents[i].getName();
		return results;
	}

	private File[] listFiles() {
		File[] contents = parentFolder.listFiles();
		if (null == contents) return new File[0];
		List<File> results = new ArrayList<File>();
		List<File> audioFiles = new ArrayList<File>();
		for (File fi : contents) {
			if (fi.canRead() && !fi.getName().startsWith(".")) {
				if (fi.isDirectory()) {
					results.add(fi);
				} else if (fi.getName().endsWith(Constants.AUDIO_END)) {
					audioFiles.add(fi);
				}
			}
		}
		Collections.sort(results, new FolderSorter());
		results.addAll(audioFiles);
		return results.toArray(new File[results.size()]);
	}

	private String[] getAudioFiles() {
		File[] contents = parentFolder.listFiles();
		if (null == contents) return new String[0];
		List<String> audioFiles = new ArrayList<String>();
		for (File fi : contents) {
			if (fi.getName().endsWith(Constants.AUDIO_END)) {
				audioFiles.add(fi.getName());
			}
		}
		return audioFiles.toArray(new String[audioFiles.size()]);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new MaterialDialog.Builder(getActivity())
				.title(parentFolder.getAbsolutePath())
				.items(getContentsArray())
				.notClickableItems(getAudioFiles())
				.titleColorAttr(R.attr.colorTextPrimary)
				.itemColorAttr(R.attr.colorTextSecondary)
				.positiveColorAttr(R.attr.colorPrimary)
				.neutralColorAttr(R.attr.colorPrimary)
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
		((MainActivity) getActivity()).setCurrentFragmentId(((MainActivity) getActivity()).getCurrentFragmentId());
		super.onDismiss(dialog);
	}
	
	@Override
	public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
		if (canGoUp && i == 0) {
			parentFolder = parentFolder.getParentFile();
			canGoUp = !"/".equals(parentFolder.getParent());
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
		dialog.setNotClickableItems(getAudioFiles());
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
