package edu.sdsu.rocket.control;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;

import java.util.ArrayList;

import edu.sdsu.rocket.control.devices.Device;
import edu.sdsu.rocket.control.devices.DeviceThread;
import edu.sdsu.rocket.logging.Log;

public class DeviceManager implements IOIOLooper {
	
	private ArrayList<Device> devices = new ArrayList<Device>();
	private ArrayList<DeviceThread> threads = new ArrayList<DeviceThread>();
	
	public void add(Device device) {
		add(device, false);
	}
	
	public void add(Device device, boolean spawnThread) {
		if (spawnThread) {
			Log.i("Spawning thread for device: " + device.info());
			
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
				device.setup(ioio);
			}
			for (DeviceThread thread : threads) {
				thread.device.setup(ioio);
				thread.run();
			}
		} catch (ConnectionLostException e) {
			e.printStackTrace();
			// TODO log connection lost
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
			// TODO log connection lost
		} catch (InterruptedException e) {
			e.printStackTrace();
			// TODO log interrupted
		}
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
			// TODO log thread sleep exception
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
