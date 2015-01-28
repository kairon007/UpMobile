package org.upmobile.musix.fragments;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.upmobile.musix.R;
import org.upmobile.musix.activities.SongDetailsActivity;
import org.upmobile.musix.listadapters.SongListAdapter;
import org.upmobile.musix.models.Song;
import org.upmobile.musix.services.MusicService;
import org.upmobile.musix.utils.TypefaceHelper;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

public class SongsListFragment extends Fragment
        implements MediaController.MediaPlayerControl {


    public SongsListFragment() {
        // Required empty public constructor
    }

    Context mContext;
    ListView listView;
    private static View rootView;
    TypefaceHelper typefaceHelper;
    ProgressDialog progressDialog;
    ArrayList<Song> songArrayList = new ArrayList<Song>();

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false, playbackPaused = false;

    // controller variables
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();

    SongListAdapter songListAdapter;

    //    private int forwardTime = 5000;
//    private int backwardTime = 5000;
    SeekBar seekBar;
    ImageView playingAlbumCover;
    ImageButton btnPrev, btnNext, btnStop, btnPlayPause, btnShuffle;
    TextView txtStartTimeField, txtEndTimeField, txtCurrentSongTitle, txtCurrentSongArtistName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {

            rootView = inflater.inflate(R.layout.fragment_songs, container, false);
            mContext = rootView.getContext();
            setupViews(rootView);

        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            if (playIntent == null) {
                playIntent = new Intent(mContext, MusicService.class);
                mContext.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                mContext.startService(playIntent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paused) {
            paused = false;
        }
        if (isPlaying()) {
            updateProgressBar();
        }
    }

    @Override
    public void onDestroy() {
        mContext.stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setupViews(View root) {

        setHasOptionsMenu(true);

        typefaceHelper = new TypefaceHelper(mContext);

        seekBar = (SeekBar) root.findViewById(R.id.songSeekBar);
        txtStartTimeField = (TextView) root.findViewById(R.id.songStartTime);
        txtEndTimeField = (TextView) root.findViewById(R.id.songTotalTime);
        btnNext = (ImageButton) root.findViewById(R.id.btnNext);
        btnPlayPause = (ImageButton) root.findViewById(R.id.btnPlayPause);
        btnPrev = (ImageButton) root.findViewById(R.id.btnPrev);
        btnShuffle = (ImageButton) root.findViewById(R.id.btnRandom);
        btnStop = (ImageButton) root.findViewById(R.id.btnStop);
        txtCurrentSongTitle = (TextView) root.findViewById(R.id.txtCurrentSongTitle);
        txtCurrentSongArtistName = (TextView) root.findViewById(R.id.txtCurrentSongArtistName);
        playingAlbumCover = (ImageView) root.findViewById(R.id.imageAlbumPlaying);

        seekBar.setClickable(true);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int seekPosition;
                seekPosition = seekBar.getProgress();

                // forward or backward to certain seconds
                seekTo(seekPosition);
            }
        });

        txtStartTimeField.setTypeface(typefaceHelper.getRobotoLight());
        txtEndTimeField.setTypeface(typefaceHelper.getRobotoLight());
        txtCurrentSongTitle.setTypeface(typefaceHelper.getRobotoLight());
        txtCurrentSongArtistName.setTypeface(typefaceHelper.getRobotoLight());

        listView = (ListView) root.findViewById(R.id.listViewSongs);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                musicService.setSong(position);
                musicService.playSong();

                btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));

                if (playbackPaused) {
                    playbackPaused = false;
                }

                updateProgressBar();

            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playPrev();

                if (playbackPaused) {
                    playbackPaused = false;
                }

                updateProgressBar();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playNext();
                if (playbackPaused) {
                    playbackPaused = false;
                }
                updateProgressBar();
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isPlaying()) {
                        pause();
                        btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                    } else {

                        if (playbackPaused) {
                            playbackPaused = false;
                            start();
                        } else {
                            musicService.setSong(0);
                            musicService.playSong();
                        }

                        btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
                    }
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }

                updateProgressBar();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying()) {
                    playbackPaused = false;
                    musicService.stopPlayer();
                    btnPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                }
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.setShuffle();
            }
        });

        registerForContextMenu(listView);


        songListAdapter = new SongListAdapter(mContext, songArrayList);
        listView.setAdapter(songListAdapter);

        getSongsTask();
    }

    private void getSongsTask() {
        try {

            GetSongsTask getSongsTask = new GetSongsTask();
            getSongsTask.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void viewSongDetails(int position) {
        Song song = songArrayList.get(position);
        Intent intent = new Intent(mContext, SongDetailsActivity.class);
        intent.putExtra("ALBUM_ID", song.getAlbumId());
        intent.putExtra("ALBUM_TITLE", song.getAlbumTitle());
        intent.putExtra("ARTIST_NAME", song.getArtistName());
        intent.putExtra("SONG_TITLE", song.getTitle());
        intent.putExtra("SONG_GENRE", song.getGenre());

        startActivity(intent);
    }

    private class GetSongsTask extends AsyncTask<Void, Void, Boolean> {
        Uri externalContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri albumArtworkUri = Uri.parse("content://media/external/audio/albumart");

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
        }

        Cursor musicCursor = null;
        ContentResolver musicResolver = mContext.getContentResolver();

        Bitmap bitmapAlbumArt = null;
        Uri albumURI;

        boolean result = false;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                // try to get music from external storage first
                musicCursor = musicResolver.query(externalContentUri, null, null, null, MediaStore.Audio.Media.TITLE);
                if (musicCursor != null && musicCursor.moveToFirst()) {
                    runCursor();
                }

                result = true;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        private void runCursor() {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int albumTitleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int genreColumn = musicCursor.getColumnIndex("genre_name");

            // check columns
            int is_ringtoneColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE);
            int is_notificationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION);
            int is_alarmColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM);

            do {

                long songId = musicCursor.getLong(idColumn);
                String songTitle = musicCursor.getString(titleColumn);
                String songArtistName = musicCursor.getString(artistColumn);
                String songAlbumName = musicCursor.getString(albumTitleColumn);
                String genreName = "";
                if (genreColumn >= 0) {
                    genreName = musicCursor.getString(genreColumn);
                } else {
                    genreName = "N/A";
                }

                int songAlbumId = musicCursor.getInt(albumIdColumn);

                // check if is music
                int isRingtone = musicCursor.getInt(is_ringtoneColumn);
                int isNotifitcation = musicCursor.getInt(is_notificationColumn);
                int isAlarm = musicCursor.getInt(is_alarmColumn);

                if (isRingtone == 1 || isNotifitcation == 1 || isAlarm == 1) {
                    continue;
                }

                bitmapAlbumArt = null;
                if (songAlbumId > 0) {
                    // get album art
                    albumURI = ContentUris.withAppendedId(albumArtworkUri, songAlbumId);
                }

                //add songs to list
                Song song = new Song(songId, songTitle, songArtistName,
                        songAlbumName, songAlbumId,
                        bitmapAlbumArt, genreName, albumURI);

                try {
                    songListAdapter.add(song);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

            }
            while (musicCursor.moveToNext());
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Scanning..");
            progressDialog.show();
        }
    }// end Task

    @Override
    public void start() {
        musicService.start();
    }

    private void updateProgressBar() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startTime = 0;
        seekBar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTime, 100);
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {

            finalTime = getDuration();
            startTime = getCurrentPosition();

            long minutes, seconds;
            String mins, secs;

            if (finalTime > 0) {
                seekBar.setMax((int) finalTime);

                minutes = TimeUnit.MILLISECONDS.toMinutes((long) finalTime);
                seconds = TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime));

                if (minutes < 10) {
                    mins = String.format("0%d", minutes);
                } else {
                    mins = String.valueOf(minutes);
                }
                if (seconds < 10) {
                    secs = String.format("0%d", seconds);
                } else {
                    secs = String.valueOf(seconds);
                }

                txtEndTimeField.setText(mins + ":" + secs);
            }

            minutes = TimeUnit.MILLISECONDS.toMinutes((long) startTime);
            seconds = TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime));

            if (minutes < 10) {
                mins = String.format("0%d", minutes);
            } else {
                mins = String.valueOf(minutes);
            }
            if (seconds < 10) {
                secs = String.format("0%d", seconds);
            } else {
                secs = String.valueOf(seconds);
            }

            try {
                Song currentSong = songArrayList.get(getCurrentListPosition());
                if (currentSong != null) {

                    Bitmap albumArt = songListAdapter.getItem(getCurrentListPosition()).getAlbumCoverArt();
                    if (albumArt != null) {
                        playingAlbumCover.setImageBitmap(albumArt);
                    } else {
                        playingAlbumCover.setImageDrawable(getResources().getDrawable(R.drawable.def_player_cover));
                    }

                    txtCurrentSongTitle.setText(currentSong.getTitle());
                    txtCurrentSongArtistName.setText(currentSong.getArtistName());

                    txtCurrentSongTitle.requestFocus();
                    txtCurrentSongArtistName.requestFocus();

                } else {
                    txtCurrentSongTitle.setText("");
                    txtCurrentSongArtistName.setText("");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            txtStartTimeField.setText(mins + ":" + secs);
            seekBar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getPosition();
        }
        return 0;
    }

    public int getCurrentListPosition() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getCurrenListPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.isPlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
    // -------------------------}

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicService = binder.getService();
            //pass list
            musicService.setList(songArrayList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();

        menu.setHeaderTitle("Song options");
        menu.setHeaderIcon(R.drawable.def_player_cover);

        inflater.inflate(R.menu.songctxmenu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.action_song_details:
                viewSongDetails(info.position);
                break;
        }
        return true;
    }

}
