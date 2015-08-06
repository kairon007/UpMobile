package org.upmobile.clearmusicdownloader.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.menu.ResideMenu;

import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.ui.ArtistView;

/**
 * Created by Aleksandr on 06.08.2015.
 */
public class ArtistFragment extends Fragment {

    private ArtistView artistView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        artistView = new ArtistView(inflater);
        ((MainActivity) getActivity()).showTopFrame();
        ((MainActivity) getActivity()).setResideMenuListener(new ResideMenu.OnMenuListener() {

            @Override
            public void openMenu() {
                artistView.forceDelete();
            }

            @Override
            public void closeMenu() {
            }
        });
        return artistView.getView();
    }

    @Override
    public void onResume() {
        super.onResume();
        artistView.onResume();
    }

    @Override
    public void onPause() {
        artistView.onPause();
        artistView.forceDelete();
        super.onPause();
    }

    public void forceDelete() {
        artistView.forceDelete();
    }
}