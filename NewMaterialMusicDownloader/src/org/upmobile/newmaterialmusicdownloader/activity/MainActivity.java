package org.upmobile.newmaterialmusicdownloader.activity;

import java.io.File;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;
import org.upmobile.newmaterialmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.SearchFragment;
import org.upmobile.newmaterialmusicdownloader.ui.dialog.FolderSelectorDialog;
import org.upmobile.newmaterialmusicdownloader.ui.dialog.FolderSelectorDialog.FolderSelectCallback;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.ui.widget.CircularImageView;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.devspark.appmsg.AppMsg;
import com.devspark.appmsg.AppMsg.Style;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class MainActivity extends BaseMiniPlayerActivity implements Constants, FolderSelectCallback {

	private Drawer.Result drawerResult = null;
	private ActionBarDrawerToggle toggle;
	private SearchView searchView;
	private int currentFragmentId = SEARCH_FRAGMENT;
	private boolean isVisibleSearchView = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		changeFragment(SEARCH_FRAGMENT);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		drawerResult = new Drawer()
			.withActivity(this)
			.withToolbar(toolbar)
			.withActionBarDrawerToggle(true)
			.withHeader(R.layout.drawer_header)
			.addDrawerItems(new PrimaryDrawerItem().withName(R.string.tab_search).withIcon(R.drawable.ic_search_grey).withTextColor(R.color.material_primary_text),
				new PrimaryDrawerItem().withName(R.string.tab_downloads).withIcon(R.drawable.ic_file_download_grey).withTextColor(R.color.material_primary_text),
				new PrimaryDrawerItem().withName(R.string.tab_playlist).withIcon(R.drawable.ic_queue_music_grey).withTextColor(R.color.material_primary_text),
				new PrimaryDrawerItem().withName(R.string.tab_library).withIcon(R.drawable.ic_my_library_music_grey).withTextColor(R.color.material_primary_text), new SectionDrawerItem().withName(R.string.tab_settings).withTextColor(R.color.material_primary_text),
				new SecondaryDrawerItem().withName(getDirectory()).withIcon(R.drawable.ic_settings_applications_grey)).withOnDrawerListener(new Drawer.OnDrawerListener() {
				@Override
				public void onDrawerOpened(View drawerView) {
					Util.hideKeyboard(MainActivity.this, drawerView);
				}

				@Override
				public void onDrawerClosed(View drawerView) {
				}
			}).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
					changeFragment(position - 1);
				}
			}).build();

		toggle = new ActionBarDrawerToggle(this, drawerResult.getDrawerLayout(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		toggle.setDrawerIndicatorEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (currentFragmentId == PLAYER_FRAGMENT) {
					onBackPressed();
				} else {
					drawerResult.openDrawer();
				}
			}
		});
	}

	@Override
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		super.onStart();
	}

	@Override
	protected void onResume() {
		if (null != service && service.isPlaying()) {
			showPlayerElement(true);
		} else if (PlaybackService.hasInstance()) {
			service = PlaybackService.get(this);
		}
		super.onResume();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		toggle.syncState();
	}

	@Override
	protected void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (service == null) {
			service = PlaybackService.get(this);
		}
		if (service.hasArray()) {
			out.putParcelableArrayList(ARRAY_SAVE, service.getArrayPlayback());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		toggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) searchItem.getActionView();
		searchView.setQueryHint(getResources().getString(R.string.hint_main_search));
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String q) {
				searchView.clearFocus();
				Util.hideKeyboard(MainActivity.this, searchView);
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_search).setVisible(isVisibleSearchView);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		setSearchViewVisibility(getPreviousFragmentName(2));
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		if (null != player && player.isVisible()) {
			showMiniPlayer(true);
			getFragmentManager().popBackStack();
		} else {
			if (null != service && isMiniPlayerPrepared()) {
				service.stopPressed();
				showPlayerElement(false);
			} else {
				finish();
			}
		}
	}

	public void changeFragment(int fragmentId) {
		Fragment selectedFragment = null;

		if (null != drawerResult) {
			if (fragmentId < SETTINGS_FRAGMENT) {
				currentFragmentId = fragmentId;
			}
			drawerResult.getListView().setItemChecked(currentFragmentId + 1, true);
		}

		currentFragmentId = fragmentId;
		boolean isAnimate = false;
		switch (fragmentId) {
		case SEARCH_FRAGMENT:
			selectedFragment = new SearchFragment();
			break;
		case DOWNLOADS_FRAGMENT:
			selectedFragment = new DownloadsFragment();
			break;
		case PLAYLIST_FRAGMENT:
			selectedFragment = new PlaylistFragment();
			break;
		case LIBRARY_FRAGMENT:
			selectedFragment = new LibraryFragment();
			break;
		case PLAYER_FRAGMENT:
			selectedFragment = new PlayerFragment();
			isAnimate = true;
			break;
		case SETTINGS_FRAGMENT:
		case 6:
			new FolderSelectorDialog().show(this);
			break;
		default:
			selectedFragment = new SearchFragment();
			break;
		}
		if (null != selectedFragment) {
			setSearchViewVisibility(selectedFragment.getClass().getSimpleName());
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			if (isAnimate) {
				transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down);
			}
			transaction.replace(R.id.content_frame, selectedFragment, selectedFragment.getClass().getSimpleName()).addToBackStack(selectedFragment.getClass().getSimpleName()).setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
		}
	}

	@Override
	public void onFolderSelection(File folder) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putString(PREF_DIRECTORY, folder.getAbsolutePath());
		editor.putString(PREF_DIRECTORY_PREFIX, File.separator + folder.getAbsoluteFile().getName() + File.separator);
		editor.commit();
		showPlayerElement(PlaybackService.get(this).isPlaying());
	}

	public void setDraverEnabled(boolean isVisibleDraver) {
		toggle.setDrawerIndicatorEnabled(isVisibleDraver);
		drawerResult.getDrawerLayout().setDrawerLockMode(isVisibleDraver ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	public void setCurrentFragmentId(int currentFragmentId) {
		this.currentFragmentId = currentFragmentId;
	}

	@Override
	public void showPlayerElement(boolean flag) {
		int draverItemsCount = drawerResult.getAdapter().getCount();
		if (flag) {
			if (draverItemsCount < FULL_DRAVER_SIZE) {
				drawerResult.removeItem(PLAYER_FRAGMENT + 1);
				drawerResult.addItem(new PrimaryDrawerItem().withName(R.string.tab_now_plaing).withIcon(R.drawable.ic_headset_grey).withTextColor(R.color.material_primary_text), PLAYER_FRAGMENT);
				drawerResult.addItem(new SectionDrawerItem().withName(R.string.tab_settings).withTextColor(R.color.material_primary_text));
				drawerResult.addItem(new SecondaryDrawerItem().withName(getDirectory()).withIcon(R.drawable.ic_settings_applications_grey));
			}
			drawerResult.removeItem(SETTINGS_FRAGMENT + 1);
			drawerResult.addItem(new SecondaryDrawerItem().withName(getDirectory()).withIcon(R.drawable.ic_settings_applications_grey));
		} else {
			if (draverItemsCount > LESS_DRAVER_SIZE) {
				drawerResult.removeItem(PLAYER_FRAGMENT);
			}
			drawerResult.removeItem(SETTINGS_FRAGMENT);
			drawerResult.addItem(new SecondaryDrawerItem().withName(getDirectory()).withIcon(R.drawable.ic_settings_applications_grey));
		}
	}

	public void onQueryTextSubmitAct(String query) {
		String lastFragmentName = getPreviousFragmentName(1);
		if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
			LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
			if (fragment.isVisible()) {
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		} else if (lastFragmentName.equals(PlaylistFragment.class.getSimpleName())) {
			PlaylistFragment fragment = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
			if (fragment.isVisible()) {
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		}
	}

	public void onQueryTextChangeAct(String newText) {
		if ("".equals(newText)) {
			String fragmentName = getPreviousFragmentName(1);
			Fragment fragment = getFragmentManager().findFragmentByTag(fragmentName);
			if (LibraryFragment.class == fragment.getClass()) {
				if (fragment.isVisible()) {
					((LibraryFragment) fragment).clearFilter();
				}
			} else if (PlaylistFragment.class == fragment.getClass()) {
				if (fragment.isVisible()) {
					((PlaylistFragment) fragment).clearFilter();
				}
			}
		}
	}

	public void setTitle(int title) {
		setTitle(getString(title));
	}

	public void setTitle(String title) {
		getSupportActionBar().setTitle(title);
	}

	public void showMessage(String message) {
		AppMsg.cancelAll(this);
		AppMsg.makeText(this, message, new Style(3000, Util.getResIdFromAttribute(this, R.attr.colorPrimary))).show();
	}

	public void showMessage(int message) {
		showMessage(getString(message));
	}

	protected String getPreviousFragmentName(int position) {
		if (getFragmentManager().getBackStackEntryCount() < position)
			return SearchFragment.class.getSimpleName();
		android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - position);
		String previousFragmentName = backEntry.getName();
		return previousFragmentName;
	}

	@Override
	protected String getDirectory() {
		return NewMaterialApp.getDirectory();
	}

	@Override
	protected int getMiniPlayerID() {
		return R.id.mini_player;
	}

	@Override
	protected int getMiniPlayerClickableID() {
		return R.id.mini_player_main;
	}

	@Override
	protected int getFakeViewID() {
		return R.id.fake_view;
	}

	@Override
	protected void showPlayerFragment() {
		changeFragment(PLAYER_FRAGMENT);
	}

	@Override
	protected void setPlayPauseMini(boolean playPayse) {
		((ImageView) findViewById(R.id.mini_player_play_pause)).setColorFilter(getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)));
		((ImageView) findViewById(R.id.mini_player_play_pause)).setImageResource(playPayse ? R.drawable.ic_play_arrow_grey : R.drawable.ic_pause_grey);
	}

	@Override
	protected void setCover(Bitmap bmp) {
		if (null == bmp) {
			((CircularImageView) findViewById(R.id.mini_player_cover)).setImageResource(R.drawable.ic_album_grey);
			return;
		}
		((CircularImageView) findViewById(R.id.mini_player_cover)).setImageBitmap(bmp);
	}

	@Override
	protected void setImageDownloadButton() {
		((ImageView) findViewById(R.id.mini_player_download)).setColorFilter(getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)));
		((ImageView) findViewById(R.id.mini_player_download)).setImageResource(R.drawable.ic_file_download_grey);
	}

	protected void setSearchViewVisibility(String fragmentName) {
		isVisibleSearchView = (fragmentName.equals(LibraryFragment.class.getSimpleName())) || (fragmentName.equals(PlaylistFragment.class.getSimpleName()));
	}

}