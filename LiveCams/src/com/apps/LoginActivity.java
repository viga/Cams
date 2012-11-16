package com.apps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.utils.Utils;
import com.vdsp.SettingAndStatus;

public class LoginActivity extends Activity {
	private int 	requestCode;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        Intent intent=this.getIntent();
        requestCode=intent.getIntExtra("requestcode",0);
        ((EditText)findViewById(R.id.srvIpaddrEt)).setText(
        	Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr));
        ((EditText)findViewById(R.id.srvPortEt)).setText(
        	""+SettingAndStatus.settings.srvport);
        ((EditText)findViewById(R.id.devIdEt)).setText(
        	""+SettingAndStatus.settings.devid);
        Button bt=(Button)findViewById(R.id.confirmBt);
        if(1==requestCode){
        	setTitle(getString(R.string.logintitle));
            bt.setText(getString(R.string.confirm1));
            bt.setOnClickListener(
            	new OnClickListener(){
            		public void onClick(View v){
            			Intent intent=new Intent();
            			intent.putExtra("srvipaddr",((EditText)findViewById(R.id.srvIpaddrEt)).getText().toString());
        				intent.putExtra("srvport",((EditText)findViewById(R.id.srvPortEt)).getText().toString());
        				intent.putExtra("devid",((EditText)findViewById(R.id.devIdEt)).getText().toString());
        				intent.putExtra("operation","login");
            			LoginActivity.this.setResult(requestCode,intent);
        				LoginActivity.this.finish();
            		}
            });
        }else{
        	setTitle(getString(R.string.logouttitle));
        	disableEditText((EditText)findViewById(R.id.srvIpaddrEt));
        	disableEditText((EditText)findViewById(R.id.srvPortEt));
        	disableEditText((EditText)findViewById(R.id.devIdEt));
            bt.setText(getString(R.string.confirm2));
            bt.setOnClickListener(
            	new OnClickListener(){
                	public void onClick(View v){
                		Intent intent=new Intent();
            			intent.putExtra("operation","logout");
            			LoginActivity.this.setResult(requestCode,intent);
            			LoginActivity.this.finish();
                	}
            });
        }
		((Button)findViewById(R.id.cancelBt)).setOnClickListener(
	        new OnClickListener(){
	        	public void onClick(View v){
	        		Intent intent=new Intent();
	    			intent.putExtra("operation","cancel");
	    			LoginActivity.this.setResult(requestCode,intent);
	    			LoginActivity.this.finish();
	        	}
		});

        WindowManager winmgr=getWindowManager();
        Display disp=winmgr.getDefaultDisplay();
        LayoutParams param=getWindow().getAttributes();
        param.width=(int)(disp.getWidth()*0.9);
        param.height=(int)(disp.getHeight()*0.9);
        getWindow().setAttributes(param);
    }
    
    /*禁止EditText输入*/
    private void disableEditText(EditText et){
        et.setCursorVisible(false);
        et.setFocusable(false);
    }
}
