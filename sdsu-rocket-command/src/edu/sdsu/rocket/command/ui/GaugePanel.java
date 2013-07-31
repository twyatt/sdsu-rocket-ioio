package edu.sdsu.rocket.command.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import edu.sdsu.rocket.command.helpers.MathHelper;

public class GaugePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5326082960871953104L;

	private static final float TICK_MULTIPLIER = 0.8f;
	
	/**
	 * Padding around edges.
	 */
	private static final int PADDING = 30; // pixels
	
	private float min;
	private float max;
	private float ticks;

	private float value;
	
	public GaugePanel(float min, float max, float ticks) {
		this.min = min;
		this.max = max;
		this.ticks = ticks;
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
	
	public Dimension getGaugeSize() {
		return new Dimension(getWidth() - PADDING * 2, getHeight() - PADDING * 2);
	}
	
	public Point getCenter() {
		return new Point(getWidth() / 2, getHeight() / 2);
	}
	
	private void drawGaugeBorder(Graphics g) {
		Dimension size = getGaugeSize();
		g.drawOval(PADDING, PADDING, size.width, size.height);
	}
	
	private void drawNeedle(Graphics g) {
		float angle = convertValueToAngle(value);
		
		Point center = getCenter();
		center.translate(0, 1);
		drawLineFromEdge(g, angle, center);
		
		center.translate(1, -1);
		drawLineFromEdge(g, angle, center);
		
		center.translate(-1, -1);
		drawLineFromEdge(g, angle, center);
		
		center.translate(-1, 1);
		drawLineFromEdge(g, angle, center);
	}
	
	private void drawTicks(Graphics g) {
		float interval = (max - min) / ticks;
		
		for (float value = min; value < max; value += interval) {
			drawTickLine(g, convertValueToAngle(value));
			drawTickLabel(g, convertValueToAngle(value), value);
		}
	}
	
	private void drawTickLine(Graphics g, float angle) {
		Dimension size = getGaugeSize();
		Point center = getCenter();
		Point point = MathHelper.getPointOnOval(size.width / 2f * TICK_MULTIPLIER, size.height / 2f * TICK_MULTIPLIER, angle);
		point.translate(center.x, center.y);
		drawLineFromEdge(g, angle, point);
	}
	
	private void drawTickLabel(Graphics g, float angle, float value) {
		Dimension size = getGaugeSize();
		Point center = getCenter();
		Point point = MathHelper.getPointOnOval(size.width / 2f * 1.25f, size.height / 2f * 1.25f, angle);
		point.translate(center.x, center.y);
		
		String label = String.valueOf(value);
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(label, g);
		point.translate((int) -bounds.getWidth() / 2, (int) bounds.getHeight() / 2);
		
		g.drawString(label, point.x, point.y);
	}
	
	private float convertValueToAngle(float value) {
		float angle = MathHelper.linearConversion(min, max, 0, 1, value) * 2 * (float) Math.PI; // 0 to 2pi
		angle += (float) Math.PI / 2;
		return angle;
	}
	
	private void drawLineFromEdge(Graphics g, float angle, Point to) {
		float a = getWidth()  / 2f - PADDING;
		float b = getHeight() / 2f - PADDING;
		
		Point center = getCenter();
		
		Point from = new Point(
			(int) Math.round(a * Math.cos(angle)) + center.x,
			(int) Math.round(b * Math.sin(angle)) + center.y
		);
		
		g.drawLine(from.x, from.y, to.x, to.y);
	}
	
	/*
	 * Overridden JPanel methods.
	 */
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.BLACK);
		drawGaugeBorder(g);
		
		g.setFont(new Font("Arial", Font.PLAIN, 10));
		g.setColor(Color.BLACK);
		drawTicks(g);
		
		g.setColor(Color.BLUE);
		drawNeedle(g);
	}

}
