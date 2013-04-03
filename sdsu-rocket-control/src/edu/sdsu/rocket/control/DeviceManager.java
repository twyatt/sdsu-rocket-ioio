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
	
	public void add(Device device) {
		add(device, false);
	}
	
	public void add(Device device, boolean spawnThread) {
		if (spawnThread) {
			App.log.i(App.TAG, "Spawning thread for device: " + device.info());
			
			DeviceThread thread = new DeviceThread(device);
			threads.add(thread);
		} else {
			this.devices.add(device);
		}
		
		// TODO allow devices to be added after setup
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
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "Interrupted exception during IOIO loop");
			return;
		}
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
			App.log.i(App.TAG, "Thread sleep exception during IOIO loop");
			return;
		}
	}
	
	@Override
	public void disconnected() {
		App.log.i(App.TAG, "IOIO disconnect");
		
		for (DeviceThread thread : threads) {
			thread.close();
		}
	}

}
