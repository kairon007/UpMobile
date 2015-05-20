/*
 * Copyright (C) 2012 Christopher Eby <kreed@kreed.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ru.johnlife.lifetoolsmp3;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

/**
 * Receives media button events and calls to PlaybackService to respond
 * appropriately.
 */
public class MediaButtonReceiver extends BroadcastReceiver {
	/**
	 * Whether the phone is currently in a call. 1 for yes, 0 for no, -1 for
	 * uninitialized.
	 */
	private static int sInCall = -1;
	
	/**
	 * Time of the last play/pause click. Used to detect double-clicks.
	 */
	private static long sLastClickTime = 0;

	/**
	 * Return whether the phone is currently in a call.
	 *
	 * @param context
	 *            A context to use.
	 */
	private static boolean isInCall(Context context) {
		if (sInCall == -1) {
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			sInCall = (byte) (manager.getCallState() == TelephonyManager.CALL_STATE_IDLE ? 0 : 1);
		}
		return sInCall == 1;
	}

	/**
	 * Set the cached value for whether the phone is in a call.
	 *
	 * @param value
	 *            True if in a call, false otherwise.
	 */
	public static void setInCall(boolean value) {
		sInCall = value ? 1 : 0;
	}

	/**
	 * Process a media button key press.
	 *
	 * @param context
	 *            A context to use.
	 * @param event
	 *            The key press event.
	 * @return True if the event was handled and the broadcast should be
	 *         aborted.
	 */
	public boolean processKey(Context context, KeyEvent event) {
		if (event == null || isInCall(context)) return false;
		int action = event.getAction();
		PlaybackService player = PlaybackService.get(context);
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			if (action == KeyEvent.ACTION_DOWN) {
				long time = SystemClock.uptimeMillis();
				if (time - sLastClickTime < 400) {
					player.shift(1);
				} else if (player.isPlaying()) {
					player.pause();
				} else {
					player.play();
				}
				sLastClickTime = time;
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			if (action == KeyEvent.ACTION_DOWN) {
				player.shift(1);
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			if (action == KeyEvent.ACTION_DOWN) {
				player.shift(-1);
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
			if (action == KeyEvent.ACTION_DOWN) {
				player.play();
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			if (action == KeyEvent.ACTION_DOWN) {
				player.pause();
			}
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * Request focus on the media buttons from AudioManager if media buttons are
	 * enabled.
	 *
	 * @param context
	 *            A context to use.
	 */
	public static void registerMediaButton(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) return;
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		ComponentName receiver = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
		audioManager.registerMediaButtonEventReceiver(receiver);
	}

	/**
	 * Unregister the media buttons from AudioManager.
	 *
	 * @param context
	 *            A context to use.
	 */
	public static void unregisterMediaButton(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) return;
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		ComponentName receiver = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
		audioManager.unregisterMediaButtonEventReceiver(receiver);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			boolean handled = processKey(context.getApplicationContext(), event);
//			if (handled && isOrderedBroadcast()) {
//				abortBroadcast();
//			}
		}
	}
}
