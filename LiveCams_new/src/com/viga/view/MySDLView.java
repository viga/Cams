package com.viga.view;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import com.viga.activity.VideoPlayActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public  class MySDLView extends SurfaceView implements SurfaceHolder.Callback {

	// This is what SDL runs in. It invokes SDL_main(), eventually
	private Thread mSDLThread;
	// EGL private objects
	@SuppressWarnings("unused")
	private EGLContext mEGLContext;
	private EGLSurface mEGLSurface;
	private EGLDisplay mEGLDisplay;
	// Startup
	public MySDLView(Context context) {
		super(context);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
	}

	public MySDLView(Context context, AttributeSet set) {
		super(context, set);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
	}

	// Called when we have a valid drawing surface
	public void surfaceCreated(SurfaceHolder holder) {
	}

	// Called when we lose the surface
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Send a quit message to the application
	//	VideoPlayActivity.nativeQuit();
		// Now wait for the SDL thread to quit
		if (mSDLThread != null) {
			try {
				mSDLThread.join();
			} catch (Exception e) {
				Log.v("SDL", "Problem stopping thread: " + e);
			}
			mSDLThread = null;
			// Log.v("SDL", "Finished waiting for SDL thread");
		}
	}

	// Called when the surface is resized
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		int sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565 by default
		switch (format) {
		case PixelFormat.A_8:
			Log.v("SDL", "pixel format A_8");
			break;
		case PixelFormat.LA_88:
			Log.v("SDL", "pixel format LA_88");
			break;
		case PixelFormat.L_8:
			Log.v("SDL", "pixel format L_8");
			break;
		case PixelFormat.RGBA_4444:
			Log.v("SDL", "pixel format RGBA_4444");
			sdlFormat = 0x85421002; // SDL_PIXELFORMAT_RGBA4444
			break;
		case PixelFormat.RGBA_5551:
			Log.v("SDL", "pixel format RGBA_5551");
			sdlFormat = 0x85441002; // SDL_PIXELFORMAT_RGBA5551
			break;
		case PixelFormat.RGBA_8888:
			Log.v("SDL", "pixel format RGBA_8888");
			sdlFormat = 0x86462004; // SDL_PIXELFORMAT_RGBA8888
			break;
		case PixelFormat.RGBX_8888:
			Log.v("SDL", "pixel format RGBX_8888");
			sdlFormat = 0x86262004; // SDL_PIXELFORMAT_RGBX8888
			break;
		case PixelFormat.RGB_332:
			Log.v("SDL", "pixel format RGB_332");
			sdlFormat = 0x84110801; // SDL_PIXELFORMAT_RGB332
			break;
		case PixelFormat.RGB_565:
			Log.v("SDL", "pixel format RGB_565");
			sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565
			break;
		case PixelFormat.RGB_888:
			Log.v("SDL", "pixel format RGB_888");
			// Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
			sdlFormat = 0x86161804; // SDL_PIXELFORMAT_RGB888
			break;
		default:
			Log.v("SDL", "pixel format unknown " + format);
			break; 
		}
		//VideoPlayActivity.onNativeResize(width, height, sdlFormat);
		// Now start up the C app thread
		if (mSDLThread == null) {
			mSDLThread = new Thread(new SDLrun(), "SDLThread");
			mSDLThread.start();
		}
	}

	// unused
	public void onDraw(Canvas canvas) {

	}

	// EGL functions
	public boolean initEGL(int majorVersion, int minorVersion) {
		Log.v("SDL", "Starting up OpenGL ES " + majorVersion + "." + minorVersion);

		try {
			EGL10 egl = (EGL10) EGLContext.getEGL();

			EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

			int[] version = new int[2];
			egl.eglInitialize(dpy, version);
			int EGL_OPENGL_ES_BIT = 1;
			int EGL_OPENGL_ES2_BIT = 4;
			int renderableType = 0;
			if (majorVersion == 2) {
				renderableType = EGL_OPENGL_ES2_BIT;
			} else if (majorVersion == 1) {
				renderableType = EGL_OPENGL_ES_BIT;
			}
			int[] configSpec = {
					// EGL10.EGL_DEPTH_SIZE, 16,
					EGL10.EGL_RENDERABLE_TYPE, renderableType, EGL10.EGL_NONE };
			EGLConfig[] configs = new EGLConfig[1];
			int[] num_config = new int[1];
			if (!egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config) || num_config[0] == 0) {
				Log.e("SDL", "No EGL config available");
				return false;
			}
			EGLConfig config = configs[0];

			int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
			int contextAttrs[] = new int[] { EGL_CONTEXT_CLIENT_VERSION, majorVersion, EGL10.EGL_NONE };
			EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, contextAttrs);
			if (ctx == EGL10.EGL_NO_CONTEXT) {
				Log.e("SDL", "Couldn't create context");
				return false;
			}

			EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, this, null);
			if (surface == EGL10.EGL_NO_SURFACE) {
				Log.e("SDL", "Couldn't create surface");
				return false;
			}

			if (!egl.eglMakeCurrent(dpy, surface, surface, ctx)) {
				Log.e("SDL", "Couldn't make context current");
				return false;
			}

			mEGLContext = ctx;
			mEGLDisplay = dpy;
			mEGLSurface = surface;

		} catch (Exception e) {
			Log.v("SDL", e + "");
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v("SDL", s.toString());
			}
		}

		return true;
	}

	// EGL buffer flip
	public void flipEGL() {
		try {
			EGL10 egl = (EGL10) EGLContext.getEGL();

			egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, null);

			// drawing here

			egl.eglWaitGL();

			egl.eglSwapBuffers(mEGLDisplay, mEGLSurface);

		} catch (Exception e) {
			Log.v("SDL", "flipEGL(): " + e);
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v("SDL", s.toString());
			}
		}
	}
}
class SDLrun implements Runnable {
    public void run() {
        // Runs SDL_main()
     //   VideoPlayActivity.nativeInit();
    }
}