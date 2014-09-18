package ru.johnlife.lifetoolsmp3;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Util {
	
	private final static DateFormat isoDateFormat = new SimpleDateFormat("mm:ss", Locale.US);
	
	public static String formatTimeIsoDate(long date) {
		return isoDateFormat.format(new Date(date));
	}
	
	public static String formatTimeSimple(int duration) {
		duration /= 1000;
		int min = duration / 60;
		int sec = duration % 60;
		return String.format("%d:%02d", min, sec);
	}
}
