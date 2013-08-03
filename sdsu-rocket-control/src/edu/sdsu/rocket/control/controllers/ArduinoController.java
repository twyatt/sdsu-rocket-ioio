package edu.sdsu.rocket.control.controllers;

import java.io.IOException;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.devices.Arduino;
import edu.sdsu.rocket.control.devices.Arduino.ArduinoListener;
import edu.sdsu.rocket.control.models.Rocket;

public class ArduinoController implements ArduinoListener {
	
	public static final char LOX_VOLTAGE_REQUEST = 'L';

	private Arduino arduino;
	private Rocket rocket;

	public ArduinoController(Arduino arduino, Rocket rocket) {
		this.arduino = arduino;
		this.rocket = rocket;
	}

	@Override
	public void onRequest(char request) {
		switch (request) {
		case LOX_VOLTAGE_REQUEST:
			onLOXVoltageRequest();
			break;
		default:
			App.log.e(App.TAG, "Unknown Arduino request: " + request);
		}
	}

	private void onLOXVoltageRequest() {
		writeLOXVoltage();
	}
	
	public void writeLOXVoltage() {
		try {
			arduino.getOutputStream().writeFloat(rocket.tankPressureLOX.getVoltage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
