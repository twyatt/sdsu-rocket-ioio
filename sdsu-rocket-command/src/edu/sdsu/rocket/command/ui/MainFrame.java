package edu.sdsu.rocket.command.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
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
import javax.swing.border.BevelBorder;

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
	private JLabel lblX;
	private JLabel lblY;
	private JLabel lblZ;
	private GraphPanel accelerometerPanel;
	private GaugePanel loxPanel;
	private JSlider frequencySlider;
	private JLabel frequencyLabel;
	private JPanel sensorRequestRatePanel;
	private JLabel lblSensorFrequency;

	private BreakWirePanel breakWirePanel;
	private JPanel statusPanel;
	private JLabel lblBreakWire;
	private JPanel controlPanel;
	private JButton button;
	private JButton btnOpenLoxVent;
	private JButton btnAbort;
	private JButton btnCloseLoxVent;
	private JPanel bottomPanel;
	private JPanel lastMessagePanel;
	private JLabel lblLatency;
	private JLabel latencyLabel;

	public MainFrame() {
		controller = new RocketController(rocket).setListener(this);
		client.setPacketListener(controller).setListener(this);
		
		setSize(new Dimension(889, 628));
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
		lblInfo.setText("");
	}
	
	@Override
	public void onChange() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				lblInfo.setText(rocket.ident);
				
				breakWirePanel.setState(rocket.breakWire.state);
				
				float x = rocket.accelerometer.getX();
				float y = rocket.accelerometer.getY();
				float z = rocket.accelerometer.getZ();
				
//				float x = rocket.internalAccelerometer.getX();
//				float y = rocket.internalAccelerometer.getY();
//				float z = rocket.internalAccelerometer.getZ();
				
				lblX.setText(String.valueOf(x));
				lblY.setText(String.valueOf(y));
				lblZ.setText(String.valueOf(z));
				
				accelerometerPanel.point(x, Color.RED);
				accelerometerPanel.point(y, Color.GREEN);
				accelerometerPanel.point(z, Color.BLUE);
				accelerometerPanel.step();
				
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
		
		JToolBar toolBar = createToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		JPanel mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		
		lblInfo = new JLabel("");
		mainPanel.add(lblInfo);
		
		JPanel panel = new JPanel();
		mainPanel.add(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("184px"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("16px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(10dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		lblX = new JLabel("X");
		panel.add(lblX, "2, 2, center, top");
		
		lblY = new JLabel("Y");
		panel.add(lblY, "2, 4, center, default");
		
		lblZ = new JLabel("Z");
		panel.add(lblZ, "2, 6, center, default");
		
		accelerometerPanel = new GraphPanel(-10 /* min */, 10 /* max */);
		accelerometerPanel.setBorder(null);
		accelerometerPanel.setBackground(Color.WHITE);
		accelerometerPanel.setPreferredSize(new Dimension(300, 150));
		mainPanel.add(accelerometerPanel);
		
		loxPanel = new GaugePanel(0 /* min */, 500 /* max */, 10);
		loxPanel.setPreferredSize(new Dimension(200, 200));
		mainPanel.add(loxPanel);
		
		statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		mainPanel.add(statusPanel);
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
		mainPanel.add(controlPanel);
		
		button = new JButton("Ignite");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					controller.sendIgniteRequest();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(MainFrame.this, "Failed to send ignition request.\n" + e.getMessage());
				}
			}
		});
		controlPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("202px:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:max(30dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:max(35dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:45px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:max(40dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		btnOpenLoxVent = new JButton("Open LOX Vent Valve");
		btnOpenLoxVent.setPreferredSize(new Dimension(175, 50));
		controlPanel.add(btnOpenLoxVent, "2, 2, fill, fill");
		
		btnCloseLoxVent = new JButton("Close LOX Vent Valve");
		controlPanel.add(btnCloseLoxVent, "2, 4, fill, fill");
		controlPanel.add(button, "2, 6, fill, fill");
		
		btnAbort = new JButton("Abort");
		controlPanel.add(btnAbort, "2, 8, fill, fill");
		
		bottomPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottomPanel.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		
		lastMessagePanel = new JPanel();
		lastMessagePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		bottomPanel.add(lastMessagePanel);
		
		lblLatency = new JLabel("Latency");
		lastMessagePanel.add(lblLatency);
		
		latencyLabel = new JLabel("0 ms");
		lastMessagePanel.add(latencyLabel);
		
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
