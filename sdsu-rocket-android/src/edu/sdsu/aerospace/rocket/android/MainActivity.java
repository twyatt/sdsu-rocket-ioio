package edu.sdsu.aerospace.rocket.android;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends IOIOActivity {

	/**
	 * Per "good convention" recommendations found at:
	 * {@link http://developer.android.com/reference/android/util/Log.html}
	 */
	private static final String TAG = "SDSURocket";
	
	private static final int BUTTON_PIN = 35;
	
	private TextView ioioStatusTextView;
	private ToggleButton buttonToggleButton;
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new IOIOLooper() {
			private DigitalInput button;
			
			@Override
			public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
				button = ioio.openDigitalInput(BUTTON_PIN, DigitalInput.Spec.Mode.PULL_UP);
				Log.i(TAG, "IOIO connected.");
				setText("IOIO connected.");
			}
			
			@Override
			public void loop() throws ConnectionLostException, InterruptedException {
				final boolean reading = button.read();
				setState(!reading);
				
				Thread.sleep(100);
			}
			
			@Override
			public void incompatible() {
				Log.e(TAG, "IOIO incompatible.");
				setText("IOIO incompatible.");
			}
			
			@Override
			public void disconnected() {
				Log.e(TAG, "IOIO disconnected.");
				setText("IOIO disconnected.");
			}
		};
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ioioStatusTextView = (TextView) findViewById(R.id.ioio_status);
		buttonToggleButton = (ToggleButton) findViewById(R.id.button_state);
		
		// http://developer.android.com/training/basics/location/locationmanager.html#TaskGetLocationManagerRef
//		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
	
	private void setText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ioioStatusTextView.setText(text);
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
