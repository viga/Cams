package com.apps;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.vdsp.CmdSend;
import com.vdsp.DataProc;
import com.vdsp.DispatchHandler;
import com.vdsp.SettingAndStatus;

public class VideoPlayActivity extends Activity implements OnTouchListener,OnSeekBarChangeListener{
	private Handler			oldHandler=null;
	private SurfaceView 	surfaceView=null;  
	private SurfaceHolder 	surfaceHolder=null;  
	private RelativeLayout  playrl;
	private TextView 		vpbtv,apbtv;
	private TextView 		brightnesstv,huetv,saturationtv,contrasttv;
	private SeekBar 		brightnesssb,huesb,saturationsb,contrastsb;
	private TextView 		tv_framerate,tv_bitrate,tv_interval,tv_maxframerate,tv_maxbitrate,tv_maxinterval;
	private SeekBar 		sb_framerate,sb_bitrate,sb_interval;
	private int 			requestCode;
	private int				videoid;
	private boolean			motionCtrlFlag=false;
	private boolean			videoAdjustFlag=false;
	private boolean			encoderAdjustFlag=false;
	private PopupWindow 	motionCtrlPanel=null;
	private PopupWindow		videoAdjustPanel=null;
	private PopupWindow		encoderAdjustPanel=null;
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        	WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.play);
        
        Intent intent=this.getIntent();
        requestCode=intent.getIntExtra("requestcode",0);
        videoid=intent.getIntExtra("videoid",0);
        oldHandler=DispatchHandler.setHandler(handler); 
		
		surfaceView=(SurfaceView)findViewById(R.id.videoSv);
		surfaceHolder=surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceCallback);
		apbtv=(TextView)findViewById(R.id.audioTv);
        vpbtv=(TextView)findViewById(R.id.videoTv);
        TextView ttv=(TextView)findViewById(R.id.titleTv);
        ttv.setText(0==videoid?getString(R.string.servervideo):
        	(1==videoid?getString(R.string.frontvideo):
        	(2==videoid?getString(R.string.behindvideo):"")));
        
        playrl=(RelativeLayout)findViewById(R.id.playRl);
        playrl.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v,int keyCode,KeyEvent event) {
				if(KeyEvent.KEYCODE_BACK==keyCode) {
					if(motionCtrlFlag){						
						motionCtrlPanel.dismiss();
						motionCtrlFlag=false;
					}else if(videoAdjustFlag){
						videoAdjustPanel.dismiss();
						videoAdjustFlag=false;
					}else if(encoderAdjustFlag){
						encoderAdjustPanel.dismiss();
						encoderAdjustFlag=false;
					}else{
						VideoPlayActivity.this.setResult(requestCode);
					}
				}
				return true;
			}
        });
        
        CmdSend.getVideoQuality(true);
        CmdSend.getEncoderParam(true);
    }

    @Override
    public void onStop(){
    	if(DispatchHandler.isCurrent(handler)){
    		DispatchHandler.setHandler(oldHandler);
    	}
    	super.onStop();
    }
    
    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	if(KeyEvent.KEYCODE_BACK==keyCode){
    		VideoPlayActivity.this.setResult(requestCode);
    	}
    	return(super.onKeyDown(keyCode,event));
    }*/

    /*云台控制按钮操作处理*/
	public boolean onTouch(View v, MotionEvent event){
		switch(v.getId()){
		case R.id.toup:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.moveToUp();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.todown:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.moveToDown();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.toleft:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.moveToLeft();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.toright:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.moveToRight();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.faster:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.moveFaster();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.slower:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.moveSlower();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.brighten:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.brightenAperture();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.darken:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.darkenAperture();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.nearer:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.focusToNear();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		case R.id.farer:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				CmdSend.focusToFar();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				CmdSend.stopMotionCtrl();
			}
			break;
		default:
			break;
		}
		
		return true;
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	int actionCode=event.getAction();
    	if(actionCode==MotionEvent.ACTION_DOWN){ 
    		if(!motionCtrlFlag && !videoAdjustFlag){
    			CmdSend.noticeSnap(1,true);
    			return true;
    		}
    	}
    	return(super.onTouchEvent(event));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
    	if(0!=videoid){//只有背负视频才能控制调节
    		menu.removeItem(Menu.FIRST+1);
	    	if(!SettingAndStatus.deviceDataSending){
				menu.add(Menu.NONE,Menu.FIRST+1,1,"上传视频").setIcon(
					android.R.drawable.ic_menu_share);
	    	}else{
				menu.add(Menu.NONE,Menu.FIRST+1,1,"停止上传").setIcon(
					android.R.drawable.ic_menu_share);
	    	}
			return true;
    	}else{
    		return false;
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	if(0!=videoid){//只有背负视频才能控制调节
	    	if(!SettingAndStatus.deviceDataSending){
				menu.add(Menu.NONE,Menu.FIRST+1,1,"上传视频").setIcon(
					android.R.drawable.ic_menu_share);
	    	}else{
				menu.add(Menu.NONE,Menu.FIRST+1,1,"停止上传").setIcon(
					android.R.drawable.ic_menu_share);    		
	    	}
			menu.add(Menu.NONE,Menu.FIRST+2,2,"编码设置").setIcon(
				android.R.drawable.ic_menu_edit);
			menu.add(Menu.NONE,Menu.FIRST+3,3,"视频微调").setIcon(
					android.R.drawable.ic_menu_agenda);
			menu.add(Menu.NONE,Menu.FIRST+4,4,"云台控制").setIcon(
					android.R.drawable.ic_menu_sort_by_size);
			return true;
    	}else{
    		return false;
    	}
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(0!=videoid){//只有背负视频才能控制调节
			switch(item.getItemId()){
			case Menu.FIRST + 1:
				if(!SettingAndStatus.deviceDataSending){
					CmdSend.requestSendData(videoid,true,true,true);
				}else{
					SettingAndStatus.deviceDataSending=false;
					CmdSend.stopSendData(false);
				}
				break;
			case Menu.FIRST + 2:
				encoderAdjustFlag=true;
				if(null==encoderAdjustPanel){
					encoderAdjustInit(SettingAndStatus.encoderParam.videosize,
						SettingAndStatus.encoderParam.bitrate,
						SettingAndStatus.encoderParam.framerate,
						SettingAndStatus.encoderParam.maxframerate,
						SettingAndStatus.encoderParam.interval);
				}
				encoderAdjustPanel.showAtLocation(playrl,Gravity.BOTTOM|Gravity.LEFT,0,50);
				break;
			case Menu.FIRST + 3:
				videoAdjustFlag=true;
				if(null==videoAdjustPanel){
					videoAdjustInit(SettingAndStatus.videoQuality.brightness,
						SettingAndStatus.videoQuality.hue,
						SettingAndStatus.videoQuality.contrast,
						SettingAndStatus.videoQuality.saturation);
				}
				videoAdjustPanel.showAtLocation(playrl,Gravity.BOTTOM|Gravity.LEFT,0,50);
				break;
			case Menu.FIRST + 4:
				motionCtrlFlag=true;
				if(null==motionCtrlPanel){
					motionCtrlInit();
				}
				motionCtrlPanel.showAtLocation(playrl,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
				break;
			default:
				break;
			}
			return true;
		}else{
			return false;
		}
	}
	
	/*视频微调操作处理*/
	private final static long interval=1000;
	private long begintime;
	public void onStartTrackingTouch(SeekBar seekBar){
		begintime=(new Date()).getTime();
	}
	public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser){
		long currenttime=(new Date()).getTime();
		if(videoAdjustFlag){
			if(seekBar==brightnesssb && SettingAndStatus.videoQuality.brightness!=progress){
				brightnesstv.setText(""+progress);
				if(currenttime-begintime>interval){
					CmdSend.adjustVideoQuality(progress,256,256,256,false);
					SettingAndStatus.videoQuality.brightness=progress;
					begintime=currenttime;
				}
			}else if(seekBar==huesb && SettingAndStatus.videoQuality.hue!=progress){
				huetv.setText(""+(progress-128));
				if(currenttime-begintime>interval){
					CmdSend.adjustVideoQuality(256,progress-128,256,256,false);
					SettingAndStatus.videoQuality.hue=progress;
					begintime=currenttime;
				}			
			}else if (seekBar==contrastsb && SettingAndStatus.videoQuality.contrast!=progress){
				contrasttv.setText(""+progress);
				if(currenttime-begintime>interval){
					CmdSend.adjustVideoQuality(256,256,progress,256,false);
					SettingAndStatus.videoQuality.contrast=progress;
					begintime=currenttime;
				}
			}else if (seekBar==saturationsb && SettingAndStatus.videoQuality.saturation!=progress){
				saturationtv.setText(""+progress);
				if(currenttime-begintime>interval){
					CmdSend.adjustVideoQuality(256,256,256,progress,false);
					SettingAndStatus.videoQuality.saturation=progress;
					begintime=currenttime;
				}
			}
		}
		if(encoderAdjustFlag){
			if(seekBar==sb_bitrate && SettingAndStatus.encoderParam.bitrate!=progress){
				tv_bitrate.setText(""+progress);
				if(currenttime-begintime>interval){
					CmdSend.adjustEncoderParam(-1,progress*1000,-1,-1,false);
					SettingAndStatus.encoderParam.bitrate=progress;
					begintime=currenttime;
				}
			}else if (seekBar==sb_framerate && SettingAndStatus.encoderParam.framerate!=progress){
				tv_framerate.setText(""+progress);
				if(currenttime-begintime>interval){
					CmdSend.adjustEncoderParam(-1,-1,progress,-1,false);
					SettingAndStatus.encoderParam.framerate=progress;
					begintime=currenttime;
				}
			}else if (seekBar==sb_interval && SettingAndStatus.encoderParam.interval!=progress){
				tv_interval.setText(""+progress);
				if(currenttime-begintime>interval){
					CmdSend.adjustEncoderParam(-1,-1,-1,progress,false);
					SettingAndStatus.encoderParam.interval=progress;
					begintime=currenttime;
				}
			}
		}
	}
	public void onStopTrackingTouch(SeekBar seekBar){
		int progress=seekBar.getProgress();
		if(videoAdjustFlag){
			if(seekBar==brightnesssb && SettingAndStatus.videoQuality.brightness!=progress){
				brightnesstv.setText(""+progress);
				CmdSend.adjustVideoQuality(progress,256,256,256,false);
				SettingAndStatus.videoQuality.brightness=progress;
			}else if(seekBar==huesb && SettingAndStatus.videoQuality.hue!=progress){
				huetv.setText(""+(progress-128));
				CmdSend.adjustVideoQuality(256,progress-128,256,256,false);
				SettingAndStatus.videoQuality.hue=progress;
			}else if (seekBar==contrastsb && SettingAndStatus.videoQuality.contrast!=progress){
				contrasttv.setText(""+progress);
				CmdSend.adjustVideoQuality(256,256,progress,256,false);
				SettingAndStatus.videoQuality.contrast=progress;
			}else if (seekBar==saturationsb && SettingAndStatus.videoQuality.saturation!=progress){
				saturationtv.setText(""+progress);
				CmdSend.adjustVideoQuality(256,256,256,progress,false);
				SettingAndStatus.videoQuality.saturation=progress;
			}
		}
		if(encoderAdjustFlag){
			if(seekBar==sb_bitrate && SettingAndStatus.encoderParam.bitrate!=progress){
				tv_bitrate.setText(""+progress);
				CmdSend.adjustEncoderParam(-1,progress*1000,-1,-1,false);
				SettingAndStatus.encoderParam.bitrate=progress;
			}else if (seekBar==sb_framerate && SettingAndStatus.encoderParam.framerate!=progress){
				tv_framerate.setText(""+progress);
				CmdSend.adjustEncoderParam(-1,-1,progress,-1,false);
				SettingAndStatus.encoderParam.framerate=progress;
			}else if (seekBar==sb_interval && SettingAndStatus.encoderParam.interval!=progress){
				tv_interval.setText(""+progress);
				CmdSend.adjustEncoderParam(-1,-1,-1,progress,false);
				SettingAndStatus.encoderParam.interval=progress;
			}
		}
	}
	
	/*云台控制初始化*/
	private void motionCtrlInit(){
		//设置云台控制按钮操作处理函数
		View v=View.inflate(VideoPlayActivity.this,R.layout.motion_control,null);
		v.setOnTouchListener(this);
		v.findViewById(R.id.toup).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.toleft).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.toright).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.todown).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.slower).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.faster).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.brighten).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.darken).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.farer).setOnTouchListener(VideoPlayActivity.this);
		v.findViewById(R.id.nearer).setOnTouchListener(VideoPlayActivity.this);

		//初始化云台控制按钮并显示
		motionCtrlPanel=new PopupWindow(v,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,true);
		motionCtrlPanel.setBackgroundDrawable(null);
		motionCtrlPanel.setBackgroundDrawable(new BitmapDrawable());
		motionCtrlPanel.setFocusable(true);
		motionCtrlPanel.setFocusable(true);
	}

	/*视频微调初始化*/
	private void videoAdjustInit(int brightness,int hue,int contrast,int saturation){
		//获取视频微调相关控件
		View v=View.inflate(VideoPlayActivity.this,R.layout.video_adjust,null);
		brightnesstv=(TextView)v.findViewById(R.id.tv_brightness);
		huetv=(TextView)v.findViewById(R.id.tv_hue);
		contrasttv=(TextView)v.findViewById(R.id.tv_contrast);
		saturationtv=(TextView)v.findViewById(R.id.tv_saturation);
		brightnesssb=(SeekBar)v.findViewById(R.id.sb_brightness);
		huesb=(SeekBar)v.findViewById(R.id.sb_hue);
		contrastsb=(SeekBar)v.findViewById(R.id.sb_contrast);
		saturationsb=(SeekBar)v.findViewById(R.id.sb_saturation);

		//设置最大值和当前值
		brightnesssb.setMax(255);
		brightnesssb.setProgress(brightness);
		brightnesstv.setText(""+brightness);
		huesb.setMax(255);
		huesb.setProgress(hue+128);
		huetv.setText(""+hue);
		contrastsb.setMax(255);
		contrastsb.setProgress(contrast);
		contrasttv.setText(""+contrast);
		saturationsb.setMax(255);
		saturationsb.setProgress(saturation);
		saturationtv.setText(""+saturation);
		
		//设置监听划动改变
		brightnesssb.setOnSeekBarChangeListener(VideoPlayActivity.this);
		huesb.setOnSeekBarChangeListener(VideoPlayActivity.this);
		saturationsb.setOnSeekBarChangeListener(VideoPlayActivity.this);
		contrastsb.setOnSeekBarChangeListener(VideoPlayActivity.this);
		
		//初始化视频微调界面
		videoAdjustPanel=new PopupWindow(v,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,true);
		videoAdjustPanel.setBackgroundDrawable(null);
		videoAdjustPanel.setBackgroundDrawable(new BitmapDrawable());
		videoAdjustPanel.setFocusable(true);
		videoAdjustPanel.setFocusable(true);
	}

	/*编码微调初始化*/
	private void encoderAdjustInit(int videosize,int bitrate,int framerate,int maxframerate,int interval){
		View v=View.inflate(VideoPlayActivity.this,R.layout.encoder_adjust,null);
		
		//显示当前视频尺寸
		RadioButton bt=null;
		if(0==videosize){
			bt=(RadioButton)v.findViewById(R.id.rb_cif);
		}else if(1==videosize){
			bt=(RadioButton)v.findViewById(R.id.rb_hd1);
		}else if(2==videosize){
			bt=(RadioButton)v.findViewById(R.id.rb_d1);
		}
		if(null!=bt){
			bt.setChecked(true);
		}
		
		//设置监听视频尺寸切换
		RadioGroup rg=(RadioGroup)v.findViewById(R.id.rg_videosize);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(RadioGroup group,int checkedId){
				if(checkedId==R.id.rb_cif){
					CmdSend.adjustEncoderParam(0,-1,-1,-1,false);
				}else if(checkedId==R.id.rb_hd1){
					CmdSend.adjustEncoderParam(1,-1,-1,-1,false);
				}else if(checkedId==R.id.rb_d1){
					CmdSend.adjustEncoderParam(2,-1,-1,-1,false);
				}
			}
		});
		
		
		//获取编码微调相关控件
		sb_framerate = (SeekBar) v.findViewById(R.id.sb_framerate);
		sb_bitrate = (SeekBar) v.findViewById(R.id.sb_bitrate);
		sb_interval = (SeekBar) v.findViewById(R.id.sb_interval);
		tv_framerate = (TextView) v.findViewById(R.id.tv_framerate);
		tv_bitrate = (TextView) v.findViewById(R.id.tv_bitrate);
		tv_interval = (TextView) v.findViewById(R.id.tv_interval);
		tv_maxframerate = (TextView) v.findViewById(R.id.tv_maxframerate);
		tv_maxbitrate = (TextView) v.findViewById(R.id.tv_maxbitrate);
		tv_maxinterval = (TextView) v.findViewById(R.id.tv_maxinterval);

		//设置最大值和当前值
		sb_bitrate.setMax(2500);
		sb_bitrate.setProgress(bitrate/1000);
		tv_bitrate.setText(bitrate/1000+"");
		tv_maxbitrate.setText("2500kbps");
		sb_framerate.setMax(maxframerate);
		sb_framerate.setProgress(framerate);
		tv_framerate.setText(framerate+"");
		tv_maxframerate.setText(maxframerate+"fps");
		sb_interval.setMax(100);
		sb_interval.setProgress(interval);
		tv_interval.setText(interval+"");
		tv_maxinterval.setText("120帧");

		//设置监听划动改变
		sb_framerate.setOnSeekBarChangeListener(VideoPlayActivity.this);
		sb_bitrate.setOnSeekBarChangeListener(VideoPlayActivity.this);
		sb_interval.setOnSeekBarChangeListener(VideoPlayActivity.this);
		
		//初始化编码微调界面
		encoderAdjustPanel=new PopupWindow(v,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,true);
		encoderAdjustPanel.setBackgroundDrawable(null);
		encoderAdjustPanel.setBackgroundDrawable(new BitmapDrawable());
		encoderAdjustPanel.setFocusable(true);
		encoderAdjustPanel.setFocusable(true);
	}
	
    /*界面消息处理*/
    private Handler handler=new Handler(){
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
    		case 127://刷新音视频码率统计
				float bitrate=msg.arg2/1000;
    			if(0==msg.arg1){
    				String str=new String("视频："+bitrate+"kbps");
    				vpbtv.setText(str);
    			}else if(1==msg.arg1){
    				String str=new String("音频："+bitrate+"kbps");
    				apbtv.setText(str);
    			}
    			break;
    		default:
    			super.handleMessage(msg);
    			break;
    		}
    	}
    };
    
    /*向音视频解码播放线程传递句柄*/
    private SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback(){
    	public void surfaceCreated(SurfaceHolder holder){
    		if(null!=DataProc.videoRxAndPlay){
    			DataProc.videoRxAndPlay.setUserData(holder);
    		}    			
    	}
    	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
    	}
    	public void surfaceDestroyed(SurfaceHolder holder){ 
    		if(null!=DataProc.videoRxAndPlay){
    			synchronized(holder){
    				DataProc.videoRxAndPlay.setUserData(null);
    			}
    		}    			
    	}
    };   
}
