package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;

public class FillTanksObjective implements Objective {
	
	boolean cycleLOX;
	boolean cycleEthanol;

	@Override
	public void start(Rocket rocket) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void command(Rocket rocket, String command) {
		if (command.equalsIgnoreCase(Network.CLOSE_ETHANOL_TANK)) {
			App.log.i(App.TAG, "Closing ethanol tank.");
			rocket.servoEthanol.close();
		}
		
		if (command.equalsIgnoreCase(Network.OPEN_ETHANOL_TANK)) {
			App.log.i(App.TAG, "Opening ethanol tank.");
			rocket.servoEthanol.open();
		}
		
		if (command.equalsIgnoreCase(Network.CLOSE_LOX_TANK_COMMAND)) {
			App.log.i(App.TAG, "Closing LOX tank.");
			rocket.servoLOX.close();
		}
		
		if (command.equalsIgnoreCase(Network.OPEN_LOX_TANK_COMMAND)) {
			App.log.i(App.TAG, "Opening LOX tank.");
			rocket.servoLOX.open();
		}
		
		// TODO tank open/close cycling
	}
	
	@Override
	public void loop(Rocket rocket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(Rocket rocket) {
		cycleLOX = false;
		cycleEthanol = false;
	}

}
