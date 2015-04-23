package ru.johnlife.lifetoolsmp3;

public class TestApp {

	private static Object lock = new Object();
	private static boolean isWork = false;
	private static long start = 0;
	private static long stop = 0;
	private static long time = 0;

	public static void start() {
		synchronized (lock) {
			if (isWork) {
				stop();
			}
			isWork = true;
			start = time = System.currentTimeMillis();
			String strThread =  Thread.currentThread().getName();
			android.util.Log.d("logks", "_|START TEST|_start = " + start + ", thread name - \"" + strThread + "\"");
		}
	}

	public static void stop() {
		synchronized (lock) {
			if (isWork) {
				time = 0;
				isWork = false;
				stop = System.currentTimeMillis();
				long total = stop - start;
				String strThread =  Thread.currentThread().getName();
				android.util.Log.d("logks", "_|STOP TEST|_stop = " + stop + ", total time = " + total + ", thread name - \"" + strThread + "\"");
				android.util.Log.d("logks", "______________________________________________");
				stop = start = 0;
			}
		}
	}

	public static void check(String place) {
		synchronized (lock) {
			if (isWork) {
				long temp = System.currentTimeMillis();
				String strThread =  Thread.currentThread().getName();
				android.util.Log.d("logks", "_|TEST|_check time = " + (temp - time) + " in " + place + ", thread name - \"" + strThread + "\"");
				time = temp;
			}
		}
	}

}
