package com.stonenotes.live;

import android.app.Application;

public class BaseApplication extends Application {
	private static BaseApplication instance;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		instance = this;
	}
	
	public static BaseApplication getInstance() {
		return instance;
	}

}
