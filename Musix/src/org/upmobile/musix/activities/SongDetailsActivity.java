package org.upmobile.musix.activities;

import java.io.InputStream;

import org.upmobile.musix.R;
import org.upmobile.musix.utils.BitmapHelper;
import org.upmobile.musix.utils.TypefaceHelper;

import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class SongDetailsActivity extends ActionBarActivity {

	private Context mContext;
	private TypefaceHelper typefaceHelper;
	private TextView txtArtistName, txtAlbumName, txtSongTitle, txtGenre;
	private ImageView albumCover;
	private MusicData song;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_details);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = getApplicationContext();
		typefaceHelper = new TypefaceHelper(mContext);
		setupViews();
	}

	private void setupViews() {
		try {

			Bundle extras = getIntent().getExtras();
			String title, artistName, albumTitle;
			song = (MusicData) extras.get("EXTRA_SONG_DETAIL");
			title = song.getTitle();
			artistName = song.getArtist();
			albumTitle = song.getAlbum();
			txtSongTitle = (TextView) findViewById(R.id.songDetail_Title);
			txtArtistName = (TextView) findViewById(R.id.songDetail_Artist);
			txtAlbumName = (TextView) findViewById(R.id.songDetail_Album);
			albumCover = (ImageView) findViewById(R.id.songDetail_CoverArt);
			txtGenre = (TextView) findViewById(R.id.songDetail_Genre);
			txtSongTitle.setTypeface(typefaceHelper.getRobotoMedium());
			txtAlbumName.setTypeface(typefaceHelper.getRobotoLight());
			txtArtistName.setTypeface(typefaceHelper.getRobotoLight());
			txtGenre.setTypeface(typefaceHelper.getRobotoLight());
			txtSongTitle.setText(title);
			txtArtistName.setText(artistName);
			txtAlbumName.setText("Album: " + albumTitle);
			getSupportActionBar().setTitle(title);
			Bitmap bitmapAlbumArt = song.getCover(this);
			if (bitmapAlbumArt != null) {
				albumCover.setImageBitmap(bitmapAlbumArt);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

}
