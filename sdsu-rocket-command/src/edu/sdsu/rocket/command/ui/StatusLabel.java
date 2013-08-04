package edu.sdsu.rocket.command.ui;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class StatusLabel extends JLabel {

	private Color neautralColor = new Color(128, 128, 128); // dark grey
	private Color positiveColor = new Color(  0, 128,   0); // dark green
	private Color negativeColor = new Color(128,   0,   0); // dark red
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1200626399653134808L;
	
	public StatusLabel() {
		super("");
		setOpaque(true);
		setupUI();
	}
	
	public StatusLabel setNeautralColor(Color color) {
		neautralColor = color;
		return this;
	}
	
	public StatusLabel setPositiveColor(Color color) {
		positiveColor = color;
		return this;
	}
	
	public StatusLabel setNegativeColor(Color color) {
		negativeColor = color;
		return this;
	}
	
	public void setNeautral(String text) {
		setBackground(neautralColor);
		setText(text);
	}
	
	public void setPositive(String text) {
		setBackground(positiveColor);
		setText(text);
	}
	
	public void setNegative(String text) {
		setBackground(negativeColor);
		setText(text);
	}
	
	private void setupUI() {
		setForeground(Color.WHITE);
		setHorizontalAlignment(SwingConstants.CENTER);
	}
	
}
