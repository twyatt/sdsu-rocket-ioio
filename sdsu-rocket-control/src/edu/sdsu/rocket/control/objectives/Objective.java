package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.control.models.Rocket;

public interface Objective {

	void start(Rocket rocket);
	void command(Rocket rocket, String command);
	void loop(Rocket rocket);
	void stop(Rocket rocket);

}
