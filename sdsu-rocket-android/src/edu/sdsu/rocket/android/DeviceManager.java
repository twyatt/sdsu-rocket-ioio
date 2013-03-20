package edu.sdsu.rocket.android;

import java.util.ArrayList;

import edu.sdsu.rocket.android.devices.Device;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;

public class DeviceManager implements IOIOLooper {
	
	private ArrayList<Device> devices = new ArrayList<Device>();
	
	public void add(Device device) {
		this.devices.add(device);
	}
	
	/**
	 * Methods for IOIOLooper.
	 */

	@Override
	public void incompatible() {
		// TODO log incompatible
	}

	@Override
	public void setup(IOIO ioio) {
		try {
			for (Device device : devices) {
				device.setup(ioio);
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
		// TODO log disconnect
	}

}
