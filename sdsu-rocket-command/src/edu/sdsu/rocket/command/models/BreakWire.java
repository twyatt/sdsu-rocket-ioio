package edu.sdsu.rocket.command.models;

public class BreakWire {
	
	public enum State {
		UNKNOWN,
		NOT_BROKEN,
		BROKEN,
	}

	public State state = State.UNKNOWN;

	public void setState(byte b) {
		if (b == 1) {
			state = State.BROKEN;
		} else if (b == 0) {
			state = State.NOT_BROKEN;
		} else {
			state = State.UNKNOWN;
		}
	}
	
}
