package com.special;

import java.util.ArrayList;
import java.util.Arrays;

import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.special.menu.ResideMenu;
import com.special.menu.ResideMenuItem;

public abstract class BaseClearActivity extends BaseMiniPlayerActivity implements View.OnClickListener{

	private ResideMenu resideMenu;
    private ResideMenuItem[] menuItems;
    private Fragment[] fragments;
    private String[] titles;
    private LinearLayout topFrame;
	private Fragment lastOpenedFragment;
	private TextView tvTitle;
  
    protected abstract Fragment[] getFragments();
    
    protected abstract ResideMenuItem[] getMenuItems();
    
    protected abstract String[] getTitlePage();
    
    protected abstract Bundle getArguments();
    
    protected abstract Fragment getPlayerFragment();
    
    protected abstract void showDialog();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        menuItems = getMenuItems();
        fragments = getFragments();
        titles = getTitlePage();
        setUpMenu();
        changeFragment(getFragments()[0]);
        tvTitle.setText(titles[0]);
        hidePlayerElement();
    }

	private void init() {
		topFrame = (LinearLayout) findViewById(R.id.layout_top);
        tvTitle = (TextView) findViewById(R.id.page_title);
	}

    private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setShadowVisible(true);
        resideMenu.setHeaderView(findViewById(R.id.actionbar));
        resideMenu.setMenuListener(menuListener);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip. 
        resideMenu.setScaleValue(0.6f);
        for (ResideMenuItem item : menuItems) {
        	item.setOnClickListener(this);
        	item.setBackgroundResource(R.drawable.button_selector_inverse_light);
        	resideMenu.addMenuItem(item);
        }
        findViewById(R.id.title_bar_left_menu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)BaseClearActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				resideMenu.openMenu();
			}
		});
        findViewById(R.id.title_bar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openMenu();
			}
		});
    }

    @Override
    public void onClick(View view) {
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        for (int i = 0; i < menuItems.length; i++) {
        	if (view == menuItems[i]) {
        		if (menuItems[i].getType() == ResideMenuItem.Types.TYPE_SETTINGS) {
        			showDialog(); 
        			break;
        		}
        		if (lastOpenedFragment.getClass().getSimpleName().equals(fragments[i].getClass().getSimpleName())) {
        			resideMenu.closeMenu();
        			return;
        		}
        		if (fragments[i].getClass().getSimpleName().equals("PlayerFragment") && null != getArguments()) {
        			Fragment fragment = getPlayerFragment();
        			fragment.setArguments(getArguments());
        			changeFragment(fragment);
        		} else {
        			changeFragment(fragments[i]);
        		}
        		tvTitle.setText(titles[i]);
        	}
        }
        resideMenu.closeMenu();
    }

	//Example of menuListener
    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() { }

        @Override
        public void closeMenu() { }
    };

    public void changeFragment(Fragment targetFragment){
        this.lastOpenedFragment = targetFragment;
		resideMenu.clearIgnoredViewList();
		getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, targetFragment.getClass().getSimpleName())
                .addToBackStack(targetFragment.getClass().getSimpleName()) 
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    //return the residemenu to fragments
    public ResideMenu getResideMenu(){
        return resideMenu;
    }
    
    /**
     * Beware, this method is suitable only for api > 11 if you want to port, you need to think of another way
     */
    
	@Override
	public void onBackPressed() {
		if (lastOpenedFragment.getClass().getSimpleName().equals(getFragments()[3].getClass().getSimpleName())){
			getFragmentManager().popBackStack();
			FragmentManager.BackStackEntry backEntry = (BackStackEntry) getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 2);
			String lastFragmentName = backEntry.getName();
			lastOpenedFragment = getFragmentManager().findFragmentByTag(lastFragmentName);
			String title = getNameCurrentFragment(lastFragmentName);
			tvTitle.setText(title);
		} else {
			stopChildsServices();
			finish();
		}
	}

	private String getNameCurrentFragment(String lastFragmentName) {
		for (int i = 0; i < getFragments().length; i++) {
			String fragmentName =  getFragments()[i].getClass().getSimpleName();
			if (lastFragmentName.equals(fragmentName)) {
				return titles[i];
			} 
		}
		return titles[0];
	}
    
//    @SuppressLint("NewApi") @Override
//	public void onBackPressed() {
//		if (lastOpenedFragment.getClass().getSimpleName().equals(getFragments()[3].getClass().getSimpleName())) {
//			getSupportFragmentManager().popBackStack();
//			FragmentManager.BackStackEntry backEntry = (BackStackEntry) getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 2);
//			String lastFragmentName = backEntry.getName();
//			if (lastFragmentName.equals(getFragments()[0].getClass().getSimpleName())) {
//				tvTitle.setText(titles[0]);
//			} else if (lastFragmentName.equals(getFragments()[1].getClass().getSimpleName())) {
//				tvTitle.setText(titles[1]);
//			} else if (lastFragmentName.equals(getFragments()[2].getClass().getSimpleName())) {
//				tvTitle.setText(titles[2]);
//			}
//			lastOpenedFragment = getSupportFragmentManager().findFragmentByTag(lastFragmentName);
//			return;
//		}
//    	if (resideMenu.isOpened()){
//    		resideMenu.closeMenu();
//    	} else {
//    		resideMenu.openMenu();
//    	}
//    }
    
	public void showTopFrame() {
		topFrame.setVisibility(View.VISIBLE);
	}

	public void hideTopFrame() {
		topFrame.setVisibility(View.GONE);
	}

	public void hidePlayerElement() {
		resideMenu.hideLastElement();
	}

	public void showPlayerElement() {
		resideMenu.showLastElement();
	}

	public void stopChildsServices() {
	}

	public void openMenu() {
		resideMenu.openMenu();
	}
	
	public void reDrawMenu(){
        resideMenu.setMenuItems(new ArrayList<ResideMenuItem>(Arrays.asList(getMenuItems())));
	}
	
}
