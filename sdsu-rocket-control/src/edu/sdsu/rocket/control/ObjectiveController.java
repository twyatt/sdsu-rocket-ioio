package edu.sdsu.rocket.control;

import java.util.HashMap;
import java.util.Map;

import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.objectives.Objective;

public class ObjectiveController implements Runnable {

	private Rocket rocket;
	
	Map<String, Objective> objectives = new HashMap<String, Objective>();
	private Objective active;

	private long sleep;

	public ObjectiveController(Rocket rocket, int threadSleep) {
		this.rocket = rocket;
		sleep = threadSleep;
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
			
			if (active != null) {
				active.stop(rocket);
			}
			active = objective;
			active.start(rocket);
			
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
	 * Runnable interface methods.
	 */
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			if (active != null) {
				active.loop(rocket);
			}
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
