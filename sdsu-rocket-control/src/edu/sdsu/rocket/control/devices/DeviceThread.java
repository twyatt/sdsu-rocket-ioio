package edu.sdsu.rocket.control.devices;

import edu.sdsu.rocket.control.App;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class DeviceThread extends Thread {

	public final Device device;

	/**
	 * Duration to sleep between thread loops in milliseconds.
	 */
	private int sleep;

	/**
	 * Creates a device thread.
	 * 
	 * @param device
	 * @param sleep  Duration to sleep between thread loops (milliseconds).
	 */
	public DeviceThread(Device device, int sleep) {
		this.device = device;
		setThreadSleep(sleep);
	}
	
	/**
	 * Sets the thread frequency (loops per minute).
	 * 
	 * @param frequency Loop frequency (Hz).
	 */
	public void setThreadFrequency(int frequency) {
		// rounding by adding 0.5f before casting to int
		setThreadSleep((int) (frequency / 60f + 0.5f));
	}
	
	/**
	 * Sets the duration to sleep between thread loops.
	 * 
	 * @param sleep Thread loop sleep duration (milliseconds).
	 */
	public void setThreadSleep(int sleep) {
		this.sleep = sleep;
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
