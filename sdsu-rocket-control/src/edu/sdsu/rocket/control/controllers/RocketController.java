package edu.sdsu.rocket.control.controllers;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.DeviceManager;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.models.Rocket.SensorPriority;
import edu.sdsu.rocket.helpers.Threaded;

public class RocketController extends Threaded {
	
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
		
//		deviceManager.add(rocket.ignitor, false);
//		deviceManager.add(rocket.fuelValve, false);
//		deviceManager.add(rocket.breakWire, false);
//		
//		deviceManager.add(rocket.tankPressureLOX,     true /* threaded */);
//		deviceManager.add(rocket.tankPressureEthanol, true /* threaded */);
//		deviceManager.add(rocket.tankPressureEngine,  true /* threaded */);
//		
//		deviceManager.add(rocket.ethanolValve, false);
//		
//		deviceManager.add(rocket.barometer, true /* threaded */);
		
		deviceManager.add(rocket.accelerometer, true /* threaded */);
		
		Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		rocket.internalAccelerometer
			.setDataSource(accelerometerSensor)
			.setSensorManager(sensorManager);
		
//		setSensorPriority(SensorPriority.SENSOR_PRIORITY_LOW);
		setSensorPriority(SensorPriority.SENSOR_PRIORITY_HIGH);
	}
	
	public void setSensorPriority(SensorPriority priority) {
		// TODO update phone's accelerometer rate
		
		if (SensorPriority.SENSOR_PRIORITY_HIGH.equals(priority)) {
			rocket.tankPressureLOX.setSleep(5 /* milliseconds */);
			rocket.tankPressureEthanol.setSleep(5 /* milliseconds */);
			rocket.tankPressureEngine.setSleep(1 /* milliseconds */);
			rocket.barometer.setSleep(1 /* milliseconds */);
			rocket.accelerometer.setFrequency(100f /* Hz */);
		} else if (SensorPriority.SENSOR_PRIORITY_MEDIUM.equals(priority)) {
			rocket.tankPressureLOX.setSleep(50 /* milliseconds */);
			rocket.tankPressureEthanol.setSleep(50 /* milliseconds */);
			rocket.tankPressureEngine.setSleep(10 /* milliseconds */);
			rocket.barometer.setSleep(10 /* milliseconds */);
			rocket.accelerometer.setFrequency(10f /* Hz */);
		} else { // SENSOR_PRIORITY_LOW
			rocket.tankPressureLOX.setSleep(500 /* milliseconds */);
			rocket.tankPressureEthanol.setSleep(500 /* milliseconds */);
			rocket.tankPressureEngine.setSleep(500 /* milliseconds */);
			rocket.barometer.setSleep(200 /* milliseconds */); // FIXME set frequency
			rocket.accelerometer.setFrequency(1f /* Hz */);
		}
	}
	
	public void openLOXVent() {
		App.log.i(App.TAG, "Opening LOX vent.");
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
		rocket.loxValve.close();
		isLOXCycling = false;
	}
	
	public void openEthanolVent() {
		App.log.i(App.TAG, "Opening Ethanol vent.");
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
		rocket.ethanolValve.close();
		isEthanolCycling = false;
	}
	
	public void ignite() {
		App.log.i(App.TAG, "Igniting ignitor.");
		rocket.ignitor.ignite();
	}
	
	public void launch(boolean force) {
		if (rocket.breakWire.isBroken() || force) {
			isLOXCycling = false;
			isEthanolCycling = false;
			
			App.log.i(App.TAG, "Opening fuel valve!");
			rocket.fuelValve.open();
			
			App.log.i(App.TAG, "Setting sensor priority to high.");
			setSensorPriority(SensorPriority.SENSOR_PRIORITY_HIGH);
		}
	}
	
	public void abortLaunch() {
		rocket.fuelValve.close();
		rocket.loxValve.open();
		rocket.ethanolValve.open();
		App.log.i(App.TAG, "Closing fuel valves and opening tank vents!");
		
		App.data.disable();
		App.log.i(App.TAG, "Launch aborted!");
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
		
		// TODO send rocket status? i.e. break wire, ready for launch status
	}

	@Override
	public void interrupted() {
		// silently ignore
	}

}
