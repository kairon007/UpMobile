package ru.johnlife.lifetoolsmp3;

import java.util.ArrayList;
import java.util.Iterator;

import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import ru.johnlife.lifetoolsmp3.ui.Player;
import android.view.View;

public class StateKeeper {
	
	private static StateKeeper instance = null;
	private Iterator<Engine> taskIterator;
	private ArrayList<Song> results = null;
	private String[] titleArtistLyrics;
	private String[] fields;
	private RemoteSong song;
	private Player playerInstance;
	private View view;
	private String message;
	private String songName;
	private String lyrics;
	private String directoryChooserPath;
	private String newDirName;
	private int listViewPosition;
	private int currentPlayersId;
	private boolean isPlaying = false;
	
	/**
	 * The class flags hold various states.
	 */
	private Integer generalFlags = 0;
	/**
	 * It indicates Stream Dialog is opened or not
	 */
	public static final int STREAM_DIALOG = 1;
	/**
	 * It indicates ID3Edit Dialog is opened or not
	 */
	public static final int EDITTAG_DIALOG = 2;
	/**
	 * It indicates Directory Chooser Dialog is opened or not
	 */
	public static final int DIRCHOOSE_DIALOG = 4;
	/**
	 * It indicates New Directory Dialog is opened or not
	 */
	public static final int NEWDIR_DIALOG = 8;
	/**
	 * It indicates Lyrics Dialog is opened or not
	 */
	public static final int LYRICS_DIALOG = 16;
	/**
	 * It indicates Progress Dialog is opened or not
	 */
	public static final int PROGRESS_DIALOG = 32;
	/**
	 * It indicates button in Directory Chooser Dialog is enabled or not
	 */
	public static final int BTN_ENABLED = 64;
	/**
     * Mask for use with setFlags indicating bits used for state 
     * of dialog (for open dialog).
     */
	private static final int DIALOG_OPEN_MASKS = 127;
	/**
	 * It use for switch engines. If it is true then after change engines search is resumed
	 */
	public static final int SEARCH_MODE_OPTION = 128;
	/**
	 * It indicates search is executed or not
	 */
	public static final int SEARCH_EXE_OPTION = 256;
	/**
	 * Song download with cover or not
	 */
	public static final int USE_COVER_OPTION = 512;
	
	/**
     * Mask for use with setFlags indicating bits used for search options.
     */
	private static final int OPTIONS_MASKS = 896;
	/**
	 * Mask for close selected options.
	 */
	private static final int CLOSE_FLAG = 0;
	
	
	private StateKeeper() {
	}

	public static StateKeeper getInstance() {
		if (instance == null) {
			instance = new StateKeeper();
		}
		return instance;
	}
	
	/**
	 * 
	 * @param flag - Flag)
	 * @param mask - Mask)
	 * @param openClose - It is status of operations. If true - it is open, if it false - close
	 */
	private void setFlags(int flag, int mask, boolean openClose) {
		int old;
		if (openClose) {
			old = flag | generalFlags;
			generalFlags = Integer.valueOf(old);
			return;
		} else {
			if (checkState(flag)) {
				old = flag ^ generalFlags;
				generalFlags = Integer.valueOf(old);
			} 
		}
	}

	public ArrayList<Song> getResults() {
		return results;
	}
	
	/**
	 * @param dialog - use constants from this class which have ending _DIALOG or BTN_ENABLED
	 * 
	 * set state as open in selected dialog 
	 */
	public void openDialog(int dialog) {
		setFlags(dialog, DIALOG_OPEN_MASKS, true);
	}
	/**
	 * @param dialog - use constants from this class which have ending _DIALOG or BTN_ENABLED
	 * 
	 * set state as close in selected dialog
	 */
	public void closeDialog(int dialog) {
		setFlags(dialog, CLOSE_FLAG, false);
	}
	/**
	 * @param options - use constants from this class which have ending _OPTION
	 * 
	 * set state as enabled in selected options
	 */
	public void activateOptions(int options) {
		setFlags(options, OPTIONS_MASKS, true);
	}
	/**
	 * @param options - use constants from this class which have ending _OPTION
	 * 
	 * set state state as disabled in selected options
	 */
	public void deactivateOptions(int options) {
		setFlags(options, CLOSE_FLAG, false);
	}
	/**
	 * @param flag - use needed constant from this class
	 * @return is enabled or disabled this flag
	 */
	public boolean checkState(int flag) {
		int buff = generalFlags;
		boolean result = (generalFlags & flag) == flag;
		generalFlags = buff;
		return result;
	}

	public int getCurrentPlayersId() {
		return currentPlayersId;
	}

	public void setCurrentPlayersId(int currentAudioSessionId) {
		this.currentPlayersId = currentAudioSessionId;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlauing) {
		this.isPlaying = isPlauing;
	}

	public String getSongName() {
		return songName;
	}

	public Iterator<Engine> getTaskIterator() {
		return taskIterator;
	}

	public void saveStateAdapter(OnlineSearchView searchView) {
		//TODO savestate
		results = new ArrayList<Song>();
		if (searchView != null && searchView.getResultAdapter() != null) {
			for (int i = 0; i < searchView.getResultAdapter().getCount(); i++) {
				Song song = searchView.getResultAdapter().getItem(i);
				results.add(song);
			}
			listViewPosition = searchView.getListViewPosition();
			songName = searchView.getSearchField().getText().toString();
			taskIterator = searchView.getTaskIterator();
			message = searchView.getMessage();
			if (song == null) {
				song = searchView.getDownloadSong();
			}
			playerInstance = searchView.getPlayer();
		}
	}

	public Player getPlayerInstance() {
		return playerInstance;
	}
	
	public String[] getID3Fields() {
		return fields;
	}
	
	public void setID3Fields(String[] fields) {
		this.fields = fields;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setLyricsString(String lyrics) {
		this.lyrics = lyrics;
	}

	public String[] getTitleArtistLyrics() {
		return titleArtistLyrics;
	}
	
	public void setTitleArtistLyrics(String[] titleArtistLyrics) {
		this.titleArtistLyrics = titleArtistLyrics;
	}

	public String getDirectoryChooserPath() {
		return directoryChooserPath;
	}

	public void setDirectoryChooserPath(String directoryChooserPath) {
		this.directoryChooserPath = directoryChooserPath;
	}
	
	public void restoreState(OnlineSearchView view) {
		if(checkState(PROGRESS_DIALOG)){
			//TODO что это за position и listViewPosition, откуда они вызывались и для чего нужны
			view.getDownloadUrl(this.view, listViewPosition);
		}
		if(checkState(STREAM_DIALOG)){
			view.prepareSong(song, true, "restoreState");
		}
		if(checkState(EDITTAG_DIALOG)){
			view.createId3Dialog(getID3Fields());
		} else if (checkState(LYRICS_DIALOG)) {
			view.createLyricsDialog(getTitleArtistLyrics(), getLyrics());
		} else {
			if (checkState(DIRCHOOSE_DIALOG)){
				view.getPlayer().createDirectoryChooserDialog();
			}
			if (checkState(NEWDIR_DIALOG)) {
				view.getPlayer().createNewDirDialog(newDirName);
			}
		}
		
	}

	public void setNewDirName(String name) {
		newDirName = name;
	}

	public int getListViewPosition() {
		return listViewPosition;
	}

	public String getMessage() {
		return message;
	}
}
