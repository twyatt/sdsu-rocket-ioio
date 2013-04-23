package edu.sdsu.rocket.control;

import edu.sdsu.rocket.control.logging.AndroidLog;
import edu.sdsu.rocket.control.logging.Logger;

public class App {

	/**
	 * Per "good convention" recommendations found at:
	 * {@link http://developer.android.com/reference/android/util/Log.html}
	 */
	public static final String TAG = "SDSURocketControl";
	
	public static Logger log = new AndroidLog();
	
}
