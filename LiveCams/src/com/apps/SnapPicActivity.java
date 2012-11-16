package com.apps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.utils.Utils;
import com.vdsp.CmdSend;
import com.vdsp.DispatchHandler;
import com.vdsp.SettingAndStatus;

public class SnapPicActivity extends Activity {
	private final static String 	TAG ="LIVECAMS";
	private SurfaceView 			surfaceView=null;  
	private SurfaceHolder 			surfaceHolder=null;  
	private Camera 					camera=null;  
	private boolean					capturing=false;
	private Handler					oldHandler=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        	WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        oldHandler=DispatchHandler.setHandler(handler);  
        setupSurfaceView();
    }

    @Override
    public void onStop(){
    	DispatchHandler.setHandler(oldHandler);
    	super.onStop();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
    	if(KeyEvent.KEYCODE_CAMERA==keyCode){
    		if(!capturing){
    			capturing=true;
    			camera.takePicture(null,null,jpegCallback);
    		}else{
    			Log.v(TAG,"提示：相机忙，请稍后再拍!");
    		}
    		return true;
    	}else{
    		return(super.onKeyDown(keyCode,event));
    	}    	
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	int actionCode=event.getAction();
    	if(actionCode==MotionEvent.ACTION_DOWN){ 
    		if(!capturing){
    			capturing=true;
    			camera.takePicture(null,null,jpegCallback);
    		}else{
    			Log.v(TAG,"提示：相机忙，请稍后再拍!");
    		}
    		return true;
    	}else{
    		return(super.onTouchEvent(event));
    	}    	
    }
    
	/*界面消息处理*/
    private Handler handler = new Handler() {
    	public void handleMessage(android.os.Message msg){
    		switch(msg.what){
    		case 255:
    		case 254:
    			String info=(String)msg.obj;
    			Context context=getApplicationContext();
				Toast toast=Toast.makeText(context,info,
					255==msg.what?Toast.LENGTH_SHORT:Toast.LENGTH_LONG);
				toast.show();
    			break;
    		default:
    			super.handleMessage(msg);
    			break;
    		}
    	}
    };
    
    /*初始化界面*/
	private void setupSurfaceView(){
		surfaceView=(SurfaceView)findViewById(R.id.cameraPreview);
		surfaceHolder=surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	      
    /*启动照片处理任务*/
    private Camera.PictureCallback jpegCallback=new Camera.PictureCallback() {  
    	public void onPictureTaken(byte[] data, Camera camera) {  
    		new SavePictureTask().execute(data);
    		camera.startPreview();
    		capturing=false;
    	}  
    };
    
    /*上传照片和存成文件*/
    private class SavePictureTask extends AsyncTask<byte[], String, String> { 
    	protected String doInBackground(byte[]...param){
			Date date=new Date();
			int length=param[0].length;
			
			//上传照片
			CmdSend.uploadSnap(date,param[0],length,true);
 			
			//存成文件
    		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
    			if(SettingAndStatus.settings.picsave){
	    			SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmssSSS");
		    		String filename=format.format(date)+".jpg";
		    		String root=Environment.getExternalStorageDirectory().getPath();
		    		String path=root+"/LiveCams";
		    		File   file=new File(path);	
		    		if(!file.exists()){
		    			if(!file.mkdir()){
		    				Log.v(TAG,"无法创建目录："+path+"!");
		    			}
		    		}	  
		    		if(file.exists()){
			    		file=new File(path+"/"+filename);
			    		try{
			    			FileOutputStream fos=new FileOutputStream(file);
			    			fos.write(param[0]);
			    			fos.close();
			    			return file.getPath()+"("+length/1024+"KB)";
			    		}catch(Exception e){
			    			Log.v(TAG,"写照片文件失败!");  
			    			e.printStackTrace();
			    		}
		    		}
    			}
    		}else{
    			Log.v(TAG,"未检测到SD卡，请插入SD卡!");    			
    		}
    		
    		return null;
    	}
    	protected void onPostExecute(String result){
    		if(SettingAndStatus.settings.picsave){
	    		if(null!=result){
	    			Log.v(TAG,"照片： "+result);
	    			Utils.showShortNotice(SnapPicActivity.this,"照片："+result);
	    		}else{
	    			Log.v(TAG,"照片未能存成文件!");
	    			Utils.showLongNotice(SnapPicActivity.this,"警告：照片未能存成文件!");
	    		}
    		}
		}
    }
    
    /*相机操作*/
    private SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback(){
    	public void surfaceCreated(SurfaceHolder holder) {  
			camera = Camera.open();
			try {  
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {  
				camera.release();
				camera = null;  
			}  
    	}
    	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
    		Camera.Parameters param = camera.getParameters();
    		param.setPictureFormat(PixelFormat.JPEG);
    		if(0!=SettingAndStatus.settings.picwidth && 0!=SettingAndStatus.settings.picheight){
    			param.setPictureSize(SettingAndStatus.settings.picwidth,
    				SettingAndStatus.settings.picheight);
    		}
    		if(0!=SettingAndStatus.settings.picquality){
    			param.setJpegQuality(SettingAndStatus.settings.picquality);
    		}
    		camera.setParameters(param); 
    		camera.startPreview();
    	}
    	public void surfaceDestroyed(SurfaceHolder holder) { 
    		camera.stopPreview();
    		camera.release();
    		camera = null; 
    	}
    };   
}