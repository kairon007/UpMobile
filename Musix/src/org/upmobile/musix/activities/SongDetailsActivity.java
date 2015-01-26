package org.upmobile.musix.activities;

import java.io.InputStream;

import org.upmobile.musix.R;
import org.upmobile.musix.utils.BitmapHelper;
import org.upmobile.musix.utils.TypefaceHelper;

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

    Context mContext;
    TypefaceHelper typefaceHelper;
    TextView txtArtistName, txtAlbumName, txtSongTitle, txtGenre;
    ImageView albumCover;
    BitmapHelper bitmapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_details);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = getApplicationContext();
        bitmapHelper = new BitmapHelper(mContext);
        typefaceHelper = new TypefaceHelper(mContext);

        setupViews();
    }

    private void setupViews() {
        try {

            Bundle extras = getIntent().getExtras();
            String title, artistName, albumTitle, genre;
            long albumID;

            title = extras.getString("SONG_TITLE");
            artistName = extras.getString("ARTIST_NAME");
            albumTitle = extras.getString("ALBUM_TITLE");
            albumID = extras.getLong("ALBUM_ID");
            genre = extras.getString("SONG_GENRE");

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
            txtGenre.setText("Genre: " + genre);

            getSupportActionBar().setTitle(title);

            Bitmap bitmapAlbumArt = getAlbumCoverArt(albumID);
            if (bitmapAlbumArt != null) {
                albumCover.setImageBitmap(bitmapAlbumArt);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Bitmap getAlbumCoverArt(long albumID) {
        Bitmap bitmapAlbumArt;
        InputStream inputStream;
        Uri albumArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumURI = ContentUris.withAppendedId(albumArtworkUri, albumID);
        ContentResolver musicResolver = mContext.getContentResolver();

        try {

            inputStream = musicResolver.openInputStream(albumURI);
            bitmapAlbumArt = BitmapFactory.decodeStream(inputStream);

        } catch (Exception e) {
            bitmapAlbumArt = null;
        }
        if (bitmapAlbumArt != null) {
            return bitmapHelper.scaleToDisplayAspectRatio(bitmapAlbumArt);
        }
        return null;
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
