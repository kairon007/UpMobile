package org.upmobile.musicpro.activity;

import java.util.ArrayList;
import java.util.List;

import org.upmobile.musicpro.R;
import org.upmobile.musicpro.config.GlobalValue;
import org.upmobile.musicpro.database.DatabaseUtility;
import org.upmobile.musicpro.fragment.PlayerFragment;
import org.upmobile.musicpro.modelmanager.ModelManager;
import org.upmobile.musicpro.modelmanager.ModelManagerListener;
import org.upmobile.musicpro.object.CategoryMusic;
import org.upmobile.musicpro.object.Playlist;
import org.upmobile.musicpro.object.Song;
import org.upmobile.musicpro.service.MusicService;
import org.upmobile.musicpro.service.MusicService.ServiceBinder;
import org.upmobile.musicpro.service.PlayerListener;
import org.upmobile.musicpro.slidingmenu.SlidingMenu;
import org.upmobile.musicpro.util.Logger;
import org.upmobile.musicpro.widget.AutoBgButton;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnClickListener {
	private static final String EXTRA_IS_PLAYING_BEFORE = "EXTRA_IS_PLAYING_BEFORE";
	private static final String EXTRA_FOOTER_VISIBILITY = "EXTRA_FOOTER_VISIBILITY";
	public static final int TOP_CHART = 0;
	public static final int NOMINATIONS = 1;
	public static final int CATEGORY_MUSIC = 2;
	public static final int PLAYLIST = 3;
	public static final int SEARCH = 4;
	public static final int SEARCH_ONLINE = 6;
	public static final int LIBRARY = 7;

	public static final int LIST_SONG_FRAGMENT = 0;
	public static final int CATEGORY_MUSIC_FRAGMENT = 1;
	public static final int PLAYLIST_FRAGMENT = 2;
	public static final int SEARCH_FRAGMENT = 3;
	public static final int SETTING_FRAGMENT = 4;
	public static final int PLAYER_FRAGMENT = 5;
	public static final int SEARCH_ONLINE_FRAGMENT = 6;
	public static final int LIBRARY_FRAGMENT = 7;

	public static final int FROM_LIST_SONG = 0;
	public static final int FROM_NOTICATION = 1;
	public static final int FROM_SEARCH = 2;
	public static final int FROM_OTHER = 3;

	public static final int NOTIFICATION_ID = 231109;

	private FragmentManager fm;
	private Fragment[] arrayFragments;
	public SlidingMenu menu;
	public ModelManager modelManager;
	private HeadsetIntentReceiver headsetReceiver;
	private TelephonyManager telephonyManager;

	private AutoBgButton btnPreviousFooter, btnPlayFooter, btnNextFooter;
	// private ImageView imgSongFooter;
	private View layoutPlayerFooter;
	private TextView lblSongNameFooter, lblArtistFooter;

	private TextView lblTopChart, lblNominations, lblCategoryMusic,
			lblPlaylist, lblSearch, lblSearchOnline, lblLibrary;

	private boolean doubleBackToExitPressedOnce;
	private boolean isPlayingBeforeCall;

	public int currentFragment;
	public int currentMusicType;
	public int toMusicPlayer;
	private Playlist currentPlaylist;

	public String nextPageNomination;
	public String nextPageTopWeek;

	public List<Song> listNominations;
	public List<Song> listTopWeek;

	public DatabaseUtility databaseUtility;

	private MusicService mService;
	private Intent intentService;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ServiceBinder binder = (ServiceBinder) service;
			mService = binder.getService();
			mService.setListSongs(GlobalValue.listSongPlay);
			mService.setListener(new PlayerListener() {
				@Override
				public void onSeekChanged(String lengthTime,
						String currentTime, int progress) {
					((PlayerFragment) arrayFragments[PLAYER_FRAGMENT])
							.seekChanged(lengthTime, currentTime, progress);
				}

				@Override
				public void onChangeSong(int indexSong) {
					((PlayerFragment) arrayFragments[PLAYER_FRAGMENT]).changeSong(indexSong);
					lblSongNameFooter.setText(GlobalValue.getCurrentSong().getName());
					lblArtistFooter.setText(GlobalValue.getCurrentSong().getArtist());
				}
			});
			setVisibilityFooter();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};
	
	private class HeadsetIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
				mService.pauseMusic();
				setButtonPlay();
			}
		}
	};
	
	PhoneStateListener phoneStateListener = new PhoneStateListener() {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				isPlayingBeforeCall = mService.isPlay();
				mService.pauseMusic();
				setButtonPlay();
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (isPlayingBeforeCall) {
					mService.playOrPauseMusic();
					setButtonPlay();
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	};
	
	@Override
	protected void onRestart() {
		super.onRestart();
	};


	private boolean fromLibrary = false; //indicate �alling music - true from library PlayerService
	private PlaybackService playbackService;
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {
		
		@Override
		public void start(AbstractSong song) {
			btnPlayFooter.setBackgroundResource(R.drawable.bg_btn_pause_small);
			lblSongNameFooter.setText(song.getTitle());
			lblArtistFooter.setText(song.getArtist());
		}

		@Override
		public void play(AbstractSong song) {
			btnPlayFooter.setBackgroundResource(R.drawable.bg_btn_pause_small);
		}

		@Override
		public void pause(AbstractSong song) {
			btnPlayFooter.setBackgroundResource(R.drawable.bg_btn_play_small);
		}

		@Override
		public void stop(AbstractSong song) {
			
		}

		@Override
		public void update(AbstractSong song) {
			lblSongNameFooter.setText(song.getTitle());
			lblSongNameFooter.setText(song.getTitle());
			lblArtistFooter.setText(song.getArtist());
		}

		@Override
		public void error() {
			
		}

		@Override
		public void stopPressed() {
			
		}
		
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		splashLogics();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		initList();
		databaseUtility = new DatabaseUtility(this);
		setContentView(R.layout.activity_main);
		startService(new Intent(this, PlaybackService.class));
		modelManager = new ModelManager(this);
		initService();
		initMenu();
		initUI();
		initControl();
		initFragment();
		telephonyManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		try {
			getIntent().getExtras().get("notification");
			toMusicPlayer = MainActivity.FROM_NOTICATION;
			showFragment(PLAYER_FRAGMENT);
		} catch (Exception e) {
			if (GlobalValue.currentMenu == PLAYER_FRAGMENT) {
				onClickPlayerFooter();
			} else {
				setSelect(GlobalValue.currentMenu);
			}
		}
		headsetReceiver = new HeadsetIntentReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(headsetReceiver, filter);

		if (null != savedInstanceState) {
			isPlayingBeforeCall = savedInstanceState.getBoolean(EXTRA_IS_PLAYING_BEFORE);
			if (savedInstanceState.getBoolean(EXTRA_FOOTER_VISIBILITY)) {
				if (GlobalValue.currentMenu !=  PLAYER_FRAGMENT) {
					layoutPlayerFooter.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			unbindService(mConnection);
		} catch (Exception e) {
			e.printStackTrace();
			cancelNotification();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(headsetReceiver);
		cancelNotification();
		super.onDestroy();
	}
	
	private void splashLogics() {
//		LanguageUtil.setLocale(new MySharedPreferences(this).getLanguage(),
//				this);

		GlobalValue.constructor(this);
		modelManager = new ModelManager(this);
		modelManager.getBaseUrl(new ModelManagerListener() {
			@Override
			public void onSuccess(Object object) {
				getListMusicType();
			}

			@Override
			public void onError() {
				getListMusicType();
			}
		});
	}
	
	private void getListMusicType() {
		modelManager.getListMusicTypes(new ModelManagerListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Object object) {
				GlobalValue.listCategoryMusics.addAll((List<CategoryMusic>) object);
			}

			@Override
			public void onError() {
				Toast.makeText(getApplicationContext(), "There is an error with the internet connection. Music data cannot be loaded.", Toast.LENGTH_LONG).show();
			}
		});
	}

	public void setVisibilityFooter() {
		try {
			if (mService.isPause() || mService.isPlay()) {
				if (GlobalValue.currentMenu !=  PLAYER_FRAGMENT) {
					layoutPlayerFooter.setVisibility(View.VISIBLE);
				}
			} else {
				layoutPlayerFooter.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			layoutPlayerFooter.setVisibility(View.GONE);
		}
	}

	private void initService() {
		intentService = new Intent(this, MusicService.class);
		startService(intentService);
		bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
	}

	private void initUI() {
		btnPreviousFooter = (AutoBgButton) findViewById(R.id.btnPreviousFooter);
		btnPlayFooter = (AutoBgButton) findViewById(R.id.btnPlayFooter);
		btnNextFooter = (AutoBgButton) findViewById(R.id.btnNextFooter);
		// imgSongFooter = (ImageView) findViewById(R.id.imgSongFooter);
		layoutPlayerFooter = (LinearLayout) findViewById(R.id.layoutPlayerFooter);
		lblSongNameFooter = (TextView) findViewById(R.id.lblSongNameFooter);
		lblArtistFooter = (TextView) findViewById(R.id.lblArtistFooter);
		lblTopChart = (TextView) menu.findViewById(R.id.lblTopChart);
		lblNominations = (TextView) menu.findViewById(R.id.lblNominations);
		lblCategoryMusic = (TextView) menu.findViewById(R.id.lblCategoryMusic);
		lblPlaylist = (TextView) menu.findViewById(R.id.lblPlaylist);
		lblSearch = (TextView) menu.findViewById(R.id.lblSearch);
		lblSearchOnline = (TextView) menu.findViewById(R.id.lblSearchOnline);
		lblLibrary = (TextView) menu.findViewById(R.id.lblLibrary);

		if(GlobalValue.getCurrentSong() != null){
			lblSongNameFooter.setText(GlobalValue.getCurrentSong()
					.getName());
			lblArtistFooter.setText(GlobalValue.getCurrentSong()
					.getArtist());
		}

	}

	private void initControl() {
		btnPreviousFooter.setOnClickListener(this);
		btnPlayFooter.setOnClickListener(this);
		btnNextFooter.setOnClickListener(this);
		layoutPlayerFooter.setOnClickListener(this);
		lblTopChart.setOnClickListener(this);
		lblNominations.setOnClickListener(this);
		lblCategoryMusic.setOnClickListener(this);
		lblPlaylist.setOnClickListener(this);
		lblSearch.setOnClickListener(this);
		lblSearchOnline.setOnClickListener(this);
		lblLibrary.setOnClickListener(this);
		lblSongNameFooter.setSelected(true);
		lblArtistFooter.setSelected(true);
	}

	private void initFragment() {
		fm = getSupportFragmentManager();
		arrayFragments = new Fragment[8];
		arrayFragments[LIST_SONG_FRAGMENT] = fm.findFragmentById(R.id.fragmentListSongs);
		arrayFragments[CATEGORY_MUSIC_FRAGMENT] = fm.findFragmentById(R.id.fragmentCategoryMusic);
		arrayFragments[PLAYLIST_FRAGMENT] = fm.findFragmentById(R.id.fragmentPlaylist);
		arrayFragments[SEARCH_FRAGMENT] = fm.findFragmentById(R.id.fragmentSearch);
		arrayFragments[SETTING_FRAGMENT] = fm.findFragmentById(R.id.fragmentSetting);
		arrayFragments[PLAYER_FRAGMENT] = fm.findFragmentById(R.id.fragmentPlayer);
		arrayFragments[SEARCH_ONLINE_FRAGMENT] = fm.findFragmentById(R.id.fragmentSearchOnline);
		arrayFragments[LIBRARY_FRAGMENT] = fm.findFragmentById(R.id.fragmentLibrary);
		FragmentTransaction transaction = fm.beginTransaction();
		for (Fragment fragment : arrayFragments) {
			transaction.hide(fragment);
		}
		transaction.commit();
	}

	private void showFragment(int fragmentIndex) {
		currentFragment = fragmentIndex;
		FragmentTransaction transaction = fm.beginTransaction();
		for (Fragment fragment : arrayFragments) {
			transaction.hide(fragment);
		}
		transaction.show(arrayFragments[fragmentIndex]);
		transaction.commit();
		Logger.e(fragmentIndex);
	}
	
	private void initList() {
		listNominations = new ArrayList<Song>();
		listTopWeek = new ArrayList<Song>();
	}

	private void initMenu() {
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.layout_menu);

		// lsvMenu = (NoScrollListView)
		// menu.getMenu().findViewById(R.id.lsvMenu);
		// lsvMenu.setAdapter(musicTypeAdapter);
		// lsvMenu.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> adapter, View view, int
		// position, long id) {
		// musicTypeAdapter.setIndex(position);
		// setSelect(MUSIC_TYPE);
		// }
		// });
		initMenuControl();
	}

	private void initMenuControl() {
		// layoutSelectNominations = (View)
		// menu.findViewById(R.id.layoutSelectNominations);
		// layoutSelectTopWeek = (View)
		// menu.findViewById(R.id.layoutSelectTopWeek);
		// layoutSelectGoodApp = (View)
		// menu.findViewById(R.id.layoutSelectGoodApp);
		//
		// findViewById(R.id.layoutNominations).setOnClickListener(new
		// OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// setSelect(NOMINATIONS);
		// }
		// });
		// findViewById(R.id.layoutTopWeek).setOnClickListener(new
		// OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// setSelect(TOP_CHART);
		// }
		// });
		// findViewById(R.id.layoutGoodApp).setOnClickListener(new
		// OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// // setSelect(GOOD_APP);
		// Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
		// .parse("https://play.google.com/store/apps/collection/topselling_new_free"));
		// startActivity(browserIntent);
		// overridePendingTransition(R.anim.slide_in_left,
		// R.anim.slide_out_left);
		// }
		// });
		//
		// if (GlobalValue.listCategoryMusics.size() == 0) {
		// modelManager.getListMusicTypes(new ModelManagerListener() {
		// @SuppressWarnings("unchecked")
		// @Override
		// public void onSuccess(Object object) {
		// GlobalValue.listCategoryMusics.addAll((List<CategoryMusic>) object);
		// musicTypeAdapter.notifyDataSetChanged();
		// }
		//
		// @Override
		// public void onError() {
		// }
		// });
		// }
	}
	
	public void initPlayback() {
		fromLibrary = true;
		playbackService = PlaybackService.get(this);
		playbackService.addStatePlayerListener(stateListener);
	}
	
	public MusicService getService(boolean fromApp){
		if (fromApp) {
			fromLibrary = !fromApp;
		}
		return mService;
	}
	
	public void gotoFragment(int fragment) {
		if (currentFragment == fragment) {
			return;
		}
		if (fragment == PLAYER_FRAGMENT && PlaybackService.hasInstance()) {
			PlaybackService.get(this).stop();
		}
		GlobalValue.currentMenu = fragment;
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_left,
				R.anim.slide_out_left);
		transaction.show(arrayFragments[fragment]);
		transaction.hide(arrayFragments[currentFragment]);
		transaction.commit();
		currentFragment = fragment;
	}

	public void backFragment(int fragment, int fragmentId) {
		setBackgroundColorDrawer(fragmentId);
		GlobalValue.currentMenu = fragment;
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
		transaction.show(arrayFragments[fragment]);
		transaction.hide(arrayFragments[currentFragment]);
		transaction.commit();
		currentFragment = fragment;
	}

	private void setSelect(int select) {
		GlobalValue.currentMenu = select;
		switch (select) {
		case TOP_CHART:
			setBackgroundColorDrawer(TOP_CHART);
			showFragment(LIST_SONG_FRAGMENT);
			break;

		case NOMINATIONS:
			setBackgroundColorDrawer(NOMINATIONS);
			showFragment(LIST_SONG_FRAGMENT);
			break;

		case CATEGORY_MUSIC:
			setBackgroundColorDrawer(CATEGORY_MUSIC);
			showFragment(CATEGORY_MUSIC_FRAGMENT);
			break;

		case PLAYLIST:
			setBackgroundColorDrawer(PLAYLIST);
			showFragment(PLAYLIST_FRAGMENT);
			break;

		case SEARCH:
			setBackgroundColorDrawer(SEARCH);
			showFragment(SEARCH_FRAGMENT);
			break;

		case SEARCH_ONLINE:
			setBackgroundColorDrawer(SEARCH_ONLINE);
			showFragment(SEARCH_ONLINE_FRAGMENT);
			break;
			
		case LIBRARY:
			setBackgroundColorDrawer(LIBRARY);
			showFragment(LIBRARY_FRAGMENT);
			break;

		}
		menu.showContent();
	}

	private void setBackgroundColorDrawer(int fragmentId) {
		if (fragmentId == TOP_CHART) {
			lblTopChart.setBackgroundResource(R.drawable.bg_item_menu_select);
		} else {
			lblTopChart.setBackgroundColor(Color.TRANSPARENT);
		}
		if (fragmentId == NOMINATIONS) {
			lblNominations.setBackgroundResource(R.drawable.bg_item_menu_select);
		} else {
			lblNominations.setBackgroundColor(Color.TRANSPARENT);
		}
		if (fragmentId == CATEGORY_MUSIC) {
			lblCategoryMusic.setBackgroundResource(R.drawable.bg_item_menu_select);
		} else {
			lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
		}
		if (fragmentId == PLAYLIST) {
			lblPlaylist.setBackgroundResource(R.drawable.bg_item_menu_select);
		} else {
			lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
		}
		if (fragmentId == SEARCH) {
			lblSearch.setBackgroundResource(R.drawable.bg_item_menu_select);
		} else {
			lblSearch.setBackgroundColor(Color.TRANSPARENT);
		}
		if (fragmentId == SEARCH_ONLINE) {
			lblSearchOnline.setBackgroundResource(R.drawable.bg_item_menu_select);
		} else {
			lblSearchOnline.setBackgroundColor(Color.TRANSPARENT);
		}
		if (fragmentId == LIBRARY) {
			lblLibrary.setBackgroundResource(R.drawable.bg_item_menu_select);
		} else {
			lblLibrary.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	public void setButtonPlay() {
		if (mService.isPause()) {
			btnPlayFooter.setBackgroundResource(R.drawable.bg_btn_play_small);
		} else {
			btnPlayFooter.setBackgroundResource(R.drawable.bg_btn_pause_small);
		}
		((PlayerFragment) arrayFragments[PLAYER_FRAGMENT]).setButtonPlay();
	}

	public void cancelNotification() {
		NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nMgr.cancel(NOTIFICATION_ID);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPreviousFooter:
			if (fromLibrary) {
				playbackService.shift(-1);
			} else {
				onClickPreviousFooter();
			}
			break;

		case R.id.btnPlayFooter:
			if (fromLibrary) {
				playbackService.play(playbackService.getPlayingSong());
			} else {
				onClickPlayFooter();
			}
			break;

		case R.id.btnNextFooter:
			if (fromLibrary) {
				playbackService.shift(1);
			} else {
				onClickNextFooter();
			}
			break;

		case R.id.layoutPlayerFooter:
			onClickPlayerFooter();
			break;

		case R.id.lblTopChart:
			onClickTopChart();
			break;

		case R.id.lblNominations:
			onClickNominations();
			break;

		case R.id.lblCategoryMusic:
			onClickCategoryMusic();
			break;

		case R.id.lblPlaylist:
			onClickPlaylist();
			break;

		case R.id.lblSearch:
			onClickSearch();
			break;

		case R.id.lblSearchOnline:
			onClickSearchOnline();
			break;
		case R.id.lblLibrary:
			onClickLibrary();
			break;
		}
	}

	private void onClickPreviousFooter() {
		mService.backSongByOnClick();
	}

	private void onClickPlayFooter() {
		mService.playOrPauseMusic();
		setButtonPlay();
	}

	private void onClickNextFooter() {
		mService.nextSongByOnClick();
	}

	private void onClickPlayerFooter() {
		toMusicPlayer = FROM_OTHER;
		layoutPlayerFooter.setVisibility(View.GONE);
		GlobalValue.currentMenu = PLAYER_FRAGMENT;
		gotoFragment(PLAYER_FRAGMENT);
	}

	private void onClickTopChart() {
		setSelect(TOP_CHART);
	}

	private void onClickNominations() {
		setSelect(NOMINATIONS);
	}

	private void onClickCategoryMusic() {
		setSelect(CATEGORY_MUSIC);
	}

	private void onClickPlaylist() {
		setSelect(PLAYLIST);
	}

	private void onClickSearch() {
		setSelect(SEARCH);
	}
	
	private void onClickSearchOnline() {
		setSelect(SEARCH_ONLINE);
	}
	
	private void onClickLibrary() {
		setSelect(LIBRARY);
	}

	@Override
	public void onBackPressed() {
		if (menu.isMenuShowing()) {
			menu.showContent();
		} else {
			switch (currentFragment) {
			case PLAYER_FRAGMENT:
				if (toMusicPlayer == FROM_SEARCH) {
					backFragment(SEARCH_FRAGMENT, SEARCH);
				} else {
					backFragment(LIST_SONG_FRAGMENT, TOP_CHART);
				}
				break;

			case LIST_SONG_FRAGMENT:
				if (GlobalValue.currentMenu == CATEGORY_MUSIC) {
					backFragment(CATEGORY_MUSIC_FRAGMENT, CATEGORY_MUSIC);
				} else if (GlobalValue.currentMenu == PLAYLIST) {
					backFragment(PLAYLIST_FRAGMENT, PLAYLIST);
				} else {
					quitApp();
				}
				break;

			default:
				quitApp();
				break;
			}
		}
	}

	private void quitApp() {
		if (doubleBackToExitPressedOnce) {
			if (null == playbackService) {
				playbackService = PlaybackService.get(this);
			}
			playbackService.reset();
			finish();
			stopService(intentService);
			cancelNotification();
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
			return;
		}

		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, R.string.doubleBackToExit, Toast.LENGTH_SHORT)
				.show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				doubleBackToExitPressedOnce = false;
			}
		}, 2000);
	}
	
	public void setPlaylist(Playlist playlist) {
		currentPlaylist = playlist;
	}
	
	public Playlist getPlaylist() {
		return currentPlaylist;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(EXTRA_FOOTER_VISIBILITY, layoutPlayerFooter.getVisibility() == View.VISIBLE);
		outState.putBoolean(EXTRA_IS_PLAYING_BEFORE,isPlayingBeforeCall);
		super.onSaveInstanceState(outState);
	}
}
