package com.special;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.special.menu.ResideMenu;
import com.special.menu.ResideMenuItem;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;

public abstract class BaseClearActivity extends BaseMiniPlayerActivity implements Constants {
	
	public static final String CALL_FROM_MENU = "call.player.fragment.from.reside.menu";

	private ResideMenu resideMenu;
    private ResideMenuItem[] menuItems;
    private Fragment[] fragments;
    private String[] titles;
    private LinearLayout topFrame;
	private Fragment lastOpenedFragment;
	private TextView tvTitle;
	protected boolean isBackButtonEnabled = false;
  
    protected abstract Fragment[] getFragments();
    protected abstract ResideMenuItem[] getMenuItems();
    protected abstract String[] getTitlePage();
    protected abstract Fragment getPlayerFragment();
    protected abstract void showDialog();
    protected abstract boolean isPlaying();
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        menuItems = getMenuItems();
        fragments = getFragments();
        titles = getTitlePage();
        setUpMenu();
        changeFragment(getFragments()[Constants.SEARCH_FRAGMENT], false);
        tvTitle.setText(titles[Constants.SEARCH_FRAGMENT]);
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
        setMenuListener(menuListener);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip. 
        resideMenu.setScaleValue(0.6f);
        for (ResideMenuItem item : menuItems) {
        	item.setOnClickListener(this);
        	item.setBackgroundResource(R.drawable.button_selector_inverse_light);
        	resideMenu.addMenuItem(item);
        }
        findViewById(R.id.title_bar_left_menu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Util.hideKeyboard(BaseClearActivity.this, view);
				resideMenu.openMenu();
			}
		});
        findViewById(R.id.title_bar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Util.hideKeyboard(BaseClearActivity.this, view);
				resideMenu.openMenu();
			}
		});
    }

	public void setMenuListener(ResideMenu.OnMenuListener listener) {
		resideMenu.setMenuListener(listener);
	}

    @Override
    public void onClick(View view) {
    	super.onClick(view);
    	Util.hideKeyboard(this, view);
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
        		if (i == PLAYER_FRAGMENT) {
        			isBackButtonEnabled = true;
        			changeFragment(getPlayerFragment(), true);
        		} else {
        			showMiniPlayer(null != service ? service.isEnqueueToStream(): false);
        			changeFragment(fragments[i], false);
        		}
        		tvTitle.setText(titles[i]);
        	}
        }
        resideMenu.closeMenu();
    }

	//Example of menuListener
    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
        }

		@Override
        public void closeMenu() { }
    };
    
	public void changeFragment(Fragment targetFragment, boolean isAnimate) {
		manageSearchView(targetFragment.getClass().getSimpleName());
		currentFragmentIsPlayer = targetFragment.getClass() == getFragments()[PLAYER_FRAGMENT].getClass();
		lastOpenedFragment = targetFragment;
		resideMenu.clearIgnoredViewList();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (isAnimate && isAnimationEnabled()) {
			transaction.setCustomAnimations(R.anim.fragment_slide_in_up,
					R.anim.fragment_slide_out_up, R.anim.fragment_slide_in_down,
					R.anim.fragment_slide_out_down);
		}
		transaction.replace(R.id.main_fragment, targetFragment,targetFragment.getClass().getSimpleName())
				.addToBackStack(targetFragment.getClass().getSimpleName())
				.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();
	}

    protected void manageSearchView(String string) {
		
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
		currentFragmentIsPlayer = false;
		if (lastOpenedFragment.getClass().getSimpleName().equals(getFragments()[PLAYER_FRAGMENT].getClass().getSimpleName())){
			getFragmentManager().popBackStack();
			FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 2);
			String lastFragmentName = backEntry.getName();
			lastOpenedFragment = getFragmentManager().findFragmentByTag(lastFragmentName);
			String title = getNameCurrentFragment(lastFragmentName);
			tvTitle.setText(title);
			manageSearchView(lastFragmentName);
			showMiniPlayer(service.isEnqueueToStream());
		} else {
			if (isMiniPlayerPrepared()) {
				stopChildsServices();
			} else {
				stopService(new Intent(this, PlaybackService.class));
				finish();
			}
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
	
	public boolean isBackButtonEnabled() {
		return isBackButtonEnabled;
	}
	
	public void reDrawMenu(){
		List<ResideMenuItem> list = new ArrayList<ResideMenuItem>();
		menuItems = getMenuItems();
		for (ResideMenuItem item : menuItems) {
			item.setOnClickListener(this);
        	item.setBackgroundResource(R.drawable.button_selector_inverse_light);
        	list.add(item);
		}
        resideMenu.setMenuItems(list);
		if (!isPlaying()) {
			resideMenu.hideLastElement();
		}
	}
	
}
