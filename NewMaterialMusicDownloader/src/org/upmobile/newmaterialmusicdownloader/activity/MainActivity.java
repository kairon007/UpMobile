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

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
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

public class MainActivity extends BaseMiniPlayerActivity {

	private Drawer.Result drawerResult = null;
	private SearchView searchView;
	private boolean isVisibleSearchView = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		changeFragment(0);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		drawerResult = new Drawer()
				.withActivity(this)
				.withToolbar(toolbar)
				.withActionBarDrawerToggle(true)
				.withHeader(R.layout.drawer_header)
				.addDrawerItems(
						new PrimaryDrawerItem().withName(R.string.tab_search).withIcon(R.drawable.ic_search_grey)
								.withTextColor(R.color.material_primary_text),
						new PrimaryDrawerItem().withName(R.string.tab_downloads).withIcon(R.drawable.ic_file_download_grey)
								.withTextColor(R.color.material_primary_text),
						new PrimaryDrawerItem().withName(R.string.tab_playlist).withIcon(R.drawable.ic_queue_music_grey)
								.withTextColor(R.color.material_primary_text),
						new PrimaryDrawerItem().withName(R.string.tab_library).withIcon(R.drawable.ic_my_library_music_grey)
								.withTextColor(R.color.material_primary_text),
						new SectionDrawerItem().withName(R.string.tab_settings).withTextColor(R.color.material_primary_text),
						new SecondaryDrawerItem().withName(getDirectory()).withIcon(R.drawable.ic_settings_applications_grey))
				.withOnDrawerListener(new Drawer.OnDrawerListener() {
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
	}

	public void changeFragment(int framgment) {
		Fragment selectedFragment = null;
		boolean isAnimate = false;
		switch (framgment) {
		case Constants.SEARCH_FRAGMENT:
			selectedFragment = new SearchFragment();
			break;
		case Constants.DOWNLOADS_FRAGMENT:
			selectedFragment = new DownloadsFragment();
			break;
		case Constants.PLAYLIST_FRAGMENT:
			selectedFragment = new PlaylistFragment();
			break;
		case Constants.LIBRARY_FRAGMENT:
			selectedFragment = new LibraryFragment();
			break;
		case Constants.PLAYER_FRAGMENT:
			selectedFragment = new PlayerFragment();
			isAnimate = true;
			break;
		case Constants.SETTINGS_FRAGMENT:
		case 6:
			final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			if (null == service) {
				service = PlaybackService.get(this);
			}
			DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(this, false, new DirectoryChooserDialog.ChosenDirectoryListener() {

						@Override
						public void onChosenDir(String chDir) {
							File file = new File(chDir);
							Editor editor = sp.edit();
							editor.putString(Constants.PREF_DIRECTORY, chDir);
							editor.putString(Constants.PREF_DIRECTORY_PREFIX, File.separator + file.getAbsoluteFile().getName() + File.separator);
							editor.commit();
						}
					});
			directoryChooserDialog.chooseDirectory();
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
			transaction.replace(R.id.content_frame, selectedFragment, selectedFragment.getClass().getSimpleName())
					.addToBackStack(selectedFragment.getClass().getSimpleName()).setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
		}
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
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_search).setVisible(isVisibleSearchView);
		return super.onPrepareOptionsMenu(menu);
	}

	protected String getPreviousFragmentName(int position) {
		if (getFragmentManager().getBackStackEntryCount() < position)
			return SearchFragment.class.getSimpleName();
		android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(
				getFragmentManager().getBackStackEntryCount() - position);
		String previousFragmentName = backEntry.getName();
		return previousFragmentName;
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
	protected String getDirectory() {
		return NewMaterialApp.getDirectory();
	}

	@Override
	protected int getMiniPlayerID() {
		return R.id.mini_player_main;
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
		changeFragment(Constants.PLAYER_FRAGMENT);
	}

	@Override
	public void showPlayerElement(boolean flag) {
		drawerResult.removeItem(Constants.PLAYER_FRAGMENT + 1);
		drawerResult.addItem(new PrimaryDrawerItem().withName(R.string.tab_now_plaing).withIcon(R.drawable.ic_headset_grey).withTextColor(R.color.material_primary_text), Constants.PLAYER_FRAGMENT);
		drawerResult.addItem(new SectionDrawerItem().withName(R.string.tab_settings).withTextColor(R.color.material_primary_text));
		drawerResult.addItem(new SecondaryDrawerItem().withName(getDirectory()).withIcon(R.drawable.ic_settings_applications_grey));
	}

	public void setTitle(int title) {
		setTitle(getString(title));
	}

	public void setTitle(String title) {
		getSupportActionBar().setTitle(title);
	}

	public void showMessage(String message) {
		AppMsg.cancelAll(this);
		AppMsg.makeText(this, message, new Style(3000, getResIdFromAttribute(this, R.attr.colorPrimary))).show();
	}

	public void showMessage(int message) {
		showMessage(getString(message));
	}

	@Override
	protected void setPlayPauseMini(boolean playPayse) {
		((ImageView) findViewById(R.id.mini_player_play_pause)).setImageResource(playPayse ? R.drawable.ic_play_arrow_grey : R.drawable.ic_pause_grey);
	}
	
	protected void setSearchViewVisibility(String fragmentName) {
		isVisibleSearchView  = (fragmentName.equals(LibraryFragment.class.getSimpleName())) || (fragmentName.equals(PlaylistFragment.class.getSimpleName()));
	}
	
	private int getResIdFromAttribute(final Activity activity, final int attr) {
		if (attr == 0) return 0;
		final TypedValue typedvalueattr = new TypedValue();
		activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
		return typedvalueattr.resourceId;
	}
}
