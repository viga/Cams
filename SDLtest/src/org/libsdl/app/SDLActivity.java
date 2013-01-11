package org.libsdl.app;






import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;


/**
    SDL Activity
*/
public class SDLActivity extends Activity implements OnTouchListener {

    // Main components
    private static SDLActivity mSingleton;
    private RelativeLayout player;
    private static SDLSurface mSurface;
    private static TextView tv;
    private static SurfaceHolder holder;
    // Audio
    private static PopupWindow 	motionCtrlPanel=null;
    // Load the .so
    static {
        System.loadLibrary("SDL");
        System.loadLibrary("main");
        //System.loadLibrary("SDL_image");
        //System.loadLibrary("SDL_mixer");
        //System.loadLibrary("SDL_ttf");
    }
    public  void motionCtrlInit(){
		//设置云台控制按钮操作处理函数
		View v=View.inflate(SDLActivity.this,R.layout.motion_control,null);
		
		v.setOnTouchListener(this);
		v.findViewById(R.id.toup).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.toleft).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.toright).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.todown).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.slower).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.faster).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.brighten).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.darken).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.farer).setOnTouchListener(SDLActivity.this);
		v.findViewById(R.id.nearer).setOnTouchListener(SDLActivity.this);
		//初始化云台控制按钮并显示
		motionCtrlPanel=new PopupWindow(v,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,true);
		motionCtrlPanel.setBackgroundDrawable(null);
		motionCtrlPanel.setBackgroundDrawable(new BitmapDrawable());
		motionCtrlPanel.setFocusable(true);
		motionCtrlPanel.setFocusable(true);
		motionCtrlPanel.showAtLocation(player,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
	}
    // Setup
    protected void onCreate(Bundle savedInstanceState) {
        //Log.v("SDL", "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        	WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // So we can call stuff from static callbacks
        setContentView(R.layout.main);
        mSingleton = this;
        // Set up the surface
        //mSurface = new SDLSurface(getApplication());
        mSurface=(SDLSurface)findViewById(R.id.mysur);
        tv=(TextView) findViewById(R.id.fuck);
        player=(RelativeLayout) findViewById(R.id.player);
         holder = mSurface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	Toast.makeText(getContext(), "fuck", 1).show();
    	motionCtrlInit();
    	motionCtrlPanel.showAtLocation(player,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
    	return super.onTouchEvent(event);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
				menu.add(Menu.NONE,Menu.FIRST+1,1,"停止上传").setIcon(
					android.R.drawable.ic_menu_share);
			return true;
    }
    public boolean onCreateOptionsMenu(Menu menu){
				menu.add(Menu.NONE,Menu.FIRST+1,1,"停止上传").setIcon(
					android.R.drawable.ic_menu_share);    		
			menu.add(Menu.NONE,Menu.FIRST+2,2,"编码设置").setIcon(
				android.R.drawable.ic_menu_edit);
			menu.add(Menu.NONE,Menu.FIRST+3,3,"视频微调").setIcon(
					android.R.drawable.ic_menu_agenda);
			menu.add(Menu.NONE,Menu.FIRST+4,4,"云台控制").setIcon(
					android.R.drawable.ic_menu_sort_by_size);
			return true;
	}
    // Events
    protected void onPause() {
        Log.v("SDL", "onPause()");
        super.onPause();
    } 

    protected void onResume() {
        //Log.v("SDL", "onResume()");
        super.onResume();
    }
    @Override
    public void onBackPressed() {
    	finish();
    	mSurface.surfaceDestroyed(holder);
    }
    // Messages from the SDLMain thread
    static int COMMAND_CHANGE_TITLE = 1;

    // Handler for the messages
    Handler commandHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.arg1 == COMMAND_CHANGE_TITLE) {
                setTitle((String)msg.obj);
            }
        }
    };
    // Send a message from the SDLMain thread
    void sendCommand(int command, Object data) {
        Message msg = commandHandler.obtainMessage();
        msg.arg1 = command;
        msg.obj = data;
        commandHandler.sendMessage(msg);
    }
    // C functions we call
    public static native void nativeInit();
    public static native void nativeQuit();
    public static native void onNativeResize(int x, int y, int format);
  //  public static native void onNativeKeyDown(int keycode);
  //  public static native void onNativeKeyUp(int keycode);
  //  public static native void onNativeTouch(int touchDevId, int pointerFingerId,
   //                                         int action, float x, 
  //                                          float y, float p);
  //  public static native void onNativeAccel(float x, float y, float z);
   // public static native void nativeRunAudioThread();
    public static native void testyuv();

    // Java functions called from C

    public static boolean createGLContext(int majorVersion, int minorVersion) {
        return mSurface.initEGL(majorVersion, minorVersion);
    }

    public static void flipBuffers() {
        mSurface.flipEGL();
    }
