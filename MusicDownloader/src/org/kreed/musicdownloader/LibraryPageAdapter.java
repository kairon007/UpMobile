package org.kreed.musicdownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LibraryPageAdapter extends ArrayAdapter<File> {

	File[] files;
	private Player player;
	private LayoutInflater inflater;

	public LibraryPageAdapter(Context context, int resource, File[] files) {
		super(context, resource, files);
		this.files = files;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView = inflater.inflate(R.layout.library_item, null);
		player = new Player(itemView, files[position]);
		return player.getView();
	}

	private final static class Player extends AsyncTask<String, Void, Boolean> {

		private MediaPlayer mediaPlayer;
		private File songFile;
		private View view;
		private ProgressBar songProgress;
		private ImageButton buttonPlay;
		private ImageView coverImage;
		private TextView songTitle;
		private TextView songGenre;
		private TextView songDuration;
		private MusicMetadata metadata;
		private boolean prepared = false;
		private Runnable progressAction = new Runnable() {
			@Override
			public void run() {
				try {
					int current = mediaPlayer.getCurrentPosition();
					int total = mediaPlayer.getDuration();
					songProgress.setProgress(current);
					songDuration.setText(formatTime(current) + " / "
							+ formatTime(total));
					songProgress.postDelayed(this, 1000);
				} catch (NullPointerException e) {
				}
			}
		};

		public Player(View view, File songFile) {
			super();
			this.view = view;// this id library_item
			this.songFile = songFile;
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			buttonPlay = (ImageButton) view.findViewById(R.id.play_song);
			songTitle = (TextView) view.findViewById(R.id.title_song);
			songGenre = (TextView) view.findViewById(R.id.genre_song);
			coverImage = (ImageView) view.findViewById(R.id.cover_song);
			songDuration = (TextView) view.findViewById(R.id.duration_song);
			songProgress = (ProgressBar) view.findViewById(R.id.progress_song);
			songProgress.setVisibility(View.INVISIBLE);
			try {
				MusicMetadataSet src_set = new MyID3().read(songFile);
				if (src_set != null) {
					metadata = src_set.merged;
					String strArtist = metadata.getArtist();
					String strTitle = metadata.getSongTitle();
					String strDuration = metadata.getComment();
					Bitmap bitmap = getArtworkImage(2);
					Drawable cover = new BitmapDrawable(bitmap);
					String strGenre;
					if (metadata.containsKey("genre_id")) {
						int genre_id = (int) metadata.get("genre_id");
						strGenre = ID3v1Genre.get(genre_id);
					} else {
						strGenre = "unknown";
					}
					songTitle.setText(strArtist + " - " + strTitle);
					songDuration.setText(strDuration);
					coverImage.setImageDrawable(cover);
					songGenre.setText(strGenre);
				} else {
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			buttonPlay.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					playPause();
					if (!prepared) {
						Player.this.execute("");
					}
				}
			});
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				FileInputStream inputStream = new FileInputStream(songFile);
				mediaPlayer.setDataSource(inputStream.getFD());
				inputStream.close();
				mediaPlayer.prepare();
				prepared = true;
				if (isCancelled()) {
					releasePlayer();
				} else {
					return true;
				}
			} catch (Exception e) {
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result && prepared) {
				mediaPlayer.start();
				onResumed();
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						releasePlayer();
						onFinished();
					}
				});
				onPrepared();
			}
		}

		private void releasePlayer() {
			if (null != mediaPlayer) {
				songProgress.removeCallbacks(progressAction);
				try {
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.stop();
					}
				} catch (IllegalStateException e) {
				}
				mediaPlayer.reset();
				mediaPlayer.release();
				mediaPlayer = null;
				prepared = false;
			}
		}

		public Bitmap getArtworkImage(int maxWidth) {
			if (maxWidth == 0) {
				return null;
			}
			Vector<ImageData> pictureList = metadata.getPictureList();
			if ((pictureList == null) || (pictureList.size() == 0)) {
				return null;
			}
			ImageData imageData = (ImageData) pictureList.get(0);
			if (imageData == null) {
				return null;
			}
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			int scale = 1;
			if ((maxWidth != -1) && (opts.outWidth > maxWidth)) {
				// Find the correct scale value. It should be the power of 2.
				int scaleWidth = opts.outWidth;
				while (scaleWidth > maxWidth) {
					scaleWidth /= 2;
					scale *= 2;
				}
			}
			opts = new BitmapFactory.Options();
			opts.inSampleSize = scale;
			Bitmap bitmap = BitmapFactory.decodeByteArray(imageData.imageData,
					0, imageData.imageData.length, opts);
			return bitmap;
		}

		public View getView() {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (null != parent) {
				parent.removeView(view);
			}
			return view;
		}

		public void onPrepared() {
			int duration = mediaPlayer.getDuration();
			if (duration == -1) {
				songProgress.setIndeterminate(true);
				songProgress.setVisibility(View.INVISIBLE);
			} else {
				songProgress.setVisibility(View.VISIBLE);
				songDuration.setText(formatTime(duration));
				songProgress.setIndeterminate(false);
				songProgress.setProgress(0);
				songProgress.setMax(duration);
				songProgress.postDelayed(progressAction, 1000);
			}
		}

		public void onPaused() {
			buttonPlay.setImageResource(R.drawable.play);
		}

		public void onResumed() {
			buttonPlay.setImageResource(R.drawable.pause);
		}

		public void onFinished() {
			songProgress.setIndeterminate(false);
			songProgress.setProgress(100);
			songProgress.setMax(100);
			songProgress.removeCallbacks(progressAction);
		}

		public void playPause() {
			if (!prepared || null == mediaPlayer)
				return;
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				onPaused();
			} else {
				songProgress.setVisibility(View.VISIBLE);
				mediaPlayer.start();
				onResumed();
			}
		}

		private String formatTime(int duration) {
			duration /= 1000;
			int min = duration / 60;
			int sec = duration % 60;
			return String.format("%d:%02d", min, sec);
		}

	}

}