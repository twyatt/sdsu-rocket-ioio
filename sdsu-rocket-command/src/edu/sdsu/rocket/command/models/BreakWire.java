package edu.sdsu.rocket.command.models;

public class BreakWire {
	
	public enum State {
		UNKNOWN,
		NOT_BROKEN,
		BROKEN,
	}

	public State state = State.UNKNOWN;
	
}
