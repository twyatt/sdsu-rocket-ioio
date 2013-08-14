package edu.sdsu.rocket.command.ui;

import java.awt.BorderLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class LabeledGaugePanel extends JPanel {

	private static final DecimalFormat DISPLAY = new DecimalFormat("#.###");
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5479341005593117239L;
	
	private GaugePanel gaugePanel;
	private JLabel valueLabel;

	private String units;
	
	public LabeledGaugePanel(float min, float max, float ticks, String units) {
		this.units = units;
		setupUI(min, max, ticks);
	}
	
	public void setValue(float value) {
		gaugePanel.setValue(value);
		valueLabel.setText(DISPLAY.format(value) + " " + units);
	}

	private void setupUI(float min, float max, float ticks) {
		setLayout(new BorderLayout(0, 0));
		gaugePanel = new GaugePanel(min, max, ticks);
		add(gaugePanel);
		
		JPanel valuesPanel = new JPanel();
		valuesPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(valuesPanel, BorderLayout.SOUTH);
		
		valueLabel = new JLabel(" ");
		valuesPanel.add(valueLabel);
	}

}
