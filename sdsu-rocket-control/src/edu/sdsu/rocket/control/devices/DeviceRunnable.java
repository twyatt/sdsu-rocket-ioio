package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import edu.sdsu.rocket.control.App;

public class DeviceRunnable implements Runnable {

	// TODO support multiple devices, this should become private:
	public final Device device;
	
	/**
	 * Duration to sleep between thread loops in milliseconds.
	 */
	private int sleep = 1000; // default is 1000 ms delay (1 Hz)
	
	public DeviceRunnable(Device device) {
		this.device = device;
	}

	/**
	 * Sets the thread frequency (loops per minute).
	 * 
	 * @param frequency Loop frequency (Hz).
	 */
	public DeviceRunnable setThreadFrequency(float frequency) {
		setThreadSleep(Math.round(1000f / frequency));
		return this;
	}
	
	/**
	 * Sets the duration to sleep between thread loops.
	 * 
	 * @param sleep Thread loop sleep duration (milliseconds).
	 */
	public DeviceRunnable setThreadSleep(int sleep) {
		this.sleep = sleep;
		return this;
	}
	
	public void setup(IOIO ioio) throws ConnectionLostException {
		device.setup(ioio);
	}
	
	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				device.loop();
				Thread.sleep(sleep);
			}
		} catch (ConnectionLostException e) {
			App.log.e(App.TAG, "Connection lost with " + device.info(), e);
		} catch (InterruptedException e) {
			// thread interrupted
		}
	}

}
