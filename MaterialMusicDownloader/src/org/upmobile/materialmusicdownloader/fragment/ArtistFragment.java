package org.upmobile.materialmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.models.BaseMaterialFragment;
import org.upmobile.materialmusicdownloader.ui.ArtistView;

/**
 * Created by Aleksandr on 06.08.2015.
 */

public class ArtistFragment  extends Fragment implements BaseMaterialFragment, Constants {

    private ArtistView artistView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        artistView = new ArtistView(inflater);
        return artistView.getView();
    }

    @Override
    public void onResume() {
        MainActivity activity = ((MainActivity) getActivity());
        activity.setSelectedItem(ARTIST_FRAGMENT);
        activity.setTitle(getDrawerTitle());
        activity.setDrawerEnabled(true);
        artistView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        artistView.onPause();
        super.onPause();
    }

    @Override
    public int getDrawerIcon() {
        return R.string.font_user;
    }

    @Override
    public int getDrawerTitle() {
        return R.string.tab_artists;
    }

    @Override
    public int getDrawerTag() {
        return getClass().getSimpleName().hashCode();
    }

    public void forceDelete () {
        artistView.forceDelete();
    }
}