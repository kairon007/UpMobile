package org.upmobile.newmaterialmusicdownloader.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.upmobile.newmaterialmusicdownloader.BaseDownloadListener;
import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;
import org.upmobile.newmaterialmusicdownloader.data.NavDrawerItem;
import org.upmobile.newmaterialmusicdownloader.drawer.FragmentDrawer;
import org.upmobile.newmaterialmusicdownloader.drawer.FragmentDrawer.FragmentDrawerListener;
import org.upmobile.newmaterialmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.newmaterialmusicdownloader.fragment.SearchFragment;
import org.upmobile.newmaterialmusicdownloader.ui.dialog.FolderSelectorDialog;
import org.upmobile.newmaterialmusicdownloader.ui.dialog.FolderSelectorDialog.FolderSelectCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;
import ru.johnlife.uilibrary.widget.customviews.CircleImageView;
import ru.johnlife.uilibrary.widget.customviews.playpause.PlayPauseView;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoBar;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarStyle;

public class MainActivity extends BaseMiniPlayerActivity implements Constants, FolderSelectCallback, FragmentDrawerListener, DrawerListener {

	private int currentFragmentId = -1;
	private int lastCheckPosition = 0;
	private boolean isVisibleSearchView = false;
	private boolean isOpenFromDraver = false;
	private boolean isDrawerOpen = false;
	private SearchView searchView;
	private View floatBtnContainer;
	private Toolbar toolbar;
	private View toolbarShadow;
	private FragmentDrawer drawerFragment;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbarShadow = findViewById(R.id.toolbar_shadow);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
        drawerFragment.setFragmentDrawerListener(this);
        drawerFragment.setDrawerListener(this);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isOpenFromDraver) {
					onBackPressed();
				} else {
					drawerFragment.openDrawer();
				}
			}
		});
        changeFragment(ManagerFragmentId.searchFragment(), true);
	}
	
	@Override
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) searchItem.getActionView();
		searchView.setQueryHint(getResources().getString(R.string.hint_main_search));
		((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				if (ManagerFragmentId.playlistFragment() == getCurrentFragmentId()) {
					PlaylistFragment playlist = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
					playlist.forceDelete();
				}
				if (ManagerFragmentId.libraryFragment() == getCurrentFragmentId()) {
					LibraryFragment library = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
					library.forceDelete();
				}
			}
		});
		searchView.setOnSearchClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ManagerFragmentId.playlistFragment() == getCurrentFragmentId()) {
					PlaylistFragment playlist = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
					playlist.forceDelete();
				}
				if (ManagerFragmentId.libraryFragment() == getCurrentFragmentId()) {
					LibraryFragment library = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
					library.forceDelete();
				}
			}
		});
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
		if (drawerFragment.isVisible()) {
			drawerFragment.closeDrawer();
		} else if (null != player && player.isVisible()) {
			showMiniPlayer(service.isEnqueueToStream());
			getFragmentManager().popBackStack();
			isOpenFromDraver = true;
			setPlayerFragmentVisible(false);
		} else {
			if (ManagerFragmentId.playerFragment() == getCurrentFragmentId()) {
				service.stopPressed();
				stopService(new Intent(this, PlaybackService.class));
				finish();
			} else if (null != service && isMiniPlayerPrepared()) {
				service.stopPressed();
			} else {
				stopService(new Intent(this, PlaybackService.class));
				finish();
			}
		}
	}
	
	@Override
	public void setTitle(int titleId) {
		setTitle(getString(titleId));
	}
	
	@Override
	public void setTitle(CharSequence title) {
		getSupportActionBar().setTitle(title);
	}

    @Override
    protected void miniPlayerAnimationStart(boolean isUpAnimation) {
        PlaylistFragment playlist = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
        if (null != playlist) {
            playlist.miniPlayerAnimationStart(isUpAnimation);
        }
    }

    @Override
    protected void miniPlayerAnimationEnd(boolean isUpAnimation) {
        PlaylistFragment playlist = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
        if (null != playlist) {
            playlist.miniPlayerAnimationEnd(isUpAnimation);
        }
    }

	public void changeFragment(int fragmentId, boolean fromDraver) {
		changeFragment(fragmentId, fromDraver, null);
	}

	public void changeFragment(int fragmentId, boolean fromDraver, AbstractSong song) {
		if (getCurrentFragmentId() == fragmentId) return;
		isOpenFromDraver = fromDraver;
		if (ManagerFragmentId.playerFragment() != fragmentId) {
			setPlayerFragmentVisible(false);
			showMiniPlayer(null != service && service.isEnqueueToStream());
		}
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment selectedFragment;
		if (fragmentId == ManagerFragmentId.searchFragment()) {
			selectedFragment = new SearchFragment();
		} else if (fragmentId == ManagerFragmentId.downloadFragment()) {
			selectedFragment = new DownloadsFragment();
		} else if (fragmentId == ManagerFragmentId.playlistFragment()) {
			selectedFragment = new PlaylistFragment();
		} else if (fragmentId == ManagerFragmentId.libraryFragment()) {
			Bundle b = new Bundle();
			b.putParcelable("KEY_SELECTED_SONG", song);
			LibraryFragment libraryFragment = new LibraryFragment();
			libraryFragment.setArguments(b);
			selectedFragment = libraryFragment;
		} else if (fragmentId == ManagerFragmentId.playerFragment()) {
			selectedFragment = new PlayerFragment();
			showMiniPlayer(false);
			if (Nulldroid_Settings.ENABLE_ANIMATIONS) {
				transaction.setCustomAnimations(R.anim.fragment_slide_in_up, R.anim.fragment_slide_out_up, R.anim.fragment_slide_in_down, R.anim.fragment_slide_out_down);
			}
		} else {
			selectedFragment = new SearchFragment();
		}
        setSearchViewVisibility(selectedFragment.getClass().getSimpleName());
        transaction.replace(R.id.content_frame, selectedFragment, selectedFragment.getClass().getSimpleName()).addToBackStack(selectedFragment.getClass().getSimpleName()).setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }
	
	@Override
	public void onFolderSelection(final File folder) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putString(PREF_DIRECTORY, folder.getAbsolutePath());
		editor.putString(PREF_DIRECTORY_PREFIX, File.separator + folder.getAbsoluteFile().getName() + File.separator);
		editor.apply();
		new Thread(new Runnable() {

			@Override
			public void run() {
				final StateKeeper keeper = StateKeeper.getInstance();
				keeper.notifyLable(false);
				keeper.initSongHolder(folder.getAbsolutePath());
				checkDownloadingUrl(false);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						keeper.notifyLable(true);
					}
				});
			}
		}).start();
	}

	public void setDraverEnabled(boolean isVisibleDraver) {
		boolean value = isVisibleDraver || isOpenFromDraver;
		drawerFragment.setDrawableLockMode(value);
	}

	public void setCurrentFragmentId(int currentFragmentId) {
		this.currentFragmentId = currentFragmentId;
		currentFragmentIsPlayer = ManagerFragmentId.playerFragment() == currentFragmentId;
	}
	
	public int getCurrentFragmentId() {
		return currentFragmentId;
	}

	@Override
	public boolean onMenuOpened(final int featureId, final Menu menu) {
	    super.onMenuOpened(featureId, menu);
	    return false;
	}
	
	@Override
	public void showPlayerElement(boolean flag) {
		ManagerFragmentId.switchMode(flag);
		drawerFragment.showPlayerElement(flag);
		if (flag) {
			Fragment playlist = getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
			if (null != playlist && playlist.isVisible()) {
				setCurrentFragmentId(ManagerFragmentId.playlistFragment());
			}
		} else {
			Fragment playlist = getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
			if (null != playlist && playlist.isVisible()) {
				setCurrentFragmentId(ManagerFragmentId.playlistFragment());
			}
		}
	}

	public void onQueryTextSubmitAct(String query) {
		String lastFragmentName = getPreviousFragmentName(1);
		if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
			LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
			fragment.forceDelete();
			if (fragment.isVisible()) {
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		} else if (lastFragmentName.equals(PlaylistFragment.class.getSimpleName())) {
			PlaylistFragment fragment = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
			fragment.forceDelete();
			if (fragment.isVisible()) {
				fragment.collapseAll();
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		}
	}

	public void onQueryTextChangeAct(String newText) {
		if (newText.isEmpty()) {
			String fragmentName = getPreviousFragmentName(1);
			Fragment fragment = getFragmentManager().findFragmentByTag(fragmentName);
            if (fragment.isVisible()) {
                if (LibraryFragment.class == fragment.getClass()) {
                    ((LibraryFragment) fragment).clearFilter();
                } else if (PlaylistFragment.class == fragment.getClass()) {
                    ((PlaylistFragment) fragment).clearFilter();
                }
            }
		}
	}

	public void showToolbarShadow(boolean isShowing) {
		toolbarShadow.setVisibility(isShowing ? View.VISIBLE : View.GONE);
	}
	
	public void showMessage(String msg) {
		UndoBarController.clear(this);
		UndoBar message = new UndoBar(this);
		message.message(msg);
		message.style(UndoBarController.MESSAGESTYLE);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int width = Util.pxToDp(this, metrics.widthPixels);
		if (ManagerFragmentId.playlistFragment() != currentFragmentId || width > 520) {// for folder values-w520dp
			message.show(false);
			return;
		}
		View floatBtn = findViewById(R.id.floatingButton);
		View miniplayer = findViewById(getMiniPlayerID());
		if (null != floatBtn && miniplayer.getVisibility() != View.VISIBLE) {
			floatBtnContainer = (View) floatBtn.getParent();
			message.listener(new UndoBarController.AdvancedUndoListener() {

				@Override
				public void onUndo(@Nullable Parcelable token) {
				}

				@Override
				public void onHide(@Nullable Parcelable token) {
					floatBtnContainer.setPadding(0, 0, 0, 0);
				}

				@Override
				public void onClear(@NonNull Parcelable[] token) {
				}
			});
			message.show(false);
			int height = message.getHeightBar();
			if (height < 48) {
				height = 48;
			} else if (80 < height) {
				height = 80;
			}
			floatBtnContainer.setPadding(0, 0, 0, Util.dpToPx(this, height));
			return;
		}
		message.show(false);
	}

	public void showMessage(int message) {
		showMessage(getString(message));
	}
	
	public int getLastCheckPosition() {
		return lastCheckPosition;
	}

	public void setLastCheckPosition(int lastCheckPosition) {
		this.lastCheckPosition = lastCheckPosition;
	}

	protected String getPreviousFragmentName(int position) {
		if (getFragmentManager().getBackStackEntryCount() < position) return SearchFragment.class.getSimpleName();
		android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - position);
        return backEntry.getName();
	}
	
	@Override
	protected boolean isAnimationEnabled() {
		return Nulldroid_Settings.ENABLE_ANIMATIONS;
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
	protected int getMiniPlayerDuplicateID() {
		return R.id.mini_player_duplicate;
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
		setPlayerFragmentVisible(true);
		changeFragment(ManagerFragmentId.playerFragment(), false);
	}

	@Override
	protected void setPlayPauseMini(boolean playPause) {
		((PlayPauseView) findViewById(R.id.mini_player_play_pause)).toggle(!playPause);
	}

	@Override
	public void setCover(Bitmap bmp) {
		if (null == bmp) {
			((CircleImageView) findViewById(R.id.mini_player_cover)).setImageResource(R.drawable.ic_album_grey);
			return;
		}
		service.updatePictureNotification(bmp);
		((CircleImageView) findViewById(R.id.mini_player_cover)).setImageBitmap(bmp);
	}

	@Override
	protected void setImageDownloadButton() {
		int height = 48;
		int width = 48;
		Bitmap bitMap = Bitmap.createBitmap(Util.dpToPx(this, width), Util.dpToPx(this, height), Bitmap.Config.ARGB_8888);
		bitMap = bitMap.copy(bitMap.getConfig(), true);
		Canvas canvas = new Canvas(bitMap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		float verts[] = { 
				Util.dpToPx(this, 0),
				Util.dpToPx(this, (height / 3 - 2)), 
				Util.dpToPx(this, (height - 1)), 
				Util.dpToPx(this, (height / 3 - 2)), 
				Util.dpToPx(this, (height / 8 * 4)),
				Util.dpToPx(this, (height / 8 * 6))};
		int verticesColors[] = { 
				getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)),
				getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)),
				getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)),
				getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)),
				getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)),
				getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary))};
		canvas.drawVertices(Canvas.VertexMode.TRIANGLES, verts.length, verts, 0, null, 0, verticesColors, 0, null, 0, 0, paint);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(Util.dpToPx(this, height / 8));
		paint.setColor(getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)));
		canvas.drawLine(0, Util.dpToPx(this, (height - 6)), Util.dpToPx(this, width), Util.dpToPx(this, (height - 6)), paint);
		paint.setStrokeWidth(Util.dpToPx(this, 34));
		canvas.drawLine(Util.dpToPx(this, width / 3), 0, Util.dpToPx(this, (height / 8 * 5 + 1)), 0, paint);
		((ImageView) findViewById(R.id.mini_player_download)).setImageBitmap(bitMap);
	}

	protected void setSearchViewVisibility(String fragmentName) {
		isVisibleSearchView = (fragmentName.equals(LibraryFragment.class.getSimpleName())) || (fragmentName.equals(PlaylistFragment.class.getSimpleName()));
	}
	
	@Override
	protected void checkOnStart(final boolean showMiniPlayer) {
		super.checkOnStart(ManagerFragmentId.playerFragment() != currentFragmentId);
	}
	
	@Override
	public void download(final RemoteSong song) {
		if (isThisSongDownloaded(song)) {
			UndoBarController.clear(this);
			UndoBar undo = new UndoBar(this);
			undo.message(R.string.has_been_downloaded);
			undo.duration(5000);
			undo.noicon(true);
			undo.style(new UndoBarStyle(-1, R.string.download_anyway));
			undo.listener(new UndoBarController.UndoListener() {

				@Override
				public void onUndo(Parcelable token) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							BaseDownloadListener downloadListener = new BaseDownloadListener(MainActivity.this, song, 0, true);
							downloadListener.setDownloadPath(getDirectory());
							downloadListener.setUseAlbumCover(true);
							downloadListener.downloadSong(false);
						}
					});
				}
			});
			undo.show();
		} else {
			BaseDownloadListener downloadListener = new BaseDownloadListener(this, song, 0, true);
			downloadListener.setDownloadPath(getDirectory());
			downloadListener.setUseAlbumCover(true);
			downloadListener.downloadSong(false);
		}
	}

	public boolean isThisSongDownloaded(AbstractSong song) {
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		return !(song.getClass() != MusicData.class && StateKeeper.DOWNLOADED != state);
	}
	
	@Override
	protected BaseDownloadSongTask createDownloadListener(RemoteSong song) {
		return new BaseDownloadListener(this, song, 0, true);
	}
	
	public void setToolbarOverlay(boolean isOverlay) {
		findViewById(R.id.fake_toolbar).setVisibility(isOverlay ? View.GONE : View.VISIBLE);
		if(!isOverlay) {
			setToolbarAlpha(255);
		}
	}

	public void setToolbarAlpha(int alpha) {
		if (alpha < 11) {
			toolbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.fake_toolbar_shadow));
		} else {
			toolbar.setBackgroundColor(getResources().getColor(Util.getResIdFromAttribute(this, R.attr.colorPrimary)));
			toolbar.getBackground().setAlpha(alpha);
		}
		toolbarShadow.setAlpha((float) alpha / 255);
	}
	
	public void setupDownloadBtn() { // in PlayerFragment
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		if (null != player && player.isVisible()) {
			((PlayerFragment) player).setupDownloadButton();
		}
	}

	@Override
	public void onDrawerItemSelected(View view, int position) {
		final int fragmentId = ++position;
		if (fragmentId ==  ManagerFragmentId.settingFragment()) {
			new FolderSelectorDialog().show(this);
			return;
		}
		changeFragment(fragmentId, true);
	}
	
	public List<NavDrawerItem> getData() {
		List<NavDrawerItem> data = new ArrayList<>();
		data.add(new NavDrawerItem(R.drawable.ic_search_grey, getResources().getString(R.string.tab_search), NavDrawerItem.Type.Primary));
		data.add(new NavDrawerItem(R.drawable.ic_file_download_grey, getResources().getString(R.string.tab_downloads), NavDrawerItem.Type.Primary));
		data.add(new NavDrawerItem(R.drawable.ic_my_library_music_grey , getResources().getString(R.string.tab_library), NavDrawerItem.Type.Primary));
		data.add(new NavDrawerItem(R.drawable.ic_queue_music_grey , getResources().getString(R.string.tab_playlist), NavDrawerItem.Type.Primary));
		data.add(new NavDrawerItem(getResources().getString(R.string.tab_download_location), NavDrawerItem.Type.Secondary ));
		data.add(new NavDrawerItem(R.drawable.ic_settings_applications_grey, NewMaterialApp.getDirectory(), NavDrawerItem.Type.Primary));
		return data;
	}
	
	@Override
	public void onDrawerOpened(View drawerView) {
		drawerFragment.setItemChecked(getCurrentFragmentId());
		isDrawerOpen = true;
		invalidateOptionsMenu();
		Util.hideKeyboard(this, drawerView);
	}

	@Override
	public void onDrawerClosed(View drawerView) {
		isDrawerOpen = false;
		invalidateOptionsMenu();
		Util.hideKeyboard(this, drawerView);
	}

	@Override
	public void onDrawerSlide(View drawerView, float slideOffset) {
		toolbar.setAlpha(1 - slideOffset / 2);
		Util.hideKeyboard(this, drawerView);
	}

	@Override
	public void onDrawerStateChanged(int newState) {
		if (newState == DrawerLayout.STATE_SETTLING) {
			if (ManagerFragmentId.playerFragment() == getCurrentFragmentId()) {
				PlayerFragment player = (PlayerFragment) getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
				if (!isDrawerOpen) {
					player.hideIndicator();
				} else {
					player.showIndicator();
				}
			}
			if (ManagerFragmentId.libraryFragment() == getCurrentFragmentId()) {
				LibraryFragment library = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
				library.forceDelete();
			}
		}
	}

	public SearchView getSearchView() {
		return searchView;
	}
}
