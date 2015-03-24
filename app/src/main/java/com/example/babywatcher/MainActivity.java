package com.example.babywatcher;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView txt;

	private static final int RESULT_SETTINGS = 1;

	public static String phoneNumber;
	public static Boolean silent, callblock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txt = (TextView) findViewById(R.id.textView1);
		phoneNumber = getphoneNumber();
		showUserSettings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	public void send_to_SetAction(View view) {
		Intent intent = new Intent(MainActivity.this, SetActionActivity.class);
		startActivity(intent);
	}

	public void Send_to_setNumber(View view) {
		Intent intent = new Intent(MainActivity.this, SetContactActivity.class);
		startActivity(intent);
	}

	public void send_to_Sensitivity(View view) {
		Intent intent = new Intent(MainActivity.this,
				SetSensitivityActivity.class);
		startActivity(intent);
	}

	public void Send_to_About(View view) {
		Intent intent = new Intent(MainActivity.this, AboutActivity.class);
		startActivity(intent);
	}

	public void ExitAction(View view) {
		finish();
	}

	public void startBabyWatching(View view) { // throws InterruptedException {

		SharedPreferences app_preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String method = app_preferences.getString("CONTACT_METHOD", "");

		if (method.equals("EMAIL")) {
			if (!checkifEmailIsSet()) {

				AlertDialog dialog = new AlertDialog.Builder(view.getContext())
						.create();
				dialog.setTitle("Start Error!");
				dialog.setMessage("No Email Address is set for sending Email. Please set parent's email address");
				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Set",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent i = new Intent(MainActivity.this,
										SetContactActivity.class);
								startActivity(i);
							}
						});
				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
				dialog.setCancelable(false);
				dialog.setIcon(R.drawable.advert);
				dialog.show();

			} else {
				Intent intent = new Intent(MainActivity.this,
						com.example.noisealert.NoiseAlertByEmailActivity.class);
				startActivity(intent);
			}

		}

		else if (method.equals("CALL") || method.equals("SMS")) {

			if (!checkifPhoneNumberIsSet()) {

				AlertDialog dialog = new AlertDialog.Builder(view.getContext())
						.create();
				dialog.setTitle("Start Error!");
				dialog.setMessage("No phone number is set for call or sms. Please set parent's phone number");
				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Set",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent i = new Intent(MainActivity.this,
										SetContactActivity.class);
								startActivity(i);
							}
						});
				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
				dialog.setCancelable(false);
				dialog.setIcon(R.drawable.advert);
				dialog.show();

			} else {

				Intent intent = new Intent(MainActivity.this,
						com.example.noisealert.NoiseAlertActivity.class);
				startActivity(intent);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_settings:
			Intent i = new Intent(this, UserSettingActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			break;

		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			showUserSettings();
			break;

		}

	}

	private void showUserSettings() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		callblock = sharedPrefs.getBoolean("prefBlockCalls", false);
		silent = sharedPrefs.getBoolean("prefSilentMode", false);

		txt.setText("Silent : " + silent + " callBlock: " + callblock);

	}

	private boolean checkifEmailIsSet() {

		String contactEmail = "";
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		contactEmail = sharedPreferences.getString("parents_email_address", "");
		if (contactEmail == "") {
			return false;
		} else {
			return true;
		}
	}

	private boolean checkifPhoneNumberIsSet() {
		String savedPhoneNumber = "";
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		savedPhoneNumber = sharedPreferences.getString("parents_phone_number",
				"");
		if (savedPhoneNumber == "") {
			return false;
		} else {
			return true;
		}
	}

	private String getphoneNumber() {
		String savedPhoneNumber = "";
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		savedPhoneNumber = sharedPreferences.getString("parents_phone_number",
				"");
		return savedPhoneNumber;
	}

}
