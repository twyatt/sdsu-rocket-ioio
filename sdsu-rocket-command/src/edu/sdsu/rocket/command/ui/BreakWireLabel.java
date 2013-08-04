package edu.sdsu.rocket.command.ui;

import java.awt.Color;

import edu.sdsu.rocket.command.models.BreakWire;

public class BreakWireLabel extends StatusLabel {
	
	public static final Color UNKNOWN_COLOR    = new Color(128, 128, 128); // dark grey
	public static final Color BROKEN_COLOR     = new Color(  0, 128,   0); // dark green
	public static final Color NOT_BROKEN_COLOR = new Color(128,   0,   0); // dark red
	
	public static final String BROKEN_TEXT     = "Broken";
	public static final String NOT_BROKEN_TEXT = "NOT Broken";
	public static final String UNKNOWN_TEXT    = "Unknown";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6934879593075438740L;
	
	public BreakWireLabel() {
		super();
		setPositiveColor(BROKEN_COLOR);
		setNegativeColor(NOT_BROKEN_COLOR);
		setNeautralColor(UNKNOWN_COLOR);
		setState(BreakWire.State.UNKNOWN);
	}
	
	public void setState(BreakWire.State state) {
		switch (state) {
		case BROKEN:
			setPositive(BROKEN_TEXT);
			break;
		case NOT_BROKEN:
			setNegative(NOT_BROKEN_TEXT);
			break;
		default:
			setNeautral(UNKNOWN_TEXT);
		}
	}
	
}
