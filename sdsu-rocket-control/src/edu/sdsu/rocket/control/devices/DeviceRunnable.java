package edu.sdsu.rocket.control.devices;

import ioio.lib.api.exception.ConnectionLostException;
import edu.sdsu.rocket.control.App;

public class DeviceRunnable implements Runnable {

	private Device device;
	
	public DeviceRunnable(Device device) {
		if (device == null)
			throw new NullPointerException();
		this.device = device;
	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				device.loop();
			}
		} catch (ConnectionLostException e) {
			App.log.e(App.TAG, "Connection lost with " + device.info(), e);
		} catch (InterruptedException e) {
			App.log.e(App.TAG, "Thread interrupted for " + device.info(), e);
		}
	}

}
