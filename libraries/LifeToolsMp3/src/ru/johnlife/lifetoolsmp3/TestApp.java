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
			android.util.Log.d("logks", "_|START TEST|_start = " + start);
		}
	}

	public static void stop() {
		synchronized (lock) {
			if (isWork) {
				time = 0;
				isWork = false;
				stop = System.currentTimeMillis();
				long total = stop - start;
				android.util.Log.d("logks", "_|STOP TEST|_stop = " + stop + ", total time = " + total);
				android.util.Log.d("logks", "______________________________________________");
				stop = start = 0;
			}
		}
	}

	public static void check(String place) {
		synchronized (lock) {
			if (isWork) {
				long temp = System.currentTimeMillis();
				android.util.Log.d("logks", "_|TEST|_check time = " + (temp - time) + " in " + place);
				time = temp;
			}
		}
	}

}
