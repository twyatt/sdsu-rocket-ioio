package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;

public class FillTanksObjective implements Objective {
	
	private static final float CYCLE_CLOSE_DURATION = 1.5f; // seconds
	private static final float CYCLE_OPEN_DURATION = 10f; // seconds
	
	private boolean cycleLOX;
	private float lastCycleLOX;
	private boolean cycleIsOpenLOX;
	
	private boolean cycleEthanol;
	private float lastCycleEthanol;
	private boolean cycleIsOpenEthanol;

	@Override
	public void start(Rocket rocket) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void command(Rocket rocket, String command) {
		if (command.equalsIgnoreCase(Network.CLOSE_ETHANOL_TANK_COMMAND)) {
			cycleEthanol = false;
			closeEthanol(rocket);
		}
		
		if (command.equalsIgnoreCase(Network.OPEN_ETHANOL_TANK_COMMAND)) {
			openEthanol(rocket);
		}
		
		if (command.equalsIgnoreCase(Network.CLOSE_LOX_TANK_COMMAND)) {
			cycleLOX = false;
			closeLOX(rocket);
		}
		
		if (command.equalsIgnoreCase(Network.OPEN_LOX_TANK_COMMAND)) {
			openLOX(rocket);
		}
		
		if (command.equalsIgnoreCase(Network.CYCLE_LOX_TANK_COMMAND)) {
			cycleLOX(rocket);
		}
		
		if (command.equalsIgnoreCase(Network.CYCLE_ETHANOL_TANK_COMMAND)) {
			cycleEthanol(rocket);
		}
	}

	private void cycleEthanol(Rocket rocket) {
		App.log.i(App.TAG, "Cycling ethanol tank.");
		lastCycleEthanol = App.elapsedTime();
		cycleIsOpenEthanol = false;
		cycleEthanol = true;
	}

	private void cycleLOX(Rocket rocket) {
		App.log.i(App.TAG, "Cycling LOX tank.");
		lastCycleLOX = App.elapsedTime();
		cycleIsOpenLOX = false;
		cycleLOX = true;
	}

	private void openLOX(Rocket rocket) {
		App.log.i(App.TAG, "Opening LOX tank.");
		rocket.servoLOX.open();
	}

	private void closeLOX(Rocket rocket) {
		App.log.i(App.TAG, "Closing LOX tank.");
		rocket.servoLOX.close();
	}

	private void openEthanol(Rocket rocket) {
		App.log.i(App.TAG, "Opening ethanol tank.");
		rocket.servoEthanol.open();
	}

	private void closeEthanol(Rocket rocket) {
		App.log.i(App.TAG, "Closing ethanol tank.");
		rocket.servoEthanol.close();
	}
	
	@Override
	public void loop(Rocket rocket) {
		if (cycleLOX) {
			if (cycleIsOpenLOX) {
				if (App.elapsedTime() - lastCycleLOX >= CYCLE_OPEN_DURATION) {
					cycleIsOpenLOX = false;
					lastCycleLOX = App.elapsedTime();
					closeLOX(rocket);
				}
			} else {
				if (App.elapsedTime() - lastCycleLOX >= CYCLE_CLOSE_DURATION) {
					cycleIsOpenLOX = true;
					lastCycleLOX = App.elapsedTime();
					openLOX(rocket);
				}
			}
		}
		
		if (cycleEthanol) {
			if (cycleIsOpenEthanol) {
				if (App.elapsedTime() - lastCycleEthanol >= CYCLE_OPEN_DURATION) {
					cycleIsOpenEthanol = false;
					lastCycleEthanol = App.elapsedTime();
					closeEthanol(rocket);
				}
			} else {
				if (App.elapsedTime() - lastCycleEthanol >= CYCLE_CLOSE_DURATION) {
					cycleIsOpenEthanol = true;
					lastCycleEthanol = App.elapsedTime();
					openEthanol(rocket);
				}
			}
		}
	}

	@Override
	public void stop(Rocket rocket) {
		cycleLOX = false;
		cycleEthanol = false;
	}

}
