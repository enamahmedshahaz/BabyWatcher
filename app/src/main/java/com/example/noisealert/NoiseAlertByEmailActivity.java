package com.example.noisealert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.widget.Toast;

import com.example.babywatcher.MainActivity;
import com.example.babywatcher.R;
import com.example.babywatcher.SetSensitivityActivity;
import com.example.internetconnenable.InternetConnection;
import com.example.sendmail.GMailSender;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

public class NoiseAlertByEmailActivity extends Activity implements
		SurfaceHolder.Callback {

	private ImageView imageView;
	private SurfaceView surfaceView;
	private Bitmap bmp;
	// Camera variables
	private SurfaceHolder sHolder; // a surface holder
	private Camera mCamera; // a variable to control the camera
	private Parameters parameters; // the camera parameters

	private int attemptNumber = 0;
	private AudioManager myAudioManager;
	private int mode;
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
				try {
					callForHelp();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		setContentView(R.layout.activity_noise_alert_by_email);
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

		imageView = (ImageView) findViewById(R.id.imageView);
		// get the Surface View at the main.xml file
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		// Get a surface
		sHolder = surfaceView.getHolder();
		// add the callback interface methods defined below as the Surface View
		// callbacks
		sHolder.addCallback(this);
		// tells Android that this surface will have its data constantly
		// replaced
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void onResume() {
		super.onResume();
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
		stop();
	}

	private void start() {
		if (MainActivity.silent == true) {
			myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}

		mSensor.start();

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}
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
		updateDisplay("Baby Watcher stopped...", 0.0);
		mRunning = false;

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
	}

	private void updateDisplay(String status, double signalEMA) {
		mStatusView.setText(status);
		mDisplay.setLevel((int) signalEMA, mThreshold);
	}

	private void callForHelp() throws InterruptedException {
		attemptNumber = attemptNumber + 1;

		if (attemptNumber >= 3) {
			capture();
			sendEmail();
			stop();
		}

		else if (attemptNumber < 3) {
			mListenStatusView
					.setText("High sound level detected. Alarm will be triggered if it continuses.");
		}
	}

	public void capture() {
		// sets what code should be executed after the picture is taken
		Camera.PictureCallback mCall = new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// decode the data obtained by the camera into a Bitmap
				bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
				storeImage(bmp);
				// set the imageView
				imageView.setImageBitmap(bmp);
			}
		};
		mCamera.takePicture(null, null, mCall);
	}

	private File getOutputMediaFile() {
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory()
						+ "/Android/data/BabyWatcher/Images");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		File mediaFile;
		String mImageName = "YourBaby.jpg";
		mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ mImageName);
		return mediaFile;
	}

	private void storeImage(Bitmap image) {
		File pictureFile = getOutputMediaFile();
		if (pictureFile == null) {
			Log.d("Error",
					"Error creating media file, check storage permissions: ");
			return;
		}
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			image.compress(Bitmap.CompressFormat.JPEG, 20, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d("Error", "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d("Error", "Error accessing file: " + e.getMessage());
		}
	}

	private void sendEmail() {

		toggleInternetConnection();

		SharedPreferences app_preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String parents_email_address = app_preferences.getString(
				"parents_email_address", "").toString();

		final ProgressDialog ringProgressDialog = ProgressDialog.show(
				NoiseAlertByEmailActivity.this, "Please wait...",
				"Sending Email...", true);
		ringProgressDialog.setCancelable(false);

		new Thread(new Runnable() {
			public void run() {
				try {
					GMailSender sender = new GMailSender(
							"mybabywatch@gmail.com", "enamahmed");

					sender.addAttachment(Environment
							.getExternalStorageDirectory()
							+ "/Android/data/BabyWatcher/Images/YourBaby.jpg");

					sender.sendMail("Baby Watcher",
							"Watch your baby. Sound is detected !",
							"mybabywatch@gmail.com", parents_email_address);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							"Error while sending e-mail", Toast.LENGTH_LONG)
							.show();
				}
				ringProgressDialog.dismiss();
			}
		}).start();
		
	}

	public void toggleInternetConnection() {
		final InternetConnection iconn = new InternetConnection(
				getApplicationContext());

		if (iconn.haveNetworkConnection() == false) {

			final ProgressDialog ringProgressDialog = ProgressDialog.show(
					NoiseAlertByEmailActivity.this, "Please wait...",
					"Enabling Data connection...", true);
			ringProgressDialog.setCancelable(false);

			new Thread(new Runnable() {
				public void run() {
					try {
						iconn.turnMobileConnection(true);
						Thread.sleep(5000);
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(),
								"Data connection Failed", Toast.LENGTH_LONG)
								.show();
					}
					ringProgressDialog.dismiss();
				}
			}).start();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// get camera parameters
		parameters = mCamera.getParameters();
		// set camera parameters
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// stop the preview
		mCamera.stopPreview();
		// release the camera
		mCamera.release();
		// unbind the camera from this object
		mCamera = null;

	}

}
