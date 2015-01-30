package org.upmobile.musicpro.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.upmobile.musicpro.BaseFragment;
import org.upmobile.musicpro.Constants;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.MainActivity;
import org.upmobile.musicpro.adapter.SongAdapter;
import org.upmobile.musicpro.config.GlobalValue;
import org.upmobile.musicpro.config.WebserviceConfig;
import org.upmobile.musicpro.modelmanager.ModelManagerListener;
import org.upmobile.musicpro.object.Song;
import org.upmobile.musicpro.util.StringUtil;
import org.upmobile.musicpro.widget.AutoBgButton;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchFragment extends BaseFragment implements Constants {
	private AutoBgButton btnSearch;
	private EditText txtKeyword;
	private ListView lsvResult;
	private View lblNoResult;
	private List<Song> listResult;
	private SongAdapter songAdapter;
	private int currentUrl;
	private ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		listResult = new ArrayList<Song>();
		if (null != savedInstanceState) {
			listResult.addAll((Collection<? extends Song>) savedInstanceState.getParcelableArrayList(STATE_SEARCH_RESULTS));
		}
		initUIBase(view);
		initControl(view);
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(STATE_SEARCH_RESULTS, (ArrayList<? extends Parcelable>) listResult);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void initUIBase(View view) {
		super.initUIBase(view);
		btnSearch = (AutoBgButton) view.findViewById(R.id.btnSearch);
		txtKeyword = (EditText) view.findViewById(R.id.txtKeyword);
		lsvResult = (ListView) view.findViewById(R.id.lsvResult);
		lblNoResult = view.findViewById(R.id.lblNoResult);
	}

	private void initControl(View view) {
		setButtonMenu(view);
		btnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickSearch();
			}
		});

		txtKeyword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					onClickSearch();
					return true;
				}
				return false;
			}
		});

		setHeaderTitle(R.string.search);
		songAdapter = new SongAdapter(getActivity(), listResult);
		lsvResult.setAdapter(songAdapter);
		lsvResult.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position, long l) {
				GlobalValue.currentSongPlay = position;
				GlobalValue.listSongPlay.clear();
				GlobalValue.listSongPlay.addAll(listResult);
				getMainActivity().toMusicPlayer = MainActivity.FROM_SEARCH;
				getMainActivity().gotoFragment(MainActivity.PLAYER_FRAGMENT);
			}
		});
	}

	private void onClickSearch() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txtKeyword.getWindowToken(), 0);
		listResult.clear();
		String keyword = txtKeyword.getText().toString();
		currentUrl = 0;

		showLoadingDialog();
		getSongToSearch(getUrl(), keyword);
	}

	private String getUrl() {
		return (WebserviceConfig.URL_MUSIC_WITH_TYPE + GlobalValue.listCategoryMusics.get(currentUrl).getId() + ".xml");
	}

	private void showLoadingDialog() {
		if (progressDialog == null) {
			progressDialog = ProgressDialog.show(getActivity(), getString(R.string.app_name),
					getString(R.string.searching), true);
		} else {
			progressDialog.show();
		}
	}

	private void hideLoadingDialog() {
		if (progressDialog != null) {
			progressDialog.hide();
		}
	}

	private void getSongToSearch(String url, final String keyword) {
		getMainActivity().modelManager.getListSongs(url, new ModelManagerListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Object object) {
				Object[] objects = (Object[]) object;
				String nextPage = String.valueOf(objects[0]);
				getMainActivity().listNominations.addAll((List<Song>) objects[1]);
				for (Song song : (List<Song>) objects[1]) {
					if (song.checkNameAndArtist(keyword)) {
						addSongToListResult(song);
					}
				}

				if (StringUtil.checkEndNextPage(nextPage)) {
					currentUrl++;
					if (currentUrl < GlobalValue.listCategoryMusics.size()) {
						getSongToSearch(getUrl(), keyword);
					} else {
						if (listResult.size() == 0) {
							lblNoResult.setVisibility(View.VISIBLE);
							lsvResult.setVisibility(View.GONE);
						} else {
							songAdapter.notifyDataSetChanged();
							lblNoResult.setVisibility(View.GONE);
							lsvResult.setVisibility(View.VISIBLE);
						}
						hideLoadingDialog();
					}
				} else {
					getSongToSearch(WebserviceConfig.URL_NEXT_PAGE + nextPage + ".xml", keyword);
				}
			}

			@Override
			public void onError() {
				if (listResult.size() == 0) {
					lblNoResult.setVisibility(View.VISIBLE);
					lsvResult.setVisibility(View.GONE);
				} else {
					songAdapter.notifyDataSetChanged();
					lblNoResult.setVisibility(View.GONE);
					lsvResult.setVisibility(View.VISIBLE);
				}
				hideLoadingDialog();
			}
		});
	}

	private void addSongToListResult(Song song) {
		for (Song song2 : listResult) {
			if (song.compare(song2)) {
				return;
			}
		}
		listResult.add(song);
	}
}
