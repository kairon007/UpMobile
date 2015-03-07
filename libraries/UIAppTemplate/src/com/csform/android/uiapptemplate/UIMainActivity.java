package com.csform.android.uiapptemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.csform.android.uiapptemplate.fragment.NavigationDrawerFragment;
import com.csform.android.uiapptemplate.fragment.NavigationDrawerFragment.NavigationDrawerCallbacks;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public abstract class UIMainActivity extends BaseMiniPlayerActivity implements NavigationDrawerCallbacks, Constants {
	
	private List<BaseMaterialFragment> mFragments;
	private CharSequence mTitle;
	private String query = null;
	private SearchView searchView;
	private NavigationDrawerFragment navigationDrawerFragment;
	private boolean isVisibleSearchView = false;
	protected boolean isEnabledFilter = false;
	
	protected abstract <T extends BaseMaterialFragment> ArrayList<T> getFragments();
	protected void clickOnSearchView(String message) {}
	public int getSettingsIcon() { return 0; }
	public abstract String getDirectory();
	public abstract void showDialog();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragments = getFragments();
		setContentView(R.layout.activity_material_main);
		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}
	
    private boolean useOldToggle() {
		return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        View v = (View) findViewById(android.R.id.home);
        if (null != v) {
			if (useOldToggle()) {
				((View) v.getParent().getParent()).setPadding(48, 0, 0, 0);
			}
        }
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        int color = getResources().getColor(R.color.main_color_for_text);
        String str = Integer.toHexString(color);
        String strColor =  "#"+str.substring(2);
        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView searchIcon = (ImageView) searchView.findViewById(searchImgId);
        searchIcon.setImageResource(R.drawable.ic_search_ab);
        searchIcon.setBackgroundResource(R.drawable.spinner_selector);
        int searchCloseId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeIcon = (ImageView) searchView.findViewById(searchCloseId);
        closeIcon.setImageResource(R.drawable.ic_close_ab);
        closeIcon.setBackgroundResource(R.drawable.spinner_selector);
        int queryTextViewId = getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView autoComplete =  (TextView) searchView.findViewById(queryTextViewId);
        autoComplete.setTextColor(color);
        try {
			Class<?> clazz = Class.forName("android.widget.SearchView$SearchAutoComplete");
			SpannableStringBuilder stopHint = new SpannableStringBuilder("   ");
			stopHint.append(Html.fromHtml("<font color = " + strColor + ">" + getResources().getString(R.string.hint_main_search) + "</font>"));
			Drawable search_icon = getResources().getDrawable(R.drawable.ic_search_ab);
			Method textSizeMethod = clazz.getMethod("getTextSize");
			Float rawTextSize = (Float) textSizeMethod.invoke(autoComplete);
			int textSize = (int) (rawTextSize * 1.5);
			search_icon.setBounds(0, 0, textSize, textSize);
			stopHint.setSpan(new ImageSpan(search_icon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			Method setHintMethod = clazz.getMethod("setHint", CharSequence.class);
			setHintMethod.invoke(autoComplete, stopHint);
		} catch (Exception e) {
			android.util.Log.d("logks", getClass().getName() + "Appear problem: " + e);
		}
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
            
            @Override
            public boolean onQueryTextSubmit(String q) {
                query = q;
                hideKeyboard();
                android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() -1);
    			String lastFragmentName = backEntry.getName();
    			if (lastFragmentName.equals(getFragments().get(LIBRARY_FRAGMENT).getClass().getSimpleName())) {
    				searchView.clearFocus();
    				isEnabledFilter = true;
    				setFilter(q);
    			} else {
    				isEnabledFilter = false;
    				changeFragment(mFragments.get(SEARCH_FRAGMENT), false);
    				searchView.setIconified(true);
    			}
                return false;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
            	if (isEnabledFilter && "".equals(newText)) {
            		setFilter("");
            		isEnabledFilter = false;
            	}
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_search).setVisible(isVisibleSearchView);
		return super.onPrepareOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View view  = findViewById(R.id.drawer_layout);
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	    int itemId = item.getItemId();
		if (itemId == R.id.action_search) {
			searchView.setIconified(false);// to Expand the SearchView when clicked
			return true; 
		}    
		return false;
	}
	
	protected void setFilter(String filter) {
		
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (null != searchView) imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
	}
	
	@Override
	public void setTitle(int titleId) {
		setTitle(getString(titleId));
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		navigationDrawerFragment.setTitle(mTitle);
	}
	
	public ArrayList<BaseMaterialFragment> getItems(){
		return getFragments();
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		if (position == PLAYER_FRAGMENT) {
			showMiniPlayer(false);
		} else if (position <= LIBRARY_FRAGMENT){
			showMiniPlayer(true);
		}
		switch (position) {
		case SEARCH_FRAGMENT:
	        changeFragment(mFragments.get(0), false);
			break;
		case DOWNLOADS_FRAGMENT:
	        changeFragment(mFragments.get(1), false);
			break;
		case PLYLIST_FRAGMENT:
	        changeFragment(mFragments.get(2), false);
			break;
		case LIBRARY_FRAGMENT:
	        changeFragment(mFragments.get(3), false);
			break;
		case PLAYER_FRAGMENT:
			android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
			String lastFragmentName = backEntry.getName();
	    	BaseMaterialFragment fragment = (BaseMaterialFragment) mFragments.get(4);
		    if (!lastFragmentName.equals(fragment.getClass().getSimpleName())) {
		    	changeFragment(fragment, true);
		    }
		    break;
		case SETTINGS_FRAGMENT:
		case 6:
			showDialog();
			break;
		default:
			break;
		}
	}
	
	public void changeFragment(BaseMaterialFragment baseMaterialFragment, boolean isAnimate) {
		isVisibleSearchView = mFragments.get(0).equals(baseMaterialFragment) ? false : true;
		hideKeyboard();
		isEnabledFilter = false;
		if (null != searchView) {
			searchView.setIconified(true);
			searchView.setIconified(true);
		} 
		if (((Fragment)baseMaterialFragment).isAdded()) {
			((Fragment)baseMaterialFragment).onResume();
		}
		FragmentTransaction tr = getFragmentManager().beginTransaction();
		if (isAnimate) {
			tr.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down);
		}
		tr.replace(R.id.content_frame, (Fragment) baseMaterialFragment, baseMaterialFragment.getClass().getSimpleName())
		.addToBackStack(baseMaterialFragment.getClass().getSimpleName())
		.commit();
	}
	
	public void addPlayerElement(boolean flag) {
		if (null == navigationDrawerFragment) return;
		navigationDrawerFragment.setAdapter(flag);
	}
	
	public void setDrawerEnabled(boolean isEnabled) {
		navigationDrawerFragment.setEnabled(isEnabled);
	}

	public String getQuery() {
		return query;
	}
	
	public void setSelectedItem(int position) {
		if (null != navigationDrawerFragment) {
			navigationDrawerFragment.setSelectedItem(position);
		}
	}
	
	public void setVisibleSearchView(boolean flag) {
		isVisibleSearchView = flag;
	}
}