package org.upmobile.newmaterialmusicdownloader.fragment;

import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.adapter.NavigationDrawerAdapter;
import org.upmobile.newmaterialmusicdownloader.data.NavDrawerItem;

import ru.johnlife.lifetoolsmp3.Util;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentDrawer extends Fragment {
	
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerAdapter adapter;
    private View containerView;
    private View selectedView;

    private FragmentDrawerListener drawerListener;
 
    public FragmentDrawer() { }
 
    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
        adapter = new NavigationDrawerAdapter(getActivity(), ((MainActivity) getActivity()).getData());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
        	
            @Override
            public void onClick(View view, int position) {
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
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
                Util.hideKeyboard(getActivity(), drawerView);
            }
 
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
                Util.hideKeyboard(getActivity(), drawerView);
            }
 
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
                Util.hideKeyboard(getActivity(), drawerView);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
 
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
                    if (child != null && clickListener != null) {
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
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(event)) {
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
    
	public void showPlayerElement(boolean flag) {
		if (!flag) {
			adapter.delete(3);
		} else {
			if (adapter.getItemCount() > 6) return;
			adapter.add(3, new NavDrawerItem(R.drawable.ic_headset_grey , getResources().getString(R.string.tab_now_plaing), NavDrawerItem.Type.Primary));
		}
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
			((TextView) selectedView.findViewById(R.id.title)).setTextColor(Util.getResIdFromAttribute(getActivity(), R.attr.colorTextSecondary));
		}
		selectedView = view;
		View selectView = recyclerView.findChildViewUnder(view.getX(), view.getY());
		selectView.setBackgroundColor(getResources().getColor(R.color.selected_item));
		((TextView) selectView.findViewById(R.id.title)).setTextColor(getResources().getColor(android.R.color.holo_blue_light));
	}

}
