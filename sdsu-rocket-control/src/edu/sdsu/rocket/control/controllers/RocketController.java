package edu.sdsu.rocket.control.controllers;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.DataLogger;
import edu.sdsu.rocket.control.DeviceManager;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.models.Rocket.SensorPriority;
import edu.sdsu.rocket.helpers.Threaded;

public class RocketController extends Threaded {
	
	public static final SensorPriority DEFAULT_SENSOR_PRIORITY = SensorPriority.SENSOR_PRIORITY_LOW;
	
	private static final float CYCLE_CLOSE_DURATION = 1.5f; // seconds
	private static final float CYCLE_OPEN_DURATION = 10f; // seconds

	private final Rocket rocket;
	
	// TODO do these need to be AtomicBoolean?
	private boolean isLOXCycling;
	private boolean isEthanolCycling;
	private float lastLOXCycle;
	private float lastEthanolCycle;
	
	public RocketController(Rocket rocket) {
		super("Rocket Controller");
		
		if (rocket == null)
			throw new NullPointerException();
		this.rocket = rocket;
		
		setSleep(250L); // 4 Hz
	}
	
	public Rocket getRocket() {
		return rocket;
	}
	
	public void setup(DeviceManager deviceManager, SensorManager sensorManager) {
		deviceManager.add(rocket.connection1, true /* threaded */);
		deviceManager.add(rocket.connection2, true /* threaded */);
//		deviceManager.add(rocket.arduino,     true /* threaded */);
		
		deviceManager.add(rocket.ignitor,   false);
		deviceManager.add(rocket.fuelValve, false);
		deviceManager.add(rocket.breakWire, false);
//		deviceManager.add(rocket.camera,    false);
		
		deviceManager.add(rocket.loxPressure,     true /* threaded */);
		deviceManager.add(rocket.ethanolPressure, true /* threaded */);
		deviceManager.add(rocket.enginePressure,  true /* threaded */);
		
		deviceManager.add(rocket.loxValve,     false);
		deviceManager.add(rocket.ethanolValve, false);
		
		deviceManager.add(rocket.ignitorTemperature, true /* threaded */);
		deviceManager.add(rocket.loxTemperature,     true /* threaded */);
		deviceManager.add(rocket.barometer,          true /* threaded */);
		deviceManager.add(rocket.accelerometer,      true /* threaded */);
//		deviceManager.add(rocket.gyro,               true /* threaded */);
		
		Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		rocket.internalAccelerometer
			.setDataSource(accelerometerSensor)
			.setSensorManager(sensorManager);
		
		setSensorPriority(DEFAULT_SENSOR_PRIORITY);
	}
	
	public void setSensorPriority(SensorPriority priority) {
		// TODO update phone's accelerometer rate
		
		if (SensorPriority.SENSOR_PRIORITY_HIGH.equals(priority)) {
			rocket.loxPressure.setSleep(5 /* milliseconds */);
//			rocket.ignitorTemperature.setSleep(50 /* milliseconds */);
			rocket.loxTemperature.setSleep(50 /* milliseconds */);
			rocket.ethanolPressure.setSleep(5 /* milliseconds */);
			rocket.enginePressure.setSleep(1 /* milliseconds */);
			rocket.barometer.setSleep(0 /* milliseconds */);
			rocket.accelerometer.setFrequency(100f /* Hz */);
//			rocket.gyro.setFrequency(100f /* Hz */);
		} else if (SensorPriority.SENSOR_PRIORITY_MEDIUM.equals(priority)) {
			rocket.loxPressure.setSleep(50 /* milliseconds */);
//			rocket.ignitorTemperature.setSleep(100 /* milliseconds */);
			rocket.loxTemperature.setSleep(100 /* milliseconds */);
			rocket.ethanolPressure.setSleep(50 /* milliseconds */);
			rocket.enginePressure.setSleep(10 /* milliseconds */);
			rocket.barometer.setSleep(10 /* milliseconds */);
			rocket.accelerometer.setFrequency(10f /* Hz */);
//			rocket.gyro.setFrequency(10f /* Hz */);
		} else { // SENSOR_PRIORITY_LOW
			rocket.loxPressure.setSleep(500 /* milliseconds */);
			rocket.ignitorTemperature.setSleep(1000 /* milliseconds */);
			rocket.loxTemperature.setSleep(1000 /* milliseconds */);
			rocket.ethanolPressure.setSleep(500 /* milliseconds */);
			rocket.enginePressure.setSleep(500 /* milliseconds */);
			rocket.barometer.setSleep(200 /* milliseconds */);
			rocket.accelerometer.setFrequency(1f /* Hz */);
//			rocket.gyro.setFrequency(1f /* Hz */);
		}
	}
	
