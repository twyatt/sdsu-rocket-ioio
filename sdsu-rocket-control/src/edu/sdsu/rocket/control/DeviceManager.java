package edu.sdsu.rocket.control;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;

import java.util.ArrayList;

import edu.sdsu.rocket.control.devices.Device;
import edu.sdsu.rocket.control.devices.DeviceThread;

public class DeviceManager implements IOIOLooper {
	
	private ArrayList<Device> devices = new ArrayList<Device>();
	private ArrayList<DeviceThread> threads = new ArrayList<DeviceThread>();
	
	/**
	 * Sleep duration between IOIO thread loops (milliseconds).
	 */
	private int sleep;
	
	/**
	 * Creates a device manager.
	 * 
	 * @param threadSleep Sleep duration between IOIO thread loops (milliseconds).
	 */
	public DeviceManager(int threadSleep) {
		this.sleep = threadSleep;
	}
	
	/**
	 * Adds a device on the IOIO thread to the device manager.
	 * 
	 * @param device
	 */
	public void add(Device device) {
		App.log.i(App.TAG, "Device manager adding device: " + device.info());
		this.devices.add(device);
	}
	
	/**
	 * Adds a device a device thread to the device manager.
	 * 
	 * @param device
	 */
	public void add(DeviceThread thread) {
		App.log.i(App.TAG, "Device manager adding thread for device: " + thread.device.info());
		threads.add(thread);
	}
	
	/**
	 * IOIOLooper interface methods.
	 */

	@Override
	public void incompatible() {
		App.log.i(App.TAG, "IOIO incompatible");
	}

	@Override
	public void setup(IOIO ioio) {
		App.log.i(App.TAG, "IOIO setup");
		
		try {
			for (Device device : devices) {
				App.log.i(App.TAG, "Setting up device: " + device.info());
				device.setup(ioio);
			}
			for (DeviceThread thread : threads) {
				App.log.i(App.TAG, "Setting up device: " + thread.device.info());
				thread.device.setup(ioio);
			}
		} catch (ConnectionLostException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "Connection lost with IOIO during setup");
			return;
		}
		
		for (DeviceThread thread : threads) {
			App.log.i(App.TAG, "Starting thread for device: " + thread.device.info());
			thread.start();
		}
	}
	
	@Override
	public void loop() {
		try {
			for (Device device : devices) {
				device.loop();
			}
		} catch (ConnectionLostException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "Connection lost with IOIO during loop");
		} catch (InterruptedException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "Interrupted exception during IOIO loop");
		}
		
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "Thread sleep exception during IOIO loop");
		}
	}
	
	@Override
	public void disconnected() {
		App.log.i(App.TAG, "IOIO disconnected");
		
		for (DeviceThread thread : threads) {
			thread.close();
		}
	}

}
