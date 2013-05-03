package edu.sdsu.rocket.remote;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.CommandRequest;
import edu.sdsu.rocket.Network.SetObjectiveRequest;
import edu.sdsu.rocket.Network.SetObjectiveResponse;

public class ObjectiveActivity extends Activity {

	protected static final String EXTRA_OBJECTIVE = "edu.sdsu.rocket.objective.EXTRA";
	
	private String objective;
	
	private Client client;
	private Listener clientListener;
	
	public static Map<String, String> getCommands(String objective) {
		Map<String, String> commands = new LinkedHashMap<String, String>();
		
		if (objective.equalsIgnoreCase(Network.LAUNCH_OBJECTIVE)) {
			commands.put(Network.LAUNCH_COMMAND, "Launch");
			commands.put(Network.ABORT_COMMAND, "Abort");
		}
		
		if (objective.equalsIgnoreCase(Network.FILL_TANKS_OBJECTIVE)) {
			commands.put(Network.OPEN_LOX_TANK_COMMAND, "Open LOX Tank");
			commands.put(Network.CYCLE_LOX_TANK_COMMAND, "Cycle LOX Tank");
			commands.put(Network.CLOSE_LOX_TANK_COMMAND, "Close LOX Tank");
			commands.put(Network.OPEN_ETHANOL_TANK, "Open Ethanol Tank");
			commands.put(Network.CYCLE_ETHANOL_TANK, "Cycle Ethanol Tank");
			commands.put(Network.CLOSE_ETHANOL_TANK, "Close Ethanol Tank");
		}
		
		return commands;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras.containsKey(EXTRA_OBJECTIVE)) {
			objective = (String)extras.get(EXTRA_OBJECTIVE);
		}
		
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
		setObjectiveRequest.name = objective;
		client.sendTCP(setObjectiveRequest);
	}
	
	@Override
	protected void onDestroy() {
		if (client != null && clientListener != null) {
			client.removeListener(clientListener);
		}
		
		super.onDestroy();
	}
	
	protected void onSetObjectiveResponse(final SetObjectiveResponse response) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (response.success) {
					Toast.makeText(getApplicationContext(), "Set " + objective + " objective.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Failed to set " + objective + " objective.", Toast.LENGTH_LONG).show();
					finish();
				}
			}
		});
	}

	private void setupUI() {
		setContentView(R.layout.activity_objective);
		
		ListView list = (ListView)findViewById(R.id.commandListView);
		
		if (objective != null) {
			final Map<String, String> commands = getCommands(objective);
			String[] values = commands.values().toArray(new String[0]);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
			
			list.setAdapter(adapter);
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
					String item = (String)parent.getItemAtPosition(position);
					if (commands.containsValue(item)) {
						for (Entry<String, String> entry : commands.entrySet()) {
							if (entry.getValue().equals(item)) {
								onCommand(entry.getKey());
							}
						}
					}
				}
			});
		}
		
		// Show the Up button in the action bar.
		setupActionBar();
	}

	protected void onCommand(String command) {
		Log.i(App.TAG, "Command: " + command);
		Client client = ClientSingleton.getInstance();
		
		if (client.isConnected()) {
			CommandRequest commandRequest = new CommandRequest();
			commandRequest.command = command;
			client.sendTCP(commandRequest);
		}
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
		getMenuInflater().inflate(R.menu.objective, menu);
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
