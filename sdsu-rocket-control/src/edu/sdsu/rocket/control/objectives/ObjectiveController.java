package edu.sdsu.rocket.control.objectives;

import java.util.HashMap;
import java.util.Map;

import edu.sdsu.rocket.control.App;
import edu.sdsu.rocket.control.models.Rocket;

public class ObjectiveController {

	private Rocket rocket;
	
	Map<String, Objective> objectives = new HashMap<String, Objective>();
	private Objective active;

	private Thread thread;
	private long threadSleep;

	public ObjectiveController(Rocket rocket, long threadSleep) {
		this.rocket = rocket;
		this.threadSleep = threadSleep;
	}
	
	public boolean start() {
		if (thread != null) {
			App.log.e(App.TAG, "Objective controller thread already started.");
			return false;
		}
		
		thread = new Thread(new ObjectiveControllerRunnable(threadSleep));
		thread.start();
		
		return true;
	}
	
	public void stop() {
		if (thread == null) {
			App.log.e(App.TAG, "Unable to stop objective controller thread; thread not started.");
			return;
		}
		
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// ignore
		}
		thread = null;
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
	
	public class ObjectiveControllerRunnable implements Runnable {
		
		private long sleep;
		
		public ObjectiveControllerRunnable(long sleep) {
			this.sleep = sleep;
		}
		
		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					if (active != null) {
						active.loop(rocket);
					}
					Thread.sleep(sleep);
				}
			} catch (InterruptedException e) {
				// thread interrupted
			}
		}
		
	}
	
}
