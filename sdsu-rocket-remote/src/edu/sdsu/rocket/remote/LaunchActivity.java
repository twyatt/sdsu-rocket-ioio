package edu.sdsu.rocket.remote;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network.CommandRequest;
import edu.sdsu.rocket.Network.SetObjectiveRequest;
import edu.sdsu.rocket.Network.SetObjectiveResponse;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class LaunchActivity extends Activity {
	
	private Client client;
	private Listener clientListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
		
		client = ClientSingleton.getInstance();
		clientListener = new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				if (object instanceof SetObjectiveResponse) {
					SetObjectiveResponse response = (SetObjectiveResponse)object;
					onSetObjectiveResponse(response);
				}
			}
		};
		client.addListener(clientListener);
		
		SetObjectiveRequest setObjectiveRequest = new SetObjectiveRequest();
		setObjectiveRequest.name = "launch";
		client.sendTCP(setObjectiveRequest);
	}
	
	protected void onSetObjectiveResponse(final SetObjectiveResponse response) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (response.success) {
					Toast.makeText(getApplicationContext(), "Set launch objective.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Failed to set launch objective.", Toast.LENGTH_LONG).show();
					finish();
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (client != null && clientListener != null) {
			client.removeListener(clientListener);
		}
		
		super.onDestroy();
	}
	
	private void setupUI() {
		setContentView(R.layout.activity_launch);
		
		final Button button = (Button) findViewById(R.id.button_launch);
		button.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				CommandRequest commandRequest = new CommandRequest();
				commandRequest.command = "launch";
				client.sendTCP(commandRequest);
			}
		});
		
		setupActionBar();
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
		getMenuInflater().inflate(R.menu.launch, menu);
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
