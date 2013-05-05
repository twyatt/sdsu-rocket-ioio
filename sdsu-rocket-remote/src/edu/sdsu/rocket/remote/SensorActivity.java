package edu.sdsu.rocket.remote;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network.SensorRequest;
import edu.sdsu.rocket.Network.SensorResponse;

public class SensorActivity extends Activity {

	private Client client;
	private Listener clientListener;
	
	private TextView enginePressure;
	private TextView ethanolPressure;
	private TextView loxPressure;
	private TextView breakWire;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
		
		client = ClientSingleton.getInstance();
		clientListener = new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				if (object instanceof SensorResponse) {
					SensorResponse response = (SensorResponse)object;
					onSensorResponse(response);
				}
			}
		};
		client.addListener(clientListener);
	}
	
	@Override
	protected void onDestroy() {
		if (client != null && clientListener != null) {
			client.removeListener(clientListener);
		}
		super.onDestroy();
	}

	protected void onSensorResponse(final SensorResponse response) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				enginePressure.setText("Engine\nV: " + response.engineTransducerVoltage + "\nP: " + response.engineTransducerPressure);
				ethanolPressure.setText("Ethanol\nV: " + response.ethanolTransducerVoltage + "\nP: " + response.ethanolTransducerPressure);
				loxPressure.setText("LOX\nV: " + response.loxTransducerVoltage + "\nP: " + response.loxTransducerPressure);
				breakWire.setText("Break wire is " + (response.breakWireIsBroken ? "" : "NOT") + " broken.");
			}
		});
	}

	private void setupUI() {
		setContentView(R.layout.activity_sensor);
		
		enginePressure = (TextView)findViewById(R.id.enginePressure);
		ethanolPressure = (TextView)findViewById(R.id.ethanolPressure);
		loxPressure = (TextView)findViewById(R.id.loxPressure);
		breakWire = (TextView)findViewById(R.id.breakWire);
		
		Button refreshButton = (Button)findViewById(R.id.refresh);
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onRefresh();
			}
		});
		
		// Show the Up button in the action bar.
		setupActionBar();
	}

	protected void onRefresh() {
		SensorRequest request = new SensorRequest();
		client.sendUDP(request);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sensor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
