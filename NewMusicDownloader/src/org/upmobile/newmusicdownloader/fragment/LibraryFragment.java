package org.upmobile.newmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.ui.LibraryView;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;

public class LibraryFragment extends Fragment {

	private LibraryView libraryView;
	AbstractSong abstractSong;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(inflater);
		((MainActivity) inflater.getContext()).invalidateOptionsMenu();
		return libraryView.getView();
	}

	@Override
	public void onResume() {
		super.onResume();
		MainActivity activity = (MainActivity) getActivity();
		activity.setSelectedItem(Constants.LIBRARY_FRAGMENT);
		activity.setCurrentTag(getClass().getSimpleName());
		activity.setTitle(R.string.tab_library);
		abstractSong = getArguments().getParcelable("KEY_SELECTED_SONG");
		libraryView.highlightSong(null == abstractSong ? null : abstractSong.getComment());
		activity.setDrawerEnabled(null == abstractSong ? true : false);
		if (null != abstractSong) {
			activity.showMiniPlayer(true);
			setHasOptionsMenu(true);
		}
		getArguments().putParcelable("KEY_SELECTED_SONG", null);
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


	public void setFilter(String filter) {
		libraryView.applyFilter(filter);
		libraryView.getMessageView(getView()).setText(getString(R.string.search_no_results_for) + " " + filter);
	}

	public void clearFilter() {
		libraryView.clearFilter();
	}

}
