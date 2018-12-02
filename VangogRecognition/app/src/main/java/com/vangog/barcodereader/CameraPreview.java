package com.vangog.barcodereader;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private static final String TAG = "camera";
    private int mWidth, mHeight;
    private Context mContext;
    private MultiFormatReader mMultiFormatReader;
    private AlertDialog mDialog;
    private int mLeft, mTop, mAreaWidth, mAreaHeight;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        Parameters params = mCamera.getParameters();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            params.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
        }
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);

        //*EDIT*//params.setFocusMode("continuous-picture");
        //It is better to use defined constraints as opposed to String, thanks to AbdelHady
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mWidth = mCamera.getParameters().getPreviewSize().height;
        mHeight = mCamera.getParameters().getPreviewSize().width;
        
        params.setPreviewSize(mHeight, mWidth);
        mCamera.setParameters(params);
        
        mMultiFormatReader = new MultiFormatReader();
        
        mDialog =  new AlertDialog.Builder(mContext).create();
    }

    @Override
	public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null){
          return;
        }

        try {
            mCamera.stopPreview();
            
        } catch (Exception e){

        }

        try {
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    public void setCamera(Camera camera) {
    	mCamera = camera;
    }
    
    public void onPause() {
    	if (mCamera != null) {
    		mCamera.setPreviewCallback(null);
    		mCamera.stopPreview();
    	}
    }
    
    private PreviewCallback mPreviewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
        	
        	if (mDialog.isShowing())
        		return;

            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < mWidth; y++) {
                for (int x = 0; x < mHeight; x++)
                    rotatedData[x * mWidth + mWidth - y - 1] = data[x + y * mHeight];
            }

            LuminanceSource source = new PlanarYUVLuminanceSource(rotatedData, mWidth, mHeight, mLeft, mTop, mAreaWidth, mAreaHeight, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
              source));
            Result result;
          
            try {
				result = mMultiFormatReader.decode(bitmap, null);
				if (result != null) {
					mDialog.setTitle("Result");
					mDialog.setMessage(result.getText());
					mDialog.show();
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    };
    
    public void setArea(int left, int top, int areaWidth, int width) {
    	double ratio = width / mWidth;
    	mLeft = (int) (left / (ratio + 1));
    	mTop = (int) (top / (ratio + 1));
    	mAreaHeight = mAreaWidth = mWidth - mLeft * 2;
    }
    
}
