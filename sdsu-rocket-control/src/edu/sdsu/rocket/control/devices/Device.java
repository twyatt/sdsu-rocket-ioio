package edu.sdsu.rocket.control.devices;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import edu.sdsu.rocket.helpers.ThreadTimer;

abstract public class Device extends ThreadTimer implements IOIOLooper {

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		sleep();
	}
	
	abstract public String info();
	
}
