package edu.sdsu.rocket.command.ui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import edu.sdsu.rocket.command.io.TcpClient;
import edu.sdsu.rocket.command.io.TcpClient.TcpClientListener;
import edu.sdsu.rocket.command.models.Rocket;

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
	private JLabel lblInfo;
	private AccelerometerPanel accelerometerPanel;
	private GaugePanel loxPanel;
	private JSlider frequencySlider;
	private JLabel frequencyLabel;
	private JPanel sensorRequestRatePanel;
	private JLabel lblSensorFrequency;

	private BreakWirePanel breakWirePanel;
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

	public MainFrame() {
		controller = new RocketController(rocket).setListener(this);
		client.setPacketListener(controller).setListener(this);
		
		setSize(new Dimension(1024, 650));
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
		controller.stop();
//		lblInfo.setText("");
	}
	
	@Override
	public void onChange() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
//				lblInfo.setText(rocket.ident);
				
				breakWirePanel.setState(rocket.breakWire.state);
				
				float x = rocket.accelerometer.getX();
				float y = rocket.accelerometer.getY();
				float z = rocket.accelerometer.getZ();
				accelerometerPanel.updateWithValues(x, y, z);
				
//				float x = rocket.internalAccelerometer.getX();
//				float y = rocket.internalAccelerometer.getY();
//				float z = rocket.internalAccelerometer.getZ();
				
//				lblX.setText(String.valueOf(rocket.internalAccelerometer.getX()));
//				lblY.setText(String.valueOf(rocket.internalAccelerometer.getY()));
//				lblZ.setText(String.valueOf(rocket.internalAccelerometer.getZ()));
			}
		});
	}

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
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("29px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.insets = new Insets(0, 0, 5, 0);
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 0;
		leftPanel.add(statusPanel, gbc_statusPanel);
		
		lblBreakWire = new JLabel("Break Wire");
		lblBreakWire.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		statusPanel.add(lblBreakWire, "2, 2, right, default");
		
		breakWirePanel = new BreakWirePanel();
		breakWirePanel.setFont(new Font("Courier New", Font.BOLD, 18));
		breakWirePanel.setPreferredSize(new Dimension(140, 19));
		breakWirePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		statusPanel.add(breakWirePanel, "4, 2, left, fill");
		
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
				FormFactory.DEFAULT_ROWSPEC,}));
		
		openLOXButton = new JButton("Open LOX Vent");
		openLOXButton.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(openLOXButton, "2, 2, fill, fill");
		
		closeLOXVentButton = new JButton("Close LOX Vent");
		closeLOXVentButton.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(closeLOXVentButton, "2, 4, fill, fill");
		
		igniteButton = new JButton("Ignite");
		igniteButton.setFont(new Font("Dialog", Font.BOLD, 20));
		igniteButton.setForeground(Color.RED);
		igniteButton.setOpaque(true);
		controlPanel.add(igniteButton, "2, 6, fill, fill");
		
		abortButton = new JButton("Abort");
		abortButton.setFont(new Font("Dialog", Font.PLAIN, 20));
		controlPanel.add(abortButton, "2, 8, fill, fill");
		
		/*
		 * Right Split Pane Panel
		 */
		
		rightPanel = new JPanel();
		splitPane.setRightComponent(rightPanel);
		
		accelerometerPanel = new AccelerometerPanel(-10 /* min */, 10 /* max */);
		accelerometerPanel.setBorder(new TitledBorder(null, "Accelerometer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		accelerometerPanel.setPreferredSize(new Dimension(300, 200));
		rightPanel.add(accelerometerPanel);
		
		loxPanel = new GaugePanel(0 /* min */, 500 /* max */, 10);
		loxPanel.setPreferredSize(new Dimension(200, 200));
		rightPanel.add(loxPanel);
		
		/*
		 * Bottom Panel
		 */
		
		bottomPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottomPanel.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		
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
