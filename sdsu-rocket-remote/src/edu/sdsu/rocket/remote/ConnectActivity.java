package edu.sdsu.rocket.remote;

import java.io.IOException;
import java.net.InetAddress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.sdsu.rocket.Network;
import edu.sdsu.rocket.Network.AuthenticationRequest;
import edu.sdsu.rocket.Network.AuthenticationResponse;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ConnectActivity extends Activity {

	public static final int DISCOVER_HOST_TIMEOUT = 10000; // milliseconds
	public static final int CONNECT_TIMEOUT = 10000; // milliseconds
	
	/**
	 * The default email to populate the email field with.
	 */
//	public static final String EXTRA_EMAIL = "edu.sdsu.rocket.remote.extra.KEY";

	private Client client;
	private Listener clientListener;
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private ConnectTask connectTask;
	private DiscoverHostTask discoverHostTask;

//	private String host;
//	private String key;

	private EditText hostView;
	private EditText keyView;
	private View connectFormView;
	private View connectStatusView;
	private TextView connectStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUI();
		
		client = ClientSingleton.getInstance();
		clientListener = new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				if (object instanceof AuthenticationResponse) {
					AuthenticationResponse response = (AuthenticationResponse)object;
					onAuthenticationResponse(response);
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

	protected void onAuthenticationResponse(final AuthenticationResponse response) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (response.success) {
					Toast.makeText(getApplicationContext(), "Successfully authenticated", Toast.LENGTH_LONG).show();
					finish();
				} else {
					keyView.setError(getString(R.string.error_unable_to_connect));
					keyView.requestFocus();
					showProgress(false);
				}
			}
		});
	}

	private void setupUI() {
		setContentView(R.layout.activity_connect);
		setupActionBar();

		hostView = (EditText)findViewById(R.id.host);
		if (App.host != null) {
			hostView.setText(App.host);
		}
		
		keyView = (EditText)findViewById(R.id.key);
		if (App.key != null) {
			keyView.setText(App.key);
		}
		keyView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.connect || id == EditorInfo.IME_NULL) {
					attemptConnect();
					return true;
				}
				return false;
			}
		});

		connectFormView = findViewById(R.id.login_form);
		connectStatusView = findViewById(R.id.connect_status);
		connectStatusMessageView = (TextView)findViewById(R.id.connect_status_message);

		findViewById(R.id.connect_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptConnect();
			}
		});
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu.connect, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptConnect() {
		if (discoverHostTask != null || connectTask != null) {
			return;
		}
		
		// store values at the time of the login attempt
		App.host = hostView.getText().toString();
		App.key = hostView.getText().toString();
		
		// reset errors
		hostView.setError(null);
		keyView.setError(null);
		
		if (hostView.getText().length() == 0) {
			// no host specified, so we will attempt to discover host
			connectStatusMessageView.setText(R.string.connect_progress_discoverying_host);
			
			discoverHostTask = new DiscoverHostTask(client, Network.UDP_PORT);
			discoverHostTask.execute(Integer.valueOf(DISCOVER_HOST_TIMEOUT));
		} else {
			// connect task
			connectStatusMessageView.setText(R.string.connect_progress_connecting);
			
			connectTask = new ConnectTask(client, App.host, Network.TCP_PORT, Network.UDP_PORT);
			connectTask.execute(Integer.valueOf(DISCOVER_HOST_TIMEOUT));
		}
		
		showProgress(true);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			connectStatusView.setVisibility(View.VISIBLE);
			connectStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							connectStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			connectFormView.setVisibility(View.VISIBLE);
			connectFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							connectFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			connectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			connectFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	// android.os.AsyncTask<Params, Progress, Result>
	public class DiscoverHostTask extends AsyncTask<Integer, Void, InetAddress> {

		private Client client;
		private int udpPort;

		public DiscoverHostTask(Client client, int udpPort) {
			this.client = client;
			this.udpPort = udpPort;
		}
		
		@Override
		protected InetAddress doInBackground(Integer... params) {
			if (params != null) {
				int timeout = params[0].intValue();
				return client.discoverHost(udpPort, timeout);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(final InetAddress address) {
			discoverHostTask = null;

			if (address != null) {
				// found host, so we'll connect to it
				hostView.setText(address.getHostName());
				attemptConnect();
			} else {
				// no hosts found :-(
				showProgress(false);
				hostView.setError(getString(R.string.error_discover_host_failed));
				hostView.requestFocus();
			}
		}
		
		@Override
		protected void onCancelled() {
			discoverHostTask = null;
			showProgress(false);
		}
		
	}
	
	public class ConnectTask extends AsyncTask<Integer, Void, Boolean> {
		
		private Client client;
		private String host;
		private int tcpPort;
		private int udpPort;

		public ConnectTask(Client client, String host, int tcpPort, int udpPort) {
			this.client = client;
			this.host = host;
			this.tcpPort = tcpPort;
			this.udpPort = udpPort;
		}
		
		/**
		 * Be sure not to call this from the main thread (Android API > 10
		 * throws exception when performing network on main thread).
		 */
		private void authenticate() {
			connectStatusMessageView.setText(R.string.connect_progress_authenticating);
			AuthenticationRequest request = new AuthenticationRequest();
			request.key = App.key;
			client.sendTCP(request);
		}
		
		/*
		 * AsyncTask methods.
		 */
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			if (params != null) {
				int timeout = params[0].intValue();
				
				try {
					client.connect(timeout, host, tcpPort, udpPort);
					if (client.isConnected()) {
						authenticate();
					}
					return true;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			
			return false;
		}


		@Override
		protected void onPostExecute(final Boolean success) {
			connectTask = null;

			if (success) {
				if (keyView.getText().length() > 0) {
					authenticate();
				} else {
					Toast.makeText(getApplicationContext(), "Connection successful", Toast.LENGTH_LONG).show();
					finish();
				}
			} else {
				hostView.setError(getString(R.string.error_unable_to_connect));
				hostView.requestFocus();
				showProgress(false);
			}
		}

		@Override
		protected void onCancelled() {
			connectTask = null;
			showProgress(false);
		}
		
	}
	
}
