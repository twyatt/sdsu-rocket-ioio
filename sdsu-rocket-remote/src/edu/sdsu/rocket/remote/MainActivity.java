package edu.sdsu.rocket.remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.CommandRequest;
import edu.sdsu.rocket.Network.LogMessage;

public class MainActivity extends Activity {

	public static final int DISCOVER_HOST_TIMEOUT = 10000; // milliseconds
	public static final int CONNECT_TIMEOUT = 10000; // milliseconds
	
	private Client client = new Client();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setupUI();
		
		setupClient();
		connect();
	}
	
	private void connect() {
		int udpPort = Network.UDP_PORT;
		InetAddress address = client.discoverHost(udpPort, DISCOVER_HOST_TIMEOUT);
		
		if (address == null) {
			Log.e(App.TAG, "Failed to discover host.");
			hostInput();
		} else {
			connect(address);
		}
	}

	private void hostInput() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Remote Host");
		alert.setMessage("Enter host address of rocket control.");

		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(
			"Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					
					Editable value = input.getText();
					
					InetAddress inetAddress = null;
					try {
						inetAddress = InetAddress.getByName(value.toString());
					} catch (UnknownHostException e) {
						e.printStackTrace();
						inetAddress = null;
					}
					
					if (inetAddress == null) {
						hostInput();
					} else {
						connect(inetAddress);
					}
				}
			}
		);

		alert.setNegativeButton(
			"Cancel",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
					connect();
				}
			}
		);

		alert.show();
	}

	private void connect(InetAddress address) {
		try {
			client.connect(CONNECT_TIMEOUT, address, Network.TCP_PORT, Network.UDP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			onConnectFailed(address);
		}
	}

	private void onConnectFailed(final InetAddress address) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Connect Failed");
		alert.setMessage("Failed to connect to host: " + address.getHostName());
		
		alert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				connect(address);
			}
		});
		
		alert.setNegativeButton("Discover", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				connect();
			}
		});
		
		alert.show();
	}

	private void setupClient() {
		Network.register(client);
		client.start();
		
		client.addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				Log.i(App.TAG, "Connected to " + connection.getRemoteAddressTCP() + ".");
			}
			
			@Override
			public void received(Connection connection, Object object) {
//				System.out.println("received from " + connection.getID());
				
				if (object instanceof LogMessage) {
					LogMessage log = (LogMessage)object;
					
					if (log.level >= Network.LOG_LEVEL_ERROR) {
						Log.e(App.TAG, log.message);
					} else {
						Log.i(App.TAG, log.message);
					}
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				Log.i(App.TAG, "Disconnected from " + connection.getRemoteAddressTCP());
				
				try {
					System.out.println("Reconnecting ...");
					client.reconnect(CONNECT_TIMEOUT); // FIXME cannot be on connect thread
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		});
	}
	
	private void setupUI() {
		setContentView(R.layout.activity_main);
		
		final Button button = (Button) findViewById(R.id.button_launch);
		button.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				CommandRequest commandRequest = new CommandRequest();
				commandRequest.command = "launch";
				client.sendTCP(commandRequest);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
