package ru.johnlife.lifetoolsmp3.activity;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseMiniPlayerActivity extends Activity {

	protected final String ARRAY_SAVE = "extras_array_save";
	protected PlaybackService service;
	
	private TextView title;
	private TextView artist;
	private ImageView cover;

	protected abstract int getMiniPlayerID();
	
	@Override
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		initMiniPlayer();
		super.onStart();
	}
	
	private void initMiniPlayer() {
		title = (TextView)findViewById(R.id.mini_player_title);
		artist = (TextView)findViewById(R.id.mini_player_artist);
		cover = (ImageView)findViewById(R.id.mini_player_cover);
	}
	
	public void showMiniPlayer(boolean isShow) {
		findViewById(getMiniPlayerID()).setVisibility(isShow ? View.VISIBLE : View.GONE);
	}
	
	public void startSong(AbstractSong song) {
		title.setText(song.getTitle());
		artist.setText(song.getArtist());
		cover.setImageBitmap(song.getCover(this));
		if (null == service) {
			service = PlaybackService.get(this);
		}
		service.play(song);
	}
}
