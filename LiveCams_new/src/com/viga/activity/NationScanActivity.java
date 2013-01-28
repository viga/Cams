package com.viga.activity;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vdsp.CmdSend;
import com.vdsp.DispatchHandler;
import com.vdsp.Vca;
import com.viga.adapter.NationListAdapter;
import com.viga.engine.DataProc;
import com.viga.engine.MyApplication;
import com.viga.engine.SettingAndStatus;
import com.viga.utils.Utils;
public class NationScanActivity extends Activity implements OnClickListener {
	private final static String TAG = "NationScanActivity";
	private final static int RESTART = 1;
	private TextView tv_nation_video,tv_nation_photo;
	private ListView lv_nation_files;
	private static NationListAdapter nationadapter;
	private static List<File> filelist=new ArrayList<File>();
	private File[] filebuf;
	private File files;
	private static String path = Environment.getExternalStorageDirectory().getAbsolutePath();
	private PopupWindow mPop,mProgress;
	private SharedPreferences mysp;
	private int requestCode;
	private int currentStatus;
	private static int refreshSize=0;
	private ProgressBar myPb;
	private TextView filepr;
	private static boolean ifVedioFolder;
	private static boolean localVideoPlayer=false;
	private Intent _intent;
    private int _position;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			DecimalFormat df=new DecimalFormat("3.14");
			switch (msg.what) {
			case 211:
				refreshSize += msg.arg1;
				int total=msg.arg2;
				//System.out.println("total:"+total+"now:"+refreshSize);
				if(refreshSize>total || refreshSize==total){
					refreshSize=0;
					dismissProress();
					switchVideo();
					Toast.makeText(getApplicationContext(), "上传完成", Toast.LENGTH_SHORT).show();
				}
				myPb.setProgress(refreshSize);
				filepr.setText((df.format((float)refreshSize/total*100)+"%"));
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_nation_scan);
        MyApplication.getInstance().addActivity(NationScanActivity.this);
        _intent=this.getIntent();
        requestCode=_intent.getIntExtra("requestcode",0);
        currentStatus=_intent.getIntExtra("currentStatus", 0);
        DispatchHandler.setHandler(handler);
		initUI();
        mlistFiles("photo");    
        mysp=this.getSharedPreferences("fileupload", Context.MODE_PRIVATE);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {    
        	}else {    
        		Toast.makeText(getApplicationContext(), "SD卡不可用，返回主界面", Toast.LENGTH_LONG).show();
        		Intent intent = new Intent(this,LiveCamsActivity.class);
        		startActivity(intent);
        	 }   
		lv_nation_files.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				dismissPopUpwindow();
			}
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				dismissPopUpwindow();
			}
		});
    }
    
    private void initUI(){
    	lv_nation_files=(ListView) findViewById(R.id.lv_nation);
        tv_nation_photo=(TextView) findViewById(R.id.tv_nation_photo);
        tv_nation_video=(TextView) findViewById(R.id.tv_nation_video);
        tv_nation_photo.setOnClickListener(this);
        tv_nation_video.setOnClickListener(this);
        //tv_nation_photo.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonbg2));
		tv_nation_photo.setClickable(false);
    	
    	
    }
    //上传完成通知背负设备 
    private void switchVideo(){
    	if (SettingAndStatus.phoneDataSending) {
			SettingAndStatus.phoneDataSending = false;
			CmdSend.stopSendData(false);
			DataProc.stopVideoUpload();
			Vca vca = new Vca();
			Vca.Arg arg = vca.new Arg(-1, -1);
			Vca.control(Vca.OPID_CLRDST, arg);
		}
    }
	public void onClick(View v) {
		final File thisFile;
		int positon = 0;
		if (v.getTag() != null) {
			positon = (Integer) v.getTag();
		}
		if(filelist.size()!=0){
		thisFile=filelist.get(positon);
		_position=positon;
		}else{
			thisFile=null;
		}
		switch (v.getId()) {
		case R.id.tv_nation_photo://显示照片文件
			if(tv_nation_photo.isClickable()){
			tv_nation_photo.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_delwords_sel));
			tv_nation_photo.setClickable(false);
			tv_nation_photo.setTextColor(Color.BLACK);
			tv_nation_video.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_delwords_nor));
			tv_nation_video.setClickable(true);
			tv_nation_video.setTextColor(Color.WHITE);
			mlistFiles("photo");  
			}
			break;
		case R.id.tv_nation_video://显示视频文件
			if(tv_nation_video.isClickable()){
				tv_nation_video.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_delwords_sel));
				tv_nation_video.setClickable(false);
				tv_nation_video.setTextColor(Color.BLACK);
				tv_nation_photo.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_delwords_nor));
				tv_nation_photo.setClickable(true);
				tv_nation_photo.setTextColor(Color.WHITE);
				mlistFiles("video");
				}
			break;
		case R.id.ll_nation_pop_play:
			if(thisFile!=null){
				if("image/*".equals(Utils.getMIMEType(thisFile))){
					//查看图片：
				openFile(thisFile);
				}else{
					if(SettingAndStatus.vcaStatus.status==SettingAndStatus.VcaStatus.INIT||SettingAndStatus.vcaStatus.status==SettingAndStatus.VcaStatus.STOPPING) {
				     LiveCamsActivity lv=(LiveCamsActivity) LiveCamsActivity.getCon();
				     lv.openAndStartVca(null); 
				     localVideoPlayer=true;
					}	
					LocaluploadVideoToS(thisFile);				
					
				
				}
			}
			break;
		case R.id.ll_nation_pop_upload:
			if(thisFile!=null){
					if("image/*".equals(Utils.getMIMEType(thisFile))){
						if(uploadPicToS(thisFile)){
							Editor editor=mysp.edit();
							editor.putBoolean(thisFile.getName(), true);
		    				editor.commit();
		    				mlistFiles("photo");
						}else{
							Toast.makeText(getApplicationContext(), "照片文件："+thisFile.getName()+"上传失败", Toast.LENGTH_SHORT).show();
						}
					}else{
						if(uploadVideoToS(thisFile)){
							Editor editor=mysp.edit();
							editor.putBoolean(thisFile.getName(), true);
		    				editor.commit();
						    mlistFiles("video");
						}else{
							Toast.makeText(getApplicationContext(), "视频文件："+thisFile.getName()+"上传失败", Toast.LENGTH_SHORT).show();
						}
					}
			}
			dismissPopUpwindow();
			break;
		case R.id.ll_nation_pop_delete:
			if(thisFile!=null){
				AlertDialog.Builder builder = new Builder(NationScanActivity.this); 
		        builder.setTitle("确认删除该文件？"); 
		        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						thisFile.delete();
						//如果video文件夹  同时删除音频文件
						if("image/*".equals(Utils.getMIMEType(thisFile))){
							mlistFiles("photo");
						}else{
							File audioFile=new File(thisFile.getAbsolutePath().replace(".264", ".spx"));
							audioFile.delete();
							mlistFiles("video");
						}
						Toast.makeText(getApplicationContext(), thisFile.getName()+"已删除", Toast.LENGTH_SHORT).show();
					}
				}); 
		        builder.setNegativeButton("取消", null);
		        builder.setIcon(android.R.drawable.ic_dialog_info); 
		        builder.show(); 
			}
			break;
		default:
			break;
		}
	}
	//上传图片
	private boolean uploadPicToS(final File thisFile){
					try {
					FileInputStream fis= new FileInputStream(thisFile);
					byte[] pic=Utils.readStream(fis);
					int length = pic.length;
					Date date = new Date();
					return CmdSend.uploadNationPic(date, pic, length, true, thisFile.getName());
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
	}
	//上传视频
	private boolean uploadVideoToS(final File thisFile){
		if(CmdSend.requestSendData(0, true, true, true)){
			initProgress(Integer.parseInt(thisFile.length()+""), thisFile.getName());
			DataProc.startNationVideoUpload(thisFile,0,null);
			return true;
		}else
			return false;
		
	}
	//本地播放视频
	private void LocaluploadVideoToS(final File thisFile){	
		Intent intent = new Intent(NationScanActivity.this, LocalVideoPlayActivity.class); 	
		intent.putExtra("totalLen",Integer.parseInt(thisFile.length()+""));
		intent.putExtra("position", _position);
		startActivityForResult(intent, 7);
		DataProc.startLocalNationVideoUpload(thisFile,0,null,true);

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESTART:
			//Log.i(TAG, resultCode+"");
			Bundle _bundle=data.getExtras();
			int position=(Integer) _bundle.get("position");		
			File file=filelist.get(position);
			LocaluploadVideoToS(file);
			
			break;

		default:
			break;
		}	
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void mlistFiles(String filename) {
		lv_nation_files.removeAllViewsInLayout();
		files=new File(path+"/LiveCams/"+filename);
		filelist.removeAll(filelist);
		  filebuf=files.listFiles(); 
		  if(filebuf!=null){
		    for(File f : filebuf){
		    	if(filename=="video"){
		    		if(Utils.guolvFiles(f, ".264")){
		    			filelist.add(f); //音频文件不显示出来了
		    			ifVedioFolder=true;
		    		}
		    	}else{
		    		filelist.add(f);
		    		ifVedioFolder=false;
		    	}
		    }
		  }
		nationadapter = new NationListAdapter(filelist, NationScanActivity.this);
		nationadapter.notifyDataSetChanged();
		  lv_nation_files.setAdapter(nationadapter);
		    lv_nation_files.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					dismissPopUpwindow();
					initPop(view, position);
				}
			});
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		dismissPopUpwindow();
		return super.onTouchEvent(event);
	}
	/* 主界面消息处理 */
	
	private void initProgress(int totalLen,String filename){
		System.out.println(""+totalLen);
		View v=View.inflate(NationScanActivity.this, R.layout.popup_nation_progress, null);
		TextView filetv= (TextView) v.findViewById(R.id.nation_progress_filename);
		filepr=(TextView) v.findViewById(R.id.nation_progress_prcent);
		ImageView iv = (ImageView) v.findViewById(R.id.iv_stopUpload);
		myPb=(ProgressBar) v.findViewById(R.id.progressBar1);
		myPb.setMax(totalLen);
		filetv.setText(filename);
		mProgress = new PopupWindow(v,300,125);
		mProgress.showAtLocation(v, Gravity.CENTER, 0, 0);
		iv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(0!=refreshSize){
					DataProc.stopUnComLoad(); //上传中取消上传
					refreshSize=0;
				}
			}
		});
	}
	private void initPop(View view,int position){
		// 获取当前view对象在窗体中的位置
		int[] arrayOfInt = new int[2];
		view.getLocationInWindow(arrayOfInt);
		int i = arrayOfInt[0] + 60;
		int j = arrayOfInt[1];
		View v = View.inflate(NationScanActivity.this, R.layout.popup_item, null);
		v.setOnClickListener(NationScanActivity.this);
		LinearLayout ll_play= (LinearLayout) v.findViewById(R.id.ll_nation_pop_play);
		LinearLayout ll_delete=(LinearLayout) v.findViewById(R.id.ll_nation_pop_delete);
		LinearLayout ll_upload=(LinearLayout) v.findViewById(R.id.ll_nation_pop_upload);
		ll_play.setOnClickListener(NationScanActivity.this);
		ll_delete.setOnClickListener(NationScanActivity.this);
		ll_upload.setOnClickListener(NationScanActivity.this);
		ll_play.setTag(position);
		ll_delete.setTag(position);
		ll_upload.setTag(position);
		LinearLayout ll_pop = (LinearLayout) v.findViewById(R.id.ll_nation_pop);
		mPop=new PopupWindow(v, 320, 85);
		Drawable background = getResources().getDrawable(R.drawable.local_popup_bg);
		mPop.setBackgroundDrawable(background);
		mPop.showAtLocation(view, Gravity.LEFT | Gravity.TOP, i, j);
		ScaleAnimation sa = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f);
		sa.setDuration(200);
		ll_pop.startAnimation(sa);
	}
	private void dismissPopUpwindow() {
		/*
		 * 保证只有一个popupwindow的实例存在
		 */
		if (mPop != null) {
			mPop.dismiss();
			mPop = null;
		}
	}
	private void dismissProress(){
		if(mProgress !=null){
			mProgress.dismiss();
			mProgress=null;
		}
	}
	 private void openFile(File f) 
	  {
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    /* 调用getMIMEType()来取得MimeType */
	    String type = Utils.getMIMEType(f);
	    /* 设置intent的file与MimeType */
	    intent.setDataAndType(Uri.fromFile(f),type);
	    startActivity(intent); 
	  }
	 @Override
	protected void onPause() {
		dismissPopUpwindow();
		dismissProress();
		SettingAndStatus.avrecording=false;   
		super.onPause();
	}
	 @Override
	protected void onStop() {
		 dismissPopUpwindow();
		 dismissProress();
		 SettingAndStatus.avrecording=false;    	
		super.onStop();
	}
	 @Override
	public void onBackPressed() {
		 NationScanActivity.this.setResult(requestCode);
		 if(localVideoPlayer&&(currentStatus==SettingAndStatus.vcaStatus.INIT||currentStatus==SettingAndStatus.vcaStatus.STOPPING)){	
		 LiveCamsActivity lv=(LiveCamsActivity) LiveCamsActivity.getCon();
			lv.stopAndCloseVca();
			 //Log.i(TAG, "isstopAndCloseVca");
		 }
		 
		 if(0 !=refreshSize && mProgress!=null){
			 refreshSize=0;
			 dismissProress();
			 DataProc.stopUnComLoad();
		 }else
			 super.onBackPressed();
	}
	 @Override
	protected void onDestroy() {
		 dismissPopUpwindow();
		 dismissProress();
		super.onDestroy();
	}
	 @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 menu.add(Menu.NONE,Menu.FIRST+1,1,"删除全部").setIcon(android.R.drawable.ic_delete);
		return true;
	}
	 @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		 switch(item.getItemId()){
			case Menu.FIRST + 1:
				AlertDialog.Builder builder = new Builder(NationScanActivity.this); 
			if(ifVedioFolder)
	        builder.setTitle("确认删除所有本地视频文件？"); 
			else
			builder.setTitle("确认删除所有本地照片？"); 
	        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(ifVedioFolder){
						File file = new File(path+"/LiveCams/video");
						if(file.isDirectory()){
							File[] fs=file.listFiles();
							if(0!=fs.length)
							for(File f : fs){
								f.delete();
							}
							mlistFiles("video");
						}
					}else{
						File file = new File(path+"/LiveCams/photo");
						if(file.isDirectory()){
							File[] fs=file.listFiles();
							if(0!=fs.length)
							for(File f : fs){
								f.delete();
							}
							mlistFiles("photo");
						}
					}
					
				}
			}); 
	        builder.setNegativeButton("取消", null);
	        builder.setIcon(android.R.drawable.ic_dialog_info); 
	        builder.show();
			break;
		 }
		return true;
	 }
}
