package edu.sdsu.rocket.command.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import edu.sdsu.rocket.command.controllers.RocketController;
import edu.sdsu.rocket.command.controllers.RocketController.RocketControllerListener;
import edu.sdsu.rocket.command.io.TcpClient;
import edu.sdsu.rocket.command.io.TcpClient.TcpClientListener;
import edu.sdsu.rocket.command.models.Rocket;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

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

	public MainFrame() {
		controller = new RocketController(rocket).setListener(this);
		client.setPacketListener(controller).setListener(this);
		
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
				lblX.setText(String.valueOf(rocket.accelerometer.getX()));
				lblY.setText(String.valueOf(rocket.accelerometer.getY()));
				lblZ.setText(String.valueOf(rocket.accelerometer.getZ()));
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
		
		return contentPane;
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		
		JPanel connectionPanel = new JPanel();
		connectionPanel.setBorder(null);
		toolBar.add(connectionPanel);
		connectionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JLabel lblHost = new JLabel("Host");
		connectionPanel.add(lblHost);
		
		hostTextField = new JTextField();
		hostTextField.setText("192.168.5.5");
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
		
		return toolBar;
	}

}
