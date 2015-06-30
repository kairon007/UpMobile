package ru.johnlife.lifetoolsmp3.ui.dialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Build;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DirectoryChooserDialog {
	
	private static final String STORAGE = "/storage";
	private List<String> m_subdirs = null;
	private ArrayAdapter<String> m_listAdapter = null;
	private DialogInterface dialog;
	private ChosenDirectoryListener m_chosenDirectoryListener = null;
	private Context m_context;
	private StateKeeper keeper;
	private File parent;
	private ListView lvContent;
	private ImageButton newDirButton;
	private TextView titleText;
	private String m_sdcardDirectory = "";
	private String m_dir = "";
	private boolean m_isNewFolderEnabled = true;
	OnShowListener showlistener = new OnShowListener() {

		@Override
		public void onShow(DialogInterface dialog) {
			float textSize = 16f;
			DirectoryChooserDialog.this.dialog = dialog;
//			enableButtons();
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(textSize);
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(textSize);
		}
	};

	//////////////////////////////////////////////////////
	// Callback interface for selected directory        //
	//////////////////////////////////////////////////////
	public interface ChosenDirectoryListener {
		public void onChosenDir(String chosenDir);
	}

	public DirectoryChooserDialog(Context context, ChosenDirectoryListener chosenDirectoryListener) {
		keeper = StateKeeper.getInstance();
		keeper.activateOptions(StateKeeper.BTN_ENABLED);
		m_context = context;
		m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
		m_chosenDirectoryListener = chosenDirectoryListener;
		try {
			m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
		} catch (IOException ioe) {
		}
	}
	
	protected boolean getResourcefromTheme() {
		return false;
	}

	public void setNewFolderEnabled(boolean isNewFolderEnabled) {
		m_isNewFolderEnabled = isNewFolderEnabled;
	}

	public boolean getNewFolderEnabled() {
		return m_isNewFolderEnabled;
	}

	public void chooseDirectory() {
		// Initial directory is sdcard directory
		chooseDirectory(m_sdcardDirectory);
	}

	public void chooseDirectory(String dir) {
		File dirFile = new File(dir);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			dir = m_sdcardDirectory;
		}
		try {
			dir = new File(dir).getCanonicalPath();
		} catch (IOException ioe) {
			return;
		}
		m_dir = dir;
		m_subdirs = getDirectories(dir);
		class DirectoryOnClickListener implements DialogInterface.OnClickListener {

			public void onClick(DialogInterface dialog, int item) {
				String directory = (String) ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
				if (directory.equals(""))
					return;
				m_dir += "/" + directory;
				enableButtons();
				updateDirectory();
			}
		}
		AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(dir, m_subdirs, new DirectoryOnClickListener());
		dialogBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (m_chosenDirectoryListener != null) {
					m_chosenDirectoryListener.onChosenDir(m_dir);
				}
				StateKeeper.getInstance().setLibaryFirstPosition(0);
				keeper.closeDialog(StateKeeper.DIRCHOOSE_DIALOG);
			}
		}).setNegativeButton(android.R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				keeper.closeDialog(StateKeeper.DIRCHOOSE_DIALOG);
				StateKeeper.getInstance().setDirectoryChooserPath(null);
			}
		});
		final AlertDialog dirsDialog = dialogBuilder.create();
		dirsDialog.setOnShowListener(showlistener);
		dirsDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
					dirsDialog.dismiss();
					keeper.closeDialog(StateKeeper.DIRCHOOSE_DIALOG);
					return true;
				}
				return false;
			}
		});
		dirsDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// Show directory chooser dialog
		dirsDialog.show();
		keeper.openDialog(StateKeeper.DIRCHOOSE_DIALOG);
	}

	private boolean createSubDir(String newDir) {
		File newDirFile = new File(newDir);
		if (!newDirFile.exists()) {
			return newDirFile.mkdir();
		}

		return false;
	}

	private List<String> getDirectories(String dir) {
		List<String> dirs = new ArrayList<String>();
		File system = new File(Environment.getExternalStorageDirectory().getPath() + "/Android");
		parent = new File(dir).getParentFile();
		try {
			File dirFile = new File(dir);
			if (!dirFile.exists() || !dirFile.isDirectory()) {
				return dirs;
			}

			for (File file : dirFile.listFiles()) {
				if (file.isDirectory() && !file.getPath().equals(system.getPath())) {
					dirs.add(file.getName());
				}
			}
		} catch (Exception e) {
		}
		Collections.sort(dirs, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		return dirs;
	}

	private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems, DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder dialogBuilder;
		if (getResourcefromTheme()) {
			dialogBuilder = new AlertDialog.Builder(m_context);
		} else {
			dialogBuilder = CustomDialogBuilder.getBuilder(m_context);
		}
		LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View contentView;
//		if(isWhiteTheme || Util.getThemeName(m_context).equals(Util.WHITE_THEME)) {
//			contentView = inflater.inflate(R.layout.dir_chooser_dialog_white, null);
//		} else {
//			contentView = inflater.inflate(R.layout.dir_chooser_dialog, null);
//		}
		View contentView = inflater.inflate(Util.getResIdFromAttribute((Activity) m_context, R.attr.dir_chooser_dialog), null);
		titleText = (TextView) contentView.findViewById(R.id.directoryText);
		lvContent = (ListView) contentView.findViewById(R.id.lvPath);
		titleText.setText(title);
		ImageButton parentDirectory = (ImageButton) contentView.findViewById(R.id.imageButton1);
		newDirButton = (ImageButton) contentView.findViewById(R.id.imageButton2);
		newDirButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show new folder name input dialog
				createNewDirDialog(null);
			}
		});
		parentDirectory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (m_dir.equals(STORAGE)) {
					keeper.deactivateOptions(StateKeeper.BTN_ENABLED);
					enableButtons();
					return;
				}
				if (null == parent) return;
				m_dir = parent.getPath();
				if (m_dir.equals(STORAGE)) {
					keeper.deactivateOptions(StateKeeper.BTN_ENABLED);
					enableButtons();
				}
				updateDirectory();
			}
		});
		if (!m_isNewFolderEnabled) {
			newDirButton.setVisibility(View.GONE);
		}
		m_listAdapter = createListAdapter(listItems);
		if (listItems.isEmpty()) {
			m_listAdapter.add("");
		}
		dialogBuilder.setCancelable(false);
		lvContent.setAdapter(m_listAdapter);
		lvContent.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					String directory = (String) lvContent.getAdapter().getItem(position);
					if (directory.equals(""))
						return;
					m_dir += "/" + directory;
					if (!m_dir.equals(STORAGE)) {
						keeper.activateOptions(StateKeeper.BTN_ENABLED);
					}
					enableButtons();
					updateDirectory();
				} catch(Exception e) {
					Log.e(getClass().getSimpleName(), e.getMessage());
				}
			}
		});
		dialogBuilder.setView(contentView);
		return dialogBuilder;
	}
	
	public void enableButtons() {
		boolean enable = keeper.checkState(StateKeeper.BTN_ENABLED);
		((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(enable);
		((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setClickable(enable);
		newDirButton.setEnabled(enable);
		newDirButton.setClickable(enable);
	}

	@SuppressLint("NewApi")
	public void createNewDirDialog(String name) {
		keeper.openDialog(StateKeeper.NEWDIR_DIALOG);
		LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view;
//		if (isWhiteTheme){
//			view = inflater.inflate(R.layout.new_folder_dialog_white, null);
//		} else {
//			view = inflater.inflate(R.layout.new_folder_dialog, null);
//		}
		view = inflater.inflate(Util.getResIdFromAttribute((Activity) m_context, R.attr.new_folder_dialog), null);
		final EditText input = (EditText) view.findViewById(R.id.etNewFolder);
		input.addTextChangedListener(new CustomWatcher(input));
		if (null != name) {
			StateKeeper.getInstance().setNewDirName(name);
			input.setText(name);
		} else {
			StateKeeper.getInstance().setNewDirName("");
		}
		AlertDialog.Builder builder = CustomDialogBuilder.getBuilder(m_context).setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				keeper.closeDialog(StateKeeper.NEWDIR_DIALOG);
				Editable newDir = input.getText();
				String newDirName = newDir.toString();
				// Create new directory
				if (createSubDir(m_dir + "/" + newDirName)) {
					// Navigate into the new directory
					m_dir += "/" + newDirName;
					updateDirectory();
				} else {
					Toast.makeText(m_context, "Failed to create '" + newDirName + "' folder", Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				keeper.closeDialog(StateKeeper.NEWDIR_DIALOG);
			}
		});
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
			builder.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					keeper.closeDialog(StateKeeper.NEWDIR_DIALOG);
				}
			});
		}
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(showlistener);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		dialog.show();
	}

	private void updateDirectory() {
		if (!new File(m_dir).exists()) {
			m_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
		}
		m_subdirs.clear();
		m_subdirs.addAll(getDirectories(m_dir));
		titleText.setText(m_dir);
		m_listAdapter.notifyDataSetChanged();
		if (m_listAdapter.isEmpty()) {
			m_listAdapter.add("");
		}
		StateKeeper.getInstance().setDirectoryChooserPath(m_dir);
	}

	private ArrayAdapter<String> createListAdapter(List<String> items) {
		return new ArrayAdapter<String>(m_context, android.R.layout.select_dialog_item, android.R.id.text1, items) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				if (v instanceof TextView  && !getResourcefromTheme()) {
					// Enable list item (directory) text wrapping
					TextView tv = (TextView) v;
					tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
					tv.getLayoutParams().width = LayoutParams.MATCH_PARENT;
					tv.setGravity(Gravity.CENTER_VERTICAL);
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
//						if (isWhiteTheme || Util.getThemeName(m_context).equals(Util.WHITE_THEME) || keeper.checkState(StateKeeper.IS_PT_TEXT)) {
//							tv.setTextColor(Color.BLACK);
//						} else {
//							tv.setTextColor(Color.WHITE);
//						}
						tv.setTextColor(m_context.getResources().getColor(
								Util.getResIdFromAttribute((Activity)m_context, R.attr.directory_chooser_tv_color)));
					}
					
					tv.setTextSize(16f);
					tv.setEllipsize(null);
				}
				return v;
			}
		};
	}

	private class CustomWatcher implements TextWatcher {

		private EditText linkToInput;

		public CustomWatcher(EditText linkToInput) {
			this.linkToInput = linkToInput;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			StateKeeper.getInstance().setNewDirName(linkToInput.getText().toString());
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	}
}
