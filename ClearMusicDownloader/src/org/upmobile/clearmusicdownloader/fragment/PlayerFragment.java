package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.ui.PlayerService;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlayerFragment  extends Fragment {

	private View parentView;
	private RemoteSong selectedSong;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		selectedSong = this.getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
		parentView = inflater.inflate(R.layout.player, container, false);
		PlayerService.get(getActivity()).setPlayerView(parentView);
		PlayerService.get(getActivity()).setSongObject(selectedSong);
		PlayerService.get(getActivity()).stateManagementPlayer((byte) 1);
		return parentView;
	}
	
}
