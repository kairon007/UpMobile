package org.kreed.musicdownloader;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

@SuppressWarnings("deprecation")
public class MusicActivity extends FragmentActivity implements
		DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FragmentManager manager = getSupportFragmentManager();
		manager.beginTransaction()
				.replace(R.id.activity_main, new SearchFragment()).commit();
		// ViewGroup layout = (ViewGroup) findViewById(R.id.activity_main);
		// View searchView = SearchTab.getInstanceView(getLayoutInflater(),
		// this);
		// layout.addView(searchView);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(DialogInterface dialog, int arg1) {
		// TODO Auto-generated method stub
		dialog.dismiss();
	}

//	@SuppressWarnings("deprecation")
//	@Override
//	protected Dialog onCreateDialog(int id, Bundle args) {
//		if (id == SearchTab.STREAM_DIALOG_ID) {
//			return SearchTab.getInstance(getLayoutInflater(), this)
//					.createStreamDialog(args);
//		}
//		return super.onCreateDialog(id, args);
//	}
}
