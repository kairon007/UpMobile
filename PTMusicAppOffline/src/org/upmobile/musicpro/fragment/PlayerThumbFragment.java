package org.upmobile.musicpro.fragment;

import java.io.File;

import org.upmobile.musicpro.BaseFragment;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.DownloadUpdateActivity;
import org.upmobile.musicpro.config.GlobalValue;
import org.upmobile.musicpro.object.Song;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

public class PlayerThumbFragment extends BaseFragment {
	// private ImageView imgSong;
	private TextView lblNameSong, lblArtist;
	private String rootFolder;
	private ImageView imgSong;
	private AQuery listAq;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_player_thumb, container, false);
		listAq = new AQuery(getActivity());
		initUI(view);
		return view;
	}

	private void initUI(View view) {
		// imgSong = (ImageView) view.findViewById(R.id.imgSong);
		lblNameSong = (TextView) view.findViewById(R.id.lblNameSong);
		lblArtist = (TextView) view.findViewById(R.id.lblArtist);
		imgSong = (ImageView) view.findViewById(R.id.imgSong);
		lblNameSong.setSelected(true);
		lblArtist.setSelected(true);
		view.findViewById(R.id.btnDownload).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickDownload();
			}
		});
		view.findViewById(R.id.btnShare).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickShare();
			}
		});

		rootFolder = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/";
		File folder = new File(rootFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	public void refreshData() {	
		if (lblNameSong != null && lblArtist != null) {
			lblNameSong.setText(GlobalValue.getCurrentSong().getName());
			lblArtist.setText(GlobalValue.getCurrentSong().getArtist());
			AQuery aq = listAq.recycle(getView());
			aq.id(R.id.imgSong).image(GlobalValue.getCurrentSong().getImage(), true, true, 0, R.drawable.ic_music_node_custom,
					GlobalValue.ic_music_node_custom, AQuery.FADE_IN_NETWORK, 0);
		} else {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					refreshData();
				}
			}, 500);
		}
	}

	private void onClickDownload() {
		Song currentSong = GlobalValue.getCurrentSong();
		File file = new File(rootFolder, currentSong.getName() + " - " + currentSong.getArtist() + ".mp3");
		if (file.exists()) {
			Toast.makeText(getActivity(), R.string.songExisted, Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent(getActivity(), DownloadUpdateActivity.class);
			intent.putExtra("url_song", currentSong.getUrl());
			intent.putExtra("file_name", currentSong.getName() + " - " + currentSong.getArtist() + ".mp3");
			startActivity(intent);
		}
	}

	private void onClickShare() {
		Song currentSong = GlobalValue.getCurrentSong();
		String shareBody = currentSong.getUrl();
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				currentSong.getName() + " - " + currentSong.getArtist());
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share)));
	}
}
