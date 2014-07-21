package org.kreed.musicdownloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioGroup;

public class MainActivity extends Activity implements
		DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewGroup layout = (ViewGroup) findViewById(R.id.activity_main);
		View searchView = SearchTab.getInstanceView(getLayoutInflater(), this);
		layout.addView(searchView);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
//		ListView list = ((AlertDialog)dialog).getListView();
//		// subtract 1 for header
//		int which = list.getCheckedItemPosition() - 1;
//
//		RadioGroup group = (RadioGroup)list.findViewById(R.id.sort_direction);
//		if (group.getCheckedRadioButtonId() == R.id.descending)
//			which = ~which;

//		mPagerAdapter.setSortMode(which);
	}

	@Override
	public void onClick(DialogInterface dialog, int arg1) {
		// TODO Auto-generated method stub
		dialog.dismiss();
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (id == SearchTab.STREAM_DIALOG_ID) {
			return SearchTab.getInstance(getLayoutInflater(), this).createStreamDialog(args);
		}
		return super.onCreateDialog(id, args);
	}
}
