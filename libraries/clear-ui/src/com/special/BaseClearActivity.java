package com.special;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.special.menu.ResideMenu;
import com.special.menu.ResideMenuItem;

public abstract class BaseClearActivity extends FragmentActivity implements View.OnClickListener{

    private static final String FRAGMENT = "Fragment";
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
				resideMenu.openMenu();
			}
		});
        findViewById(R.id.title_bar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resideMenu.openMenu();
			}
		});
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {
        for (int i = 0; i < menuItems.length; i++) {
        	if (view == menuItems[i]) {
        		if (lastOpenedFragment.getClass().getSimpleName().equals(fragments[i].getClass().getSimpleName())) {
        			resideMenu.closeMenu();
        			return;
        		} else {
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
		getSupportFragmentManager()
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
			getSupportFragmentManager().popBackStack();
			FragmentManager.BackStackEntry backEntry = (BackStackEntry) getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 2);
			String lastFragmentName = backEntry.getName();
			lastOpenedFragment = getSupportFragmentManager().findFragmentByTag(lastFragmentName);
			tvTitle.setText(lastFragmentName.replace(FRAGMENT, ""));
		} else {
			stopChildsServices();
			finish();
		}
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
    
   public void stopChildsServices(){
   }
}
