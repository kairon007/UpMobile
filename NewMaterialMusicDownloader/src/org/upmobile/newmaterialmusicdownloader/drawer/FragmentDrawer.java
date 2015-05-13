package org.upmobile.newmaterialmusicdownloader.drawer;

import java.util.ArrayList;
import java.util.List;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.adapter.NavigationDrawerAdapter;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;
import org.upmobile.newmaterialmusicdownloader.data.NavDrawerItem;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentDrawer extends Fragment implements Constants {
	
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerAdapter adapter;
    private View containerView;
    private View selectedView;

    private FragmentDrawerListener drawerListener;
    
    private SharedPreferences sPref;
    
    private OnSharedPreferenceChangeListener sPrefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (PREF_DIRECTORY.equals(key)) {
				adapter.updateItem(adapter.getItemCount() - 1, new NavDrawerItem(R.drawable.ic_settings_applications_grey, NewMaterialApp.getDirectory(), NavDrawerItem.Type.Primary));
			}
		}
	};
 
    public FragmentDrawer() {
    	 sPref = MusicApp.getSharedPreferences();
    	 sPref.registerOnSharedPreferenceChangeListener(sPrefListener);
    }
 
    public void setFragmentDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }
    
    public void setDrawerListener(DrawerListener listener) {
        mDrawerLayout.setDrawerListener(listener);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, Boolean.FALSE);
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
        adapter = new NavigationDrawerAdapter(getActivity(), ((MainActivity) getActivity()).getData());
        recyclerView.setHasFixedSize(Boolean.TRUE);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
        	
            @Override
            public void onClick(View view, final int position) {
				if ((position + 1) > ManagerFragmentId.playlistFragment() && (position + 1) < ManagerFragmentId.settingFragment()) return;
            	if ((position + 1) != ManagerFragmentId.settingFragment()){
            		setItemChecked(view);
            	}
                drawerListener.onDrawerItemSelected(view, position);
                mDrawerLayout.closeDrawer(containerView);
            }
 
            @Override
            public void onLongClick(View view, int position) {}
        }));
        return layout;
    }
 
	public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        ViewGroup.LayoutParams layoutParams = containerView.getLayoutParams();
        Display display = ((Activity) containerView.getContext()).getWindowManager().getDefaultDisplay(); 
        int width = display.getWidth();
        width = width - Util.dpToPx(containerView.getContext(), 56);
        int maxWidth = Util.dpToPx(containerView.getContext(), 320);
        layoutParams.width = width > maxWidth ? maxWidth : width;
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
 
    }
	
	@Override
	public void onDetach() {
		sPref.unregisterOnSharedPreferenceChangeListener(sPrefListener);
		super.onDetach();
	}
	
 
    public static interface ClickListener {
        public void onClick(View view, int position);
 
        public void onLongClick(View view, int position);
    }
 
    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
 
        private GestureDetector gestureDetector;
        private ClickListener clickListener;
 
        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent event) { return true; }
 
                @Override
                public void onLongPress(MotionEvent event) {
                    View child = recyclerView.findChildViewUnder(event.getX(), event.getY());
                    if (null != child && null != clickListener) {
                    	child.onTouchEvent(event);
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                    super.onLongPress(event);
                }
            });
        }
        
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
            View child = rv.findChildViewUnder(event.getX(), event.getY());
            if (null != child && null != clickListener && gestureDetector.onTouchEvent(event)) {
            	child.onTouchEvent(event);
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }
        
        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent event) {}
    }
 
    public interface FragmentDrawerListener {
        public void onDrawerItemSelected(View view, int position);
    }
    
    public void setDrawableLockMode(boolean unlock){
    	mDrawerToggle.setDrawerIndicatorEnabled(unlock);
    	mDrawerLayout.setDrawerLockMode(unlock ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    
    public void openDrawer() {
    	mDrawerLayout.openDrawer(GravityCompat.START);
    }
    
    public boolean isDrawerOpened() {
    	return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }
    
	public void showPlayerElement(final boolean flag) {
		List<NavDrawerItem> list = new ArrayList<NavDrawerItem>(((MainActivity) getActivity()).getData());
		if (flag) {
			list.add(3, new NavDrawerItem(R.drawable.ic_headset_grey, getResources().getString(R.string.tab_now_plaing), NavDrawerItem.Type.Primary));
		}
		adapter.updateList(list);
	}
	
	public void setItemChecked(int position) {
		View view = recyclerView.getChildAt(--position);
		if (null != view) {
			setItemChecked(view);
		}
	}
	
	public void setItemChecked(View view){
		if (null != selectedView) {
			selectedView.setBackgroundColor(Color.TRANSPARENT);
			((TextView) selectedView.findViewById(R.id.title)).setTextColor(getResources().getColor(Util.getResIdFromAttribute(getActivity(), R.attr.colorTextSecondary)));
		}
		selectedView = view;
		View selectView = recyclerView.findChildViewUnder(view.getX(), view.getY());
		selectView.setBackgroundColor(getResources().getColor(R.color.selected_item));
		((TextView) selectView.findViewById(R.id.title)).setTextColor(getResources().getColor(android.R.color.holo_blue_light));
	}

}
