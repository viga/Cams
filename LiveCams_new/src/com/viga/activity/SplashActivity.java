package com.viga.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        new Handler().postDelayed(new Runnable(){  
            public void run() {  
            	Intent intent= new Intent(SplashActivity.this,LiveCamsActivity.class);
            	SplashActivity.this.startActivity(intent);
            	SplashActivity.this.overridePendingTransition(R.anim.inanim,R.anim.outanim);
            	SplashActivity.this.finish();
            }  
         }, 2000); 
    }
}
