package edu.sdsu.rocket.control.objectives;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.models.Rocket.SensorPriority;

public class FlightObjective implements Objective {

	@Override
	public void start(Rocket rocket) {
		App.log.i(App.TAG, "Setting sensor priority to high.");
		rocket.setSensorPriority(SensorPriority.SENSOR_PRIORITY_HIGH);
	}
	
	@Override
	public void command(Rocket rocket, String command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loop(Rocket rocket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(Rocket rocket) {
		// TODO Auto-generated method stub
		
	}

}
