package ru.johnlife.lifetoolsmp3.equalizer;

public class ProgressClass {

	private long id;
	private int p1, p2, p3, p4, p5;
	private int sk1, sk2;
	private String user;

	public long getId() {
		return id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getArc(int x){
		
		switch(x){
		
		case 1:
			return sk1;
		case 2:
			return sk2;
		
		}
		
		return 0;
		
	}
	
	public void setSeekArk(int arc, int x) {

		switch (arc) {

		case 1:
			this.sk1 = x;
			break;
		case 2:
			this.sk2 = x;
			break;

		}

	}

	public int getProgress(int x) {

		switch (x) {

		case 1:
			return p1;

		case 2:
			return p2;

		case 3:
			return p3;

		case 4:
			return p4;

		case 5:
			return p5;

		}

		return 0;
	}

	public void setProgress(int x, int p) {

		switch (x) {

		case 1:
			this.p1 = p;
			break;
		case 2:
			this.p2 = p;
			break;
		case 3:
			this.p3 = p;
			break;
		case 4:
			this.p4 = p;
			break;
		case 5:
			this.p5 = p;
			break;

		}

	}

}
