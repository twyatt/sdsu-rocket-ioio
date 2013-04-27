package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;

public class LaunchObjective implements Objective {

	private static final String LAUNCH_COMMAND = "LAUNCH";

	@Override
	public void command(Rocket rocket, String command) {
		if (command.equalsIgnoreCase(LAUNCH_COMMAND)) {
			App.log.i(App.TAG, "Initiating launch!");
			rocket.ignitor.ignite();
		}
	}

	@Override
	public void loop(Rocket rocket) {
		// TODO Auto-generated method stub
		
	}

}
