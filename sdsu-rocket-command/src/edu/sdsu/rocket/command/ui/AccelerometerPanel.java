package edu.sdsu.rocket.command.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AccelerometerPanel extends JPanel {

	private static final DecimalFormat DISPLAY = new DecimalFormat("#.#####");
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8090649155449467756L;
	
	private GraphPanel graphPanel;
	private JLabel xLabel;
	private JLabel yLabel;
	private JLabel zLabel;
	
	public AccelerometerPanel(float min, float max) {
		setupUI(min, max);
	}
	
	public void updateWithValues(float x, float y, float z) {
		xLabel.setText("X: " + DISPLAY.format(x));
		yLabel.setText("Y: " + DISPLAY.format(y));
		zLabel.setText("Z: " + DISPLAY.format(z));
		
		graphPanel.point(x, Color.RED);
		graphPanel.point(y, Color.GREEN);
		graphPanel.point(z, Color.CYAN);
		graphPanel.step();
	}

	private void setupUI(float min, float max) {
		setLayout(new BorderLayout(0, 0));
		graphPanel = new GraphPanel(min, max);
		add(graphPanel);
		
		JPanel valuesPanel = new JPanel();
		valuesPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(valuesPanel, BorderLayout.SOUTH);
		valuesPanel.setLayout(new GridLayout(1, 0, 0, 0));
		
		xLabel = new JLabel("X: ");
		valuesPanel.add(xLabel);
		
		yLabel = new JLabel("Y: ");
		valuesPanel.add(yLabel);
		
		zLabel = new JLabel("Z: ");
		valuesPanel.add(zLabel);
	}

}
