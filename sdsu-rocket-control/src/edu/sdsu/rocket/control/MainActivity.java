package edu.sdsu.rocket.control;

import ioio.lib.api.IOIO;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.widget.TextView;
import edu.sdsu.rocket.control.controllers.PacketController;
import edu.sdsu.rocket.control.controllers.RocketController;
import edu.sdsu.rocket.control.logging.AndroidLog;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.io.DatagramOutputStream;
import edu.sdsu.rocket.io.PacketMultiplexer;
import edu.sdsu.rocket.logging.LogMultiplexer;
import edu.sdsu.rocket.logging.StreamLog;

public class MainActivity extends IOIOActivity {

	private DeviceManager deviceManager;
	private PacketController command;
	
	private PowerManager.WakeLock wakelock;
	
	private TextView ioioStatusTextView;
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return deviceManager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		App.start();
		
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		disableDeviceSleep();
		
		setupUI();
//		setupLogging();
		setupDeviceManager();
		
		// http://developer.android.com/training/basics/location/locationmanager.html#TaskGetLocationManagerRef
//		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		Rocket rocket = new Rocket();
		App.rocketController = new RocketController(rocket);
		App.data = new DataLogger(rocket);
		
		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		App.rocketController.setup(deviceManager, sensorManager);
//		App.rocketController.start(); // TODO uncomment?
		
		setupPacketController(rocket);
	}

	private void disableDeviceSleep() {
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, App.TAG);
		wakelock.acquire();
	}

	private void setupPacketController(Rocket rocket) {
		PacketMultiplexer messenger = new PacketMultiplexer(rocket.connection1, rocket.connection2);
		command = new PacketController(messenger);
		rocket.connection1.setListener(command);
		rocket.connection2.setListener(command);
	}

	private void setupDeviceManager() {
		deviceManager = new DeviceManager(250L /* IOIO thread sleep in milliseconds */);
		deviceManager.setListener(new DeviceManager.DeviceManagerListener() {
			@Override
			public void incompatible() {
				updateTextView(ioioStatusTextView, "IOIO incompatible.");
			}
			@Override
			public void disconnected() {
				updateTextView(ioioStatusTextView, "IOIO disconnected.");
			}
			@Override
			public void setup(IOIO ioio) {
				updateTextView(ioioStatusTextView, "IOIO connected.");
			}
		});
	}
	
	private void setupUI() {
		setContentView(R.layout.activity_main);
		ioioStatusTextView = (TextView) findViewById(R.id.ioio_status);
	}
	
	private void setupLogging() {
		LogMultiplexer log = new LogMultiplexer(new AndroidLog());
		
		InetSocketAddress address = new InetSocketAddress("192.168.1.149", 9999);
		OutputStream udpStream;
		try {
			udpStream = new DatagramOutputStream(address);
			log.addLogger(new StreamLog(udpStream));
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		App.log = log;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
	protected void onDestroy() {
		App.log.i(App.TAG, "Shutting down.");
		
		wakelock.release(); // TODO should be released onPause
		super.onDestroy();
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
