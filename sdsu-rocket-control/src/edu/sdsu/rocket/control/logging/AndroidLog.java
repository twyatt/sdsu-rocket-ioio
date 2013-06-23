package edu.sdsu.rocket.control.logging;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.logging.Logger;
import android.util.Log;

public class AndroidLog implements Logger {

	@Override
	public void i(String tag, String msg) {
		Log.i(tag, msg);
	}

	@Override
	public void e(String tag, String msg) {
		Log.e(tag, msg);
	}
	
	@Override
	public void e(String tag, String msg, Exception e) {
		if (App.DEBUG) {
			Log.e(tag, msg, e);
		} else {
			Log.e(tag, msg);
		}
	}

}
