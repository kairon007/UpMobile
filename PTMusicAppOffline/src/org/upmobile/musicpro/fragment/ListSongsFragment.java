package org.upmobile.musicpro.fragment;

import java.util.ArrayList;
import java.util.List;

import org.upmobile.musicpro.BaseFragment;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.MainActivity;
import org.upmobile.musicpro.adapter.SongAdapter;
import org.upmobile.musicpro.config.GlobalValue;
import org.upmobile.musicpro.config.WebserviceConfig;
import org.upmobile.musicpro.modelmanager.ModelManagerListener;
import org.upmobile.musicpro.object.CategoryMusic;
import org.upmobile.musicpro.object.Song;
import org.upmobile.musicpro.slidingmenu.SlidingMenu;
import org.upmobile.musicpro.util.Logger;
import org.upmobile.musicpro.util.StringUtil;
import org.upmobile.musicpro.widget.pulltorefresh.PullToRefreshBase;
import org.upmobile.musicpro.widget.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import org.upmobile.musicpro.widget.pulltorefresh.PullToRefreshListView;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ListSongsFragment extends BaseFragment {
	private PullToRefreshListView lsvSong;
	private ListView lsvActually;
	private List<Song> listSongs;
	private SongAdapter songAdapter;
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_list_song, container, false);
		initUIBase(view);
		initControl(view);
		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			getMainActivity().menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			getMainActivity().setVisibilityFooter();
			initData();
		}
	}

	@Override
	protected void initUIBase(View view) {
		super.initUIBase(view);
		lsvSong = (PullToRefreshListView) view.findViewById(R.id.lsvSong);
		lsvActually = lsvSong.getRefreshableView();
	}

	private void initControl(View view) {
		setButtonMenu(view);
		listSongs = new ArrayList<Song>();
		songAdapter = new SongAdapter(getActivity(), listSongs);
		// lsvSong.setAdapter(songAdapter);
		// lsvSong.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> av, View v, int position, long
		// l) {
		// Logger.e("currentFragment: " + getMainActivity().currentFragment);
		// getMainActivity().toMusicPlayer = MainActivity.FROM_LIST_SONG;
		// GlobalValue.currentSongPlay = position;
		// GlobalValue.listSongPlay.clear();
		// GlobalValue.listSongPlay.addAll(listSongs);
		// GlobalValue.setFavoriteListSongPlay();
		// getMainActivity().gotoFragment(MainActivity.PLAYER_FRAGMENT);
		// }
		// });

		lsvActually.setAdapter(songAdapter);
		lsvSong.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position, long l) {
				getMainActivity().toMusicPlayer = MainActivity.FROM_LIST_SONG;
				GlobalValue.currentSongPlay = (int) l;
				GlobalValue.listSongPlay.clear();
				GlobalValue.listSongPlay.addAll(listSongs);
				getMainActivity().gotoFragment(MainActivity.PLAYER_FRAGMENT);
			}
		});

		lsvSong.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);
				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				getData(true);
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);
				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				getData(false);
			}
		});
	}

	private void getData(boolean isRefresh) {
		switch (GlobalValue.currentMenu) {
		case MainActivity.TOP_CHART:
			getTopWeekMusic(isRefresh);
			break;

		case MainActivity.NOMINATIONS:
			getNominationMusic(isRefresh);
			break;

		case MainActivity.CATEGORY_MUSIC:
			getListMusicWithType(isRefresh);
			break;
		}
	}

	private void getNominationMusic(final boolean isRefresh) {
		if (StringUtil.checkEndNextPage(getMainActivity().nextPageNomination)
				&& !isRefresh) {
			showNoMoreData();
		} else {
			String url;
			if (isRefresh) {
				url = WebserviceConfig.URL_NORMINATIONS;
			} else {
				url = WebserviceConfig.URL_NEXT_PAGE
						+ getMainActivity().nextPageNomination + ".xml";
			}
			getMainActivity().modelManager.getListSongs(url,
					new ModelManagerListener() {
						@SuppressWarnings("unchecked")
						@Override
						public void onSuccess(Object object) {
							Object[] objects = (Object[]) object;
							getMainActivity().nextPageNomination = String
									.valueOf(objects[0]);
							if (isRefresh) {
								getMainActivity().listNominations.clear();
								listSongs.clear();
							}
							getMainActivity().listNominations
									.addAll((List<Song>) objects[1]);
							listSongs.addAll((List<Song>) objects[1]);
							refreshList();
						}

						@Override
						public void onError() {
							refreshList();
						}
					});
		}
	}

	private void getTopWeekMusic(final boolean isRefresh) {
		if (StringUtil.checkEndNextPage(getMainActivity().nextPageTopWeek)
				&& !isRefresh) {
			showNoMoreData();
		} else {
			String url;
			if (isRefresh) {
				url = WebserviceConfig.URL_TOP_WEEK;
			} else {
				url = WebserviceConfig.URL_NEXT_PAGE
						+ getMainActivity().nextPageTopWeek + ".xml";
			}			
			getMainActivity().modelManager.getListSongs(url,
					new ModelManagerListener() {
						@SuppressWarnings("unchecked")
						@Override
						public void onSuccess(Object object) {
							Object[] objects = (Object[]) object;
							getMainActivity().nextPageTopWeek = String
									.valueOf(objects[0]);
							Log.d("NEXT_PAGE", getMainActivity().nextPageTopWeek);
							if (isRefresh) {
								getMainActivity().listTopWeek.clear();
								listSongs.clear();
							}
							getMainActivity().listTopWeek
									.addAll((List<Song>) objects[1]);
							listSongs.addAll((List<Song>) objects[1]);
							refreshList();
						}

						@Override
						public void onError() {
							refreshList();
						}
					});
		}
	}

	private void getListMusicWithType(final boolean isRefresh) {
		final CategoryMusic musicType = GlobalValue.listCategoryMusics
				.get(getMainActivity().currentMusicType);
		if (StringUtil.checkEndNextPage(musicType.getNextPage()) && !isRefresh) {
			showNoMoreData();
		} else {
			String url;
			if (isRefresh) {
				url = WebserviceConfig.URL_MUSIC_WITH_TYPE + musicType.getId()
						+ ".xml";
			} else {
				url = WebserviceConfig.URL_NEXT_PAGE + musicType.getNextPage()
						+ ".xml";
			}

			Logger.e("url: " + url);
			getMainActivity().modelManager.getListSongs(url,
					new ModelManagerListener() {
						@SuppressWarnings("unchecked")
						@Override
						public void onSuccess(Object object) {
							Object[] objects = (Object[]) object;
							musicType.setNextPage(String.valueOf(objects[0]));
							if (isRefresh) {
								musicType.clearSong();
								listSongs.clear();
							}
							musicType.addListSongs((List<Song>) objects[1]);
							listSongs.addAll((List<Song>) objects[1]);
							refreshList();
						}

						@Override
						public void onError() {
							refreshList();
						}
					});
		}
	}

	private void showNoMoreData() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				showToast(R.string.endPage);
				lsvSong.onRefreshComplete();
			}
		}, 100);
	}

	private void initData() {
		getMainActivity().currentFragment = MainActivity.LIST_SONG_FRAGMENT;
		switch (GlobalValue.currentMenu) {
		case MainActivity.TOP_CHART:
			setHeaderTitle(R.string.topChart);
			if (getMainActivity().listTopWeek.size() == 0) {
				getData(true);
			} else {
				listSongs.clear();
				listSongs.addAll(getMainActivity().listTopWeek);
				songAdapter.notifyDataSetChanged();
			}
			setButtonMenu(view);
			break;

		case MainActivity.NOMINATIONS:
			setHeaderTitle(R.string.nominations);
			if (getMainActivity().listNominations.size() == 0) {
				getData(true);
			} else {
				listSongs.clear();
				listSongs.addAll(getMainActivity().listNominations);
				songAdapter.notifyDataSetChanged();
			}
			setButtonMenu(view);
			break;

		case MainActivity.CATEGORY_MUSIC:
			CategoryMusic categoryMusic = GlobalValue.listCategoryMusics
					.get(getMainActivity().currentMusicType);
			setHeaderTitle(categoryMusic.getTitle());
			listSongs.clear();
			if (categoryMusic.getListSongs().size() == 0) {
				getData(true);
			} else {
				listSongs.clear();
				listSongs.addAll(categoryMusic.getListSongs());
				songAdapter.notifyDataSetChanged();
			}

			listSongs.addAll(categoryMusic.getListSongs());
			songAdapter.notifyDataSetChanged();
			setButtonBack(view);
			break;

		case MainActivity.PLAYLIST:
			if (null == getMainActivity().getPlaylist()) return;
			setHeaderTitle(getMainActivity().getPlaylist().getName());
			listSongs.clear();
			listSongs.addAll(getMainActivity().getPlaylist().getListSongs());
			songAdapter.notifyDataSetChanged();
			setButtonBack(view);
			break;
		}
	}

	private void refreshList() {
		songAdapter.notifyDataSetChanged();
		lsvSong.onRefreshComplete();
	}

	// private void initData() {
	// getMainActivity().currentFragment = MainActivity.LIST_SONG_FRAGMENT;
	// switch (GlobalValue.currentMenu) {
	// case MainActivity.NOMINATIONS:
	// setHeaderTitle(R.string.nominations);
	// if (getMainActivity().listNominations.size() == 0) {
	// getMainActivity().modelManager.getListNominations(new
	// ModelManagerListener() {
	// @SuppressWarnings("unchecked")
	// @Override
	// public void onSuccess(Object object) {
	// getMainActivity().listNominations.addAll((List<Song>) object);
	// listSongs.clear();
	// listSongs.addAll(getMainActivity().listNominations);
	// songAdapter.notifyDataSetChanged();
	// }
	//
	// @Override
	// public void onError() {
	// }
	// });
	// } else {
	// listSongs.clear();
	// listSongs.addAll(getMainActivity().listNominations);
	// songAdapter.notifyDataSetChanged();
	// }
	// break;
	//
	// case MainActivity.TOP_CHART:
	// setHeaderTitle(R.string.topChart);
	// if (getMainActivity().listTopWeek.size() == 0) {
	// getMainActivity().modelManager.getListTopWeek(new ModelManagerListener()
	// {
	// @SuppressWarnings("unchecked")
	// @Override
	// public void onSuccess(Object object) {
	// getMainActivity().listTopWeek.addAll((List<Song>) object);
	// listSongs.clear();
	// listSongs.addAll(getMainActivity().listTopWeek);
	// songAdapter.notifyDataSetChanged();
	// }
	//
	// @Override
	// public void onError() {
	// }
	// });
	// } else {
	// listSongs.clear();
	// listSongs.addAll(getMainActivity().listTopWeek);
	// songAdapter.notifyDataSetChanged();
	// }
	// break;
	//
	// case MainActivity.CATEGORY_MUSIC:
	// CategoryMusic categoryMusic =
	// GlobalValue.listCategoryMusics.get(getMainActivity().currentMusicType);
	// setHeaderTitle(categoryMusic.getTitle());
	// listSongs.clear();
	// listSongs.addAll(categoryMusic.getListSongs());
	// songAdapter.notifyDataSetChanged();
	// break;
	//
	// case MainActivity.PLAYLIST:
	// setHeaderTitle(getMainActivity().currentPlaylist.getName());
	// listSongs.clear();
	// listSongs.addAll(getMainActivity().currentPlaylist.getListSongs());
	// songAdapter.notifyDataSetChanged();
	// break;
	// }
	// }
}
