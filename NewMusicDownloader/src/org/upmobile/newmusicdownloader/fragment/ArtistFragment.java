package org.upmobile.newmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.ui.ArtistView;

/**
 * Created by Aleksandr on 06.08.2015.
 */
public class ArtistFragment extends Fragment{

    private ArtistView view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = new ArtistView(inflater);
        return view.getView();
    }

    @Override
    public void onPause() {
        view.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        view.onResume();
        MainActivity act = (MainActivity) getActivity();
        act.invalidateOptionsMenu();
        act.setSelectedItem(Constants.ARTIST_FRAGMENT);
        act.setTitle(R.string.tab_artists);
        act.setDrawerEnabled(true);
        super.onResume();
    }
}
