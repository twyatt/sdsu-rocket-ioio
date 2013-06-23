package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public interface Device {

	public void setup(IOIO ioio) throws ConnectionLostException;
	public void loop() throws ConnectionLostException, InterruptedException;
	public void disconnected();
	public String info();
	
}
