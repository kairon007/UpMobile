package org.upmobile.newmaterialmusicdownloader.fragment;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.Constants;
import android.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PlayerFragment extends Fragment implements Constants, OnClickListener , OnCheckedChangeListener{

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
	}

	@Override
	public void onClick(View v) {
		
	}
	
	@Override
	public void onResume() {
		((MainActivity) getActivity()).setTitle(R.string.tab_now_plaing);
		((MainActivity) getActivity()).invalidateOptionsMenu();
		super.onResume();
	}
}
