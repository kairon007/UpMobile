package org.upmobile.musicpro.fragment;

import java.io.File;

import org.upmobile.musicpro.BaseFragment;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.DownloadUpdateActivity;
import org.upmobile.musicpro.config.GlobalValue;
import org.upmobile.musicpro.object.Song;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
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
		StringBuilder stringBuilder = new StringBuilder(currentSong.getName()).append(" - ").append(currentSong.getArtist()).append(".mp3");
		final String sb = Util.removeSpecialCharacters(stringBuilder.toString());
		File file = new File(rootFolder);
		final DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(currentSong.getUrl());
		DownloadManager.Request request = new DownloadManager.Request(uri).addRequestHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.3) Gecko/2008092814 (Debian-3.0.1-1)");
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI	| DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(sb);
		try {
			request.setTitle(currentSong.getArtist());
			request.setDescription(currentSong.getName());
			request.setDestinationInExternalPublicDir(getSimpleDownloadPath(file.getAbsolutePath()), sb);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			manager.enqueue(request);
		} catch (IllegalArgumentException e) {
			Toast.makeText(getActivity(), R.string.turn_on_dm, Toast.LENGTH_LONG).show();
			return;
		}
		Toast.makeText(getActivity(), getActivity().getString(R.string.download_started) + " " + sb, Toast.LENGTH_SHORT).show();
	}
	
	public String getSimpleDownloadPath(String absPath) {
		return absPath.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "");
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
