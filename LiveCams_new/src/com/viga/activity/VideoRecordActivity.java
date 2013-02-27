package com.viga.activity;

import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.vdsp.DispatchHandler;
import com.viga.engine.H264Stream;
import com.viga.engine.MyApplication;
import com.viga.engine.SettingAndStatus;
import com.viga.utils.Utils;

public class VideoRecordActivity extends Activity {
	private final static String 	TAG="LIVECAMS";
	private final static String 	TAG1="LIUWANGSHU";
	private SurfaceView 			surfaceView=null;  
	private SurfaceHolder 			surfaceHolder=null;  
	private Camera 					camera=null;  
	private MediaRecorder 			mediaRecorder=null;
	private Socket 					socket=null;
	private ParcelFileDescriptor 	sockfd=null;
	private Handler					oldHandler=null;
	private int 					requestCode;
    private SharedPreferences _sharedPreferences;
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        	WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.camera);
        MyApplication.getInstance().addActivity(VideoRecordActivity.this);
        Intent intent=this.getIntent();
        requestCode=intent.getIntExtra("requestcode",0);
        oldHandler=DispatchHandler.setHandler(handler); 
        setupSurfaceView();
        int i=this.getResources().getConfiguration().orientation;
    	Log.i(TAG1, i+"");
    	
    }

    @Override
	protected void onResume() {
    	
		super.onResume();
	}

	@Override
    public void onStop(){
    	if(DispatchHandler.isCurrent(handler)){
    		DispatchHandler.setHandler(oldHandler);
    	}
    	super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){ 
    	if(KeyEvent.KEYCODE_BACK==keyCode){
    		stopAvRecorder();
    		VideoRecordActivity.this.setResult(requestCode);
    	}
    	return(super.onKeyDown(keyCode,event));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
    	int actionCode=event.getAction();
    	if(actionCode==MotionEvent.ACTION_DOWN && null!=camera){ 
    		if(!SettingAndStatus.avrecording){
    			SettingAndStatus.avrecording=true;
    			
	    		try{
	    			//与数据接收线程建立TCP链接	            	
	    			socket=new Socket("localhost",SettingAndStatus.settings.mdaport);
	    			//byte[] ip=new byte[]{(byte)192,(byte)168,(byte)4,(byte)81};//发送给192.168.4.81机器
	    			//socket=new Socket(InetAddress.getByAddress(ip),SettingAndStatus.settings.mdaport);
	    			socket.setSendBufferSize(1024*1024);
	    			sockfd=ParcelFileDescriptor.fromSocket(socket);	 
	    			
	    			//停止预览并解锁
	    			camera.stopPreview();
	    			camera.unlock();
	    			
	    			//初始化录像机
	    			int width,height;
	    			
	    			mediaRecorder=new MediaRecorder();
	    			mediaRecorder.setCamera(camera);
	    			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
	    			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); 
	    			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	    			if(0!=SettingAndStatus.settings.vframerate){
	    				mediaRecorder.setVideoFrameRate(H264Stream.intVideoFps(SettingAndStatus.settings.vframerate));
	    			}
	    			if(0!=SettingAndStatus.settings.vbitrate){
	    				mediaRecorder.setVideoEncodingBitRate(SettingAndStatus.settings.vbitrate);
	    			}
	    			if(0!=(width=H264Stream.getVideoWidth(SettingAndStatus.settings.videosize)) && 
	    				0!=(height=H264Stream.getVideoHeight(SettingAndStatus.settings.videosize))){
	    				
	    				mediaRecorder.setVideoSize(width,height);
	    				SettingAndStatus.settings.localvideosize=SettingAndStatus.settings.videosize;
	    			}
	    			mediaRecorder.setOutputFile(sockfd.getFileDescriptor());
	    			//mediaRecorder.setOutputFile("/mnt/sdcard/LiveCams/good.3gp");//直接录文件
	    			mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

		    		//启动录像机
	    			mediaRecorder.prepare();
	            	mediaRecorder.start();
	            	
	            	
	            	
	            	Log.v(TAG,"启动了MediaRecorder！");
	    		}catch(IllegalStateException e){
	    			stopAvRecorder();
                    e.printStackTrace();
                }catch(IOException e){
                	stopAvRecorder();
                    e.printStackTrace();
                }
    		}else{
    			stopAvRecorder();
    			camera.lock();
        		camera.startPreview();
    			Log.v(TAG,"停止了MediaRecorder！");
            }
    		return true;
    	}else{
    		return(super.onTouchEvent(event));
    	}    	
    }
    
    /*停止录像*/
    private void stopAvRecorder(){
		releaseMediaRecord();
		releaseSocket();
		SettingAndStatus.avrecording=false;    	
    }
    
    /*释放录像机*/
    private void releaseMediaRecord(){
	    if(null!=mediaRecorder){
	    	mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder=null;
	    }
    }
    
    /*释放网络资源*/
    private void releaseSocket(){
    	try{
    		if(null!=sockfd){
    			sockfd.close();
    			sockfd=null;
    		}
    		if(null!=socket){
    			socket.shutdownOutput();
    			socket.close();
    			socket=null;
    		}
    	}catch(IOException e){
            e.printStackTrace();
        }
    }
    
    /*初始化界面*/
	private void setupSurfaceView(){
		surfaceView=(SurfaceView)findViewById(R.id.cameraPreview);
		surfaceHolder=surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
 	
	/*界面消息处理*/
    private Handler handler = new Handler(){
    	public void handleMessage(android.os.Message msg){
    		switch(msg.what){
    		case DispatchHandler.SHOW_SHORTNOTICE://弹出本地短通知
    		case DispatchHandler.SHOW_LONGNOTICE: //弹出本地长通知
    			String info=(String)msg.obj;
    			Context context=getApplicationContext();
				Toast toast=Toast.makeText(context,info,
					DispatchHandler.SHOW_SHORTNOTICE==msg.what?
					Toast.LENGTH_SHORT:Toast.LENGTH_LONG);
				toast.show();
    			break;
    		default:
    			super.handleMessage(msg);
    			break;
    		}
    	}
    };
   
    /*摄像头操作*/
    private SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback(){
    	public void surfaceCreated(SurfaceHolder holder){  
			camera=Camera.open();
			try{  
				camera.setPreviewDisplay(holder);
			}catch(IOException e){  
				camera.release();
				camera=null;
				e.printStackTrace();
			}
    	}
    	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
    		if(null!=camera){
        		camera.startPreview();    			
    		}
    	}
    	public void surfaceDestroyed(SurfaceHolder holder){ 
	    	if(null!=camera){
	    		camera.stopPreview();
	    		camera.release();
	    		camera=null; 
	    	}
    	}
    };   
}