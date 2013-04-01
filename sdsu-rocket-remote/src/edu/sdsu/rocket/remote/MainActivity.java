package edu.sdsu.rocket.remote;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import edu.sdsu.rocket.network.UDPClient;

public class MainActivity extends Activity {

	private static final String HOST = "192.168.1.3";
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
