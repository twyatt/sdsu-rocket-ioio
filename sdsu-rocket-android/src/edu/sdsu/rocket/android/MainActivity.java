package edu.sdsu.rocket.android;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.net.InetAddress;

import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.ToggleButton;
import edu.sdsu.aerospace.rocket.network.UDPServer;
import edu.sdsu.aerospace.rocket.network.UDPServer.UDPServerListener;
import edu.sdsu.rocket.android.devices.BMP085;
import edu.sdsu.rocket.android.devices.DMO063;
import edu.sdsu.rocket.android.devices.Device;
import edu.sdsu.rocket.android.devices.P51500AA1365V;
import edu.sdsu.rocket.android.logging.UDPLog;

public class MainActivity extends IOIOActivity implements UDPServerListener {

	private static UDPServer server;
	public final static int PORT = 12161;
	
	private DeviceManager deviceManager;
	
	private TextView ioioStatusTextView;
	private TextView serverStatusTextView;
	private ToggleButton buttonToggleButton;

	@Override
	protected IOIOLooper createIOIOLooper() {
		return deviceManager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		App.log = new UDPLog("192.168.1.7", 10001);
		
		ioioStatusTextView = (TextView) findViewById(R.id.ioio_status);
		serverStatusTextView = (TextView) findViewById(R.id.server_status);
		buttonToggleButton = (ToggleButton) findViewById(R.id.button_state);
		
		// http://developer.android.com/training/basics/location/locationmanager.html#TaskGetLocationManagerRef
//		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		setupServer();
		
		deviceManager = new DeviceManager();
		setupDevices();
	}

	private void setupServer() {
		server = new UDPServer();
		server.listen(PORT);
		server.setListener(this);
		setServerText("Listening on port " + PORT + ".");
		App.log.i(App.TAG, "Listening on port " + PORT + ".");
	}

	private void setupDevices() {
		// 3.3V digital
		Device ignitor = new DMO063(3 /* pin */, 3000L /* duration (milliseconds) */);
		
		// max voltage for pin 35 = 3.3V
		Device pressure = new P51500AA1365V(35 /* pin */, 190.02f /* slope */, -139.87f /* bias */);
		
		// twiNum 0 = pin 4 (SDA) and 5 (SCL)
		BMP085 barometer = new BMP085(0 /* twiNum */, 3 /* eocPin */, 0 /* oversampling */);
		
		barometer.setListener(new BMP085.Listener() {
			@Override
			public void onBMP085Values(float pressure, double temperature) {
				float alt = BMP085.altitude(pressure, BMP085.p0);
				alt = Units.convertMetersToFeet(alt);
				double temp = Units.convertCelsiusToFahrenheit(temperature);
//				App.log.i(App.TAG, "Pressure: " + pressure + ", Temperature: " + temperature);
				setIOIOText("A: " + alt + ", \nT: " + temp);
			}
		});
		
//		deviceManager.add(ignitor);
//		deviceManager.add(pressure);
		deviceManager.add(barometer, true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public void onReceivedPacket(byte[] data, InetAddress inetAddress, int port) {
		String text = new String(data);
		setServerText(text);
		
		if (text.startsWith("LAUNCH")) {
			setState(true);
		}
	}
	
//	@Override
//	protected void onStart() {
//		super.onStart();
//
//		// http://developer.android.com/training/basics/location/locationmanager.html#TaskVerifyProvider
//		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//		final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//		if (!gpsEnabled) {
//			// Build an alert dialog here that requests that the user enable
//			// the location services, then when the user clicks the "OK" button,
//			// call enableLocationSettings()
//			Log.e(TAG, "GPS disabled.");
//		}
//	}
//
//	private void enableLocationSettings() {
//		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//		startActivity(settingsIntent);
//	}
	
	private void setIOIOText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ioioStatusTextView.setText(text);
			}
		});
	}
	
	private void setServerText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				serverStatusTextView.setText(text);
			}
		});
	}
	
	private void setState(final boolean on) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				buttonToggleButton.setChecked(on);
			}
		});
	}

}
