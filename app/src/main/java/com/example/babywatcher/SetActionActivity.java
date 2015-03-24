package com.example.babywatcher;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

public class SetActionActivity extends Activity {

	RadioButton rBtnSMS, rBtnCALL, rBtnEMAIL;
	String method;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_action);

		rBtnSMS = (RadioButton) findViewById(R.id.rBtnSMS);
		rBtnCALL = (RadioButton) findViewById(R.id.rBtnCALL);
		rBtnEMAIL = (RadioButton) findViewById(R.id.rBtnEMAIL);

		loadSavedState();
		Intent send_method_toMainActivity = new Intent(this, MainActivity.class);
		send_method_toMainActivity.putExtra("CONTACT_METHOD", method);
	}

	private void loadSavedState() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		String SavedMethod = sharedPreferences.getString("CONTACT_METHOD", "");

		if (SavedMethod.equals("SMS")) {
			rBtnSMS.setChecked(true);
		} else if (SavedMethod.equals("CALL")) {
			rBtnCALL.setChecked(true);
		} else if (SavedMethod.equals("EMAIL")) {
			rBtnEMAIL.setChecked(true);
		}
	}

	private void savePreferences(String key, String value) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		// Check which radio button was clicked
		switch (view.getId()) {
		case R.id.rBtnCALL:
			if (checked) {
				savePreferences("CONTACT_METHOD", "CALL");
				Toast.makeText(getApplicationContext(), "CALL method selected",
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.rBtnSMS:
			if (checked) {
				savePreferences("CONTACT_METHOD", "SMS");
				Toast.makeText(getApplicationContext(), "SMS method selected",
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.rBtnEMAIL:
			if (checked) {
				savePreferences("CONTACT_METHOD", "EMAIL");
				Toast.makeText(getApplicationContext(),
						"EMAIL method selected", Toast.LENGTH_SHORT).show();
			}
			break;

		}
	}
}
