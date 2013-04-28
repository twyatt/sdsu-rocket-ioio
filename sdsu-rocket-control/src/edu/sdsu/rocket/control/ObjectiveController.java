package edu.sdsu.rocket.control;

import java.util.HashMap;
import java.util.Map;

import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.objectives.Objective;

public class ObjectiveController extends Thread {

	private Rocket rocket;
	
	Map<String, Objective> objectives = new HashMap<String, Objective>();
	private Objective active;

	public ObjectiveController(Rocket rocket) {
		this.rocket = rocket;
	}
	
	public void add(String name, Objective objective) {
		objectives.put(name, objective);
	}
	
	public void add(Objective objective) {
		String name = objective.getClass().getName();
		add(name, objective);
	}
	
	/**
	 * Sets the specified objective as the active objective.
	 * 
	 * @param name
	 */
	public boolean set(String name) {
		if (objectives.containsKey(name)) {
			Objective objective = objectives.get(name);
			active = objective;
			
			App.log.i(App.TAG, "Objective set to '" + name + "'.");
			return true;
		} else {
			App.log.i(App.TAG, "Objective '" + name + "' not found.");
			return false;
		}
	}
	
	/**
	 * Sends command to active objective.
	 * 
	 * @param command
	 */
	public void command(String command) {
		if (active != null) {
			active.command(rocket, command);
		} else {
			App.log.i(App.TAG, "No active objective to send command to.");
		}
	}
	
	/*
	 * Thread interface methods.
	 */
	
	@Override
	public void run() {
		while (true) {
			active.loop(rocket);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		interrupt();
		
		try {
			join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
