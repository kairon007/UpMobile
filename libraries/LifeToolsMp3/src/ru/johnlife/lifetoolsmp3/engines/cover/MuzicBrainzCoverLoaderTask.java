package ru.johnlife.lifetoolsmp3.engines.cover;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MuzicBrainzCoverLoaderTask extends CoverLoaderTask {

	private static final String URL_PATTERN = "http://www.musicbrainz.org/ws/2/recording/?query=artist:%s+recording:%s";
	protected String artist;
	protected String title;

	public MuzicBrainzCoverLoaderTask(String artist, String title) {
		super();
		this.artist = artist;	
		this.title = title;	
	}
	
	@Override
	protected Bitmap doInBackground(Void... params) {
		try {
			String[] arrayString2, arrayString4;
			String link = String.format(URL_PATTERN, URLEncoder.encode(artist, "UTF-8"), URLEncoder.encode(title, "UTF-8"));
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(link);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			arrayString2 = filterRecordingByScore(sb.toString());
			arrayString4 = takeMBID(arrayString2);
			String fromWhereToGetThePic = getTheLinkToPicture(arrayString4);
			if (fromWhereToGetThePic == null) {
				return null;
			} else {
				String damnUrl = getTHEdamnURL(fromWhereToGetThePic, "large");
				if (damnUrl == null) return null;
				httpget = new HttpGet(damnUrl);
				response = httpclient.execute(httpget);
				entity = response.getEntity();
				BufferedInputStream bitmapStream = new BufferedInputStream(entity.getContent());
				return BitmapFactory.decodeStream(bitmapStream);
			}
		} catch (Exception e) {
			Log.e(getClass().getName(), "Error downloading the cover", e);
			return null;
		}
	}

	private String[] filterRecordingByScore(String theString) {
		String[] arrayString = new String[30];
		int positionOfSearch = 0;
		int indexOfStartingTag = 0;
		int indexOfEndingTag = 0;
		int indexOfBigEndingTag = 0;
		String startingTag = "<recording id";
		String endingTag = ">";
		String endingBigTag = "</recording>";
		if (theString == null) return arrayString;
		int i = 0;
		while ((indexOfStartingTag = theString.indexOf(startingTag, positionOfSearch)) != -1) {
			positionOfSearch = indexOfEndingTag = theString.indexOf(endingTag, indexOfStartingTag);
			String stringWithScore = theString.substring(indexOfStartingTag, indexOfEndingTag);
			positionOfSearch = indexOfBigEndingTag = theString.indexOf(endingBigTag, positionOfSearch);
			if (checkScore(stringWithScore)) {
				arrayString[i++] = theString.substring(indexOfStartingTag, indexOfBigEndingTag);
			}
		}
		return arrayString;
	}

	private boolean checkScore(String stringWithScore) {

		String firstTag = "ext:score=\"";
		int score=0;
		String endTag="\"";
		int Offset = firstTag.length();
		int indexOfFirstTag= stringWithScore.indexOf(firstTag);
		int indexOfEndTag = stringWithScore.indexOf(endTag, indexOfFirstTag+Offset);

		try
		{
			score = Integer.parseInt(stringWithScore.substring(indexOfFirstTag+Offset, indexOfEndTag));
		}
		catch(NumberFormatException e)
		{
		}
		if(score>=75)
			return true;
		else
			return false;
	}

	private String[] takeMBID(String[] arrayString2) {
		String[] arrayString3 = new String[30];

		int indexOfReleaseId = 0;
		int indexOfReleaseIdEnd = 0;
		String releaseIdEnd = "\">";

		String releaseIdTag = "<release id=\"";
		int Offset = releaseIdTag.length();
		for (int i = 0; i < arrayString2.length; i++)
			if (arrayString2[i] != null) {
				Log.e("i=" + i, arrayString2[i].indexOf(releaseIdTag) + "<-");

				indexOfReleaseId = arrayString2[i].indexOf(releaseIdTag);
				indexOfReleaseIdEnd = arrayString2[i].indexOf(releaseIdEnd,
						indexOfReleaseId);
				if(indexOfReleaseId!=-1 && indexOfReleaseIdEnd!=-1)
					arrayString3[i] = arrayString2[i].substring(indexOfReleaseId+ Offset, indexOfReleaseIdEnd);
			}
		return arrayString3;
	}

	private String getTheLinkToPicture(String[] arrayString4) {
		// TODO Auto-generated method stub
		String partialLink = "http://coverartarchive.org/release/";
		for (int i = 0; i < arrayString4.length; i++) {
			StringBuffer sb = null;
			if (arrayString4[i] != null) {
				String link = partialLink + arrayString4[i];

				try {
					HttpClient httpclient = new DefaultHttpClient(); // Create
					// HTTP
					// Client
					HttpGet httpget = new HttpGet(link); // Set the action you
					// want to
					// do
					HttpResponse response = httpclient.execute(httpget);
					HttpEntity entity = response.getEntity();
					// InputStream is = entity.getContent(); // Create an
					// InputStream
					// with the response
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(entity.getContent()));

					// Get the response
					sb = new StringBuffer();
					// BufferedReader rd = new BufferedReader(new
					// InputStreamReader(conn.getInputStream()));

					String line;
					while ((line = reader.readLine()) != null) {
						sb.append(line);
						Log.e("!!!", line + "\n");
					}
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Executeit
			}
			if(sb!=null)
				if (!sb.toString().contains("404 Not Found"))
					return sb.toString();
		}
		return null;
	}


	private String getTHEdamnURL(String fromWhereToGetThePic, String size) {
		if(size.equals("original"))
		{
			//Getting Original size
			String firstTag = "\"image\":\"";
			String finishTag = "\"";

			int Offset = firstTag.length();
			int indexOfFirstTag = fromWhereToGetThePic.indexOf(firstTag);
			int indexOfFinishTag = fromWhereToGetThePic.indexOf(finishTag,
					indexOfFirstTag + Offset);
			if(indexOfFirstTag!=-1 && indexOfFinishTag!=-1)
				return	fromWhereToGetThePic.substring(indexOfFirstTag + Offset, indexOfFinishTag);
			else
				return null;
		}

		//Getting thumbnail size large


		if(size.equals("large"))
		{

			String finishTag = "\"";
			int Offset2=0;
			int indexOfthumbnail=0;
			String tagThumbnails="\"thumbnails\"";

			String firstTag2="\"large\":\"";

			indexOfthumbnail = fromWhereToGetThePic.indexOf(tagThumbnails)+tagThumbnails.length();

			Offset2= firstTag2.length();
			int indexOfFirstTag2 = fromWhereToGetThePic.indexOf(firstTag2,indexOfthumbnail);
			int indexOfFinishTag2=fromWhereToGetThePic.indexOf(finishTag,indexOfFirstTag2+Offset2);

			if(indexOfFirstTag2!=-1 && indexOfFinishTag2!=-1)
				return	fromWhereToGetThePic.substring(indexOfFirstTag2+Offset2, indexOfFinishTag2);
			else
				return null;
		}


		//Getting thumbnail size small
		if(size.equals("small"))
		{
			int Offset3=0;
			String finishTag = "\"";
			int indexOfthumbnail=0;
			String tagThumbnails="\"thumbnails\"";
			String firstTag3="\"small\":\"";

			indexOfthumbnail = fromWhereToGetThePic.indexOf(tagThumbnails)+tagThumbnails.length();
			Offset3= firstTag3.length();
			int indexOfFirstTag3 = fromWhereToGetThePic.indexOf(firstTag3,indexOfthumbnail);
			int indexOfFinishTag3=fromWhereToGetThePic.indexOf(finishTag,indexOfFirstTag3+Offset3);

			if(indexOfFirstTag3!=-1 && indexOfFinishTag3!=-1)
				return fromWhereToGetThePic.substring(indexOfFirstTag3+Offset3, indexOfFinishTag3);
			else
				return null;

		}
		return null;
	}

}