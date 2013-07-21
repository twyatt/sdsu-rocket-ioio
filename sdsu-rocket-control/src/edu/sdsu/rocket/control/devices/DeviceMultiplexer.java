package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.ArrayList;
import java.util.List;

public class DeviceMultiplexer extends Device {

	protected final List<Device> devices = new ArrayList<Device>();
	
	public DeviceMultiplexer(Device ... devices) {
		if (devices == null)
			throw new NullPointerException();
		
		for (Device device : devices) {
			add(device);
		}
	}
	
	public void add(Device device) {
		devices.add(device);
	}
	
	/*
	 * Device interface methods.
	 */

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		for (Device device : devices) {
			device.setup(ioio);
		}
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		for (Device device : devices) {
			device.loop();
		}
		super.loop();
	}
	
	@Override
	public void disconnected() {
		for (Device device : devices) {
			device.disconnected();
		}
	}
	
	@Override
	public void incompatible() {
		for (Device device : devices) {
			device.incompatible();
		}
	}

	@Override
	public String info() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < devices.size(); i++) {
			if (i != 0)
				builder.append("; ");
			
			Device device = devices.get(i);
			builder.append(device.toString());
		}
		return builder.toString();
	}
	
}
