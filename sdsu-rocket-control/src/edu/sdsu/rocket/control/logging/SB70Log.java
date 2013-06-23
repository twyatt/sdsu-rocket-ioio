package edu.sdsu.rocket.control.logging;

import edu.sdsu.rocket.Serial;
import edu.sdsu.rocket.control.devices.SB70;
import edu.sdsu.rocket.logging.Logger;

public class SB70Log implements Logger {

	private SB70 sb70;

	public SB70Log(SB70 sb70) {
		this.sb70 = sb70;
	}
	
	public void log(Level level, String msg) {
		sb70.send(Serial.LOG_MESSAGE, msg.getBytes());
	}
	
	/*
	 * Logger interface methods.
	 */
	
	@Override
	public void i(String tag, String msg) {
		log(Level.INFO, msg);
	}

	@Override
	public void e(String tag, String msg) {
		log(Level.ERROR, msg);
	}

	@Override
	public void e(String tag, String msg, Exception e) {
		log(Level.ERROR, msg + "\n" + e.getMessage());
	}

}
