package com.viga.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.viga.engine.MyApplication;
import com.viga.engine.SettingAndStatus;
import com.viga.utils.Utils;

public class SettingDetailActivity extends Activity {
		private TextView tv;
		private EditText et1, et2;
		private Spinner sp;
		private SeekBar sb;
		private Button btConfirm,btCancle;
		private Intent intent;
		private List<Camera.Size> cslist = null;
		private Camera.Size cameraSize;
		@Override 
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
			MyApplication.getInstance().addActivity(SettingDetailActivity.this);
			 intent= new Intent(SettingDetailActivity.this,SettingActivity.class);
			if(0==SettingActivity.settingposition){
			setContentView(R.layout.setting_server);
			et1 = (EditText) findViewById(R.id.srvIpaddrEt);
			et1.setText(Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr));
			et2 = (EditText) findViewById(R.id.srvPortEt);
			et2.setText("" + SettingAndStatus.settings.srvport);
			btConfirm=(Button) findViewById(R.id.confirmBt);
			btConfirm.setOnClickListener(new OnClickListener() {
				public void onClick(View v) { 
					int srvipaddr = Utils.ipaddrToInt(et1.getText().toString());
					short srvport = Short.parseShort(et2.getText().toString());
					if (srvipaddr != SettingAndStatus.settings.srvipaddr
							|| srvport != SettingAndStatus.settings.srvport) {
						SettingAndStatus.settings.srvipaddr = srvipaddr;
						SettingAndStatus.settings.srvport = srvport;
						SettingAndStatus.modify("srvipaddr", srvipaddr);
						SettingAndStatus.modify("srvport", srvport);
					}
					if(getWindow().getAttributes().softInputMode==(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)){
						((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SettingDetailActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						}
					startActivity(intent);
					SettingDetailActivity.this.finish();
				}
			});
			btCancle=(Button) findViewById(R.id.cancelBt);
			btCancle.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(getWindow().getAttributes().softInputMode==(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)){
						((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SettingDetailActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						}
					startActivity(intent);
					SettingDetailActivity.this.finish();
				}
			});
			}
			else if(1==SettingActivity.settingposition){
				setContentView(R.layout.setting_device);
				et1 = (EditText) findViewById(R.id.devTypeEt);
				et1.setText("手机终端");
				et1.setEnabled(false);
				et2 = (EditText) findViewById(R.id.devIdEt);
				et2.setText("" + SettingAndStatus.settings.devid);
				btConfirm=(Button) findViewById(R.id.confirmBt);
				btConfirm.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						int devid = Integer.parseInt(et2.getText().toString());
						if (devid != SettingAndStatus.settings.devid) {
							SettingAndStatus.settings.devid = devid;
							SettingAndStatus.modify("devid", devid);
						}
						if(getWindow().getAttributes().softInputMode==(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)){
						((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SettingDetailActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						}
							startActivity(intent);
							SettingDetailActivity.this.finish();
						}
					  
				});
				btCancle=(Button) findViewById(R.id.cancelBt);
				btCancle.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if(getWindow().getAttributes().softInputMode==(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)){
						((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SettingDetailActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						}
						startActivity(intent);
						SettingDetailActivity.this.finish();
					}
				});   
			}
			else if(3==SettingActivity.settingposition){
				setContentView(R.layout.setting_av);
				String[] items = getResources().getStringArray(R.array.vsz4sp);
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < items.length; i++) {
					list.add(items[i]);
				}
				ArrayAdapter<String> adapter;
				adapter = new ArrayAdapter<String>(SettingDetailActivity.this,
						android.R.layout.simple_spinner_item, list);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				sp = (Spinner) findViewById(R.id.videoSizeSp);
				sp.setAdapter(adapter);
				sp.setPrompt("视频尺寸:");
				sp.setSelection(SettingAndStatus.settings.videosize);
				et1 = (EditText) findViewById(R.id.vframeRateEt);
				et1.setText("" + SettingAndStatus.settings.vframerate);
				et2 = (EditText) findViewById(R.id.vbitRateEt);
				et2.setText("" + SettingAndStatus.settings.vbitrate);
				btConfirm=(Button) findViewById(R.id.confirmBt);
				btConfirm.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						int videosize = sp.getSelectedItemPosition();
						int framerate = Integer.parseInt(et1.getText().toString());
						int bitrate = Integer.parseInt(et2.getText().toString());
						if (videosize != SettingAndStatus.settings.videosize
								|| framerate != SettingAndStatus.settings.vframerate
								|| bitrate != SettingAndStatus.settings.vbitrate) {
							SettingAndStatus.settings.videosize = videosize;
							SettingAndStatus.settings.vframerate = framerate;
							SettingAndStatus.settings.vbitrate = bitrate;
							SettingAndStatus.modify("videosize", videosize);
							SettingAndStatus.modify("vframerate", framerate);
							SettingAndStatus.modify("vbitrate", bitrate);
						}
						if(getWindow().getAttributes().softInputMode==(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)){
						((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SettingDetailActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						}
						startActivity(intent);
						SettingDetailActivity.this.finish();
						}
					
				});
				btCancle=(Button) findViewById(R.id.cancelBt);
				btCancle.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if(getWindow().getAttributes().softInputMode==(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)){
						((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SettingDetailActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						}
						startActivity(intent);
						SettingDetailActivity.this.finish();
					}
				});
				
				
			}
			else if(2==SettingActivity.settingposition){
				setContentView(R.layout.setting_snap);
				Camera camera=Camera.open();
				Camera.Parameters param = camera.getParameters();
				cslist=param.getSupportedPictureSizes();
				cameraSize=camera.new Size(SettingAndStatus.settings.picwidth,
					SettingAndStatus.settings.picheight);
				if(-1==cslist.indexOf(cameraSize)){
					cameraSize=param.getPictureSize();
					SettingAndStatus.settings.picwidth=cameraSize.width; 
					SettingAndStatus.settings.picheight=cameraSize.height;
					SettingAndStatus.modify("picwidth",cameraSize.width);
					SettingAndStatus.modify("picheight",cameraSize.height);
				}
				if(0==SettingAndStatus.settings.picquality){ 
					SettingAndStatus.settings.picquality=param.getJpegQuality();
					SettingAndStatus.modify("picquality",SettingAndStatus.settings.picquality);
				}
				camera.release();
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < cslist.size(); i++) {
					Camera.Size dim = cslist.get(i);
					list.add(dim.width + "x" + dim.height);
				}
				ArrayAdapter<String> adapter;
				adapter = new ArrayAdapter<String>(SettingDetailActivity.this,
						android.R.layout.simple_spinner_item, list);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				sp = (Spinner) findViewById(R.id.picSizeSp);
				sp.setAdapter(adapter);
				sp.setPrompt("照片尺寸(" + cslist.size() + "种):");
				sp.setSelection(cslist.indexOf(cameraSize));
				tv = (TextView) findViewById(R.id.picQualityValTv);
				tv.setText("" + SettingAndStatus.settings.picquality);
				sb = (SeekBar) findViewById(R.id.picQualitySb);
				sb.setProgress(SettingAndStatus.settings.picquality);
				sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						tv.setText("" + progress);
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				});
				btConfirm=(Button) findViewById(R.id.confirmBt);
				btConfirm.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						int index = sp.getSelectedItemPosition();
						int width = cslist.get(index).width;
						int height = cslist.get(index).height;
						int quality = sb.getProgress();
						if (width != SettingAndStatus.settings.picwidth
								|| height != SettingAndStatus.settings.picheight
								|| quality != SettingAndStatus.settings.picquality) {
							SettingAndStatus.settings.picwidth = width;
							SettingAndStatus.settings.picheight = height;
							SettingAndStatus.settings.picquality = quality;
							SettingAndStatus.modify("picwidth", width);
							SettingAndStatus.modify("picheight", height);
							SettingAndStatus.modify("picquality", quality);
						}
						startActivity(intent);
						SettingDetailActivity.this.finish();
						}
					
				});
				btCancle=(Button) findViewById(R.id.cancelBt);
				btCancle.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						//if(getWindow().getAttributes().softInputMode==(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)){
						//((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SettingDetailActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						//}
						startActivity(intent);
						SettingDetailActivity.this.finish();
					}
				});
			}
		}
		
		@Override
		protected void onPause() {
			onDestroy();
		super.onPause();
		}
		@Override
		protected void onStop() {
			onDestroy();
		super.onStop();
		}
		

}
