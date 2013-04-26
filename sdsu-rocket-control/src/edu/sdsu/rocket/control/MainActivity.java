package edu.sdsu.rocket.control;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.control.devices.ArduIMU;
import edu.sdsu.rocket.control.devices.BMP085;
import edu.sdsu.rocket.control.devices.DMO063;
import edu.sdsu.rocket.control.devices.MS5611;
import edu.sdsu.rocket.control.devices.P51500AA1365V;
import edu.sdsu.rocket.control.devices.UARTPing;
import edu.sdsu.rocket.control.devices.UARTReceiver;
import edu.sdsu.rocket.control.network.RemoteCommandController;

public class MainActivity extends IOIOActivity {

	private DeviceManager deviceManager;
//	private ObjectiveController objectiveController;
	private RemoteCommandController remoteCommand;
	
	private TextView ioioStatusTextView;
	private TextView serverStatusTextView;
	private TextView debugTextView;


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
		
		// TODO prevent device sleep
		
		deviceManager = new DeviceManager();
		setupDevices();
		
//		objectiveController = new ObjectiveController();
//		objectiveController.add(new FillTanksObjective());
//		objectiveController.add(new LaunchObjective());
		
		setupRemoteCommand();
	}

	private void setupUI() {
		setContentView(R.layout.activity_main);
		
		ioioStatusTextView = (TextView) findViewById(R.id.ioio_status);
		serverStatusTextView = (TextView) findViewById(R.id.server_status);
		debugTextView = (TextView) findViewById(R.id.debug);
	}

	private void setupRemoteCommand() {
		int tcpPort = Network.TCP_PORT;
		int udpPort = Network.UDP_PORT;
		
		remoteCommand = new RemoteCommandController(tcpPort, udpPort);
		remoteCommand.start();
		
		String ip = getIpAddr();
		setServerText("tcp://" + ip + ":" + tcpPort + "\nudp://" + ip + ":" + udpPort);
	}

	private void setupDevices() {
		// 3.3V digital
		DMO063 ignitor = new DMO063(3 /* pin */, 3000L /* duration (milliseconds) */);
		
		// max voltage for pin 35 = 3.3V
		P51500AA1365V pressure1 = new P51500AA1365V(36 /* pin */, 175.94f /* slope */, -149.6f /* bias */);
		P51500AA1365V pressure2 = new P51500AA1365V(40 /* pin */, 175.94f /* slope */, -149.6f /* bias */);
		
		// TODO test oversampling of 3 (max)
		// twiNum 0 = pin 4 (SDA) and 5 (SCL)
		BMP085 barometer1 = new BMP085(0 /* twiNum */, 3 /* eocPin */, 0 /* oversampling */);
		barometer1.setListener(new BMP085.BMP085Listener() {
			@Override
			public void onBMP085Values(float pressure, double temperature) {
				float alt = BMP085.altitude(pressure, BMP085.p0);
				alt = Units.convertMetersToFeet(alt);
				double temp = Units.convertCelsiusToFahrenheit(temperature);
//				App.log.i(App.TAG, "Pressure: " + pressure + ", Temperature: " + temperature);
				setIOIOText("A: " + alt + ", \nT: " + temp);
			}
		});
		
		MS5611 barometer2 = new MS5611(0 /* twiNum */, MS5611.ADD_CSB_LOW /* address */, 30 /* sample rate */);
		barometer2.setListener(new MS5611.MS5611Listener() {
			@Override
			public void onMS5611Values(float pressure /* mbar */, float temperature /* C */) {
				temperature += 273.15f; // K
				
				/*
				 * http://en.wikipedia.org/wiki/Density_altitude
				 */
				float Psl = 1013.25f; // standard sea level atmospheric pressure (hPa)
				float Tsl = 288.15f; // ISA standard sea level air temperature (K)
				float b = 0.234969f;
//				float altitude = 145442.156f * (1f - (float)Math.pow((pressure / Psl) / (temperature / Tsl), b));
				
				float altitude = (((float)Math.pow(Psl / pressure, 1f/5.257f) - 1f) * temperature) / 0.0065f;
				
				setIOIOText("P: " + pressure + " mbar\nT: " + (temperature - 273.15f) + " C\nA: " + altitude + " ft");
			}
		});
		
		ArduIMU imu = new ArduIMU(10 /* RX pin */);
		imu.setListener(new ArduIMU.ArduIMUListener() {
			@Override
			public void onArduIMUValues(String values) {
				App.log.i(App.TAG, "uart_rx=" + values);
			}
		});
		
		UARTPing ping = new UARTPing(10 /* TX pin */, 1000 /* thread sleep */);
		
		UARTReceiver receiver = new UARTReceiver(12 /* RX pin */);
		receiver.setListener(new UARTReceiver.UARTReceiverListener() {
			@Override
			public void onUARTMessage(final String response) {
				App.log.i(App.TAG, "uart response=" + response);
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						debugTextView.setText(response);
					}
				});
			}
		});
		
//		servo = new PS050(3, 100);
		
//		deviceManager.add(ignitor);
//		deviceManager.add(pressure1);
//		deviceManager.add(pressure2);
//		deviceManager.add(barometer1, true /* spawn thread */);
//		deviceManager.add(barometer2, true /* spawn thread */);
		deviceManager.add(imu, true /* spawn thread */);
		
//		deviceManager.add(ping, true /* spawn thread */);
//		deviceManager.add(receiver, true /* spawn thread */);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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

}
