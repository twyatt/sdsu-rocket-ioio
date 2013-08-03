package edu.sdsu.rocket.control;

import ioio.lib.api.IOIO;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.widget.TextView;
import edu.sdsu.rocket.control.controllers.ArduinoController;
import edu.sdsu.rocket.control.controllers.PacketController;
import edu.sdsu.rocket.control.controllers.RocketController;
import edu.sdsu.rocket.control.logging.AndroidLog;
import edu.sdsu.rocket.control.models.Rocket;
import edu.sdsu.rocket.io.DatagramOutputStream;
import edu.sdsu.rocket.logging.LogMultiplexer;
import edu.sdsu.rocket.logging.StreamLog;

public class MainActivity extends IOIOActivity {

	private DeviceManager deviceManager;
	private final Timer timer = new Timer();
	
	private PowerManager.WakeLock wakelock;
	
	private TextView ioioStatusTextView;
	private TextView ioioConnectsTextView;
	private TextView ioioDisconnectsTextView;
	private TextView ioioErrorsTextView;
	private TextView packetsSentTextView;
	private TextView packetsReceivedTextView;
	private TextView packetsDroppedTextView;
	private TextView upTimeTextView;
	
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
		setupStatusTimer();
	}

	private void setupStatusTimer() {
		long delay = 1000L; // ms
		long period = 1000L; // ms
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ioioErrorsTextView.setText(String.valueOf(App.stats.ioio.errors.get()));
						packetsSentTextView.setText(String.valueOf(App.stats.network.packetsSent.get()));
						packetsReceivedTextView.setText(String.valueOf(App.stats.network.packetsReceived.get()));
						packetsDroppedTextView.setText(String.valueOf(App.stats.network.packetsDropped.get()));
						upTimeTextView.setText(String.valueOf(App.elapsedTime()) + " s");
					}
				});
			}
		}, delay, period);
	}

	private void disableDeviceSleep() {
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, App.TAG);
		wakelock.acquire();
	}

	private void setupPacketController(Rocket rocket) {
		rocket.connection1.setListener(new PacketController(rocket.connection1));
		rocket.connection2.setListener(new PacketController(rocket.connection2));
		rocket.arduino.setListener(new ArduinoController(rocket.arduino, rocket));
	}

	private void setupDeviceManager() {
		deviceManager = new DeviceManager(1000L /* IOIO thread sleep in milliseconds */);
		deviceManager.setListener(new DeviceManager.DeviceManagerListener() {
			@Override
			public void incompatible() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ioioStatusTextView.setText("Incompatible");
						App.stats.ioio.errors.incrementAndGet();
					}
				});
			}
			@Override
			public void disconnected() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ioioStatusTextView.setText("Disconnected");
						ioioDisconnectsTextView.setText(String.valueOf(App.stats.ioio.disconnects.incrementAndGet()));
						App.rocketController.getRocket().internalAccelerometer.stop();
					}
				});
			}
			@Override
			public void setup(IOIO ioio) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ioioStatusTextView.setText("Connected");
						ioioConnectsTextView.setText(String.valueOf(App.stats.ioio.connects.incrementAndGet()));
						App.rocketController.getRocket().internalAccelerometer.start();
					}
				});
			}
		});
	}
	
	private void setupUI() {
		setContentView(R.layout.activity_main);
		ioioStatusTextView = (TextView) findViewById(R.id.ioio_status);
		ioioConnectsTextView = (TextView) findViewById(R.id.ioio_connects);
		ioioDisconnectsTextView = (TextView) findViewById(R.id.ioio_disconnects);
		ioioErrorsTextView = (TextView) findViewById(R.id.ioio_errors);
		packetsSentTextView = (TextView) findViewById(R.id.packets_sent);
		packetsReceivedTextView = (TextView) findViewById(R.id.packets_received);
		packetsDroppedTextView = (TextView) findViewById(R.id.packets_dropped);
		upTimeTextView = (TextView) findViewById(R.id.up_time);
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

}
