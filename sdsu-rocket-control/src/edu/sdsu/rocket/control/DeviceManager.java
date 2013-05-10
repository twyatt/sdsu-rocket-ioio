package edu.sdsu.rocket.control;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;

import java.util.ArrayList;
import java.util.List;

import edu.sdsu.rocket.control.devices.Device;
import edu.sdsu.rocket.control.devices.DeviceRunnable;

public class DeviceManager implements IOIOLooper {
	
	public interface DeviceManagerListener {
		public void incompatible();
		public void connected(IOIO ioio);
		public void disconnected();
	}
	
	private DeviceManagerListener listener;
	
	private List<Device> devices = new ArrayList<Device>();
	private List<DeviceRunnable> runnables = new ArrayList<DeviceRunnable>();
	
	private List<Thread> threads = new ArrayList<Thread>();
	
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
	
	public void setListener(DeviceManagerListener listener) {
		this.listener = listener;
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
	public void add(DeviceRunnable runnable) {
		App.log.i(App.TAG, "Device manager adding runnable for device: " + runnable.device.info());
		runnables.add(runnable);
	}
	
	/**
	 * IOIOLooper interface methods.
	 */

	@Override
	public void incompatible() {
		App.log.i(App.TAG, "IOIO incompatible");
		
		if (listener != null) {
			listener.incompatible();
		}
	}

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		App.log.i(App.TAG, "IOIO setup");
		
		if (listener != null) {
			listener.connected(ioio);
		}
		
		for (Device device : devices) {
			App.log.i(App.TAG, "Setting up device: " + device.info());
			device.setup(ioio);
		}
		for (DeviceRunnable runnable : runnables) {
			App.log.i(App.TAG, "Setting up threaded device: " + runnable.device.info());
			runnable.device.setup(ioio);
		}
		
		for (DeviceRunnable runnable : runnables) {
			App.log.i(App.TAG, "Starting thread for device: " + runnable.device.info());
			Thread thread = new Thread(runnable);
			threads.add(thread);
			thread.start();
		}
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		for (Device device : devices) {
			device.loop();
		}
		
		Thread.sleep(sleep);
	}
	
	@Override
	public void disconnected() {
		App.log.i(App.TAG, "IOIO disconnected");
		
		if (listener != null) {
			listener.disconnected();
		}
		
		for (Thread thread : threads) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		threads.clear();
	}

}
