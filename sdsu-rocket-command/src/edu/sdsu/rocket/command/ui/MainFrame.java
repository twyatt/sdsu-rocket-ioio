package edu.sdsu.rocket.command.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import edu.sdsu.rocket.command.controllers.RocketController;
import edu.sdsu.rocket.command.controllers.RocketController.RocketControllerListener;
import edu.sdsu.rocket.command.controllers.RocketController.ValveException;
import edu.sdsu.rocket.command.io.TcpClient;
import edu.sdsu.rocket.command.io.TcpClient.TcpClientListener;
import edu.sdsu.rocket.command.models.BreakWire;
import edu.sdsu.rocket.command.models.Ignitor;
import edu.sdsu.rocket.command.models.Rocket;
import edu.sdsu.rocket.command.models.Rocket.Valve;
import edu.sdsu.rocket.command.models.Rocket.ValveAction;

public class MainFrame extends JFrame implements RocketControllerListener, TcpClientListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1802254547097970234L;
	
	private static final String DISCONNECT_TEXT = "Disconnect";
	private static final String CONNECT_TEXT    = "Connect";
	
	final private Rocket rocket = new Rocket();
	
	final private TcpClient client = new TcpClient();
	final private RocketController controller;
	
	private JTextField hostTextField;
	private JTextField portTextField;
	private JButton connectButton;
	private AccelerometerPanel accelerometerPanel;
	private LabeledGaugePanel loxPanel;
	private LabeledGaugePanel ethanolPanel;
	private LabeledGaugePanel enginePanel;
	private JSlider frequencySlider;
	private JLabel frequencyLabel;
	private JPanel sensorRequestRatePanel;
	private JLabel lblSensorFrequency;
	private BreakWireLabel breakWireLabel;
	private JPanel statusPanel;
	private JLabel lblBreakWire;
	private JPanel bottomPanel;
	private JPanel latencyPanel;
	private JLabel lblLatency;
	private JLabel latencyLabel;
	private JSplitPane splitPane;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel controlPanel;
	private JButton openLOXButton;
	private JButton closeLOXVentButton;
	private JButton igniteButton;
	private JButton abortButton;
	private JButton btnLaunch;
	private JLabel lblIgnitor;
	private IgnitorLabel ignitorLabel;
	private JLabel lblLoxPressure;
	private JLabel lblEthanolPressure;
	private JButton btnOpenEthanolVent;
	private JButton btnCloseEthanolVent;
	private JButton resetIOIOButton;
	private Component horizontalStrut;
	private JPanel identPanel;
	private JLabel identLabel;
	private PressureLabel loxPressureLabel;
	private PressureLabel ethanolPressureLabel;

	private LabeledGaugePanel loxTemperaturePanel;

	private LabeledGaugePanel barometerPanel;

	private LabeledGaugePanel ignitorTemperaturePanel;

	private AccelerometerPanel internalAccelerometerPanel;

	public MainFrame() {
		controller = new RocketController(rocket).setListener(this);
		client.setPacketListener(controller).setListener(this);
		
		setSize(new Dimension(1280, 650));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setupUI();
	}
	
	private void onConnect() {
		if (CONNECT_TEXT.equals(connectButton.getText())) {
			InetAddress address;
			try {
				address = InetAddress.getByName(hostTextField.getText());
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(this, "Unknown Host.\n" + e.getMessage());
				return;
			}
			
			int port = Integer.valueOf(portTextField.getText());
			try {
				client.connect(address, port);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Failed to connect to " + address + ":" + port + ".\n" + e.getMessage());
				return;
			}
			
			connectButton.setText(DISCONNECT_TEXT);
		} else {
			client.disconnect();
			connectButton.setText(CONNECT_TEXT);
		}
	}
	
	@Override
	public void onConnected() {
		resetIOIOButton.setEnabled(true);
		controller.setWriter(client.getOutputStream());
		try {
			controller.sendIdentRequest();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Failed to send ident request.\n" + e.getMessage());
			e.printStackTrace();
		}
		controller.start();
	}
	
	@Override
	public void onDisconnected() {
		resetIOIOButton.setEnabled(false);
		controller.stop();
		
		rocket.breakWire.state = BreakWire.State.UNKNOWN;
		rocket.ignitor.state = Ignitor.State.UNKNOWN;
		onChange();
		
		ethanolPressureLabel.setPressure(Float.NaN);
		loxPressureLabel.setPressure(Float.NaN);
		identLabel.setText(" ");
	}
	
	protected void sendValveRequest(Valve valve, ValveAction action) {
		try {
			controller.sendValveRequest(valve, action);
		} catch (ValveException e) {
			JOptionPane.showMessageDialog(MainFrame.this, "Unable to perform valve request.\n" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.this, "Failed to perform valve request.\n" + e.getMessage());
		}
	}
	
	/*
	 * RocketControllerListener interface methods
	 */
	
	@Override
	public void onIdent() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				identLabel.setText(rocket.ident);
			}
		});
	}
	
	@Override
	public void onChange() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				float loxPressure = rocket.pressureLOX.getPressure();
				float ethPressure = rocket.pressureEthanol.getPressure();
				
				loxPressureLabel.setPressure(loxPressure);
				ethanolPressureLabel.setPressure(ethPressure);
				ignitorLabel.setState(rocket.ignitor.state);
				breakWireLabel.setState(rocket.breakWire.state);
				
				loxPanel.setValue(loxPressure);
				ethanolPanel.setValue(ethPressure);
				enginePanel.setValue(rocket.pressureEngine.getPressure());
				barometerPanel.setValue(rocket.barometer.getPressure());
				
				// TODO convert temps to F
				
				loxTemperaturePanel.setValue(rocket.loxTemperature.getTemperature());
				
				accelerometerPanel.updateWithValues(
					rocket.accelerometer.getX(),
					rocket.accelerometer.getY(),
					rocket.accelerometer.getZ()
				);
				
				internalAccelerometerPanel.updateWithValues(
					rocket.internalAccelerometer.getX(),
					rocket.internalAccelerometer.getY(),
					rocket.internalAccelerometer.getZ()
				);
				
				ignitorTemperaturePanel.setValue(rocket.ignitorTemperature);
			}
		});
	}
	
	/*
	 * UI
	 */

	private void setupUI() {
		setContentPane(createContentPane());
	}
	
	private JPanel createContentPane() {
		JPanel contentPane = new JPanel();
		contentPane.setBorder(null);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		/*
		 * Toolbar
		 */
		
		JToolBar toolBar = createToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		/*
		 * Main Panel
		 */
		
		splitPane = new JSplitPane();
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		/*
		 * Left Split Pane Panel
		 */
		
		leftPanel = new JPanel();
		leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPane.setLeftComponent(leftPanel);
		GridBagLayout gbl_leftPanel = new GridBagLayout();
		gbl_leftPanel.columnWidths = new int[]{265, 0};
		gbl_leftPanel.rowHeights = new int[]{20, 10, 0};
		gbl_leftPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_leftPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		leftPanel.setLayout(gbl_leftPanel);
		
		statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		statusPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.GROWING_BUTTON_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("29px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_statusPanel.insets = new Insets(0, 0, 5, 0);
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 0;
		leftPanel.add(statusPanel, gbc_statusPanel);
		
		lblEthanolPressure = new JLabel("Ethanol Pressure");
		lblEthanolPressure.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		statusPanel.add(lblEthanolPressure, "2, 2, right, default");
		
		ethanolPressureLabel = new PressureLabel(300, 400);
		ethanolPressureLabel.setFont(new Font("Courier New", Font.BOLD, 18));
		ethanolPressureLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		statusPanel.add(ethanolPressureLabel, "4, 2, fill, fill");
		
		lblLoxPressure = new JLabel("LOX Pressure");
		lblLoxPressure.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		statusPanel.add(lblLoxPressure, "2, 4, right, default");
		
		loxPressureLabel = new PressureLabel(300, 400);
		loxPressureLabel.setFont(new Font("Courier New", Font.BOLD, 18));
		loxPressureLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		statusPanel.add(loxPressureLabel, "4, 4, fill, fill");
		
		lblIgnitor = new JLabel("Ignitor");
		lblIgnitor.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		statusPanel.add(lblIgnitor, "2, 6, right, default");
		
		ignitorLabel = new IgnitorLabel();
		ignitorLabel.setFont(new Font("Courier New", Font.BOLD, 18));
		ignitorLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		statusPanel.add(ignitorLabel, "4, 6, fill, fill");
		
		lblBreakWire = new JLabel("Break Wire");
		lblBreakWire.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		statusPanel.add(lblBreakWire, "2, 8, right, default");
		
		breakWireLabel = new BreakWireLabel();
		breakWireLabel.setFont(new Font("Courier New", Font.BOLD, 18));
		breakWireLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		statusPanel.add(breakWireLabel, "4, 8, fill, fill");
		
		controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder(null, "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_controlPanel.gridx = 0;
		gbc_controlPanel.gridy = 1;
		leftPanel.add(controlPanel, gbc_controlPanel);
		controlPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("117px:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		btnOpenEthanolVent = new JButton("Open Ethanol Vent");
		btnOpenEthanolVent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendValveRequest(Rocket.Valve.ETHANOL, Rocket.ValveAction.OPEN);
			}
		});
		btnOpenEthanolVent.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(btnOpenEthanolVent, "2, 2, fill, fill");
		
		btnCloseEthanolVent = new JButton("Close Ethanol Vent");
		btnCloseEthanolVent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendValveRequest(Rocket.Valve.ETHANOL, Rocket.ValveAction.CLOSE);
			}
		});
		btnCloseEthanolVent.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(btnCloseEthanolVent, "2, 4, fill, fill");
		
		openLOXButton = new JButton("Open LOX Vent");
		openLOXButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendValveRequest(Rocket.Valve.LOX, Rocket.ValveAction.OPEN);
			}
		});
		openLOXButton.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(openLOXButton, "2, 6, fill, fill");
		
		closeLOXVentButton = new JButton("Close LOX Vent");
		closeLOXVentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendValveRequest(Rocket.Valve.LOX, Rocket.ValveAction.CLOSE);
			}
		});
		closeLOXVentButton.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(closeLOXVentButton, "2, 8, fill, fill");
		
		igniteButton = new JButton("Ignite");
		igniteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					controller.sendIgniteRequest();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(MainFrame.this, "Failed to send ignite request.\n" + e.getMessage());
				}
			}
		});
		igniteButton.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(igniteButton, "2, 10, fill, fill");
		
		btnLaunch = new JButton("Launch");
		btnLaunch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					frequencySlider.setValue(0);
					controller.sendLaunchRequest();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(MainFrame.this, "Failed to send launch request.\n" + e.getMessage());
				}
			}
		});
		btnLaunch.setForeground(Color.RED);
		btnLaunch.setFont(new Font("Dialog", Font.BOLD, 20));
		controlPanel.add(btnLaunch, "2, 12, fill, fill");
		
		abortButton = new JButton("Abort");
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					controller.sendAbortRequest();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(MainFrame.this, "Failed to send abort request.\n" + e.getMessage());
				}
			}
		});
		abortButton.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(abortButton, "2, 14, fill, fill");
		
		/*
		 * Right Split Pane Panel
		 */
		
		rightPanel = new JPanel();
		splitPane.setRightComponent(rightPanel);
		
		loxPanel = new LabeledGaugePanel(0 /* min */, 500 /* max */, 10, "PSI");
		loxPanel.setBorder(new TitledBorder(null, "LOX Pressure", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		loxPanel.setPreferredSize(new Dimension(210, 255));
		rightPanel.add(loxPanel);
		
		ethanolPanel = new LabeledGaugePanel(0 /* min */, 500 /* max */, 10, "PSI");
		ethanolPanel.setBorder(new TitledBorder(null, "Ethanol Pressure", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ethanolPanel.setPreferredSize(new Dimension(210, 255));
		rightPanel.add(ethanolPanel);
		
		enginePanel = new LabeledGaugePanel(0 /* min */, 500 /* max */, 10, "PSI");
		enginePanel.setBorder(new TitledBorder(null, "Engine Pressure", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		enginePanel.setPreferredSize(new Dimension(210, 255));
		rightPanel.add(enginePanel);
		
		loxTemperaturePanel = new LabeledGaugePanel(-200 /* min */, 100 /* max */, 10, "C");
		loxTemperaturePanel.setBorder(new TitledBorder(null, "LOX Temperature", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		loxTemperaturePanel.setPreferredSize(new Dimension(210, 255));
		rightPanel.add(loxTemperaturePanel);
		
		ignitorTemperaturePanel = new LabeledGaugePanel(0 /* min */, 500 /* max */, 10, "C");
		ignitorTemperaturePanel.setBorder(new TitledBorder(null, "Ignitor Temperature", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ignitorTemperaturePanel.setPreferredSize(new Dimension(210, 255));
		rightPanel.add(ignitorTemperaturePanel);
		
		barometerPanel = new LabeledGaugePanel(900 /* min */, 1100 /* max */, 10, "mbar");
		barometerPanel.setBorder(new TitledBorder(null, "Barometer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		barometerPanel.setPreferredSize(new Dimension(210, 255));
		rightPanel.add(barometerPanel);
		
		accelerometerPanel = new AccelerometerPanel(-10 /* min */, 10 /* max */);
		accelerometerPanel.setBorder(new TitledBorder(null, "Accelerometer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		accelerometerPanel.setPreferredSize(new Dimension(250, 200));
		rightPanel.add(accelerometerPanel);
		
		internalAccelerometerPanel = new AccelerometerPanel(-10 /* min */, 10 /* max */);
		internalAccelerometerPanel.setBorder(new TitledBorder(null, "Phone Accel.", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		internalAccelerometerPanel.setPreferredSize(new Dimension(250, 200));
		rightPanel.add(internalAccelerometerPanel);
		
		/*
		 * Bottom Panel
		 */
		
		bottomPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottomPanel.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		
		identPanel = new JPanel();
		identPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		bottomPanel.add(identPanel);
		
		identLabel = new JLabel(" ");
		identPanel.add(identLabel);
		
		latencyPanel = new JPanel();
		latencyPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		bottomPanel.add(latencyPanel);
		
		lblLatency = new JLabel("Latency");
		latencyPanel.add(lblLatency);
		
		latencyLabel = new JLabel("0 ms");
		latencyPanel.add(latencyLabel);
		
		return contentPane;
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		
		JPanel connectionPanel = new JPanel();
		connectionPanel.setBorder(null);
		toolBar.add(connectionPanel);
		connectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel lblHost = new JLabel("Host");
		connectionPanel.add(lblHost);
		
		hostTextField = new JTextField();
		hostTextField.setText("10.0.1.1");
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
		
		resetIOIOButton = new JButton("Reset IOIO");
		resetIOIOButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					controller.sendIOIOResetRequest();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(MainFrame.this, "Failed to send IOIO reset request.\n" + e.getMessage());
				}
			}
		});
		
		horizontalStrut = Box.createHorizontalStrut(20);
		connectionPanel.add(horizontalStrut);
		resetIOIOButton.setEnabled(false);
		connectionPanel.add(resetIOIOButton);
		
		sensorRequestRatePanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) sensorRequestRatePanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		toolBar.add(sensorRequestRatePanel);
		
		lblSensorFrequency = new JLabel("Sensor Frequency");
		sensorRequestRatePanel.add(lblSensorFrequency);
		
		frequencySlider = new JSlider();
		sensorRequestRatePanel.add(frequencySlider);
		frequencySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				final float frequency = ((JSlider) event.getSource()).getValue();
				controller.setFrequency(frequency);
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frequencyLabel.setText(String.valueOf(frequency) + " Hz");
					}
				});
			}
		});
		frequencySlider.setValue(1);
		
		frequencyLabel = new JLabel("1 Hz");
		sensorRequestRatePanel.add(frequencyLabel);
		
		return toolBar;
	}

}