//    public static void setActivityTitle(String title) {
//        // Called from SDLMain() thread and can't directly affect the view
//        mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
//    }
    public static Context getContext() {
        return mSingleton;
    }

    // Audio
    private static Object buf;
    public static Object audioInit(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames) {
        return buf;
    }
    public static void audioWriteShortBuffer(short[] buffer) {
    }
    
    public static void audioWriteByteBuffer(byte[] buffer) {
    }
    public static void audioQuit() {
    }
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(v.getId()){
		case R.id.toup:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				Toast.makeText(getApplicationContext(), "up down", 1).show();
			}else if (event.getAction()==MotionEvent.ACTION_UP){
				Toast.makeText(getApplicationContext(), "up up", 1).show();
			}
			break;
		case R.id.todown:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		case R.id.toleft:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		case R.id.toright:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		case R.id.faster:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		case R.id.slower:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		case R.id.brighten:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		case R.id.darken:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		case R.id.nearer:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		case R.id.farer:
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			}else if (event.getAction()==MotionEvent.ACTION_UP){
			}
			break;
		default:
			break;
		}
		
		return true;
	}
}

/**
    Simple nativeInit() runnable
*/
class SDLMain implements Runnable {
    public void run() {
        // Runs SDL_main()
        SDLActivity.nativeInit();

        //Log.v("SDL", "SDL thread terminated");
    }
}


