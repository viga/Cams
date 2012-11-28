package com.viga.activity;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.vdsp.CmdSend;
import com.viga.adapter.NationListAdapter;
import com.viga.engine.MyApplication;
import com.viga.utils.Utils;

public class NationScanActivity extends Activity implements OnClickListener {
	private TextView tv_nation_video,tv_nation_photo;
	private ListView lv_nation_files;
	private static NationListAdapter nationadapter;
	private List<File> filelist=new ArrayList<File>();
	private File[] filebuf;
	private File files;
	private static String path = Environment.getExternalStorageDirectory().getAbsolutePath();
	private PopupWindow mPop;
	private SharedPreferences mysp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_nation_scan);
        MyApplication.getInstance().addActivity(NationScanActivity.this);
        lv_nation_files=(ListView) findViewById(R.id.lv_nation);
        tv_nation_photo=(TextView) findViewById(R.id.tv_nation_photo);
        tv_nation_video=(TextView) findViewById(R.id.tv_nation_video);
        tv_nation_photo.setOnClickListener(this);
        tv_nation_video.setOnClickListener(this);
        
        tv_nation_photo.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonbg2));
		tv_nation_photo.setClickable(false);
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

	public void onClick(View v) {
		final File thisFile;
		int positon = 0;
		if (v.getTag() != null) {
			positon = (Integer) v.getTag();
		}
		if(filelist.size()!=0){
		thisFile=filelist.get(positon);
		}else{
			thisFile=null;
		}
		switch (v.getId()) {
		case R.id.tv_nation_photo://显示照片文件
			if(tv_nation_photo.isClickable()){
			tv_nation_photo.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonbg2));
			tv_nation_photo.setClickable(false);
			tv_nation_photo.setTextColor(Color.WHITE);
			tv_nation_video.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonbg1));
			tv_nation_video.setClickable(true);
			tv_nation_video.setTextColor(Color.BLACK);
			mlistFiles("photo");  
			}
			break;
		case R.id.tv_nation_video://显示视频文件
			if(tv_nation_video.isClickable()){
				tv_nation_video.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonbg2));
				tv_nation_video.setClickable(false);
				tv_nation_video.setTextColor(Color.WHITE);
				tv_nation_photo.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonbg1));
				tv_nation_photo.setClickable(true);
				tv_nation_photo.setTextColor(Color.BLACK);
				mlistFiles("video");
				}
			break;
		case R.id.ll_nation_pop_play:
			if(thisFile!=null){
				if("image/*".equals(Utils.getMIMEType(thisFile))){
					//查看图片：
				openFile(thisFile);
				}else{
					//播放视频:
					openFile(thisFile);
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
						}
					}else{
						if(uploadVideoToS(thisFile)){
							Editor editor=mysp.edit();
							editor.putBoolean(thisFile.getName(), true);
		    				editor.commit();
						mlistFiles("video");
						}
					}
			}
			break;
		case R.id.ll_nation_pop_delete:
			if(thisFile!=null){
				AlertDialog.Builder builder = new Builder(NationScanActivity.this); 
		        builder.setTitle("确认删除该文件？"); 
		        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						thisFile.delete();
						if("image/*".equals(Utils.getMIMEType(thisFile))){
							mlistFiles("photo");
						}else{
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
		//DataProc.startNationVideoUpload(thisFile.getName());
		return true;
	}
	private void mlistFiles(String filename) {
		lv_nation_files.removeAllViewsInLayout();
		files=new File(path+"/LiveCams/"+filename);
		filelist.removeAll(filelist);
		  filebuf=files.listFiles(); 
		  if(filebuf!=null){
		    for(File f : filebuf){
		    	if(Utils.guolvFiles(f, "")){
		    		filelist.add(f);
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
		mPop=new PopupWindow(v, 320, 90);
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
		super.onPause();
	}
	 @Override
	protected void onStop() {
		 dismissPopUpwindow();
		super.onStop();
	}
	 @Override
	protected void onDestroy() {
		 dismissPopUpwindow();
		super.onDestroy();
	}
}
