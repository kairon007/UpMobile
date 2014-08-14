//package org.kreed.musicdownloader;
//
//import java.io.BufferedInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.ArrayList;
//
//import org.cmc.music.common.ID3WriteException;
//import org.cmc.music.metadata.ImageData;
//import org.cmc.music.metadata.MusicMetadata;
//import org.cmc.music.metadata.MusicMetadataSet;
//import org.cmc.music.myid3.MyID3;
//import org.kreed.musicdownloader.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
//
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.CompressFormat;
//import android.graphics.BitmapFactory;
//import android.os.AsyncTask;
//import android.os.Environment;
//import android.util.Log;
//
//class DownloadFile extends AsyncTask<String, Integer, String> implements
//		OnBitmapReadyListener {
//	String songTitle;
//	String songArtist;
//	private LoadPercentageInterface progressInterface;
//	private File src;
//	private Bitmap cover;
//	private String path;
//	private boolean waitingForCover = true;
//	private MusicDataInterface musicDataInterface;
//
//	@Override
//	public void onBitmapReady(Bitmap bmp) {
//		this.cover = bmp;
//		this.waitingForCover = false;
//	}
//
//	public DownloadFile(String songTitle, String songArtist,
//			LoadPercentageInterface progressInterface,MusicDataInterface musicdatainterface) {
//		// TODO Auto-generated constructor stub
//		this.songTitle = songTitle;
//		this.songArtist = songArtist;
//		this.progressInterface = progressInterface;
//		this.musicDataInterface = musicdatainterface;
//	}
//
//	 @Override
//	 protected void onPreExecute() {
//	 super.onPreExecute();
//	 ArrayList<MusicData> mData  = new ArrayList<MusicData>();
//	 MusicData mItem = new MusicData();
//	 mItem.setSongArtist(songArtist);
//	 mItem.setSongTitle(songTitle);
//	 mItem.setSongDuration("4.33");
////	 mItem.setSongBitmap(BitmapFactory.decodeResource(getc, id));
//	 mData.add(mItem);
//	 musicDataInterface.insertData(mData);
//	 }
//	@Override
//	protected String doInBackground(String... sUrl) {
//		try {
//			URL url = new URL(sUrl[0]);
//			URLConnection connection = url.openConnection();
//			connection.connect();
//			// this will be useful so that you can show a typical 0-100%
//			// progress bar
//			int fileLength = connection.getContentLength();
//			// download the file
//			InputStream input = new BufferedInputStream(url.openStream());
//			path = Environment.getExternalStorageDirectory()
//					+ "/MusicDownloader" + songTitle + " - " + songArtist
//					+ ".mp3";
//			OutputStream output = new FileOutputStream(path);
//			byte data[] = new byte[1024];
//			long total = 0;
//			int count;
//			while ((count = input.read(data)) != -1) {
//				total += count;
//				// publishing the progress....
//				publishProgress((int) (total * 100 / fileLength));
//				output.write(data, 0, count);
//			}
//
//			output.flush();
//			output.close();
//			input.close();
//		} catch (Exception e) {
//		}
//		return null;
//	}
//
//	@Override
//	protected void onProgressUpdate(Integer... progress) {
//		super.onProgressUpdate(progress);
//		progressInterface.insertProgress(String.valueOf(progress[0]));
//	}
//
//	@Override
//	protected void onPostExecute(String result) {
//		Log.d("path---------------", path);
//		if (waitingForCover)
//			return;
//		// Cursor c = manager.query(new
//		// DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
//		// if (c == null || !c.moveToFirst()) return;
//		// String path =
//		// c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
//		// c.close();
//		src = new File(path);
//		try {
//			MusicMetadataSet src_set = new MyID3().read(src); // read metadata
//			if (src_set == null) {
//				return;
//			}
//			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
//			metadata.setSongTitle(songTitle);
//			metadata.setArtist(songArtist);
//			if (null != cover) {
//				ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
//				cover.compress(CompressFormat.JPEG, 85, out);
//				metadata.addPicture(new ImageData(out.toByteArray(),
//						"image/jpeg", "cover", 3));
//			}
//			File dst = new File(src.getParentFile(), src.getName() + "-1");
//			new MyID3().write(src, dst, src_set, metadata); // write updated
//															// metadata
//			dst.renameTo(src);
//			// this.cancel();
//		} catch (IOException e) {
//			Log.e(getClass().getSimpleName(), "error writing ID3", e);
//		} catch (ID3WriteException e) {
//			Log.e(getClass().getSimpleName(), "error writing ID3", e);
//		}
//		super.onPostExecute(result);
//	}
//
//}