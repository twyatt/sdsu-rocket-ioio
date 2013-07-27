package edu.sdsu.rocket.control.devices;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.ArrayList;
import java.util.List;

import edu.sdsu.rocket.control.App;

public class DeviceThreader extends DeviceMultiplexer {
	
	private List<Thread> threads = new ArrayList<Thread>();
	
	synchronized private void start() {
		for (Device device : devices) {
			App.log.i(App.TAG, "Starting thread for device: " + device.info());
			
			Thread thread = new Thread(new DeviceRunnable(device));
			thread.setName(device.getClass().getSimpleName());
			threads.add(thread);
			thread.start();
		}
	}
	
	synchronized private void stop() {
		for (Thread thread : threads) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				// silently ignore
			}
		}
		threads.clear();
	}
	
	/*
	 * IOIOLooper interface methods.
	 */
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		super.setup(ioio);
		start();
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		// silently ignored
	}

	@Override
	public void disconnected() {
		stop();
		super.disconnected();
	}

	@Override
	public void incompatible() {
		super.incompatible();
	}

}
