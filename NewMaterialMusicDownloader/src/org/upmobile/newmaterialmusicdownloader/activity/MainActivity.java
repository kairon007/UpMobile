package org.upmobile.newmaterialmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.app.NewMaterialMusicDownloaderApp;
import org.upmobile.newmaterialmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.SearchFragment;
import org.upmobile.newmaterialmusicdownloader.ui.dialog.FolderSelectorDialog.FolderSelectCallback;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.csform.android.uiapptemplate.font.MusicTextView;
import com.devspark.appmsg.AppMsg;
import com.devspark.appmsg.AppMsg.Style;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

public class MainActivity extends BaseMiniPlayerActivity implements Constants, FolderSelectCallback {

	private final String folderPath = NewMaterialMusicDownloaderApp.getDirectory();
	private int currentFragmentID;
	private Drawer.Result drawerResult = null;

	private FileObserver fileObserver = new FileObserver(folderPath) {

		@Override
		public void onEvent(int event, String path) {
			if (event == FileObserver.DELETE_SELF) {
				File file = new File(folderPath);
				file.mkdirs();
				getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + " LIKE '" + folderPath + "%'", null);
				getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		changeFragment(getFragments().get(0), false);
		setContentView(R.layout.activity_material_main);
		File file = new File(folderPath);
		if (!file.exists())
			file.mkdirs();
		if (null != service) {
			if (null != savedInstanceState && savedInstanceState.containsKey(ARRAY_SAVE)) {
				ArrayList<AbstractSong> list = savedInstanceState.getParcelableArrayList(ARRAY_SAVE);
				service.setArrayPlayback(list);
			}
			if (service.isPlaying())
				showPlayerElement(true);
		}
		fileObserver.startWatching();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
		drawerResult = new Drawer()
				.withActivity(this)
				.withToolbar(toolbar)
				.withActionBarDrawerToggle(true)
				.withHeader(R.layout.drawer_header)
				.addDrawerItems(
						new PrimaryDrawerItem().withName(R.string.tab_search).withIcon(R.drawable.ic_search_ab).withBadge("99").withIdentifier(1),
						new PrimaryDrawerItem().withName(R.string.tab_downloads).withIcon(FontAwesome.Icon.faw_gamepad),
						new PrimaryDrawerItem().withName(R.string.tab_playlist).withIcon(FontAwesome.Icon.faw_eye).withBadge("6").withIdentifier(2),
						new PrimaryDrawerItem().withName(R.string.tab_library).withIcon(FontAwesome.Icon.faw_eye).withBadge("6").withIdentifier(2),
						new SectionDrawerItem().withName("one"), new SecondaryDrawerItem().withName("one").withIcon(FontAwesome.Icon.faw_cog),
						new SecondaryDrawerItem().withName("one").withIcon(FontAwesome.Icon.faw_question).setEnabled(false), new DividerDrawerItem(),
						new SecondaryDrawerItem().withName("one").withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1))
				.withOnDrawerListener(new Drawer.OnDrawerListener() {
					@Override
					public void onDrawerOpened(View drawerView) {
						// Скрываем клавиатуру при открытии Navigation Drawer
						InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this
								.getSystemService(Activity.INPUT_METHOD_SERVICE);
						inputMethodManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
					}

					@Override
					public void onDrawerClosed(View drawerView) {
					}
				}).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					// Обработка клика
					public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
						if (drawerItem instanceof Nameable) {
							Toast.makeText(MainActivity.this, MainActivity.this.getString(((Nameable) drawerItem).getNameRes()), Toast.LENGTH_SHORT)
									.show();
						}
						if (drawerItem instanceof Badgeable) {
							Badgeable badgeable = (Badgeable) drawerItem;
							if (badgeable.getBadge() != null) {
								// учтите, не делайте так, если ваш бейдж
								// содержит символ "+"
								try {
									int badge = Integer.valueOf(badgeable.getBadge());
									if (badge > 0) {
										drawerResult.updateBadge(String.valueOf(badge - 1), position);
									}
								} catch (Exception e) {
									Log.d("test", "Не нажимайте на бейдж, содержащий плюс! :)");
								}
							}
						}
						changeFragment(getFragments().get(position - 1), false);
					}
				}).withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
					@Override
					// Обработка длинного клика, например, только для
					// SecondaryDrawerItem
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
						if (drawerItem instanceof SecondaryDrawerItem) {
							Toast.makeText(MainActivity.this, MainActivity.this.getString(((SecondaryDrawerItem) drawerItem).getNameRes()),
									Toast.LENGTH_SHORT).show();
						}
						return false;
					}
				}).build();
	}

	protected ArrayList<Fragment> getFragments() {
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(new SearchFragment());
		fragments.add(new DownloadsFragment());
		fragments.add(new PlaylistFragment());
		fragments.add(new LibraryFragment());
		fragments.add(new PlayerFragment());
		return fragments;
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
	public void onBackPressed() {
		super.onBackPressed();
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
//		isEnabledFilter = false;
		if (null != player && player.isVisible()) {
			showMiniPlayer(true);
			getFragmentManager().popBackStack();
		} else if (currentFragmentID == LIBRARY_FRAGMENT){
			Class<? extends AbstractSong> current = PlaybackService.get(this).getPlayingSong().getClass();
			Fragment fragment;
			if (current == MusicData.class) {
				fragment = new LibraryFragment();
				currentFragmentID = LIBRARY_FRAGMENT;
			} else {
//				setVisibleSearchView(false);
				fragment = new SearchFragment();
				currentFragmentID = SEARCH_FRAGMENT;
			}
//			getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName()).commit();
		} else {
			if (null != service && isMiniPlayerPrepared()) {
				service.stopPressed();
				showPlayerElement(false);
			} else {
				finish();
			}
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
	
	public void showMessage(String message) {
		AppMsg.cancelAll(this);
		AppMsg.makeText(this, message, new Style(3000, R.color.main_color_500)).show();
	}
	
	public void showMessage(int message) {
		showMessage(getString(message));
	}
	
	public Bitmap getDefaultBitmapCover(int outWidth, int outHeight, int property, String image) {
		return getDefaultBitmapCover(outWidth, outHeight, property, image, 0);
	}
	
	public Bitmap getDefaultBitmapCover(int outWidth, int outHeight, int property, String image, int customColor) {
		MusicTextView textCover = new MusicTextView(this);
		textCover.setText(image);
		textCover.setTextColor(getResources().getColor(customColor == 0 ? R.color.main_color_500 : customColor));
		Rect bounds = new Rect();
		Paint textPaint = textCover.getPaint();
		textPaint.getTextBounds(image, 0, image.length(), bounds);
		int height = bounds.height();
		int width = bounds.width();
		boolean defaultIsLarger = true;
		while (height < property && width < property) {
			defaultIsLarger = false;
			textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, Util.pixelsToSp(this, textCover.getTextSize()) + 1f);
			bounds = new Rect();
			textPaint = textCover.getPaint();
			textPaint.getTextBounds(image, 0, image.length(), bounds);
			height = bounds.height();
			width = bounds.width();
		}
		if (defaultIsLarger) {
			while (height > property && width > property) {
				defaultIsLarger = false;
				textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, Util.pixelsToSp(this, textCover.getTextSize()) - 1f);
				bounds = new Rect();
				textPaint = textCover.getPaint();
				textPaint.getTextBounds(image, 0, image.length(), bounds);
				height = bounds.height();
				width = bounds.width();
			}
		}
		return Util.textViewToBitmap(textCover, outWidth, outHeight);
	}

	@Override
	public String getDirectory() {
		return NewMaterialMusicDownloaderApp.getDirectory();
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
	protected void setCover(Bitmap bmp) {
		if (null == bmp) {
			String cover = getString(R.string.font_musics);
			bmp = getDefaultBitmapCover(64, 62, 60, cover);
		}
		((ImageView)findViewById(R.id.mini_player_cover)).setImageBitmap(bmp);
	}
	
	@Override
	protected void setPlayPauseMini(boolean playPayse) {
		Bitmap bmp = getDefaultBitmapCover(Util.dpToPx(this, 46), Util.dpToPx(this, 46), Util.dpToPx(this, 45), playPayse ? getString(R.string.font_pause_mini) : getString(R.string.font_play_mini));
		((ImageView) findViewById(R.id.mini_player_play_pause)).setImageBitmap(bmp);
	}
	
	@Override
	protected void setImageDownloadButton() {
		Bitmap bmp = getDefaultBitmapCover(Util.dpToPx(this, 46), Util.dpToPx(this, 46), Util.dpToPx(this, 45), getString(R.string.font_download_button));
		((ImageView) findViewById(R.id.mini_player_download)).setImageBitmap(bmp);
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
	
//	@Override
//	public void onQueryTextSubmitAct(String query) {
//		String lastFragmentName = getPreviousFragmentName(1);
//		if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
//			LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
//			if (fragment.isVisible()) {
//				if (query.isEmpty()) {
//					fragment.clearFilter();
//				} else {
//					fragment.setFilter(query);
//				}
//			}
//		} else if(lastFragmentName.equals(PlaylistFragment.class.getSimpleName())){
//			PlaylistFragment fragment = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
//			if (fragment.isVisible()) {
//				if (query.isEmpty()) {
//					fragment.clearFilter();
//				} else {
//					fragment.setFilter(query);
//				}
//			}
//		}
//	}

//	@Override
//	public void onQueryTextChangeAct(String newText) {
//		if ("".equals(newText)) {
//			String fragmentName =  getPreviousFragmentName(1);
//			Fragment fragment = getFragmentManager().findFragmentByTag(fragmentName);
//			if (LibraryFragment.class == fragment.getClass()) {
//				if (fragment.isVisible()) {
//					((LibraryFragment) fragment).clearFilter();
//				}
//			} else if (PlaylistFragment.class == fragment.getClass()) {
//				if (fragment.isVisible()) {
//					((PlaylistFragment) fragment).clearFilter();
//				}
//			}
//		}
//	}
	
//	@Override
//	protected void showPlayerFragment() {
//		setDrawerEnabled(false);
//		onNavigationDrawerItemSelected(PLAYER_FRAGMENT);
//	}
//	
//	@Override
//	public int getSettingsIcon() {
//		return R.string.font_play_settings;
//	}

	@Override
	protected int getFakeViewID() {
		return R.id.fake_view;
	}

	@Override
	protected void showPlayerFragment() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void showPlayerElement(boolean flag) {
		// TODO Auto-generated method stub

	}

	public void changeFragment(Fragment fragment, boolean isAnimate) {
		if (((Fragment) fragment).isAdded()) {
			((Fragment) fragment).onResume();
		}
		FragmentTransaction tr = getFragmentManager().beginTransaction();
		if (isAnimate) {
			tr.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down);
		}
		tr.replace(R.id.content_frame, (Fragment) fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName())
				.commit();
	}

}