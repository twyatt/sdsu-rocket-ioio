package edu.sdsu.rocket.command.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class GraphPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4766987226939633278L;
	
	private BufferedImage bufferedImage;

	private final float min;
	private final float max;
	
	/**
	 * http://stackoverflow.com/questions/929103/convert-a-number-range-to-another-range-maintaining-ratio
	 */
	public static float linearConversion(float oldMin, float oldMax, float newMin, float newMax, float value) {
		return ((value - oldMin) / (oldMax - oldMin)) * (newMax - newMin) + newMin;
	}
	
	public GraphPanel(float min, float max) {
		this.min = min;
		this.max = max;
	}
	
	public void point(float y, Color color) {
		int imageY = toImageY(y);
		if (imageY < 0 || imageY > bufferedImage.getHeight() - 1)
			return; // out of bounds
		
		bufferedImage.setRGB(bufferedImage.getWidth() - 1, imageY, color.getRGB());
		repaint();
	}
	
	public void step() {
		step(1);
	}
	
	public void step(int x) {
		Graphics g = bufferedImage.getGraphics();
		g.drawImage(bufferedImage, -x, 0, null);
		g.clearRect(bufferedImage.getWidth() - x, 0, x, bufferedImage.getHeight());
		repaint();
	}
	
	private int toImageY(float y) {
		float pos = linearConversion(min, max, 0, bufferedImage.getHeight(), y);
		return Math.round(bufferedImage.getHeight() - pos);
	}
	
	private void setup() {
		bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	}
	
	private void drawCoordinates(Graphics g, Color color) {
		Color originalColor = g.getColor();
		
		g.setColor(color);
		g.drawLine(
			0, getHeight() / 2,
			getWidth(), getHeight() / 2
		);
		g.drawLine(
			0, 0,
			0, getHeight()
		);
		
		g.drawString(String.valueOf(max), 3, g.getFontMetrics().getHeight());
		g.drawString(String.valueOf(min), 3, getHeight() - 3);
		
		g.setColor(originalColor);
	}
	
	/*
	 * Overridden JPanel methods.
	 */
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (bufferedImage == null) {
			setup();
		}
		
		if (bufferedImage.getWidth() != getWidth() || bufferedImage.getHeight() != getHeight()) { // needs scaling
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(bufferedImage, 0, 0, getWidth(), getHeight(), getBackground(), null /* observer */);
		} else {
			g.drawImage(bufferedImage, 0, 0, getBackground(), null /* observer */);
		}
		
		drawCoordinates(g, Color.WHITE);
	}

}
