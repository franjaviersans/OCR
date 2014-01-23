package com.example.ocr;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.imgproc.Imgproc;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import com.example.ocr.R;
import com.example.ocr.ViewfinderView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;



public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	private String TAG = "OCR ACTIVITY: ";
	private CameraView mOpenCvCameraView;
	private ViewfinderView viewfinderView;
	private MatrixSizes mMatrix;
	private Mat lastPreview;
	private Rect frame;
	private TessBaseAPI baseApi;
	
	
	
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
	     frame = new Rect();
	     
	     //Init Teressat
	     baseApi = new TessBaseAPI();
	     baseApi.init(Environment.getExternalStorageDirectory().getPath()+File.separator, "eng");
	     baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
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
	    
	     mMatrix.ResWidth  	= width;
	     mMatrix.ResHeigth 	= height;
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
		lastPreview = inputFrame.rgba();
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
	    //mOpenCvCameraView.takePicture();
	    //Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show();
	    
			
		Log.i(TAG, "Bitmap is " + lastPreview.width()+ "x" + lastPreview.height());
		
		int width = mMatrix.MatWidth / 2;
		int height = mMatrix.MatHeigth / 2;
		
		
		
		frame.left 	= mMatrix.ResWidth/2 - width;
		frame.right	= mMatrix.ResWidth/2 + width;
		frame.top 	= mMatrix.ResHeigth/2 - height;
		frame.bottom = mMatrix.ResHeigth/2 + height;
		
		
		OCRTask OCRT = new OCRTask();
		OCRT.execute(lastPreview);
		
			
			
		return false;
	}
	
	/*@Override
	public Mat onCameraFrame(Mat inputFrame) {
		// TODO Auto-generated method stub
		return inputFrame;
	}*/
	
	

	private class OCRTask extends AsyncTask<Mat, Void, boolean[]>{
	    //private final String CLASS_NAME = OCRTask.class.getSimpleName();
		
		@Override
		protected boolean[] doInBackground(Mat... ImageMat) {
			ArrayList<String> FinalCells = new ArrayList<String>();
			Bitmap bit;
	    	String mPictureFileName;
	    	Mat ProcFrame = ImageMat[0].clone();
	    	Mat cropped;
	    	FileOutputStream fos;
	    	
	    	Log.i(TAG, "Init del Tesseract");
	    	
	    	
	    	for(int i=0;i<6;++i)
				for(int j=0;j<10;++j)
				{
					Log.i(TAG, "Va por la celda  " + i+" "+j);
					mPictureFileName = Environment.getExternalStorageDirectory().getPath() + "/carpeta/muestra"+i+j+".jpg";
					
					cropped = new Mat(ProcFrame,	
										new Range(frame.top + (mMatrix.CellHeigth * i), frame.top + (mMatrix.CellHeigth * (i+1)) - 10),
										new Range(frame.left + (mMatrix.CellWidth * j) + 10,frame.left + (mMatrix.CellWidth * (j+1) - 10)));
					
					
					Imgproc.cvtColor(cropped, cropped, Imgproc.COLOR_BGR2GRAY);                
                    Imgproc.medianBlur(cropped, cropped, 3);
                    Imgproc.threshold(cropped, cropped, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU );
                    
					
					//DO THE OCR
					bit = Bitmap.createBitmap(cropped.width(), cropped.height(),Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(cropped, bit);
					
					baseApi.setImage(ReadFile.readBitmap(bit));
				    String textResult = baseApi.getUTF8Text();
				    
				    Log.i(TAG, "Obtuve  " + textResult);
					//END OCR
				    
				    FinalCells.add(textResult);
				    
				    
				    
					try {
					    fos = new FileOutputStream(mPictureFileName);
					
					    bit.compress(Bitmap.CompressFormat.JPEG, 95, fos);
					    fos.close();
					
					} catch (java.io.IOException e) {
					    Log.e(TAG, "Exception in photoCallback", e);
					}
				}
	    	
	    	
	    		for(int i=0;i<FinalCells.size();++i)
	    		{
	    			Log.i(TAG, "CELDAS!!!!! " + FinalCells.get(i));
	    		}
	    	
	    	boolean v[] = new boolean[1];
	    	return v;
		}
	}
}