	public void openLOXVent() {
		App.log.i(App.TAG, "Opening LOX vent.");
		App.data.event(DataLogger.Event.LOX_OPEN);
		rocket.loxValve.open();
		isLOXCycling = false;
	}
	
	public void cycleLOXVent() {
		App.log.i(App.TAG, "Cycling LOX vent.");
		lastLOXCycle = App.elapsedTime();
		isLOXCycling = true;
	}
	
	public void closeLOXVent() {
		App.log.i(App.TAG, "Closing LOX vent.");
		App.data.event(DataLogger.Event.LOX_CLOSE);
		rocket.loxValve.close();
		isLOXCycling = false;
	}
	
	public void openEthanolVent() {
		App.log.i(App.TAG, "Opening Ethanol vent.");
		App.data.event(DataLogger.Event.ETHANOL_OPEN);
		rocket.ethanolValve.open();
		isEthanolCycling = false;
	}
	
	public void cycleEthanolVent() {
		App.log.i(App.TAG, "Cycling Ethanol vent.");
		lastEthanolCycle = App.elapsedTime();
		isEthanolCycling = true;
	}
	
	public void closeEthanolVent() {
		App.log.i(App.TAG, "Closing Ethanol vent.");
		App.data.event(DataLogger.Event.ETHANOL_CLOSE);
		rocket.ethanolValve.close();
		isEthanolCycling = false;
	}
	
	public void ignite() {
		App.log.i(App.TAG, "Igniting ignitor.");
		App.data.event(DataLogger.Event.IGNITE);
		rocket.ignitor.ignite();
		App.data.enable();
	}
	
	public void launch() {
		isLOXCycling = false;
		isEthanolCycling = false;
		App.data.event(DataLogger.Event.LAUNCH);
		
		rocket.ignitor.cancel();
		
		App.log.i(App.TAG, "Opening fuel valve!");
		rocket.fuelValve.open();
		
		App.log.i(App.TAG, "Setting sensor priority to high.");
		setSensorPriority(SensorPriority.SENSOR_PRIORITY_HIGH);
		
//		rocket.camera.high();
	}
	
	public void abortLaunch() {
		rocket.fuelValve.close();
		rocket.ignitor.cancel();
		rocket.loxValve.open();
		rocket.ethanolValve.open();
		App.log.i(App.TAG, "Closing fuel valves, cancelling ignitor and opening tank vents!");
		
		setSensorPriority(DEFAULT_SENSOR_PRIORITY);
		App.data.disable();
		
//		rocket.camera.low();
		App.log.i(App.TAG, "Launch aborted!");
		
		App.data.event(DataLogger.Event.ABORT);
	}
	
	/*
	 * Threaded interface methods.
	 */

	@Override
	public void loop() {
		if (isLOXCycling) {
			if (App.elapsedTime() - lastLOXCycle >= CYCLE_OPEN_DURATION) {
				if (rocket.loxValve.isOpen()) {
					lastLOXCycle = App.elapsedTime();
					rocket.loxValve.close();
				}
			} else { // closed
				if (App.elapsedTime() - lastLOXCycle >= CYCLE_CLOSE_DURATION) {
					lastLOXCycle = App.elapsedTime();
					rocket.loxValve.open();
				}
			}
		}
		
		if (isEthanolCycling) {
			if (rocket.ethanolValve.isOpen()) {
				if (App.elapsedTime() - lastEthanolCycle >= CYCLE_OPEN_DURATION) {
					lastEthanolCycle = App.elapsedTime();
					rocket.ethanolValve.close();
				}
			} else { // closed
				if (App.elapsedTime() - lastEthanolCycle >= CYCLE_CLOSE_DURATION) {
					lastEthanolCycle = App.elapsedTime();
					rocket.ethanolValve.open();
				}
			}
		}
	}

	@Override
	public void interrupted() {
		// silently ignore
	}

}
