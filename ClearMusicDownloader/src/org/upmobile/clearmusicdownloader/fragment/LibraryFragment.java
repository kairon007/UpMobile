package org.upmobile.clearmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.menu.ResideMenu.OnMenuListener;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.ui.LibraryView;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;

public class LibraryFragment extends Fragment {

	private LibraryView libraryView;
    private AbstractSong song;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		libraryView = new LibraryView(inflater);
		((MainActivity) getActivity()).showTopFrame();
		((MainActivity) getActivity()).setResideMenuListener(new OnMenuListener() {

            @Override
            public void openMenu() {
                libraryView.forceDelete();
            }

            @Override
            public void closeMenu() {
            }
        });
		return libraryView.getView();
	}
	
	public void setFilter(String filter) {
		libraryView.applyFilter(filter);
		libraryView.getMessageView(getView()).setText(getString(R.string.search_no_results_for) + " " + filter);
	}
	
	public void clearFilter() {
		libraryView.getMessageView(getView()).setText(R.string.library_empty);
		libraryView.clearFilter();
	}
	
	@Override
	public void onResume() {
		Bundle arguments = getArguments();
		if (null != arguments) {
			song = arguments.getParcelable("KEY_SELECTED_SONG");
		}
        libraryView.highlightSong(null == song ? null : song.getComment());
        if (null != song) {
			((MainActivity) getActivity()).setTvTitle(getResources().getStringArray(R.array.titles)[Constants.SONGS_FRAGMENT]);
			getActivity().findViewById(R.id.title_bar_left_menu).setTag(true);
			getActivity().findViewById(R.id.title_bar_left_menu).setBackgroundDrawable(getResources().getDrawable(R.drawable.titlebar_back_selector));
            ((MainActivity) getActivity()).showMiniPlayer(true);
            getActivity().findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
            getActivity().findViewById(R.id.title_bar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
		libraryView.onResume();
		if (null != arguments) {
			arguments.putParcelable("KEY_SELECTED_SONG", null);
		}
		super.onResume();
	}
	
	@Override
	public void onPause() {
		libraryView.forceDelete();
		libraryView.onPause();
		super.onPause();
	}

}
