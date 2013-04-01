package edu.sdsu.rocket.control;

import edu.sdsu.rocket.control.devices.BMP085.BMP085Listener;
import edu.sdsu.rocket.control.devices.P51500AA1365V.P51500AA1365VListener;

public class DataLogger implements BMP085Listener, P51500AA1365VListener {

	@Override
	public void onBMP085Values(float pressure, double temperature) {
		// TODO log to file
	}

	@Override
	public void onP51500AA1365VValue(float pressure) {
		// TODO log to file
	}
	
}
