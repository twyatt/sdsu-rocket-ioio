package edu.sdsu.rocket.command.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1802254547097970234L;
	
	private static final String DISCONNECT_TEXT = "Disconnect";
	private static final String CONNECT_TEXT    = "Connect";
	
	private Socket client;
	
	private JTextField hostTextField;
	private JTextField portTextField;
	private JButton connectButton;

	public MainFrame() {
		setSize(new Dimension(741, 506));
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setupUI();
	}
	
	private void onConnect() {
		if (CONNECT_TEXT.equals(connectButton.getText())) {
			InetAddress address;
			try {
				address = InetAddress.getByName(hostTextField.getText());
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(this, "Unknown Host\n" + e.getMessage());
				return;
			}
			
			try {
				int port = Integer.valueOf(portTextField.getText());
				client = new Socket(address, port);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Failed to create network socket.");
				return;
			}
			
			connectButton.setText(DISCONNECT_TEXT);
		} else {
			if (client != null && client.isConnected()) {
				try {
					client.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, "Failed to close network socket.");
				}
			}
			
			connectButton.setText(CONNECT_TEXT);
		}
	}

	private void setupUI() {
		setContentPane(createContentPane());
	}
	
	private JPanel createContentPane() {
		JPanel contentPane = new JPanel();
		contentPane.setBorder(null);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		JPanel connectionPanel = new JPanel();
		connectionPanel.setBorder(null);
		toolBar.add(connectionPanel);
		connectionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JLabel lblHost = new JLabel("Host");
		connectionPanel.add(lblHost);
		
		hostTextField = new JTextField();
		lblHost.setLabelFor(hostTextField);
		connectionPanel.add(hostTextField);
		hostTextField.setColumns(10);
		
		JLabel lblPort = new JLabel("Port");
		connectionPanel.add(lblPort);
		
		portTextField = new JTextField();
		portTextField.setText("23");
		connectionPanel.add(portTextField);
		portTextField.setColumns(3);
		
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				onConnect();
			}
		});
		connectionPanel.add(connectButton);
		
		JPanel objectivePanel = new JPanel();
		objectivePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.add(objectivePanel, BorderLayout.WEST);
		
		JTabbedPane objectivePane = new JTabbedPane(JTabbedPane.TOP);
		objectivePane.setPreferredSize(new Dimension(250, 400));
		objectivePanel.add(objectivePane);
		
		JPanel fillTanksPanel = new JPanel();
		objectivePane.addTab("Fill Tanks", null, fillTanksPanel, null);
		fillTanksPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JButton setFillTanksObjectiveButton = new JButton("Set Objective");
		setFillTanksObjectiveButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		fillTanksPanel.add(setFillTanksObjectiveButton);
		
		JButton openLOXButton = new JButton("Open LOX");
		openLOXButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		openLOXButton.setEnabled(false);
		fillTanksPanel.add(openLOXButton);
		
		JButton closeLOXButton = new JButton("Close LOX");
		closeLOXButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		closeLOXButton.setEnabled(false);
		fillTanksPanel.add(closeLOXButton);
		
		JButton cycleLOXButton = new JButton("Cycle LOX");
		cycleLOXButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		cycleLOXButton.setEnabled(false);
		fillTanksPanel.add(cycleLOXButton);
		
		JButton openEthanolButton = new JButton("Open Ethanol");
		openEthanolButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		openEthanolButton.setEnabled(false);
		fillTanksPanel.add(openEthanolButton);
		
		JButton closeEthanolButton = new JButton("Close Ethanol");
		closeEthanolButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		closeEthanolButton.setEnabled(false);
		fillTanksPanel.add(closeEthanolButton);
		
		JButton cycleEthanolButton = new JButton("Cycle Ethanol");
		cycleEthanolButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		cycleEthanolButton.setEnabled(false);
		fillTanksPanel.add(cycleEthanolButton);
		
		JPanel launchPanel = new JPanel();
		objectivePane.addTab("Launch", null, launchPanel, null);
		launchPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JButton setLaunchObjectiveButton = new JButton("Set Objective");
		setLaunchObjectiveButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		launchPanel.add(setLaunchObjectiveButton);
		
		JButton launchButton = new JButton("Launch");
		launchButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		launchButton.setEnabled(false);
		launchPanel.add(launchButton);
		
		JButton abortButton = new JButton("Abort");
		abortButton.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
		launchPanel.add(abortButton);
		
		JPanel sensorPanel = new JPanel();
		sensorPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPane.add(sensorPanel, BorderLayout.CENTER);
		sensorPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblLox = new JLabel("LOX:");
		lblLox.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
		sensorPanel.add(lblLox, "2, 2, right, default");
		
		JLabel LOXLabel = new JLabel("?");
		lblLox.setLabelFor(LOXLabel);
		LOXLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
		sensorPanel.add(LOXLabel, "4, 2");
		
		JLabel lblEthanol = new JLabel("Ethanol:");
		lblEthanol.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
		sensorPanel.add(lblEthanol, "2, 4, right, default");
		
		JLabel ethanolLabel = new JLabel("?");
		lblEthanol.setLabelFor(ethanolLabel);
		ethanolLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
		sensorPanel.add(ethanolLabel, "4, 4");
		
		JLabel lblEngine = new JLabel("Engine:");
		lblEngine.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
		sensorPanel.add(lblEngine, "2, 6, right, default");
		
		JLabel engineLabel = new JLabel("?");
		lblEngine.setLabelFor(engineLabel);
		engineLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
		sensorPanel.add(engineLabel, "4, 6");
		
		JLabel lblBarometer = new JLabel("Barometer:");
		lblBarometer.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
		sensorPanel.add(lblBarometer, "2, 8, right, default");
		
		JLabel barometerLabel = new JLabel("?");
		lblBarometer.setLabelFor(barometerLabel);
		barometerLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
		sensorPanel.add(barometerLabel, "4, 8");
		
		return contentPane;
	}
	
}
