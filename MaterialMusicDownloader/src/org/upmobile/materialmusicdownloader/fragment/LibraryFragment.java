package org.upmobile.materialmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.models.BaseMaterialFragment;
import org.upmobile.materialmusicdownloader.ui.LibraryView;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;

public class LibraryFragment extends Fragment implements BaseMaterialFragment, Constants {

	private LibraryView libraryView;
	private AbstractSong abstractSong;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(inflater);
		return libraryView.getView();
	}
	
	@Override
	public void onResume() {
		MainActivity activity = ((MainActivity) getActivity());
		activity.setSelectedItem(LIBRARY_FRAGMENT);
		activity.setTitle(getDrawerTitle());
		activity.setVisibleSearchView(true);
		abstractSong = getArguments().getParcelable("KEY_SELECTED_SONG");
		activity.setDrawerEnabled(null == abstractSong ? true : false);
		libraryView.highlightSong(null == abstractSong ? null : abstractSong.getComment());
		if (null != abstractSong) {
			activity.showMiniPlayer(true);
			setHasOptionsMenu(true);}
		libraryView.onResume();
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (null != abstractSong) {
			getActivity().onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onPause() {
		libraryView.onPause();
		super.onPause();
	}
	
	@Override
	public int getDrawerIcon() {
		return R.string.font_musics;
	}
	
	public void setFilter(String filter) {
		libraryView.applyFilter(filter);
		libraryView.getMessageView(getView()).setText(getString(R.string.search_no_results_for) + " " + filter);
	}
	
	public void clearFilter() {
		libraryView.clearFilter();
	}

	@Override
	public int getDrawerTitle() {
		return R.string.tab_library;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}
	
	public void forceDelete () {
		libraryView.forceDelete();
	}
}
