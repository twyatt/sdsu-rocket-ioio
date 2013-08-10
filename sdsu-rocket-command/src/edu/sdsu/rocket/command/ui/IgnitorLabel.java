package edu.sdsu.rocket.command.ui;

import java.awt.Color;

import edu.sdsu.rocket.command.models.Ignitor;

public class IgnitorLabel extends StatusLabel {

	public static final Color UNKNOWN_COLOR  = new Color(128, 128, 128); // dark grey
	public static final Color ACTIVE_COLOR   = new Color(  0, 128,   0); // dark green
	public static final Color ACTIVED_COLOR  = new Color(  0, 128,   0); // dark green
	public static final Color INACTIVE_COLOR = new Color(128,   0,   0); // dark red
	
	public static final String INACTIVE_TEXT  = "Inactive";
	public static final String ACTIVE_TEXT    = "Active";
	public static final String ACTIVATED_TEXT = "Activated";
	public static final String UNKNOWN_TEXT   = "Unknown";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5247411840280168198L;
	
	public IgnitorLabel() {
		super();
		setPositiveColor(ACTIVE_COLOR);
		setNegativeColor(INACTIVE_COLOR);
		setNeautralColor(UNKNOWN_COLOR);
		setState(Ignitor.State.UNKNOWN);
	}
	
	public void setState(Ignitor.State state) {
		switch (state) {
		case INACTIVE:
			setNegative(INACTIVE_TEXT);
			break;
		case ACTIVE:
			setPositive(ACTIVE_TEXT);
			break;
		case ACTIVATED:
			setPositive(ACTIVATED_TEXT);
			break;
		default:
			setNeautral(UNKNOWN_TEXT);
		}
	}

}
