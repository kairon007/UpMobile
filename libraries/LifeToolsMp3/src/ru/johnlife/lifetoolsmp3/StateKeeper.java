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
	private Player playerInstance;
	private RemoteSong downloadSong;
	private View viewItem;
	private String[] titleArtistLyrics;
	private String[] templateFields;
	private String songField;
	private String message;
	private String lyrics;
	private String directoryChooserPath;
	private String newDirName;
	private int listViewPosition;
	private int currentPlayersId;
	/**
	 * The class flags hold various states.
	 */
	private Integer generalFlags = 0;
	/**
	 * It indicates Stream Dialog is opened or not
	 */
	public static final int STREAM_DIALOG = 1;// 1
	/**
	 * It indicates ID3Edit Dialog is opened or not
	 */
	public static final int EDITTAG_DIALOG = 2;// 2
	/**
	 * It indicates Directory Chooser Dialog is opened or not
	 */
	public static final int DIRCHOOSE_DIALOG = 4;// 3
	/**
	 * It indicates New Directory Dialog is opened or not
	 */
	public static final int NEWDIR_DIALOG = 8;// 4
	/**
	 * It indicates Lyrics Dialog is opened or not
	 */
	public static final int LYRICS_DIALOG = 16;// 5
	/**
	 * It indicates Progress Dialog is opened or not
	 */
	public static final int PROGRESS_DIALOG = 32;// 6
	/**
	 * It indicates button in Directory Chooser Dialog is enabled or not
	 */
	public static final int BTN_ENABLED = 64;// 7
	/**
	 * It use for switch engines. If it is true then after change engines search is resumed
	 */
	public static final int SEARCH_STOP_OPTION = 128;// 8
	/**
	 * It indicates search is executed or not
	 */
	public static final int SEARCH_EXE_OPTION = 256;// 9
	/**
	 * Song download with cover or not
	 */
	public static final int USE_COVER_OPTION = 512;// 10
	/**
	 * Indicate state media player in stream dialog 
	 */
	public static final int IS_PLAYING_OPTION = 1024;// 11
	/**
	 * Indicate state spinner
	 */
	public static final int IS_EXPANDING_OPTION = 2048;// 12
	/**
     * Mask for use with setFlags indicating bits used for search options.
     */
	private static final int OPEN_MASKS = 4095;
	/**
	 * Mask for close selected options.
	 */
	private static final int CLOSE_MASKS = 0;
	
	
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
		if (flag == STREAM_DIALOG) {
			playerInstance = null;
			directoryChooserPath = null;
			newDirName = null;
			currentPlayersId = 0;
			generalFlags  = generalFlags ^ USE_COVER_OPTION;
		} else if (flag == LYRICS_DIALOG) {
			lyrics = null;
		} else if (flag == EDITTAG_DIALOG) {
			templateFields = null;
		}
	}
	
	/**
	 * @param dialog - use constants from this class which have ending _DIALOG or BTN_ENABLED
	 * 
	 * set state as open in selected dialog 
	 */
	public void openDialog(int dialog) {
		setFlags(dialog, OPEN_MASKS, true);
	}
	/**
	 * @param dialog - use constants from this class which have ending _DIALOG or BTN_ENABLED
	 * 
	 * set state as close in selected dialog
	 */
	public void closeDialog(int dialog) {
		setFlags(dialog, CLOSE_MASKS, false);
	}
	/**
	 * @param options - use constants from this class which have ending _OPTION
	 * 
	 * set state as enabled in selected options
	 */
	public void activateOptions(int options) {
		setFlags(options, OPEN_MASKS, true);
	}
	/**
	 * @param options - use constants from this class which have ending _OPTION
	 * 
	 * set state state as disabled in selected options
	 */
	public void deactivateOptions(int options) {
		setFlags(options, CLOSE_MASKS, false);
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

	public void saveStateAdapter(OnlineSearchView searchView) {
		//TODO save state
		if (songField != null) songField = searchView.getSearchField().getText().toString();
		if (searchView != null) taskIterator = searchView.getTaskIterator();
		if (generalFlags == 0 || generalFlags == 2048) return;
		results = new ArrayList<Song>();
		for (int i = 0; i < searchView.getResultAdapter().getCount(); i++) {
			Song song = searchView.getResultAdapter().getItem(i);
			results.add(song);
		}
		if (checkState(STREAM_DIALOG)) downloadSong = searchView.getDownloadSong();
		listViewPosition = searchView.getListViewPosition();
		viewItem = searchView.getViewItem();
		message = searchView.getMessage();
		playerInstance = searchView.getPlayer();
	}
	
	public void restoreState(OnlineSearchView view) {
		if (null != songField && !Util.removeSpecialCharacters(songField).equals("")) {
			view.setSearchField(songField);
		}
		if (generalFlags == 0) return;
		if (checkState(STREAM_DIALOG)) {
			view.setDownloadSong(downloadSong);
			playerInstance.setDownloadSong(downloadSong);
		}
		if (taskIterator != null) {
			view.setTaskIterator(taskIterator);
		}
		if(checkState(PROGRESS_DIALOG)){
			view.getDownloadUrl(viewItem, listViewPosition);
		}
		if(checkState(STREAM_DIALOG)){
			view.setPlayer(playerInstance);
			view.prepareSong(downloadSong, true);
		}
		if(checkState(EDITTAG_DIALOG)){
			if (null == templateFields) {
				playerInstance.createId3dialog(new String[] {downloadSong.getArtist(), downloadSong.getTitle(), downloadSong.album});
			} else {
				playerInstance.createId3dialog(templateFields);
			}
		} else if (checkState(LYRICS_DIALOG)) {
			playerInstance.createLyricsDialog(titleArtistLyrics[0], titleArtistLyrics[1], lyrics);
		} else {
			if (checkState(DIRCHOOSE_DIALOG)){
				playerInstance.createDirectoryChooserDialog();
			}
			if (checkState(NEWDIR_DIALOG)) {
				playerInstance.createNewDirDialog(newDirName);
			}
		}
	}
	
	public String[] getTemplateFields() {
		return templateFields;
	}
	
	public void setDownloadSong(RemoteSong downloadSong) {
		this.downloadSong = downloadSong;
	}
	
	public int getCurrentPlayersId() {
		return currentPlayersId;
	}

	public void setCurrentPlayersId(int currentAudioSessionId) {
		this.currentPlayersId = currentAudioSessionId;
	}
	
	public ArrayList<Song> getResults() {
		return results;
	}

	public Iterator<Engine> getTaskIterator() {
		return taskIterator;
	}

	public Player getPlayerInstance() {
		return playerInstance;
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

	public void setNewDirName(String name) {
		newDirName = name;
	}

	public int getListViewPosition() {
		return listViewPosition;
	}

	public String getMessage() {
		return message;
	}

	public void setID3Fields(String[] strings) {
		templateFields = strings;
	}
}
