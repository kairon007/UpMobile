package com.special;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.menu.ResideMenu;

public class HomeFragment extends Fragment {

    private View parentView;
    private ResideMenu resideMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_home, container, false);
        setUpViews();
        return parentView;
    }

    private void setUpViews() {
        BaseClearActivity parentActivity = (BaseClearActivity) getActivity();
        resideMenu = parentActivity.getResideMenu();

        parentView.findViewById(R.id.btn_open_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu();
            }
        });
        
        parentView.findViewById(R.id.btn_open_site).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	String url = "http://codecanyon.net/user/sherdleapps/portfolio?ref=sherdleapps";
            	Intent i = new Intent(Intent.ACTION_VIEW);
            	i.setData(Uri.parse(url));
            	startActivity(i);
            }
        });
        
        
    }

}
