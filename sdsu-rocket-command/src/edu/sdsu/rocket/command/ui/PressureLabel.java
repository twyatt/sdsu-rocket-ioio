package edu.sdsu.rocket.command.ui;

import java.awt.Color;

public class PressureLabel extends StatusLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -790087708287939584L;
	
	public static final Color UNKNOWN_COLOR = new Color(128, 128, 128); // dark grey
	public static final Color HIGH_COLOR    = new Color(128,   0,   0); // dark red
	public static final Color OK_COLOR      = new Color(  0, 128,   0); // dark green
	public static final Color LOW_COLOR     = new Color(255, 140,   0); // dark orange
	
	public static final String UNKNOWN_TEXT = "Unknown";
	public static final String HIGH_TEXT    = "HIGH";
	public static final String OK_TEXT      = "OK";
	public static final String LOW_TEXT     = "LOW";

	private float low;
	private float high;
	
	public PressureLabel(float low, float high) {
		super();
		this.low = low;
		this.high = high;
		
		setHighColor(HIGH_COLOR);
		setOKColor(OK_COLOR);
		setLowColor(LOW_COLOR);
		setPressure(Float.NaN);
	}
	
	public PressureLabel setHighColor(Color color) {
		setNegativeColor(color);
		return this;
	}
	
	public PressureLabel setOKColor(Color color) {
		setPositiveColor(color);
		return this;
	}
	
	public PressureLabel setLowColor(Color color) {
		setNeautralColor(color);
		return this;
	}
	
	public void setPressure(float pressure) {
		if (Float.isNaN(pressure)) {
			setBackground(UNKNOWN_COLOR);
			setText(UNKNOWN_TEXT);
		} else if (pressure <= low) {
			setNeautral(LOW_TEXT);
		} else if (pressure >= high) {
			setNegative(HIGH_TEXT);
		} else {
			setPositive(OK_TEXT);
		}
	}

}
