package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.control.models.Rocket;

public interface Objective {

	void command(Rocket rocket, String command);
	void loop(Rocket rocket);

}
