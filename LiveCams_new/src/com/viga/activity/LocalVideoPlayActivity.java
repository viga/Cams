package com.viga.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.vdsp.CmdSend;
import com.vdsp.DispatchHandler;
import com.vdsp.Vca;
import com.vdsp.Vca.Arg;
import com.viga.engine.DataProc;
import com.viga.engine.SettingAndStatus;
import com.viga.utils.Utils;

public class LocalVideoPlayActivity extends Activity implements OnClickListener {
	private final static int RESTART = 1;
	private SurfaceView surfaceView = null;
	private SurfaceHolder surfaceHolder = null;
	private PowerManager.WakeLock mw;
	private ProgressBar _progressBar;
	private static int refreshSize = 4000;
    private ImageButton _imageButton;
    private Intent _intent;
    private int totalLen;
    private int position;
    private Boolean begin=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.local_play);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mw = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myLock");
		mw.acquire();
		DataProc.startRxAndPlay(0, 8, null, null);
		Vca vca = new Vca();
		Vca.Arg arg = vca.new Arg(1, -1);
		arg.ip[0] = Utils.ipaddrToInt("127.0.0.1");
		short port = (short) Vca.control(Vca.OPID_GETRCVPORT, arg);
		arg.port[0] = port;
	    Vca.control(Vca.OPID_ADDDST, arg);
	
		DispatchHandler.setHandler(handler);
		
		_intent=this.getIntent();
		Bundle bundle=_intent.getExtras();
		totalLen=(Integer) bundle.get("totalLen");
		position=(Integer) bundle.get("position");
		initUI();   
	}

	private void initUI() {
		surfaceView = (SurfaceView) findViewById(R.id.local_videoSv);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceCallback);
		_progressBar = (ProgressBar) this.findViewById(R.id.localprogressBar);
		_progressBar.setMax(totalLen);
		_imageButton=(ImageButton) this.findViewById(R.id.local_imageButton);	
		_imageButton.setOnClickListener(this);
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 212:
				int total = msg.arg2;
				if(begin){
				int size=(int) (total*0.03);//首次进入进度条的计算矫正误差
				refreshSize=size+ msg.arg1;
				begin=false;
				}else{
					refreshSize+=msg.arg1;
					
				}
				if (refreshSize > total || refreshSize == total) {
					refreshSize = 0;
					_imageButton.setVisibility(View.VISIBLE);
				}
				_progressBar.setProgress(refreshSize);

				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		DataProc.stopAudioRxAndPlay();
		DataProc.stopVideoRxAndPlay();
		this.finish();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		DataProc.stopAudioRxAndPlay();
		DataProc.stopVideoRxAndPlay();
	
		if (mw != null) {
			mw.release();
			mw = null;
		}
		if(0!=refreshSize){
			DataProc.stopUnComLoad();
			refreshSize=0;
		}
		this.finish();
		super.onBackPressed();
	}

	
	
	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			if (null != DataProc.videoRxAndPlay) {
				DataProc.videoRxAndPlay.setUserData(holder);
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if (null != DataProc.videoRxAndPlay) {
				synchronized (holder) {
					DataProc.videoRxAndPlay.setUserData(null);
				}
			}
		}
	};
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.local_imageButton:
			_intent=new Intent(LocalVideoPlayActivity.this,NationScanActivity.class);
			_intent.putExtra("position", position);
		     setResult(RESTART, _intent);
			this.finish();
			
			break;

		default:
			break;
		}
		
	}
}
