package edu.sdsu.rocket.control;

import edu.sdsu.rocket.control.logging.AndroidLog;
import edu.sdsu.rocket.logging.Logger;

public class App {

	/**
	 * Per "good convention" recommendations found at:
	 * {@link http://developer.android.com/reference/android/util/Log.html}
	 */
	public static final String TAG = "SDSURocketControl";
	
	public static Logger log = new AndroidLog();
	public static DataLogger data;

	private static long startTime;
	
	public static void start() {
		startTime = getNanoTime();
	}
	
	public static long getNanoTime() {
		return System.nanoTime();
	}
	
	/**
	 * Returns number of seconds since start() was called.
	 * 
	 * @return Seconds since start.
	 */
	public static float elapsedTime() {
		long time = System.nanoTime();
		return (time - startTime) / 1000000000.0f;
	}
	
}
