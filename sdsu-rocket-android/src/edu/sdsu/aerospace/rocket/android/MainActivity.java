package edu.sdsu.aerospace.rocket.android;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import edu.sdsu.aerospace.rocket.UDPServer;
import edu.sdsu.aerospace.rocket.UDPServer.UDPServerListener;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends IOIOActivity implements UDPServerListener {

	/**
	 * Per "good convention" recommendations found at:
	 * {@link http://developer.android.com/reference/android/util/Log.html}
	 */
	private static final String TAG = "SDSURocket";
	
	private static UDPServer server;
	public final static int PORT = 12161;
	
	private static final int DIGITAL_OUTPUT_PIN = 3; // 3.3V
	private static final long IGNITION_DURATION = 3000L; // milliseconds

	protected static final int PRESSURE_TRANSDUCER_INPUT_PIN = 35; // analog, 3.3V max input
	
	private TextView ioioStatusTextView;
	private TextView ioioVoltageTextView;
	private TextView serverStatusTextView;
	private ToggleButton buttonToggleButton;

	private Timer timer;
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new IOIOLooper() {
			private DigitalOutput output;
			private AnalogInput input;
			
			@Override
			public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
//				output = ioio.openDigitalOutput(DIGITAL_OUTPUT_PIN);
				input = ioio.openAnalogInput(PRESSURE_TRANSDUCER_INPUT_PIN);
				
				Log.i(TAG, "IOIO connected.");
				setIOIOText("IOIO connected.");
			}
			
			@Override
			public void loop() throws ConnectionLostException, InterruptedException {
				// TODO setState from UDP packets
//				setState(!reading);
				
//				boolean state = buttonToggleButton.isChecked();
//				output.write(state);
				
				float voltage = input.getVoltage();
				float psi = 190.02f * voltage - 139.87f;
				setIOIOVText(Float.toString(psi));
				
				Thread.sleep(100);
			}
			
			@Override
			public void incompatible() {
				Log.e(TAG, "IOIO incompatible.");
				setIOIOText("IOIO incompatible.");
			}
			
			@Override
			public void disconnected() {
				Log.e(TAG, "IOIO disconnected.");
				setIOIOText("IOIO disconnected.");
			}
		};
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ioioStatusTextView = (TextView) findViewById(R.id.ioio_status);
		ioioVoltageTextView = (TextView) findViewById(R.id.ioio_v_in);
		serverStatusTextView = (TextView) findViewById(R.id.server_status);
		buttonToggleButton = (ToggleButton) findViewById(R.id.button_state);
		
		// http://developer.android.com/training/basics/location/locationmanager.html#TaskGetLocationManagerRef
//		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		server = new UDPServer();
		server.listen(PORT);
		server.setListener(this);
		setServerText("Listening on port " + PORT + ".");
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
		
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				setServerText("");
				setState(false);
				timer.cancel();
				timer = null;
			}
		}, IGNITION_DURATION);
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
	
	private void setIOIOVText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ioioVoltageTextView.setText(text);
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
