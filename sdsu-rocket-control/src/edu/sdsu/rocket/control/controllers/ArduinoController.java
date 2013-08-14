package edu.sdsu.rocket.control.controllers;

import java.io.IOException;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.devices.Arduino;
import edu.sdsu.rocket.control.devices.Arduino.ArduinoListener;
import edu.sdsu.rocket.control.models.Rocket;

public class ArduinoController implements ArduinoListener {
	
	public static final int LOX_PRESSURE_REQUEST  = 0;
	public static final int ETHANOL_TANK_PRESSURE = 1;
	public static final int ENGINE_PRESSURE       = 2;
	public static final int LOX_VALVE_TEMPERATURE = 3;
	public static final int PHONE_BAY_TEMPERATURE = 4;
	public static final int ANGLE_OF_ROCKET       = 6;

	private Arduino arduino;
	private Rocket rocket;

	public ArduinoController(Arduino arduino, Rocket rocket) {
		this.arduino = arduino;
		this.rocket = rocket;
	}

	@Override
	public void onRequest(int request, float value) {
		System.out.println("Received ignitor temperature of " + value + " C");
		App.rocketController.getRocket().ignitorTemperature = value;
		
		try {
			System.out.println("Processing Arduino request: " + request);
			switch (request) {
			case LOX_PRESSURE_REQUEST:
				arduino.sendResponse(rocket.loxPressure.getPressure());
				break;
			case ETHANOL_TANK_PRESSURE:
				arduino.sendResponse(rocket.ethanolPressure.getPressure());
				break;
			case ENGINE_PRESSURE:
				arduino.sendResponse(rocket.enginePressure.getPressure());
				break;
			case LOX_VALVE_TEMPERATURE:
				arduino.sendResponse(rocket.loxTemperature.getTemperature());
				break;
			case PHONE_BAY_TEMPERATURE:
				arduino.sendResponse(rocket.loxTemperature.getInternalTemperature());
				break;
			case ANGLE_OF_ROCKET:
				arduino.sendResponse(rocket.accelerometer.getZ()); // FIXME send angle
				break;
			default:
				App.log.e(App.TAG, "Unknown Arduino request: " + request);
			}
		} catch (IOException e) {
			e.printStackTrace();
			App.log.e(App.TAG, "Failed to send response to Arduino.", e);
		}
	}
	
}