/**
    SDLSurface. This is what we draw on, so we need to know when it's created
    in order to do anything useful. 

    Because of this, that's where we set up the SDL thread
*/
//class SDLSurface extends SurfaceView implements SurfaceHolder.Callback, 
//    View.OnKeyListener, View.OnTouchListener, SensorEventListener  {
//
//    // This is what SDL runs in. It invokes SDL_main(), eventually
//    private Thread mSDLThread;    
//    
//    // EGL private objects
//    @SuppressWarnings("unused")
//	private EGLContext  mEGLContext;
//    private EGLSurface  mEGLSurface;
//    private EGLDisplay  mEGLDisplay;
//
//    // Sensors
//    private static SensorManager mSensorManager;
//
//    // Startup    
//    public SDLSurface(Context context) {
//        super(context);
//        getHolder().addCallback(this); 
//    
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        requestFocus();
//        setOnKeyListener(this); 
//        setOnTouchListener(this);   
//
//        mSensorManager = (SensorManager)context.getSystemService("sensor");  
//    }
//
//    // Called when we have a valid drawing surface
//    public void surfaceCreated(SurfaceHolder holder) {
//        //Log.v("SDL", "surfaceCreated()");
//
//        enableSensor(Sensor.TYPE_ACCELEROMETER, true);
//    }
//  
//    // Called when we lose the surface
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        //Log.v("SDL", "surfaceDestroyed()");
//
//        // Send a quit message to the application
//        SDLActivity.nativeQuit();
//
//        // Now wait for the SDL thread to quit
//        if (mSDLThread != null) {
//            try {
//                mSDLThread.join();
//            } catch(Exception e) {
//                Log.v("SDL", "Problem stopping thread: " + e);
//            }
//            mSDLThread = null;
//
//            //Log.v("SDL", "Finished waiting for SDL thread");
//        }
//
//        enableSensor(Sensor.TYPE_ACCELEROMETER, false);
//    }
//
//    // Called when the surface is resized
//    public void surfaceChanged(SurfaceHolder holder,
//                               int format, int width, int height) {
//        //Log.v("SDL", "surfaceChanged()");
//
//        int sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565 by default
//        switch (format) {
//        case PixelFormat.A_8:
//            Log.v("SDL", "pixel format A_8");
//            break;
//        case PixelFormat.LA_88:
//            Log.v("SDL", "pixel format LA_88");
//            break;
//        case PixelFormat.L_8:
//            Log.v("SDL", "pixel format L_8");
//            break;
//        case PixelFormat.RGBA_4444:
//            Log.v("SDL", "pixel format RGBA_4444");
//            sdlFormat = 0x85421002; // SDL_PIXELFORMAT_RGBA4444
//            break;
//        case PixelFormat.RGBA_5551:
//            Log.v("SDL", "pixel format RGBA_5551");
//            sdlFormat = 0x85441002; // SDL_PIXELFORMAT_RGBA5551
//            break;
//        case PixelFormat.RGBA_8888:
//            Log.v("SDL", "pixel format RGBA_8888");
//            sdlFormat = 0x86462004; // SDL_PIXELFORMAT_RGBA8888
//            break;
//        case PixelFormat.RGBX_8888:
//            Log.v("SDL", "pixel format RGBX_8888");
//            sdlFormat = 0x86262004; // SDL_PIXELFORMAT_RGBX8888
//            break;
//        case PixelFormat.RGB_332:
//            Log.v("SDL", "pixel format RGB_332");
//            sdlFormat = 0x84110801; // SDL_PIXELFORMAT_RGB332
//            break;
//        case PixelFormat.RGB_565:
//            Log.v("SDL", "pixel format RGB_565");
//            sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565
//            break;
//        case PixelFormat.RGB_888:
//            Log.v("SDL", "pixel format RGB_888");
//            // Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
//            sdlFormat = 0x86161804; // SDL_PIXELFORMAT_RGB888
//            break;
//        default:
//            Log.v("SDL", "pixel format unknown " + format);
//            break;
//        }
//        SDLActivity.onNativeResize(width, height, sdlFormat);
//
//        // Now start up the C app thread
//        if (mSDLThread == null) {
//            mSDLThread = new Thread(new SDLMain(), "SDLThread"); 
//            mSDLThread.start();       
//        }
//    }
//
//    // unused
//    public void onDraw(Canvas canvas) {
//    	
//    }
//
//
//    // EGL functions
//    public boolean initEGL(int majorVersion, int minorVersion) {
//        Log.v("SDL", "Starting up OpenGL ES " + majorVersion + "." + minorVersion);
//
//        try {
//            EGL10 egl = (EGL10)EGLContext.getEGL();
//
//            EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
//
//            int[] version = new int[2];
//            egl.eglInitialize(dpy, version);
//
//            int EGL_OPENGL_ES_BIT = 1;
//            int EGL_OPENGL_ES2_BIT = 4;
//            int renderableType = 0;
//            if (majorVersion == 2) {
//                renderableType = EGL_OPENGL_ES2_BIT;
//            } else if (majorVersion == 1) {
//                renderableType = EGL_OPENGL_ES_BIT;
//            }
//            int[] configSpec = {
//                //EGL10.EGL_DEPTH_SIZE,   16,
//                EGL10.EGL_RENDERABLE_TYPE, renderableType,
//                EGL10.EGL_NONE
//            };
//            EGLConfig[] configs = new EGLConfig[1];
//            int[] num_config = new int[1];
//            if (!egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config) || num_config[0] == 0) {
//                Log.e("SDL", "No EGL config available");
//                return false;
//            }
//            EGLConfig config = configs[0];
//
//            int EGL_CONTEXT_CLIENT_VERSION=0x3098;
//            int contextAttrs[] = new int[]
//            {
//                EGL_CONTEXT_CLIENT_VERSION, majorVersion,
//                EGL10.EGL_NONE
//            }; 
//            EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, contextAttrs);
//            if (ctx == EGL10.EGL_NO_CONTEXT) {
//                Log.e("SDL", "Couldn't create context");
//                return false;
//            }
//
//            EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, this, null);
//            if (surface == EGL10.EGL_NO_SURFACE) {
//                Log.e("SDL", "Couldn't create surface");
//                return false;
//            }
//
//            if (!egl.eglMakeCurrent(dpy, surface, surface, ctx)) {
//                Log.e("SDL", "Couldn't make context current");
//                return false;
//            }
//
//            mEGLContext = ctx;
//            mEGLDisplay = dpy;
//            mEGLSurface = surface;
//
//        } catch(Exception e) {
//            Log.v("SDL", e + "");
//            for (StackTraceElement s : e.getStackTrace()) {
//                Log.v("SDL", s.toString());
//            }
//        }
//
//        return true;
//    }
//
//    // EGL buffer flip
//    public void flipEGL() {
//        try {
//            EGL10 egl = (EGL10)EGLContext.getEGL();
//
//            egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, null);
//
//            // drawing here
//
//            egl.eglWaitGL();
//
//            egl.eglSwapBuffers(mEGLDisplay, mEGLSurface);
//
//            
//        } catch(Exception e) {
//            Log.v("SDL", "flipEGL(): " + e);
//            for (StackTraceElement s : e.getStackTrace()) {
//                Log.v("SDL", s.toString());
//            }
//        }
//    }
//
//    // Key events
//    public boolean onKey(View  v, int keyCode, KeyEvent event) {
//
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            //Log.v("SDL", "key down: " + keyCode);
//            SDLActivity.onNativeKeyDown(keyCode);
//            return true;
//        }
//        else if (event.getAction() == KeyEvent.ACTION_UP) {
//            //Log.v("SDL", "key up: " + keyCode);
//            SDLActivity.onNativeKeyUp(keyCode);
//            return true;
//        }
//        
//        return false;
//    }
//
//    // Touch events
//    public boolean onTouch(View v, MotionEvent event) {
//        {
//             final int touchDevId = event.getDeviceId();
//             final int pointerCount = event.getPointerCount();
//             // touchId, pointerId, action, x, y, pressure
//             int actionPointerIndex = event.getActionIndex();
//             int pointerFingerId = event.getPointerId(actionPointerIndex);
//             int action = event.getActionMasked();
//
//             float x = event.getX(actionPointerIndex);
//             float y = event.getY(actionPointerIndex);
//             float p = event.getPressure(actionPointerIndex);
//
//             if (action == MotionEvent.ACTION_MOVE && pointerCount > 1) {
//                // TODO send motion to every pointer if its position has
//                // changed since prev event.
//                for (int i = 0; i < pointerCount; i++) {
//                    pointerFingerId = event.getPointerId(i);
//                    x = event.getX(i);
//                    y = event.getY(i);
//                    p = event.getPressure(i);
//                    SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
//                }
//             } else {
//                SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
//             }
//        }
//      return true;
//   } 
//
//    // Sensor events
//    public void enableSensor(int sensortype, boolean enabled) {
//        // TODO: This uses getDefaultSensor - what if we have >1 accels?
//        if (enabled) {
//            mSensorManager.registerListener(this, 
//                            mSensorManager.getDefaultSensor(sensortype), 
//                            SensorManager.SENSOR_DELAY_GAME, null);
//        } else {
//            mSensorManager.unregisterListener(this, 
//                            mSensorManager.getDefaultSensor(sensortype));
//        }
//    }
//    
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        // TODO
//    }
//
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            SDLActivity.onNativeAccel(event.values[0],
//                                      event.values[1],
//                                      event.values[2]);
//        }
//    }
//
//}

