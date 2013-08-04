package edu.sdsu.rocket.control;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import edu.sdsu.rocket.control.devices.Device;
import edu.sdsu.rocket.control.devices.DeviceMultiplexer;
import edu.sdsu.rocket.control.devices.DeviceThreader;

/**
 * Provides a way to manage multiple devices connected to the IOIO. A device can
 * either be added to the IOIO thread or a thread can be spawned for the device.
 */
public class DeviceManager implements IOIOLooper {
	
	public interface DeviceManagerListener {
		public void setup(IOIO ioio);
		public void disconnected();
		public void incompatible();
	}
	
	private DeviceManagerListener listener;
	
	/**
	 * Manages the devices to be run on the IOIO thread.
	 */
	private DeviceMultiplexer multiplexer = new DeviceMultiplexer();
	
	/**
	 * Manages devices that will run on their own threads.
	 */
	private DeviceThreader threader = new DeviceThreader();
	
	/**
	 * Creates a device manager.
	 * 
	 * @param sleep Sleep duration between IOIO thread loops (milliseconds).
	 */
	public DeviceManager(long sleep) {
		multiplexer.setSleep(sleep);
	}
	
	public void setListener(DeviceManagerListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Adds a device to the device manager.
	 * 
	 * @param device
	 * @param threaded
	 */
	public void add(Device device, boolean threaded) {
		if (threaded) {
			App.log.i(App.TAG, "Device manager adding device to seperate thread: " + device.info());
			threader.add(device);
		} else {
			App.log.i(App.TAG, "Device manager adding device to IOIO thread: " + device.info());
			device.setSleep(0L); // disable device sleep as device will run on IOIO thread with it's own sleep
			multiplexer.add(device);
		}
	}
	
	/**
	 * IOIOLooper interface methods.
	 */

	@Override
	public void incompatible() {
		if (listener != null) {
			listener.incompatible();
		}
		
		multiplexer.incompatible();
		threader.incompatible();
	}

	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		if (listener != null) {
			listener.setup(ioio);
		}
		
		multiplexer.setup(ioio);
		threader.setup(ioio);
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		multiplexer.loop();		
	}
	
	@Override
	public void disconnected() {
		if (listener != null) {
			listener.disconnected();
		}
		
		multiplexer.disconnected();
		threader.disconnected();
	}

}
