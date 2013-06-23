package edu.sdsu.rocket.control;

import edu.sdsu.rocket.control.logging.AndroidLog;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.models.Statistics;
import edu.sdsu.rocket.control.objectives.ObjectiveController;
import edu.sdsu.rocket.logging.Logger;

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
	
	public static Rocket rocket;
	
	public static Logger log = new AndroidLog();
	public static Statistics stats = new Statistics();
	public static ObjectiveController objective;
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
