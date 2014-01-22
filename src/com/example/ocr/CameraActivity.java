package com.example.ocr;


import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import com.example.ocr.R;
import com.example.ocr.ViewfinderView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;



public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	private String TAG = "OCR ACTIVITY: ";
	private CameraView mOpenCvCameraView;
	private ViewfinderView viewfinderView;
	private MatrixSizes mMatrix;
	
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i(TAG, "OpenCV loaded successfully");
	                mOpenCvCameraView.enableView();
	                mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	     getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     setContentView(R.layout.camera_activity);
	     
	     viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
	     mOpenCvCameraView = (CameraView) findViewById(R.id.HelloOpenCvView);
	     mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	     mOpenCvCameraView.setCvCameraViewListener(this); 
	     mOpenCvCameraView.setOnTouchListener(this); 
	     
	     //Obtain windows size
	     Point size = new Point();
	     Display display = getWindowManager().getDefaultDisplay();
	     display.getSize(size);
	     
	     mMatrix = new MatrixSizes();
	     
	     mMatrix.WinWidth  = size.x;
	     mMatrix.WinHeigth = size.y;
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
	   	 //Obtain and set preview size
		 Point size = new Point();
		 
	     Size preSize = mOpenCvCameraView.getResolution();
	     size.set(preSize.width, preSize.height);
	     
	     
	     Log.i(TAG, "Preview size " + preSize.width + "x" + preSize.height);
	     
	     mMatrix.ResWidth  	= size.x;
	     mMatrix.ResHeigth 	= size.y;
	     mMatrix.MatWidth 	= (int) ((double) mMatrix.ResWidth * 0.95);
	     mMatrix.MatHeigth 	= (int) ((double) mMatrix.ResWidth * 0.95 / 1.9);
	     mMatrix.CellWidth  = mMatrix.MatWidth / 10;
	     mMatrix.CellHeigth = mMatrix.MatHeigth / 6;
	     
	     
	     
	     Log.i(TAG, "Hasta Aqui va la cosa size " + mMatrix.ResWidth  + "x" + mMatrix.ResHeigth
	    		 + mMatrix.MatWidth  + "x" + mMatrix.MatHeigth
	    		 + mMatrix.ResWidth  + "x" + mMatrix.CellHeigth);
	     
	     mOpenCvCameraView.setMarix(mMatrix);
	     viewfinderView.setMatrix(mMatrix);
	 }
	
	 public void onCameraViewStopped() {
	 }
	
	 public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	     return inputFrame.rgba();
	 }
	
	@Override
	public void onResume()
	{
	    super.onResume();
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_7, this, mLoaderCallback);
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.i(TAG,"onTouch event");
	    //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	    //String currentDateandTime = sdf.format(new Date());
	    //String fileName = Environment.getExternalStorageDirectory().getPath() +
	    //                       "/sample_picture_" + currentDateandTime + ".jpg";
	    mOpenCvCameraView.takePicture();
	    Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show();
	        
		return false;
	}
	
	/*@Override
	public Mat onCameraFrame(Mat inputFrame) {
		// TODO Auto-generated method stub
		return inputFrame;
	}*/

}
