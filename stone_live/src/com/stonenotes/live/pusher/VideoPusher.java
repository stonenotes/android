package com.stonenotes.live.pusher;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.stonenotes.live.BaseApplication;
import com.stonenotes.live.jni.PushNative;
import com.stonenotes.live.params.VideoParam;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.WindowManager;

public class VideoPusher extends Pusher implements Callback, PreviewCallback {
	private static final String TAG = VideoPusher.class.getSimpleName();
	private SurfaceHolder surfaceHolder;
	private Camera mCamera;
	private VideoParam videoParams;
	private byte[] buffers;
	private boolean isPushing = false;
	private PushNative pushNative;

	public VideoPusher(SurfaceHolder surfaceHolder, VideoParam videoParams, PushNative pushNative) {
		this.surfaceHolder = surfaceHolder;
		this.videoParams = videoParams;
		this.pushNative = pushNative;
		surfaceHolder.addCallback(this);
	}

	@Override
	public void startPush() {
		// 设置视频参数
		pushNative.setVideoOptions(videoParams.getWidth(), videoParams.getHeight(), videoParams.getBitrate(),
				videoParams.getFps());
		isPushing = true;
	}

	@Override
	public void stopPush() {
		isPushing = false;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	@Override
	public void release() {
		stopPreview();
	}

	/**
	 * 切换摄像头
	 */
	public void switchCamera() {
		if (videoParams.getCameraId() == CameraInfo.CAMERA_FACING_BACK) {
			videoParams.setCameraId(CameraInfo.CAMERA_FACING_FRONT);
		} else {
			videoParams.setCameraId(CameraInfo.CAMERA_FACING_BACK);
		}
		// 重新预览
		stopPreview();
		startPreview();
	}

	/**
	 * 开始预览
	 */
	private void startPreview() {
		try {
			// SurfaceView初始化完成，开始相机预览
			mCamera = Camera.open(videoParams.getCameraId());
			// Camera.Parameters parameters = mCamera.getParameters();

			int PreviewWidth = 0;
			int PreviewHeight = 0;
			Camera.Parameters parameters = mCamera.getParameters();
			// 选择合适的预览尺寸
			List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
			
			for(Camera.Size size : sizeList) {
				Log.i(TAG, "w: " + size.width + ", h:" + size.height);
				if (size.width > 400 && size.width < 500 && size.height < 400) {
					PreviewWidth = size.width;
					PreviewHeight = size.height;
				}
			}
			if(PreviewWidth == 0) {
				PreviewWidth = sizeList.get(0).width;
				PreviewHeight = sizeList.get(0).height;
			}
			
			Log.i(TAG, "PreviewWidth: " + PreviewWidth + ", PreviewHeight:" + PreviewHeight);
			videoParams = new VideoParam(PreviewWidth, PreviewHeight, CameraInfo.CAMERA_FACING_BACK);
			// 设置相机参数
			parameters.setPreviewFormat(ImageFormat.NV21); // YUV 预览图像的像素格式
			parameters.setPreviewSize(videoParams.getWidth(), videoParams.getHeight()); // 预览画面宽高
			mCamera.setParameters(parameters);
			// parameters.setPreviewFpsRange(videoParams.getFps()-1,
			// videoParams.getFps());
			mCamera.setPreviewDisplay(surfaceHolder);
			// 获取预览图像数据
			buffers = new byte[videoParams.getWidth() * videoParams.getHeight() * 4];
			mCamera.addCallbackBuffer(buffers);
			mCamera.setPreviewCallbackWithBuffer(this);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 停止预览
	 */
	private void stopPreview() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (mCamera != null) {
			mCamera.addCallbackBuffer(buffers);
		}

		if (isPushing) {
			// 回调函数中获取图像数据，然后给Native代码编码
			pushNative.fireVideo(data);
		}
	}

}
