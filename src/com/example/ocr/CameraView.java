package com.example.ocr;

import java.io.FileOutputStream;
import java.util.List;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;

public class CameraView extends JavaCameraView implements PictureCallback {

	private static final String TAG = "OCR ACTIVITY: Taking Photo:";
    private String mPictureFileName = Environment.getExternalStorageDirectory().getPath() + "/carpeta/muestra.jpg";
    private MatrixSizes mMatrix;
	
    public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mMatrix = new MatrixSizes();
	}
    /*
    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }*/

    /*
    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }*/

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    
    public void takePicture() {
        Log.i(TAG, "Taking picture");

        
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        Log.i(TAG, "Taking picture and saving to "+mPictureFileName);
        
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        Bitmap bit = BitmapFactory.decodeByteArray(data,0, data.length, sizeOptions);
        Mat Image = new Mat();
        Utils.bitmapToMat(bit,Image);
        
        
        Log.i(TAG, "Bitmap is " + sizeOptions.outWidth + "x" + sizeOptions.outHeight);
        Log.i(TAG, "Bitmap is " + Image.cols() + "x" + Image.rows()+ " " +Image.channels() );
        
        
        
        
        int newWidth	= (int) ((double) sizeOptions.outWidth * 0.95);
	    int newHeight 	= (int) ((double) sizeOptions.outWidth * 0.95 / 1.9);
        //int newWidth 	=	mMatrix.MatWidth * sizeOptions.outWidth / mMatrix.ResWidth;
        //int newHeight 	= 	mMatrix.MatHeigth * sizeOptions.outHeight / mMatrix.ResHeigth;
        int centerx 	= 	sizeOptions.outWidth / 2;
        int centery 	=  	sizeOptions.outHeight / 2;
        
        Log.i(TAG, "Bitmap is " + (centerx - newWidth / 2) + "x" + (centery - newHeight/2)+ " " +newWidth + "x" + newHeight);
        //Rect roi = new Rect(, centery - newHeight/2, newWidth, newHeight);
        Mat cropped = new Mat(Image,	new Range(centery - newHeight/2, centery + newHeight/2), 
        								new Range(centerx - newWidth / 2, centerx + newWidth / 2));

       // bit = Bitmap.createBitmap(cropped.width(), cropped.height(),Bitmap.Config.ARGB_8888);
        Log.i(TAG, "Bitmap is " + cropped.width()+ "x" + cropped.height());
       // Utils.matToBitmap(cropped, bit);
        
        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);

            bit.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e(TAG, "Exception in photoCallback", e);
        }

    }
    
    public void setMarix(MatrixSizes mMat){
    	mMatrix = mMat;
    }
}
