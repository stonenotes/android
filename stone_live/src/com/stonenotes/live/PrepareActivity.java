package com.stonenotes.live;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class PrepareActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prepare);
	}
	
	public void startLive(View view) {
		EditText edit = (EditText) findViewById(R.id.edt_url);
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("url", edit.getText().toString().trim());
		startActivity(intent);
	}
}
