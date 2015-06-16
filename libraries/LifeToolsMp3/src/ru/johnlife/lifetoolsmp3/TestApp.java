package ru.johnlife.lifetoolsmp3;

public class TestApp {

	private static Object lock = new Object();
	private static boolean isWork = Boolean.FALSE;
	private static long start = 0L;
	private static long stop = 0L;
	private static long time = 0L;
	
	private static String method = "";
	
	public static void start() {
		synchronized (lock) {
			if (isWork) {
				stop();
			}
			isWork = Boolean.TRUE;
			start = time = System.currentTimeMillis();
			String strThread =  Thread.currentThread().getName();
			android.util.Log.d("logd", "_|START TEST|_start = " + start + "|, thread name - \"" + strThread + "\"");
		}
	}

	public static void start(String m) {
		synchronized (lock) {
			if (isWork) {
				stop();
			}
			method = m;
			isWork = Boolean.TRUE;
			start = time = System.currentTimeMillis();
			String strThread =  Thread.currentThread().getName();
			android.util.Log.d("logd", "_|START TEST|_start = " + start + ", in method = |" + method + "|, thread name - \"" + strThread + "\"");
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
				android.util.Log.d("logd", "_|STOP TEST|_stop = " + stop + ", total time = " + total + ", in method = |" + method + "| thread name - \"" + strThread + "\"");
				android.util.Log.d("logd", "______________________________________________");
				stop = start = 0;
			}
		}
	}

	public static void check(String place) {
		synchronized (lock) {
			if (isWork) {
				long temp = System.currentTimeMillis();
				String strThread =  Thread.currentThread().getName();
				android.util.Log.d("logd", "_|TEST|_check time = " + (temp - time) + " in " + place + ", thread name - \"" + strThread + "\"");
				time = temp;
			}
		}
	}

}
