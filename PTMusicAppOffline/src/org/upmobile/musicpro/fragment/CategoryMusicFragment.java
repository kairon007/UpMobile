package org.upmobile.musicpro.fragment;

import org.upmobile.musicpro.BaseFragment;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.MainActivity;
import org.upmobile.musicpro.adapter.CategoryMusicAdapter;
import org.upmobile.musicpro.config.GlobalValue;
import org.upmobile.musicpro.slidingmenu.SlidingMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class CategoryMusicFragment extends BaseFragment {
	private GridView grvCategoryMusic;
	private CategoryMusicAdapter categoryMusicAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_category_music, container, false);
		initUIBase(view);
		setButtonMenu(view);
		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			getMainActivity().menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			getMainActivity().setVisibilityFooter();
			categoryMusicAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void initUIBase(View view) {
		super.initUIBase(view);
		setHeaderTitle(R.string.categoryMusic);
		grvCategoryMusic = (GridView) view.findViewById(R.id.grvCategoryMusic);
		categoryMusicAdapter = new CategoryMusicAdapter(getActivity(), GlobalValue.listCategoryMusics);
		grvCategoryMusic.setAdapter(categoryMusicAdapter);
		grvCategoryMusic.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position, long l) {
				getMainActivity().currentMusicType = position;
				getMainActivity().gotoFragment(MainActivity.LIST_SONG_FRAGMENT);
			}
		});
	}
}
