package edu.sdsu.rocket.control;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.network.RemoteCommandController;
import edu.sdsu.rocket.control.objectives.FillTanksObjective;
import edu.sdsu.rocket.control.objectives.LaunchObjective;

public class MainActivity extends IOIOActivity {

	private Rocket rocket;
	
	private DeviceManager deviceManager;
	private ObjectiveController objectiveController;
	private RemoteCommandController remoteCommand;
	
	private TextView ioioStatusTextView;
	private TextView serverStatusTextView;
	private TextView connectionCountTextView;


	@Override
	protected IOIOLooper createIOIOLooper() {
		return deviceManager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setupUI();
		
		// http://developer.android.com/training/basics/location/locationmanager.html#TaskGetLocationManagerRef
//		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// FIXME prevent device sleep
		
		App.start();
		rocket = new Rocket();
		App.data = new DataLogger(rocket);
		
		deviceManager = new DeviceManager();
		setupDevices();
		setupObjectives(rocket);
		
		setupRemoteCommand();
	}

	private void setupObjectives(Rocket rocket) {
		objectiveController = new ObjectiveController(rocket);
		objectiveController.add(Network.FILL_TANKS_OBJECTIVE, new FillTanksObjective());
		objectiveController.add(Network.LAUNCH_OBJECTIVE, new LaunchObjective());
	}

	private void setupUI() {
		setContentView(R.layout.activity_main);
		
		ioioStatusTextView = (TextView) findViewById(R.id.ioio_status);
		serverStatusTextView = (TextView) findViewById(R.id.server_status);
		connectionCountTextView = (TextView) findViewById(R.id.server_connections);
	}

	private void setupRemoteCommand() {
		int tcpPort = Network.TCP_PORT;
		int udpPort = Network.UDP_PORT;
		
		remoteCommand = new RemoteCommandController(tcpPort, udpPort, objectiveController);
		remoteCommand.start();
		remoteCommand.server.addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				int count = remoteCommand.server.getConnections().length;
				updateTextView(connectionCountTextView, String.valueOf(count) + " connections");
				App.log.i(App.TAG, "Connection count: " + count);
			}
			@Override
			public void disconnected(Connection connection) {
				int count = remoteCommand.server.getConnections().length;
				updateTextView(connectionCountTextView, String.valueOf(count) + " connections");
			}
		});
		
		String ip = getIpAddr();
		updateTextView(serverStatusTextView, "tcp://" + ip + ":" + tcpPort + "\nudp://" + ip + ":" + udpPort);
	}

	private void setupDevices() {
//		deviceManager.add(rocket.ignitor);
//		deviceManager.add(rocket.pressure1);
//		deviceManager.add(rocket.pressure2);
		deviceManager.add(rocket.servoLOX);
//		deviceManager.add(rocket.barometer1, true /* spawn thread */);
//		deviceManager.add(rocket.barometer2, true /* spawn thread */);
//		deviceManager.add(rocket.imu, true /* spawn thread */);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		// http://developer.android.com/training/basics/location/locationmanager.html#TaskVerifyProvider
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!gpsEnabled) {
			// Build an alert dialog here that requests that the user enable
			// the location services, then when the user clicks the "OK" button,
			// call enableLocationSettings()
			App.log.e(App.TAG, "GPS disabled.");
		}
	}
//
//	private void enableLocationSettings() {
//		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//		startActivity(settingsIntent);
//	}

	/**
	 * http://stackoverflow.com/questions/7975473/detect-wifi-ip-address-on-android
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public String getIpAddr() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		
		String ipString = String.format(
			"%d.%d.%d.%d",
			(ip & 0xff),
			(ip >> 8 & 0xff),
			(ip >> 16 & 0xff),
			(ip >> 24 & 0xff)
		);
		
		return ipString;
	}
	
	private void updateTextView(final TextView textView, final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView.setText(text);
			}
		});
	}

}
