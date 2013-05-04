package edu.sdsu.rocket.control.devices;

import edu.sdsu.rocket.control.App;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class DeviceThread extends Thread {

	public final Device device;

	/**
	 * Duration to sleep between thread loops in milliseconds.
	 */
	private int sleep = 1000; // default is 1000 ms delay (1 Hz)

	public DeviceThread(Device device) {
		this.device = device;
	}
	
	/**
	 * Sets the thread frequency (loops per minute).
	 * 
	 * @param frequency Loop frequency (Hz).
	 */
	public DeviceThread setThreadFrequency(float frequency) {
		setThreadSleep(Math.round(1000f / frequency));
		return this;
	}
	
	/**
	 * Sets the duration to sleep between thread loops.
	 * 
	 * @param sleep Thread loop sleep duration (milliseconds).
	 */
	public DeviceThread setThreadSleep(int sleep) {
		this.sleep = sleep;
		return this;
	}
	
	public void setup(IOIO ioio) throws ConnectionLostException {
		device.setup(ioio);
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				device.loop();
				Thread.sleep(sleep);
			}
		} catch (ConnectionLostException e) {
			e.printStackTrace();
			App.log.e(App.TAG, "Connection lost with " + device.info(), e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			App.log.e(App.TAG, "Interrupted with " + device.info(), e);
		}
	}
	
	public void close() {
		interrupt();
		
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			App.log.e(App.TAG, "Interrupted while closing " + device.info(), e);
		}
	}
	
}
