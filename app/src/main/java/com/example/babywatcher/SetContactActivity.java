package com.example.babywatcher;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetContactActivity extends Activity implements OnClickListener {

	public static String parentsPhnNum;

	EditText phoneNumber = null;
	EditText emailAddress = null;
	String contactNumber = null;
	String contactEmail = null;
	Button btnManualAdd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_number);
		phoneNumber = (EditText) findViewById(R.id.edtxtNumber);
		emailAddress = (EditText) findViewById(R.id.edtxtEmail);
		btnManualAdd = (Button) findViewById(R.id.btnManualAdd);
		btnManualAdd.setOnClickListener(this);
		loadSavedNumber();
		send_Number_ToMain_Activity();
	}

	private void send_Number_ToMain_Activity() {
		Intent send_number_to_mainActivity = new Intent(this,
				MainActivity.class);
		send_number_to_mainActivity.putExtra("parents_number", contactNumber);
	}

	private void loadSavedNumber() {

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String savedPhoneNumber = sharedPreferences.getString(
				"parents_phone_number", contactNumber);

		String savedEmailAddress = sharedPreferences.getString(
				"parents_email_address", contactEmail);

		phoneNumber.setText(savedPhoneNumber);
		emailAddress.setText(savedEmailAddress);
	}

	private void saveNumber(String key, String value) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void btnAdd_actionPerformed(View view) {
		contactNumber = phoneNumber.getText().toString();
		saveNumber("parents_phone_number", contactNumber);

		if (contactNumber.length() != 0) {

			Toast.makeText(getApplicationContext(), "phone number saved",
					Toast.LENGTH_LONG).show();
		} else if (contactNumber.length() == 0) {
			Toast.makeText(getApplicationContext(), "Set a valid phone number",
					Toast.LENGTH_LONG).show();
		}
	}

	public void btnAddEmail_actionPerformed(View view) {
		contactEmail = emailAddress.getText().toString();

		if (isValidEmail(contactEmail) == true) {
			saveNumber("parents_email_address", contactEmail);
			Toast.makeText(getApplicationContext(), "E-mail address saved",
					Toast.LENGTH_LONG).show();

		} else if (isValidEmail(contactEmail) == false) {
			Toast.makeText(getApplicationContext(), "Invalid E-mail format",
					Toast.LENGTH_LONG).show();
		}

	}

	public static boolean isValidEmail(String email) {
		Boolean b = true;
		String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		b = email.matches(EMAIL_REGEX);
		return b;
	}

	@Override
	public void onClick(View v) {
		
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		startActivityForResult(i, 1);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Uri uri = data.getData();
			if (uri != null) {
				Cursor c = null;
				try {
					c = getContentResolver()
							.query(uri,
									new String[] {
											ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
											ContactsContract.CommonDataKinds.Phone.NUMBER,
											ContactsContract.CommonDataKinds.Phone.TYPE },
									null, null, null);

					if (c != null && c.moveToFirst()) {
						// String name = c.getString(0);
						// int type = c.getInt(2);
						String number = c.getString(1).replaceAll(
								"[ ( | ) | \\- ]", "");

						phoneNumber.setText(number);

						contactNumber = phoneNumber.getText().toString();
						saveNumber("parents_phone_number", contactNumber);
						Toast.makeText(getApplicationContext(),
								"phone number saved", Toast.LENGTH_LONG).show();
					}
				} finally {
					if (c != null) {
						c.close();
					}
				}
			}
		}
	}

}
