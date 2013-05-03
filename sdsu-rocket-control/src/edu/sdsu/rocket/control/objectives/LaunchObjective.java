package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;

public class LaunchObjective implements Objective {

	@Override
	public void start(Rocket rocket) {
		App.data.enable();
	}
	
	@Override
	public void command(Rocket rocket, String command) {
		if (command.equalsIgnoreCase(Network.LAUNCH_COMMAND)) {
			App.log.i(App.TAG, "Initiating launch!");
//			rocket.ignitor.ignite();
			// TODO countdown
		}
		
		if (command.equalsIgnoreCase(Network.ABORT_COMMAND)) {
			App.log.i(App.TAG, "Aborting launch!");
			// TODO abort launch
		}
	}

	@Override
	public void loop(Rocket rocket) {
		// TODO Auto-generated method stub
		// TODO perform countdown
	}

	@Override
	public void stop(Rocket rocket) {
		// TODO cancel launch if in progress
		App.data.disable();
	}

}
