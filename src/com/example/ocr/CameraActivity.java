package com.example.ocr;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import com.example.ocr.R;
import com.example.ocr.ViewfinderView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;



public class CameraActivity extends Activity implements CvCameraViewListener {
	private String TAG = "OCR ACTIVITY: ";
	private CameraBridgeViewBase mOpenCvCameraView;
	private ViewfinderView viewfinderView;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	     getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     setContentView(R.layout.camera_activity);
	     
	     viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
	     mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
	     mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	     mOpenCvCameraView.setCvCameraViewListener(this);
	     
	     Point size = new Point();
	     Display display = getWindowManager().getDefaultDisplay();
	     display.getSize(size);
	     
	     viewfinderView.SetSize(size);
	     
	     
	 }
	
	 @Override
	 public void onPause()
	 {
	     super.onPause();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }
	
	 public void onDestroy() {
	     super.onDestroy();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }
	
	 public void onCameraViewStarted(int width, int height) {
	 }
	
	 public void onCameraViewStopped() {
	 }
	
	 public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	     return inputFrame.rgba();
	 }
	 
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i(TAG, "OpenCV loaded successfully");
	                mOpenCvCameraView.enableView();
	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};
	
	@Override
	public void onResume()
	{
	    super.onResume();
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_7, this, mLoaderCallback);
	}

	@Override
	public Mat onCameraFrame(Mat inputFrame) {
		// TODO Auto-generated method stub
		return inputFrame;
	}
	
}
