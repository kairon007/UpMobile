package org.kreed.vanilla.engines;


public abstract class SearchWithPages extends BaseSearchTask {

	protected int page = 1;
	protected static int maxPages = 1;

	public SearchWithPages(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
}
