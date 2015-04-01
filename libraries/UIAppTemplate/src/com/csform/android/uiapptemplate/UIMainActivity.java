package com.csform.android.uiapptemplate;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

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
	protected boolean hadClosedDraver = false;
	private int currentPosition = -1;
	
	protected abstract <T extends BaseMaterialFragment> ArrayList<T> getFragments();
	protected void clickOnSearchView(String message) {}
	public int getSettingsIcon() { return 0; }
	public abstract String getDirectory();
	public abstract void showDialog();
	protected abstract Bitmap getSearchActinBarIcon();
	protected abstract Bitmap getCloseActinBarIcon();
	
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
        View v = findViewById(android.R.id.home);
        if (null != v) {
			if (useOldToggle()) {
				((View) v.getParent().getParent()).setPadding(32, 0, 0, 0);
			}
        }
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.hint_main_search));
//        int color = getResources().getColor(R.color.main_color_for_search_fragment_text);
//        String str = Integer.toHexString(color);
//        String strColor =  "#"+str.substring(2);
//        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
//        ImageView searchIcon = (ImageView) searchView.findViewById(searchImgId);
//        searchIcon.setImageBitmap(getSearchActinBarIcon());
//        searchIcon.setBackgroundResource(R.drawable.spinner_selector);
//        int searchCloseId = getResources().getIdentifier("android:id/search_close_btn", null, null);
//        ImageView closeIcon = (ImageView) searchView.findViewById(searchCloseId);
//        closeIcon.setImageBitmap(getCloseActinBarIcon());
//        closeIcon.setBackgroundResource(R.drawable.spinner_selector);
//        int queryTextViewId = getResources().getIdentifier("android:id/search_src_text", null, null);
//        TextView autoComplete =  (TextView) searchView.findViewById(queryTextViewId);
//        autoComplete.setTextColor(color);
//        try {
//			Class<?> clazz = Class.forName("android.widget.SearchView$SearchAutoComplete");
//			SpannableStringBuilder stopHint = new SpannableStringBuilder("   ");
//			stopHint.append(Html.fromHtml("<font color = " + strColor + ">" + getResources().getString(R.string.hint_main_search) + "</font>"));
//			Drawable search_icon = new BitmapDrawable(getResources(),getSearchActinBarIcon());
//			Method textSizeMethod = clazz.getMethod("getTextSize");
//			Float rawTextSize = (Float) textSizeMethod.invoke(autoComplete);
//			int textSize = (int) (rawTextSize * 1.5);
//			search_icon.setBounds(0, 0, textSize, textSize);
//			stopHint.setSpan(new ImageSpan(search_icon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			Method setHintMethod = clazz.getMethod("setHint", CharSequence.class);
//			setHintMethod.invoke(autoComplete, stopHint);
//		} catch (Exception e) {
//			android.util.Log.d(getClass().getName() , "Appear problem: " + e);
//		}
        searchView.setOnSearchClickListener(new OnClickListener() {
			
        	@Override
			public void onClick(View v) {
				if (navigationDrawerFragment.isDrawerOpen()) {
					navigationDrawerFragment.closeDrawer();
				}
			}
		});
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String q) {
				searchView.clearFocus();
				Util.hideKeyboard(UIMainActivity.this, searchView);
				onQueryTextSubmitAct(q);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				onQueryTextChangeAct(newText);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
    }
	
	
	protected void onQueryTextSubmitAct(String query) {
		
	}
	
	protected void onQueryTextChangeAct(String query) {
		
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_search).setVisible(isVisibleSearchView);
		return super.onPrepareOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View view  = findViewById(R.id.drawer_layout);
		Util.hideKeyboard(this, view);
	    int itemId = item.getItemId();
		if (itemId == R.id.action_search) {
			searchView.setIconified(false);// to Expand the SearchView when clicked
			return true; 
		}    
		return false;
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
		if (position == currentPosition) {
			return;
		}
		if (position == PLAYER_FRAGMENT) {
			showMiniPlayer(false);
		} else if (position <= LIBRARY_FRAGMENT){
			showMiniPlayer(true);
		}
		switch (position) {
		case SEARCH_FRAGMENT:
	        changeFragment(mFragments.get(SEARCH_FRAGMENT), false);
			break;
		case DOWNLOADS_FRAGMENT:
	        changeFragment(mFragments.get(DOWNLOADS_FRAGMENT), false);
			break;
		case PLAYLIST_FRAGMENT:
	        changeFragment(mFragments.get(PLAYLIST_FRAGMENT), false);
			break;
		case LIBRARY_FRAGMENT:
	        changeFragment(mFragments.get(LIBRARY_FRAGMENT), false);
			break;
		case PLAYER_FRAGMENT:
			android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
			String lastFragmentName = backEntry.getName();
	    	BaseMaterialFragment fragment = mFragments.get(Constants.PLAYER_FRAGMENT);
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
		currentPosition = position;
	}
	
	protected void setSearchViewVisibility(String fragmentName) {
		isVisibleSearchView = (fragmentName.equals((mFragments.get(LIBRARY_FRAGMENT)).getClass().getSimpleName())) || (fragmentName.equals((mFragments.get(PLAYLIST_FRAGMENT)).getClass().getSimpleName()));
	}
	
	public void changeFragment(BaseMaterialFragment baseMaterialFragment, boolean isAnimate) {
		String fragmentName =  ((Fragment) baseMaterialFragment).getClass().getSimpleName();
		setSearchViewVisibility(fragmentName);
		if (null != searchView) {
			Util.hideKeyboard(this, searchView);
		}
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
			tr.setCustomAnimations(R.anim.fragment_slide_in_up, R.anim.fragment_slide_out_up, R.anim.fragment_slide_in_down, R.anim.fragment_slide_out_down);
		}
		tr.replace(R.id.content_frame, (Fragment) baseMaterialFragment, baseMaterialFragment.getClass().getSimpleName())
		.addToBackStack(baseMaterialFragment.getClass().getSimpleName())
		.commit();
	}
	
	@Override
	public void onBackPressed() {
		setSearchViewVisibility(getPreviousFragmentName(2));
		hadClosedDraver = navigationDrawerFragment.isDrawerOpen();
		if (hadClosedDraver) {
			navigationDrawerFragment.closeDrawer();
		}
	}
	
	protected String getPreviousFragmentName(int position) {
		if (getFragmentManager().getBackStackEntryCount() < position) return getFragments().get(0).getClass().getSimpleName();
		android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - position);
		String previousFragmentName = backEntry.getName();
		return previousFragmentName;
	}
	
	public void addPlayerElement(boolean flag) {
		if (null == navigationDrawerFragment) return;
		navigationDrawerFragment.setAdapter(flag);
	}
	
	public void setDrawerEnabled(boolean isEnabled) {
		navigationDrawerFragment.setEnabled(isEnabled);
	}
	
	protected boolean isDraverClosed() {
		return navigationDrawerFragment.isDrawerIndicatorEnabled();
	}

	public String getQuery() {
		return query;
	}
	
	public void setSelectedItem(int position) {
		currentPosition = position;
		if (null != navigationDrawerFragment) {
			navigationDrawerFragment.setSelectedItem(position);
		}
	}
	
	public void setVisibleSearchView(boolean flag) {
		isVisibleSearchView = flag;
	}
}