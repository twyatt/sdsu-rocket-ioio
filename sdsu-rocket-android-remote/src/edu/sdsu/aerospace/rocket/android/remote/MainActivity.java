package edu.sdsu.aerospace.rocket.android.remote;

import edu.sdsu.aerospace.rocket.UDPClient;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	// Motorola Triumph
	private static final String HOST = "130.191.178.84";
	
	// MacBook Pro
//	private static final String HOST = "146.244.179.223";
	
	private static final int PORT = 12161;
	private UDPClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final Button button = (Button) findViewById(R.id.button_launch);
		button.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				client.send("LAUNCH");
			}
		});
		
		client = new UDPClient(HOST, PORT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
