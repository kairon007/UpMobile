package ru.johnlife.lifetoolsmp3.engines;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.SoundCloudV1Song;
import android.util.Log;

public class SearchNothing extends SearchWithPages {
	private int specialIndex = 0;
	private int pag;

	public SearchNothing(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	private int getPage() {
		this.pag = page;
		return (pag - 1) * 50;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
    	
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {

		}
		page = maxPages + 1;
    	

		return null;
	}


}
