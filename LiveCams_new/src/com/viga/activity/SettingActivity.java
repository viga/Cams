package com.viga.activity;

import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vdsp.Vca;
import com.viga.engine.H264Stream;
import com.viga.engine.MyApplication;
import com.viga.engine.SettingAndStatus;
import com.viga.utils.Utils;
import com.viga.view.SwitchView;
import com.viga.view.SwitchView.OnCheckedChangeListener;

public class SettingActivity extends Activity implements OnClickListener {
	private SwitchView sv_setting_auto_login, sv_setting_before_login, sv_setting_before_quit,sv_setting_save_video,
			sv_setting_record_shuangxiang, sv_setting_play_shuangxiang, sv_setting_fullscreen,
			sv_setting_save_photo,sv_setting_use_bluetooth;
	private LinearLayout ll_setting_ip,ll_setting_deviceid,ll_setting_videoadjust,ll_setting_photoadjust;
	private TextView tv_setting_ip,tv_setting_deviceid,tv_setting_videoadjust,tv_setting_photoadjust;
	private List<Camera.Size> cslist = null;
	private Camera.Size cameraSize;
	private Vca vca = new Vca();
	private BluetoothAdapter mbt;
	public static int settingposition;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.setting_activity);
		//initView();
		MyApplication.getInstance().addActivity(SettingActivity.this);
	}

	private void initView() {
		mbt=BluetoothAdapter.getDefaultAdapter();
		if(mbt.isEnabled()){
			SettingAndStatus.modify("useBlueTooth", 1);
		}else{
			SettingAndStatus.modify("useBlueTooth", 0);
		}
		//SettingAndStatus.init(this);
		SettingAndStatus.load(null);
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
		sv_setting_auto_login=(SwitchView) findViewById(R.id.sv_setting_auto_login);
		sv_setting_before_login=(SwitchView) findViewById(R.id.sv_setting_before_login);
		sv_setting_before_quit=(SwitchView) findViewById(R.id.sv_setting_before_quit);
		sv_setting_save_video=(SwitchView) findViewById(R.id.sv_setting_save_video);
		sv_setting_record_shuangxiang=(SwitchView) findViewById(R.id.sv_setting_record_shuangxiang);
		sv_setting_play_shuangxiang=(SwitchView) findViewById(R.id.sv_setting_play_shuangxiang);
		sv_setting_fullscreen=(SwitchView) findViewById(R.id.sv_setting_fullscreen);
		sv_setting_save_photo=(SwitchView) findViewById(R.id.sv_setting_save_photo);
		sv_setting_use_bluetooth=(SwitchView) findViewById(R.id.sv_setting_use_bluetooth);
		
		sv_setting_auto_login.setChecked(SettingAndStatus.settings.autologin ? true : false);
		sv_setting_before_login.setChecked(SettingAndStatus.settings.logincheck ? true : false);
		sv_setting_before_quit.setChecked(SettingAndStatus.settings.logoutcheck ? true : false);
		sv_setting_save_video.setChecked(SettingAndStatus.settings.avrecord  ? true : false);
		sv_setting_record_shuangxiang.setChecked(SettingAndStatus.settings.audioduplex0 ? true : false);
		sv_setting_play_shuangxiang.setChecked(SettingAndStatus.settings.audioduplex1 ? true : false);
		sv_setting_fullscreen.setChecked(SettingAndStatus.settings.fullscreen ? true : false);
		sv_setting_save_photo.setChecked(SettingAndStatus.settings.picsave ? true : false);
		sv_setting_use_bluetooth.setChecked(SettingAndStatus.settings.useBlueTooth ? true : false);
		
		sv_setting_auto_login.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.autologin=isChecked;
				SettingAndStatus.modify("autologin", isChecked ? 1 : 0);
			}
		});
		sv_setting_before_login.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.logincheck=isChecked;
				SettingAndStatus.modify("logincheck", isChecked ? 1 : 0);
			}
		});
		sv_setting_before_quit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.logoutcheck=isChecked;
				SettingAndStatus.modify("logoutcheck", isChecked ? 1 : 0);
			}
		});
		sv_setting_save_video.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.avrecord=isChecked;
				SettingAndStatus.modify("avrecord", isChecked ? 1 : 0);
			}
		});
		sv_setting_record_shuangxiang.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.audioduplex0=isChecked;
				SettingAndStatus.modify("audioduplex0", isChecked ? 1 : 0);
			}
		});
		sv_setting_play_shuangxiang.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.audioduplex1=isChecked;
				SettingAndStatus.modify("audioduplex1", isChecked ? 1 : 0);
			}
		});
		sv_setting_fullscreen.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.fullscreen=isChecked;
				SettingAndStatus.modify("fullscreen", isChecked ? 1 : 0);
    			if(SettingAndStatus.vcaStatus.status>=SettingAndStatus.VcaStatus.OPENED){
    				Vca.Arg arg=vca.new Arg(-1,-1);
        			if(isChecked){
        				arg.setWxH(SettingAndStatus.displayheight,SettingAndStatus.displaywidth);
        			}
        			Vca.control(Vca.OPID_CHGDESTSIZE,arg);
    			}
			}
		});
		sv_setting_save_photo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.picsave=isChecked;
				SettingAndStatus.modify("picsave", isChecked ? 1 : 0);
			}
		});
		sv_setting_use_bluetooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(boolean isChecked) {
				SettingAndStatus.settings.useBlueTooth=isChecked;
				if(SettingAndStatus.settings.useBlueTooth){
					 Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
					 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		             startActivity(intent);
				} else {
					 mbt.disable();
				}
			}
		});
		
		ll_setting_ip=(LinearLayout) findViewById(R.id.ll_setting_ip);
		ll_setting_deviceid=(LinearLayout) findViewById(R.id.ll_setting_deviceid);
		ll_setting_videoadjust=(LinearLayout) findViewById(R.id.ll_setting_videoadjust);
		ll_setting_photoadjust=(LinearLayout) findViewById(R.id.ll_setting_photoadjust);
		
		ll_setting_ip.setOnClickListener(this);
		ll_setting_deviceid.setOnClickListener(this);
		ll_setting_videoadjust.setOnClickListener(this);
		ll_setting_photoadjust.setOnClickListener(this);
		
		tv_setting_ip=(TextView) findViewById(R.id.tv_setting_ip);
		tv_setting_deviceid=(TextView) findViewById(R.id.tv_setting_deviceid);
		tv_setting_videoadjust=(TextView) findViewById(R.id.tv_setting_videoadjust);
		tv_setting_photoadjust=(TextView) findViewById(R.id.tv_setting_photoadjust);
		
		tv_setting_ip.setText(Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr) + ":"
				+ SettingAndStatus.settings.srvport);
		tv_setting_deviceid.setText("手机终端/" + SettingAndStatus.settings.devid);
		tv_setting_videoadjust.setText(H264Stream.stringVideoSize(SettingAndStatus.settings.videosize) + "/"
				+ SettingAndStatus.settings.vbitrate + "bps/" + SettingAndStatus.settings.vframerate + "fps");
		tv_setting_photoadjust.setText(SettingAndStatus.settings.picwidth + "x" + SettingAndStatus.settings.picheight
				+ "/Q" + SettingAndStatus.settings.picquality);
	}

