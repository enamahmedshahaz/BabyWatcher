package com.example.noisealert;

import com.example.babywatcher.MainActivity;
import com.example.babywatcher.R;
import com.example.babywatcher.SetSensitivityActivity;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class NoiseAlertActivity extends Activity {

	private int attemptNumber = 0;
	private AudioManager myAudioManager;
	public static ActivityManager manager;
	private int mode;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.noise_alert, menu);
		return true;
	}

	/* constants */
	private static final int POLL_INTERVAL = 300;

	/** running state **/
	private boolean mRunning = false;

	/** config state **/
	private int mThreshold;

	private PowerManager.WakeLock mWakeLock;

	private Handler mHandler = new Handler();

	/* References to view elements */
	private TextView mStatusView;
	private TextView mListenStatusView;
	private SoundLevelView mDisplay;

	/* sound data source */
	private SoundMeter mSensor;

	/****************** Define runnable thread again and again detect noise *********/

	private Runnable mSleepTask = new Runnable() {
		@Override
		public void run() {
			// Log.i("Noise", "runnable mSleepTask");
			start();
		}
	};

	// Create runnable thread to Monitor Voice
	private Runnable mPollTask = new Runnable() {
		@Override
		public void run() {

			double amp = mSensor.getAmplitude();
			// Log.i("Noise", "runnable mPollTask");
			updateDisplay("The Baby Watcher is listening...", amp);

			if ((amp > mThreshold)) {
				callForHelp();
				// Log.i("Noise", "==== onCreate ===");
			}

			// Runnable(mPollTask) will again execute after POLL_INTERVAL
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);

		}
	};

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Defined SoundLevelView in main.xml file
		setContentView(R.layout.activity_noise_alert);
		mStatusView = (TextView) findViewById(R.id.status);
		mListenStatusView = (TextView) findViewById(R.id.listenStatus);
		// Used to record voice
		mSensor = new SoundMeter();
		mDisplay = (SoundLevelView) findViewById(R.id.volume);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"NoiseAlertActivity");

		myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mode = myAudioManager.getRingerMode();
		manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

	}

	@Override
	public void onResume() {

		super.onResume();
		// Log.i("Noise", "==== onResume ===");

		initializeApplicationConstants();
		mDisplay.setLevel(0, mThreshold);

		if (!mRunning) {
			mRunning = true;
			start();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		// Log.i("Noise", "==== onStop ===");
		// Stop noise monitoring
		stop();
	}

	private void start() {
		// Log.i("Noise", "==== start ===");

		if (MainActivity.silent == true) {
			myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT); // silent
																			// mode
		}

		mSensor.start();

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}

		// Noise monitoring start
		// Runnable(mPollTask) will execute after POLL_INTERVAL
		mHandler.postDelayed(mPollTask, POLL_INTERVAL);
	}

	private void stop() {
		Log.i("Noise", "==== Stop Noise Monitoring===");
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
		mHandler.removeCallbacks(mSleepTask);
		mHandler.removeCallbacks(mPollTask);
		mSensor.stop();
		mDisplay.setLevel(0, 0);
		updateDisplay("stopped...", 0.0);
		mRunning = false;

		// /////////////////////////////////
		if (mode == AudioManager.RINGER_MODE_SILENT) {

			myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		} else if (mode == AudioManager.RINGER_MODE_VIBRATE) {

			myAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		} else if (mode == AudioManager.RINGER_MODE_NORMAL) {

			myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		}

	}

	private void initializeApplicationConstants() {
		// Set Noise Threshold
		mThreshold = SetSensitivityActivity.threshold * 2;
		// mThreshold = 8;

	}

	private void updateDisplay(String status, double signalEMA) {
		mStatusView.setText(status);
		//
		mDisplay.setLevel((int) signalEMA, mThreshold);
	}

	private void callForHelp() {
		// stop();
		++attemptNumber;
		if (attemptNumber < 3) {
			mListenStatusView
					.setText("High sound level detected. Alarm will be triggered if it continuses.");
		} else if (attemptNumber >= 3) {
			makeCallOrSMS();
		}
	}

	/************ Begin: code to make call/sms ***********/

	private void makeCallOrSMS() {

		SharedPreferences app_preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		String method = app_preferences.getString("CONTACT_METHOD", "");
		String parents_phoneNo = app_preferences.getString(
				"parents_phone_number", "");
		String SMS_to_parents = "Your baby is crying...";

		if (method.equals("SMS")) {
			try {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(parents_phoneNo, null,
						SMS_to_parents, null, null);
				Toast.makeText(getApplicationContext(), "SMS sent!",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "SMS sending failed",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		} else if (method.equals("CALL")) {

			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + parents_phoneNo + ""));
			startActivity(callIntent);
		}
		stop();
	}

	/************ code to make call/sms ***********/

}
