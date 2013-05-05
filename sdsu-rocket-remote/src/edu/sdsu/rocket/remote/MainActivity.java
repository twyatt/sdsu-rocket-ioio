package edu.sdsu.rocket.remote;

import edu.sdsu.rocket.Network;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
	}
	
	private void setupUI() {
		setContentView(R.layout.activity_main);
		
		Button connect = (Button)findViewById(R.id.button_connect_activity);
		connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
				startActivity(intent);
			}
		});
		
		Button launch = (Button)findViewById(R.id.button_launch_activity);
		launch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, LaunchActivity.class);
				startActivity(intent);
			}
		});
		
		Button fillTanks = (Button)findViewById(R.id.button_fill_tanks_activity);
		fillTanks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ObjectiveActivity.class);
				intent.putExtra(ObjectiveActivity.EXTRA_OBJECTIVE, Network.FILL_TANKS_OBJECTIVE);
				startActivity(intent);
			}
		});
		
		Button sensorsButton = (Button)findViewById(R.id.button_sensors_activity);
		sensorsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SensorActivity.class);
				startActivity(intent);
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
