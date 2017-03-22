package com.stonenotes.live;

import com.stonenotes.live.R;
import com.stonenotes.live.jni.PushNative;
import com.stonenotes.live.listener.LiveStateChangeListener;
import com.stonenotes.live.pusher.LivePusher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements LiveStateChangeListener {

	static String URL = "rtmp://192.168.0.103:1935/live/stone";
	private LivePusher live;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PushNative.CONNECT_FAILED:
				Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
				break;
			case PushNative.INIT_FAILED:
				Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
				break;	
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		URL = getIntent().getStringExtra("url");
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
		//相机图像的预览
		live = new LivePusher(surfaceView.getHolder());
	}

	/**
	 * 开始直播
	 * @param btn
	 */
	public void mStartLive(View view) {
		Button btn = (Button)view;
		if(btn.getText().equals("开始直播")){
			live.startPush(URL,this);
			btn.setText("停止直播");
		}else{
			live.stopPush();
			btn.setText("开始直播");
		}
	}

	/**
	 * 切换摄像头
	 * @param btn
	 */
	public void mSwitchCamera(View btn) {
		live.switchCamera();
	}

	//改方法执行仍然在子线程中，发送消息到UI主线程
	@Override
	public void onError(int code) {
		handler.sendEmptyMessage(code);
	}

}
