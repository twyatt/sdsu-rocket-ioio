package edu.sdsu.rocket.control;

import ioio.lib.api.IOIO;
import edu.sdsu.rocket.control.controllers.RocketController;
import edu.sdsu.rocket.control.logging.AndroidLog;
import edu.sdsu.rocket.logging.Logger;
import edu.sdsu.rocket.models.Statistics;

public class App {

	/**
	 * Per "good convention" recommendations found at:
	 * {@link http://developer.android.com/reference/android/util/Log.html}
	 */
	public static final String TAG = "SDSURocketControl";

	/**
	 * When set to false, logging of most exception stack traces will be
	 * suppressed.
	 */
	public static final boolean DEBUG = true;
	
	public static IOIO ioio;
	public static Logger log = new AndroidLog();
	public static Statistics stats = new Statistics();
	public static RocketController rocketController;
	public static DataLogger data;

	private static long startTime;

	private static long instanceId = 0;
	
	public static long getInstanceId() {
		if (instanceId == 0) {
			instanceId = System.currentTimeMillis();
		}
		return instanceId;
	}
	
	public static void start() {
		startTime = nanoTime();
	}
	
	/**
	 * Returns number of seconds since start() was called.
	 * 
	 * @return Seconds since start.
	 */
	public static float elapsedTime() {
		long time = nanoTime();
		return (time - startTime) / 1000000000.0f;
	}
	
	public static long nanoTime() {
		return System.nanoTime();
	}
	
}
