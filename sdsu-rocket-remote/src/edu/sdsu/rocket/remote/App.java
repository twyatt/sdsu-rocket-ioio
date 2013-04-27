package edu.sdsu.rocket.remote;

import com.esotericsoftware.kryonet.Client;

public class App {

	/**
	 * Per "good convention" recommendations found at:
	 * {@link http://developer.android.com/reference/android/util/Log.html}
	 */
	public static final String TAG = "SDSURocketRemote";

	// FIXME should probably be in a singleton instead
	public static Client client = new Client();
	
}
