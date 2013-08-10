package edu.sdsu.rocket.control.controllers;

import java.io.IOException;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.devices.Arduino;
import edu.sdsu.rocket.control.devices.Arduino.ArduinoListener;
import edu.sdsu.rocket.control.models.Rocket;

public class ArduinoController implements ArduinoListener {
	
	public static final char LOX_VOLTAGE_REQUEST = (byte) 0x00;

	private Arduino arduino;
	private Rocket rocket;

	public ArduinoController(Arduino arduino, Rocket rocket) {
		this.arduino = arduino;
		this.rocket = rocket;
	}

	@Override
	public void onRequest(byte request) {
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
			String string = String.valueOf(rocket.tankPressureLOX.getVoltage());
			arduino.getOutputStream().writeUTF(string + " ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
