package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;

public class LaunchObjective implements Objective {

	private static final float COUNTDOWN_DURATION = 10f; // seconds
	
	private float countdownStartTime;
	private boolean countdownActive;

	private boolean waitForIgnitionConfirmationActive;
	
	private float lastMessageTime;

	@Override
	public void command(Rocket rocket, String command) {
		if (command.equalsIgnoreCase(Network.LAUNCH_COMMAND)) {
			startCountdown();
		}
		
		if (command.equalsIgnoreCase(Network.ABORT_COMMAND)) {
			abortLaunch(rocket);
		}
	}

	private void startCountdown() {
		App.log.i(App.TAG, "Initiating launch!");
		
		App.data.enable();
		countdownStartTime = App.elapsedTime();
		countdownActive = true;
	}
	
	private void igniteRocket(Rocket rocket) {
		countdownActive = false;
		waitForIgnitionConfirmationActive = true;
		
		App.log.i(App.TAG, "Igniting model rocket motor!");
		rocket.ignitor.ignite();
	}
	
	public void abortLaunch(Rocket rocket) {
		countdownActive = false;
		waitForIgnitionConfirmationActive = false;
		App.data.disable();
		
		// FIXME implement:
//		rocket.fuelValveClose.ignite();
		
		App.log.i(App.TAG, "Launch aborted!");
	}
	
	/*
	 * Objective interface methods.
	 */
	
	@Override
	public void start(Rocket rocket) {
		
	}

	@Override
	public void loop(Rocket rocket) {
		if (countdownActive) {
			float countdownElapsed = App.elapsedTime() - countdownStartTime;
			
			if (App.elapsedTime() - lastMessageTime >= 1f) {
				lastMessageTime = App.elapsedTime();
				App.log.i(App.TAG, "Countdown: " + Math.round(COUNTDOWN_DURATION - countdownElapsed));
			}
			
			if (countdownElapsed >= COUNTDOWN_DURATION) {
				igniteRocket(rocket);
			}
		}
		
		// ignitor has been lit, now we need to check if ignitor break wire has been broken
		if (waitForIgnitionConfirmationActive) {
			App.log.i(App.TAG, "Waiting for break wire.");
			
//			if (rocket.breakWire.isBroken()) {
//				rocket.fuelValveOpen.ignite();
//				App.objective.set(Network.FLIGHT_OBJECTIVE);
//			}
		}
	}

	@Override
	public void stop(Rocket rocket) {
		abortLaunch(rocket);
	}
	
}
