package edu.sdsu.rocket.command.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import edu.sdsu.rocket.command.models.BreakWire.State;

public class BreakWirePanel extends JLabel {
	
	public static final Color UNKNOWN_COLOR    = new Color(128,   0, 0); // dark red
	public static final Color BROKEN_COLOR     = new Color(  0, 128, 0); // dark green
	public static final Color NOT_BROKEN_COLOR = new Color(128,   0, 0); // dark red
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6934879593075438740L;
	
	private State state = State.UNKNOWN;
	
	public BreakWirePanel() {
		super("");
		
		setOpaque(true);
		setupUI();
		refresh();
	}

	public void setState(State state) {
		this.state = state;
		refresh();
	}
	
	public State getState() {
		return state;
	}
	
	public void refresh() {
		String text = "";
		
		switch (this.state) {
		case BROKEN:
			text = "Broken";
			setBackground(BROKEN_COLOR);
			break;
		case NOT_BROKEN:
			text = "NOT Broken";
			setBackground(NOT_BROKEN_COLOR);
			break;
		default:
			text = "Unknown";
			setBackground(UNKNOWN_COLOR);
		}
		
		setText(text);
	}
	
	private void setupUI() {
		setForeground(Color.WHITE);
		setHorizontalAlignment(SwingConstants.CENTER);
	}

}
