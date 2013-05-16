package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.models.Rocket.SensorPriority;

public class LaunchObjective implements Objective {

	private enum Mode {
		STANDBY,
		COUNTDOWN,
		WAIT_FOR_BREAKWIRE,
		FUEL_DELAY,
	}
	
	private Mode mode = Mode.STANDBY;
	
	private static final float COUNTDOWN_DURATION = 10f; // seconds
	private static final float LAUNCH_DELAY_AFTER_BREAKWIRE = 2f; // seconds
	
	private float countdownStartTime;
	private float lastMessageTime;
	private float breakWireTime;

	@Override
	public void command(Rocket rocket, String command) {
		if (command.equalsIgnoreCase(Network.LAUNCH_COMMAND)) {
			if (Mode.WAIT_FOR_BREAKWIRE.equals(mode) || Mode.FUEL_DELAY.equals(mode)) {
				// force open fuel
				openFuel(rocket);
			} else {
				startCountdown();
			}
		}
		
		if (command.equalsIgnoreCase(Network.ABORT_COMMAND)) {
			abortLaunch(rocket);
		}
	}

	private void startCountdown() {
		App.log.i(App.TAG, "Initiating launch!");
		App.data.enable();
		
		countdownStartTime = App.elapsedTime();
		mode = Mode.COUNTDOWN;
	}
	
	private void igniteModelRocketMotor(Rocket rocket) {
		App.log.i(App.TAG, "Waiting for break wire.");
		mode = Mode.WAIT_FOR_BREAKWIRE;
		
		App.log.i(App.TAG, "Igniting model rocket motor!");
		rocket.ignitor.activate();
	}
	
	public void abortLaunch(Rocket rocket) {
		rocket.fuelValve.deactivate();
		rocket.servoLOX.open();
		rocket.servoEthanol.open();
		App.log.i(App.TAG, "Closing fuel valves and opening tank vents!");
		
		mode = Mode.STANDBY;
		App.data.disable();
		
		App.log.i(App.TAG, "Launch aborted!");
	}
	
	private void openFuel(Rocket rocket) {
		App.log.i(App.TAG, "Opening fuel valves!");
		mode = Mode.STANDBY;
		rocket.fuelValve.activate();
		
		App.objective.set(Network.FLIGHT_OBJECTIVE);
	}
	
	/*
	 * Objective interface methods.
	 */
	
	@Override
	public void start(Rocket rocket) {
		App.log.i(App.TAG, "Setting sensor priority to low.");
		rocket.setSensorPriority(SensorPriority.SENSOR_PRIORITY_LOW);
	}

	@Override
	public void loop(Rocket rocket) {
		if (Mode.COUNTDOWN.equals(mode)) {
			float countdownElapsed = App.elapsedTime() - countdownStartTime;
			
			if (App.elapsedTime() - lastMessageTime >= 1f) {
				lastMessageTime = App.elapsedTime();
				App.log.i(App.TAG, "Countdown: " + Math.round(COUNTDOWN_DURATION - countdownElapsed));
			}
			
			if (countdownElapsed > COUNTDOWN_DURATION) {
				igniteModelRocketMotor(rocket);
			}
		} else if (Mode.WAIT_FOR_BREAKWIRE.equals(mode)) {
			// ignitor has been lit, now we need to check if ignitor break wire has been broken
			if (rocket.breakWire.isBroken()) {
				App.log.i(App.TAG, "Break wire is BROKEN.");
				breakWireTime = App.elapsedTime();
				mode = Mode.FUEL_DELAY;
			}
		} else if (Mode.FUEL_DELAY.equals(mode)) {
			float delayElapsed = App.elapsedTime() - breakWireTime;
			
			if (App.elapsedTime() - lastMessageTime >= 1f) {
				lastMessageTime = App.elapsedTime();
				App.log.i(App.TAG, "Delay countdown: " + Math.round(LAUNCH_DELAY_AFTER_BREAKWIRE - delayElapsed));
			}
			
			if (delayElapsed > LAUNCH_DELAY_AFTER_BREAKWIRE) {
				openFuel(rocket);
			}
		}
	}

	@Override
	public void stop(Rocket rocket) {
		
	}
	
}
