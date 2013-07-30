package edu.sdsu.rocket.command.ui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class GaugePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5326082960871953104L;
	
	private float min;
	private float max;

	private float value;
	
	/**
	 * http://stackoverflow.com/questions/929103/convert-a-number-range-to-another-range-maintaining-ratio
	 */
	public static float linearConversion(float oldMin, float oldMax, float newMin, float newMax, float value) {
		return ((value - oldMin) / (oldMax - oldMin)) * (newMax - newMin) + newMin;
	}
	
	public GaugePanel(float min, float max) {
		this.min = min;
		this.max = max;
	}
	
	public void setValue(float value) {
		if (value < min) {
			value = min;
		}
		if (value > max) {
			value = max;
		}
		this.value = value;
		repaint();
	}
	
	private void drawBorder(Graphics g, Color color) {
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		int size = Math.min(getWidth(), getHeight());
		
		g.setColor(color);
		g.drawOval(centerX - size / 2, centerY - size / 2, size, size);
	}
	
	private void drawDial(Graphics g, Color color) {
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		
		float radians = linearConversion(min, max, 0, 1, value) * 2 * (float) Math.PI; // 0 to 2pi
		radians += (float) Math.PI / 2;
		int radius = Math.min(getWidth(), getHeight()) / 2;
		
		int x = Math.round(radius * (float) Math.cos(radians)) + centerX;
		if (x < 0 || x > getWidth())
			return;
		
		int y = Math.round(radius * (float) Math.sin(radians)) + centerY;
		if (y < 0 || y > getHeight())
			return;
		
		g.setColor(color);
		g.drawLine(centerX, centerY, x, y);
	}
	
	/*
	 * Overridden JPanel methods.
	 */
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBorder(g, Color.BLACK);
		drawDial(g, Color.BLUE);
	}

}
