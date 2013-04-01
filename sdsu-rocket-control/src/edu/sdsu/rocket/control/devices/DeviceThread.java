package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class DeviceThread extends Thread {

	public final Device device;

	public DeviceThread(Device device) {
		this.device = device;
	}
	
	public void setup(IOIO ioio) throws ConnectionLostException {
		device.setup(ioio);
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				device.loop();
			}
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		interrupt();
		
		try {
			join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
