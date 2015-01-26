package org.upmobile.musicpro.fragment;

import org.upmobile.musicpro.BaseFragment;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.slidingmenu.SlidingMenu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends BaseFragment {
	
	private TextView lblCompanyWebsite;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);
		initUIBase(view);
		setButtonMenu(view);
		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			getMainActivity().menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			getMainActivity().setVisibilityFooter();
		}
	}

	@Override
	protected void initUIBase(View view) {
		super.initUIBase(view);
		lblCompanyWebsite = (TextView) view.findViewById(R.id.lblCompanyWebsite);
				

		lblCompanyWebsite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uriUrl = Uri.parse(getActivity().getString(R.string.companyWebsite));
				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
				startActivity(launchBrowser);
			}
		});
	}
}
