package ru.johnlife.lifetoolsmp3.engines;

public class Engine {
	
	private Class<? extends BaseSearchTask> engineClass;
	private int page;
	
	public Engine(Class<? extends BaseSearchTask>engineClass, int page) {
		this.engineClass = engineClass;
		this.page = page;
	}
	
	public Class<? extends BaseSearchTask> getEngineClass() {
		return engineClass;
	}

	public int getPage() {
		return page;
	}

}
