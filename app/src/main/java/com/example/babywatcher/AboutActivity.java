package com.example.babywatcher;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}

	public void about_us(View view) {
		Intent intent = new Intent(this, AboutAuthorActivity.class);
		startActivity(intent);
	}

	public void show_help(View view) {
		Intent intent = new Intent(this, HelpActivity.class);
		startActivity(intent);
	}

}
