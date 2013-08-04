package edu.sdsu.rocket.command.models;


public class Ignitor {

	public enum State {
		UNKNOWN,
		INACTIVE,
		ACTIVE,
		ACTIVATED,
	}

	public State state = State.UNKNOWN;

	public void setState(byte b) {
		if (b == 1) { // active
			state = Ignitor.State.ACTIVE;
		} else if (b == 0) { // inactive
			if (Ignitor.State.ACTIVE.equals(state) || Ignitor.State.ACTIVATED.equals(state)) {
				state = Ignitor.State.ACTIVATED;
			} else {
				state = Ignitor.State.INACTIVE;
			}
		}
	}
	
}
