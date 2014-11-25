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
	private String[] tempID3Fields;
	private String songField;
	private String message;
	private String lyrics;
	private String directoryChooserPath;
	private String newDirName;
	private int listViewPosition;
	private int clickPosition;
	private int currentPlayersId;
	private int tempID3UseCover;
	private boolean useCover = true;
	/**
	 * The class flags hold various states.
	 */
	private Integer generalFlags = 0;
	/**
	 * It indicates Stream Dialog is opened or not
	 */
	public static final int STREAM_DIALOG = 0x00000001;// 1
	/**
	 * It indicates ID3Edit Dialog is opened or not
	 */
	public static final int EDITTAG_DIALOG = 0x00000002;// 2
	/**
	 * It indicates Directory Chooser Dialog is opened or not
	 */
	public static final int DIRCHOOSE_DIALOG = 0x00000004;// 3
	/**
	 * It indicates New Directory Dialog is opened or not
	 */
	public static final int NEWDIR_DIALOG = 0x00000008;// 4
	/**
	 * It indicates Lyrics Dialog is opened or not
	 */
	public static final int LYRICS_DIALOG = 0x00000010;// 5
	/**
	 * It indicates Progress Dialog is opened or not
	 */
	public static final int PROGRESS_DIALOG = 0x00000020;// 6
	/**
	 * It indicates button in Directory Chooser Dialog is enabled or not
	 */
	public static final int BTN_ENABLED = 0x00000040;// 7
	/**
	 * It use for switch engines. If it is true then after change engines search is resumed
	 */
	public static final int SEARCH_STOP_OPTION = 0x00000080;// 8
	/**
	 * It indicates search is executed or not
	 */
	public static final int SEARCH_EXE_OPTION = 0x00000100;// 9
	/**
	 * Indicate has been any change tag of song in EditMp3Tags Dialog or not
	 */
	public static final int MANIPULATE_TEXT_OPTION = 0x00000200;// 10
	/**
	 * Indicate state media player in stream dialog 
	 */
	public static final int IS_PLAYING_OPTION = 0x00000400;// 11
	/**
	 * Indicate state spinner
	 */
	public static final int IS_EXPANDING_OPTION = 0x00000800;// 12
	/**
     * Mask for use with setFlags indicating bits used for search options.
     */
	private static final int OPEN_MASKS = 0x00000FFF;
	/**
	 * Mask for close selected options.
	 */
	private static final int CLOSE_MASKS = 0x00000000;
	
	
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
			downloadSong = null;
			titleArtistLyrics = null;
			currentPlayersId = 0;
			lyrics = null;
			useCover = true;
		} else if (flag == EDITTAG_DIALOG) {
			deactivateOptions(MANIPULATE_TEXT_OPTION);
			tempID3UseCover = 0;
			tempID3Fields = null;
		} else  if (flag == PROGRESS_DIALOG){
			setClickPosition(0);
			viewItem = null;
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
		songField = searchView.getSearchField().getText().toString();
		results = searchView.getResultAdapter().getAll();
		if (results != null && !results.isEmpty()) listViewPosition = searchView.getListViewPosition();
		else message = searchView.getMessage();
		if (searchView != null) taskIterator = searchView.getTaskIterator();
		if (generalFlags == 0 || generalFlags == 2048) return;
		if (viewItem == null) viewItem = searchView.getViewItem();
		if (checkState(STREAM_DIALOG)) {
			if (downloadSong == null) downloadSong = searchView.getDownloadSong();
			playerInstance = searchView.getPlayer();
		} else if (checkState(PROGRESS_DIALOG)) setClickPosition(searchView.getClickPosition());
	}
	
	public void restoreState(OnlineSearchView view) {
		if (null != songField && !Util.removeSpecialCharacters(songField).equals("")) {
			view.setSearchField(songField);
		}
		if (results == null || results.isEmpty()) {
			view.setMessage(message);
			if (generalFlags == 0) return;
		} else {
			view.restoreAdapter(results, listViewPosition);
			results = null;
		}
		if (taskIterator != null) {
			view.setTaskIterator(taskIterator);
		}
		if (checkState(PROGRESS_DIALOG)) {
			view.getDownloadUrl(viewItem, getClickPosition());
		} else {
			if (checkState(STREAM_DIALOG)) {
				view.setDownloadSong(downloadSong);
				playerInstance.setDownloadSong(downloadSong);
				view.setPlayer(playerInstance);
				view.prepareSong(downloadSong, true, null);
			}
			if (checkState(EDITTAG_DIALOG)) {
				if (null == tempID3Fields) {
					playerInstance.createId3dialog(new String[] { downloadSong.getArtist(), downloadSong.getTitle(), downloadSong.album });
				} else {
					playerInstance.createId3dialog(tempID3Fields);
				}
			} else if (checkState(LYRICS_DIALOG)) {
				playerInstance.createLyricsDialog();
			} else {
				if (checkState(DIRCHOOSE_DIALOG)) {
					playerInstance.createDirectoryChooserDialog();
				}
				if (checkState(NEWDIR_DIALOG)) {
					playerInstance.createNewDirDialog(newDirName);
				}
			}
		}
	}
	
	public int getTempID3UseCover() {
		return tempID3UseCover;
	}

	public void setTempID3UseCover(int tempUseCover) {
		if (tempUseCover > 0) this.tempID3UseCover = 1;
		else if (tempUseCover < 0) this.tempID3UseCover = -1;
		else this.tempID3UseCover = 0;
	}

	public void setTempID3Fields(String[] strings) {
		tempID3Fields = strings;
	}
	
	public String[] getTempID3Fields() {
		return tempID3Fields;
	}
	
	public boolean isUseCover() {
		return useCover;
	}

	public void setUseCover(boolean useCover) {
		this.useCover = useCover;
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

	public int getClickPosition() {
		return clickPosition;
	}

	public void setClickPosition(int clickPosition) {
		this.clickPosition = clickPosition;
	}

}
