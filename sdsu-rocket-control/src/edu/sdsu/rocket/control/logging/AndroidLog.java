package edu.sdsu.rocket.control.logging;

import android.util.Log;

public class AndroidLog implements Logger {

	@Override
	public void i(String tag, String msg) {
		Log.i(tag, msg);
	}

}