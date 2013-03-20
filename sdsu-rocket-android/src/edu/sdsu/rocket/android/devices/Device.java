package edu.sdsu.rocket.android.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public interface Device {

	public void setup(IOIO ioio) throws ConnectionLostException;
	public void loop() throws ConnectionLostException, InterruptedException;
	
}
