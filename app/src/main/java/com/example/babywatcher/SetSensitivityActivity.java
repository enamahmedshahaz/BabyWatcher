package com.example.babywatcher;

import android.os.Bundle;
import android.app.Activity;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SetSensitivityActivity extends Activity implements
		OnSeekBarChangeListener {

	public static int threshold = 5;
	private SeekBar seekbar;
	private TextView txtlevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensitivity);
		seekbar = (SeekBar) findViewById(R.id.seekBar);
		txtlevel = (TextView) findViewById(R.id.textLevel);
		seekbar.setOnSeekBarChangeListener(this);
		txtlevel.setText("Sensitivity:  " + threshold + "/" + seekbar.getMax());
		seekbar.setProgress(threshold);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		threshold = progress;
		txtlevel.setText("Sensitivity:  " + threshold + "/" + seekBar.getMax());
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

}