//
//	/* 服务器配置对话框 */
//	private class ModifySettingDialog extends Dialog {
//		private int position;
//		private EditText et1, et2;
//		private TextView tv;
//		private Spinner sp;
//		private SeekBar sb;
//
//		public ModifySettingDialog(Context context, int which) {
//			super(context);
//			ModifySettingDialog.this.position = which;
//		}
//
//		@Override
//		public void onCreate(Bundle savedInstanceState) {
//			super.onCreate(savedInstanceState);
//			if (0 == position) {
//				setContentView(R.layout.setting_server);
//				setTitle("修改服务器IP地址和端口：");
//				et1 = (EditText) findViewById(R.id.srvIpaddrEt);
//				et1.setText(Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr));
//				et2 = (EditText) findViewById(R.id.srvPortEt);
//				et2.setText("" + SettingAndStatus.settings.srvport);
//			} else if (1 == position) {
//				setContentView(R.layout.setting_device);
//				setTitle("修改设备类型和ID号：");
//				et1 = (EditText) findViewById(R.id.devTypeEt);
//				et1.setText("手机终端");
//				disableEditText(et1);
//				et2 = (EditText) findViewById(R.id.devIdEt);
//				et2.setText("" + SettingAndStatus.settings.devid);
//			} else if (5 == position) {
//				setContentView(R.layout.setting_av);
//				setTitle("修改录像参数：");
//				String[] items = getResources().getStringArray(R.array.vsz4sp);
//				ArrayList<String> list = new ArrayList<String>();
//				for (int i = 0; i < items.length; i++) {
//					list.add(items[i]);
//				}
//				ArrayAdapter<String> adapter;
//				adapter = new ArrayAdapter<String>(SettingActivity.this,
//						android.R.layout.simple_spinner_item, list);
//				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//				sp = (Spinner) findViewById(R.id.videoSizeSp);
//				sp.setAdapter(adapter);
//				sp.setPrompt("视频尺寸:");
//				sp.setSelection(SettingAndStatus.settings.videosize);
//				et1 = (EditText) findViewById(R.id.vframeRateEt);
//				et1.setText("" + SettingAndStatus.settings.vframerate);
//				et2 = (EditText) findViewById(R.id.vbitRateEt);
//				et2.setText("" + SettingAndStatus.settings.vbitrate);
//			} else if (10 == position) {
//				setContentView(R.layout.setting_snap);
//				setTitle("修改拍照参数：");
//				ArrayList<String> list = new ArrayList<String>();
//				for (int i = 0; i < cslist.size(); i++) {
//					Camera.Size dim = cslist.get(i);
//					list.add(dim.width + "x" + dim.height);
//				}
//				ArrayAdapter<String> adapter;
//				adapter = new ArrayAdapter<String>(SettingActivity.this,
//						android.R.layout.simple_spinner_item, list);
//				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//				sp = (Spinner) findViewById(R.id.picSizeSp);
//				sp.setAdapter(adapter);
//				sp.setPrompt("照片尺寸(" + cslist.size() + "种):");
//				sp.setSelection(cslist.indexOf(cameraSize));
//				tv = (TextView) findViewById(R.id.picQualityValTv);
//				tv.setText("" + SettingAndStatus.settings.picquality);
//				sb = (SeekBar) findViewById(R.id.picQualitySb);
//				sb.setProgress(SettingAndStatus.settings.picquality);
//				sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//					public void onStartTrackingTouch(SeekBar seekBar) {
//					}
//
//					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//						tv.setText("" + progress);
//					}
//
//					public void onStopTrackingTouch(SeekBar seekBar) {
//					}
//				});
//			}
//			Button bt = (Button) findViewById(R.id.confirmBt);
//			bt.setText(getString(R.string.confirm3));
//			bt.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					if (0 == position) {
//						int srvipaddr = Utils.ipaddrToInt(et1.getText().toString());
//						short srvport = Short.parseShort(et2.getText().toString());
//						if (srvipaddr != SettingAndStatus.settings.srvipaddr
//								|| srvport != SettingAndStatus.settings.srvport) {
//							SettingAndStatus.settings.srvipaddr = srvipaddr;
//							SettingAndStatus.settings.srvport = srvport;
//							SettingAndStatus.modify("srvipaddr", srvipaddr);
//							SettingAndStatus.modify("srvport", srvport);
//							tv_setting_ip.setText(Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr) + ":"
//									+ SettingAndStatus.settings.srvport);
//						}
//					} else if (1 == position) {
//						int devid = Integer.parseInt(et2.getText().toString());
//						if (devid != SettingAndStatus.settings.devid) {
//							SettingAndStatus.settings.devid = devid;
//							SettingAndStatus.modify("devid", devid);
//							tv_setting_deviceid.setText("手机终端/" + SettingAndStatus.settings.devid);
//						}
//					} else if (5 == position) {
//						int videosize = sp.getSelectedItemPosition();
//						int framerate = Integer.parseInt(et1.getText().toString());
//						int bitrate = Integer.parseInt(et2.getText().toString());
//						if (videosize != SettingAndStatus.settings.videosize
//								|| framerate != SettingAndStatus.settings.vframerate
//								|| bitrate != SettingAndStatus.settings.vbitrate) {
//							SettingAndStatus.settings.videosize = videosize;
//							SettingAndStatus.settings.vframerate = framerate;
//							SettingAndStatus.settings.vbitrate = bitrate;
//							SettingAndStatus.modify("videosize", videosize);
//							SettingAndStatus.modify("framerate", framerate);
//							SettingAndStatus.modify("bitrate", bitrate);
//							tv_setting_videoadjust.setText(H264Stream.stringVideoSize(SettingAndStatus.settings.videosize) + "/"
//									+ SettingAndStatus.settings.vbitrate + "bps/" + SettingAndStatus.settings.vframerate + "fps");
//						}
//					} else if (10 == position) {
//						int index = sp.getSelectedItemPosition();
//						int width = cslist.get(index).width;
//						int height = cslist.get(index).height;
//						int quality = sb.getProgress();
//						if (width != SettingAndStatus.settings.picwidth
//								|| height != SettingAndStatus.settings.picheight
//								|| quality != SettingAndStatus.settings.picquality) {
//							SettingAndStatus.settings.picwidth = width;
//							SettingAndStatus.settings.picheight = height;
//							SettingAndStatus.settings.picquality = quality;
//							SettingAndStatus.modify("width", width);
//							SettingAndStatus.modify("height", height);
//							SettingAndStatus.modify("quality", quality);
//							tv_setting_photoadjust.setText(SettingAndStatus.settings.picwidth + "x" + SettingAndStatus.settings.picheight
//									+ "/Q" + SettingAndStatus.settings.picquality);
//						}
//					}
//					
//					ModifySettingDialog.this.dismiss();
//				}
//			});
//			((Button) findViewById(R.id.cancelBt)).setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					initView();
//					ModifySettingDialog.this.cancel();
//				}
//			});
//
//			WindowManager winmgr = getWindowManager();
//			Display disp = winmgr.getDefaultDisplay();
//			LayoutParams param = getWindow().getAttributes();
//			param.width = (int) (disp.getWidth() * 0.9);
//			param.height = (int) (disp.getHeight() * (4 == position ? 0.9 : 0.75));
//			getWindow().setAttributes(param);
//		}
//		
//		
//	}
//	
//	/* 禁止EditText输入 */
//	private void disableEditText(EditText et) {
//		et.setCursorVisible(false);
//		et.setFocusable(false);
//	}


//	/* 子View容器 */
//	private final class ViewSet {
//		public TextView title;
//		public TextView text;
//		public Button click;
//	}\
	@Override
	protected void onStart() {
		initView();
		super.onStart();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		SettingAndStatus.exit();
		super.onDestroy();
	}
	public void onClick(View v) {
		Intent intent = new Intent(SettingActivity.this,SettingDetailActivity.class);
		switch (v.getId()) {
		case R.id.ll_setting_ip:
			settingposition=0;
			startActivity(intent);
			break;
		case R.id.ll_setting_deviceid:
			settingposition=1;
			startActivity(intent);
			break;
		case R.id.ll_setting_photoadjust:
			settingposition=2;
			startActivity(intent);
			break;
		case R.id.ll_setting_videoadjust:
			settingposition=3;
			startActivity(intent);
			break;
		default:
			break;
		}
	}
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.setClass(this, LiveCamsActivity.class);
		startActivity(intent);	
		onDestroy();
		finish();
		super.onBackPressed();
	}
}




