package org.upmobile.newmaterialmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.ui.ArtistView;

/**
 * Created by Aleksandr on 04.08.2015.
 */
public class ArtistFragment extends Fragment {

    private ArtistView artistView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        artistView = new ArtistView(inflater);
        ((MainActivity) inflater.getContext()).invalidateOptionsMenu();
        return artistView.getView();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity act = (MainActivity) getActivity();
        act.setCurrentFragmentId(ManagerFragmentId.artistFragment());
        act.setTitle(R.string.tab_artists);
        act.showToolbarShadow(true);
        act.setDrawerEnabled(true);
        artistView.onResume();
    }

    @Override
    public void onPause() {
        artistView.onPause();
        super.onPause();
    }

    public void forceDelete() {
        artistView.forceDelete();
    }
}