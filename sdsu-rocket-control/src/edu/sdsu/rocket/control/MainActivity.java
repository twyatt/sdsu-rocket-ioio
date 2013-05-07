package edu.sdsu.rocket.control;

import java.io.IOException;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.widget.TextView;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.control.devices.DeviceThread;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.control.network.RemoteCommandController;
import edu.sdsu.rocket.control.objectives.FillTanksObjective;
import edu.sdsu.rocket.control.objectives.FlightObjective;
import edu.sdsu.rocket.control.objectives.LaunchObjective;

public class MainActivity extends IOIOActivity {

	private Rocket rocket;
	
	private DeviceManager deviceManager;
	private RemoteCommandController remoteCommand;
	
	private TextView serverStatusTextView;
	private TextView connectionCountTextView;

	private Thread objectiveThread;
	private PowerManager.WakeLock wakelock;

	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return deviceManager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		App.start();
		
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setupUI();
		
		// http://developer.android.com/training/basics/location/locationmanager.html#TaskGetLocationManagerRef
//		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// prevent device from sleeping
		PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, App.TAG);
		wakelock.acquire();
		
		App.rocket = new Rocket();
		rocket = App.rocket;
		
		App.data = new DataLogger(rocket);
		
		deviceManager = new DeviceManager(200 /* ioio thread sleep */);
		setupDevices();
		setupObjectives(rocket);
		
		setupRemoteCommand(App.objective);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
	protected void onDestroy() {
		objectiveThread.interrupt();
		wakelock.release();
		super.onDestroy();
	}

	private void setupObjectives(Rocket rocket) {
		App.objective = new ObjectiveController(rocket, 250 /* thread sleep in milliseconds */);
		App.objective.add(Network.FILL_TANKS_OBJECTIVE, new FillTanksObjective());
		App.objective.add(Network.LAUNCH_OBJECTIVE, new LaunchObjective());
		App.objective.add(Network.FLIGHT_OBJECTIVE, new FlightObjective());
		objectiveThread = new Thread(App.objective);
		
		App.log.i(App.TAG, "Starting objective thread.");
		objectiveThread.start();
	}

	private void setupUI() {
		setContentView(R.layout.activity_main);
		
		serverStatusTextView = (TextView) findViewById(R.id.server_status);
		connectionCountTextView = (TextView) findViewById(R.id.server_connections);
	}

	private void setupRemoteCommand(ObjectiveController objectiveController) {
		int tcpPort = Network.TCP_PORT;
		int udpPort = Network.UDP_PORT;
		
		remoteCommand = new RemoteCommandController(tcpPort, udpPort, objectiveController);
		try {
			remoteCommand.start();
		} catch (IOException e) {
			updateTextView(serverStatusTextView, "Failed to bind server.");
			e.printStackTrace();
			return;
		}
		
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
		deviceManager.add(rocket.ignitor);
		deviceManager.add(rocket.fuelValve);
		deviceManager.add(rocket.breakWire);
		
		deviceManager.add(
			new DeviceThread(rocket.tankPressureLOX)
				.setThreadSleep(500)
		);
		deviceManager.add(
			new DeviceThread(rocket.tankPressureEthanol)
				.setThreadSleep(500)
		);
		deviceManager.add(
			new DeviceThread(rocket.tankPressureEngine)
				.setThreadSleep(500)
		);
//		
		deviceManager.add(rocket.servoLOX);
		deviceManager.add(rocket.servoEthanol);
//		
//		deviceManager.add(
//			new DeviceThread(rocket.barometer1)
//				.setThreadSleep(200 /* milliseconds */)
//		);
//		deviceManager.add(
//			new DeviceThread(rocket.barometer2)
//				.setThreadSleep(200 /* milliseconds */)
//		);
//		
//		deviceManager.add(
//			new DeviceThread(rocket.imu)
//				.setThreadFrequency(8 /* Hz */)
//		);
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
