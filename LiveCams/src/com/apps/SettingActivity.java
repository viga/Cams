package com.apps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.utils.Utils;
import com.vdsp.H264Stream;
import com.vdsp.SettingAndStatus;
import com.vdsp.Vca;

public class SettingActivity extends Activity {
	private ListView settingListView=null;
	private SettingAdapter settingAdapter;
	private ArrayList<HashMap<String,Object>> settingList=null;
	private List<Camera.Size> cslist=null;
	private Camera.Size cameraSize;
	private Vca	vca=new Vca();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        	WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.setting);
        
        settingListView=(ListView)findViewById(R.id.settingLv);
        settingList=loadSettings();
        settingAdapter=new SettingAdapter(this,settingList);
        settingListView.setAdapter(settingAdapter);        
    }
	
	/*加入所有的参数*/
	private ArrayList<HashMap<String,Object>> loadSettings(){
    	ArrayList<HashMap<String,Object>> list=new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> map;
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
		//0
		map=new HashMap<String,Object>();
		map.put("ItemTitle","服务器IP地址和端口:");
		map.put("ItemText",Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr)+":"+
				SettingAndStatus.settings.srvport);
		list.add(map);
		//1
		map=new HashMap<String,Object>();
		map.put("ItemTitle","设备类型和ID号:");
		map.put("ItemText","手机终端/"+SettingAndStatus.settings.devid);
		list.add(map);
		//2
		map=new HashMap<String,Object>();
		map.put("ItemTitle","启动时是否自动登录?");
		map.put("ItemText",SettingAndStatus.settings.autologin?"是":"否");
		list.add(map);		
		//3
		map=new HashMap<String,Object>();
		map.put("ItemTitle","登录前是否进行信息确认?");
		map.put("ItemText",SettingAndStatus.settings.logincheck?"是":"否");
		list.add(map);
		//4
		map=new HashMap<String,Object>();
		map.put("ItemTitle","断开链接前是否进行确认?");
		map.put("ItemText",SettingAndStatus.settings.logoutcheck?"是":"否");
		list.add(map);
		//5
		map=new HashMap<String,Object>();
		map.put("ItemTitle","录像参数:");
		map.put("ItemText",H264Stream.stringVideoSize(SettingAndStatus.settings.videosize)+
				"/"+SettingAndStatus.settings.vbitrate+"bps/"+
				SettingAndStatus.settings.vframerate+"fps");
		list.add(map);
		//6
		map=new HashMap<String,Object>();
		map.put("ItemTitle","录像时是否存成文件?");
		map.put("ItemText",(SettingAndStatus.settings.avrecord?"是":"否")+
				"("+Utils.getSDCardInfo()+")");
		list.add(map);
		//7
		map=new HashMap<String,Object>();
		map.put("ItemTitle","录像时是否双向语音?");
		map.put("ItemText",SettingAndStatus.settings.audioduplex0?"是":"否");
		list.add(map);
		//8
		map=new HashMap<String,Object>();
		map.put("ItemTitle","播放时是否双向语音?");
		map.put("ItemText",SettingAndStatus.settings.audioduplex1?"是":"否");
		list.add(map);
		//9
		map=new HashMap<String,Object>();
		map.put("ItemTitle","播放时视频是否全屏?");
		map.put("ItemText",SettingAndStatus.settings.fullscreen?"是":"否");
		list.add(map);
		//10
		map=new HashMap<String,Object>();
		map.put("ItemTitle","拍照参数:");
		map.put("ItemText",SettingAndStatus.settings.picwidth+"x"+
				SettingAndStatus.settings.picheight+"/Q"+
				SettingAndStatus.settings.picquality);
		list.add(map);
		//11
		map=new HashMap<String,Object>();
		map.put("ItemTitle","拍照时是否存成文件?");
		map.put("ItemText",(SettingAndStatus.settings.picsave?"是":"否")+
				"("+Utils.getSDCardInfo()+")");
		list.add(map);
    	return list;
	}
	
	/*服务器配置对话框*/
	private class ModifySettingDialog extends Dialog{
		private int	position;
		private EditText et1,et2;
		private TextView tv;
		private Spinner sp;
		private SeekBar sb;
		public ModifySettingDialog(Context context, int which){
			super(context);
			ModifySettingDialog.this.position=which;
		}
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			if(0==position){
				setContentView(R.layout.setting_server);			
				setTitle("修改服务器IP地址和端口：");
				et1=(EditText)findViewById(R.id.srvIpaddrEt);
		        et1.setText(Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr));
		        et2=(EditText)findViewById(R.id.srvPortEt);
	            et2.setText(""+SettingAndStatus.settings.srvport);
			}else if(1==position){
				setContentView(R.layout.setting_device);			
				setTitle("修改设备类型和ID号：");
				et1=(EditText)findViewById(R.id.devTypeEt);
		        et1.setText("手机终端");
		        disableEditText(et1);
		        et2=(EditText)findViewById(R.id.devIdEt);
	            et2.setText(""+SettingAndStatus.settings.devid);
			}else if(5==position){
				setContentView(R.layout.setting_av);			
				setTitle("修改录像参数：");
				String[] items=getResources().getStringArray(R.array.vsz4sp);
				ArrayList<String> list=new ArrayList<String>();
				for(int i=0;i<items.length;i++){
		        	list.add(items[i]);
		        }
				ArrayAdapter<String> adapter;
				adapter=new ArrayAdapter<String>(SettingActivity.this,
					android.R.layout.simple_spinner_item,list);
		        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		        sp=(Spinner)findViewById(R.id.videoSizeSp);
				sp.setAdapter(adapter);
				sp.setPrompt("视频尺寸:");
				sp.setSelection(SettingAndStatus.settings.videosize);
				et1=(EditText)findViewById(R.id.vframeRateEt);
		        et1.setText(""+SettingAndStatus.settings.vframerate);
		        et2=(EditText)findViewById(R.id.vbitRateEt);
	            et2.setText(""+SettingAndStatus.settings.vbitrate);
			}else if(10==position){
				setContentView(R.layout.setting_snap);			
				setTitle("修改拍照参数：");
				ArrayList<String> list=new ArrayList<String>();
				for(int i=0;i<cslist.size();i++){
					Camera.Size dim=cslist.get(i);
		        	list.add(dim.width+"x"+dim.height);
		        }
				ArrayAdapter<String> adapter;
				adapter=new ArrayAdapter<String>(SettingActivity.this,
					android.R.layout.simple_spinner_item,list);
		        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		        sp=(Spinner)findViewById(R.id.picSizeSp);
				sp.setAdapter(adapter);
				sp.setPrompt("照片尺寸("+cslist.size()+"种):");
				sp.setSelection(cslist.indexOf(cameraSize));
				tv=(TextView)findViewById(R.id.picQualityValTv);
				tv.setText(""+SettingAndStatus.settings.picquality);
				sb=(SeekBar)findViewById(R.id.picQualitySb);
				sb.setProgress(SettingAndStatus.settings.picquality);
				sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
					public void onStartTrackingTouch(SeekBar seekBar) { 
					}
					public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) { 
						tv.setText(""+progress);
					}
					public void onStopTrackingTouch(SeekBar seekBar) { 
					}
				});
			}
            Button bt=(Button)findViewById(R.id.confirmBt);
            bt.setText(getString(R.string.confirm3));
            bt.setOnClickListener(
            	new View.OnClickListener(){
            		public void onClick(View v){
            			HashMap<String,Object> map=settingList.get(position);
            			if(0==position){
	            			int srvipaddr=Utils.ipaddrToInt(et1.getText().toString());
	            			short srvport=Short.parseShort(et2.getText().toString());
	            			if(srvipaddr!=SettingAndStatus.settings.srvipaddr || 
	            				srvport!=SettingAndStatus.settings.srvport){
		            			SettingAndStatus.settings.srvipaddr=srvipaddr;
		            			SettingAndStatus.settings.srvport=srvport;
		            			SettingAndStatus.modify("srvipaddr",srvipaddr);
		            			SettingAndStatus.modify("srvport",srvport);
		    					map.clear();
		    					map.put("ItemTitle","服务器IP地址和端口:");
		    					map.put("ItemText",Utils.intToIpaddr(srvipaddr)+":"+srvport);
		    					settingAdapter.notifyDataSetChanged();
	            			}
            			}else if(1==position){
            				int devid=Integer.parseInt(et2.getText().toString());
            				if(devid!=SettingAndStatus.settings.devid){
            					SettingAndStatus.settings.devid=devid;
		            			SettingAndStatus.modify("devid",devid);
		    					map.clear();
		    					map.put("ItemTitle","设备类型和ID号:");
		    					map.put("ItemText","手机终端/"+devid);
		    					settingAdapter.notifyDataSetChanged();
            				}
            			}else if(5==position){
            				int videosize=sp.getSelectedItemPosition();
            				int framerate=Integer.parseInt(et1.getText().toString());
            				int bitrate=Integer.parseInt(et2.getText().toString());
            				if(videosize!=SettingAndStatus.settings.videosize || 
            					framerate!=SettingAndStatus.settings.vframerate || 
            					bitrate!=SettingAndStatus.settings.vbitrate){
		            			SettingAndStatus.settings.videosize=videosize;
		            			SettingAndStatus.settings.vframerate=framerate;
		            			SettingAndStatus.settings.vbitrate=bitrate;
		            			SettingAndStatus.modify("videosize",videosize);
		            			SettingAndStatus.modify("framerate",framerate);
		            			SettingAndStatus.modify("bitrate",bitrate);
		    					map.clear();
		    					map.put("ItemTitle","录像参数:");
		    					map.put("ItemText",H264Stream.stringVideoSize(videosize)+
		    							"/"+bitrate+"bps/"+framerate+"fps");
		    					settingAdapter.notifyDataSetChanged();
	            			}
            			}else if(10==position){
            				int index=sp.getSelectedItemPosition();
            				int width=cslist.get(index).width;
            				int height=cslist.get(index).height;
            				int quality=sb.getProgress();
            				if(width!=SettingAndStatus.settings.picwidth || 
            					height!=SettingAndStatus.settings.picheight ||
            					quality!=SettingAndStatus.settings.picquality){
		            			SettingAndStatus.settings.picwidth=width;
		            			SettingAndStatus.settings.picheight=height;
		            			SettingAndStatus.settings.picquality=quality;
		            			SettingAndStatus.modify("width",width);
		            			SettingAndStatus.modify("height",height);
		            			SettingAndStatus.modify("quality",quality);
		    					map.clear();
		    					map.put("ItemTitle","拍照参数:");
		    					map.put("ItemText",width+"x"+height+"/Q"+quality);
		    					settingAdapter.notifyDataSetChanged();
	            			}
            			}
            			ModifySettingDialog.this.dismiss();
            		}
            });
    		((Button)findViewById(R.id.cancelBt)).setOnClickListener(
		        new View.OnClickListener(){
		        	public void onClick(View v){
		        		ModifySettingDialog.this.cancel();
		        	}
			});

    		WindowManager winmgr=getWindowManager();
	        Display disp=winmgr.getDefaultDisplay();
	        LayoutParams param=getWindow().getAttributes();
	        param.width=(int)(disp.getWidth()*0.9);
	        param.height=(int)(disp.getHeight()*(4==position?0.9:0.75));
	        getWindow().setAttributes(param);
		}
	}
	
    /*禁止EditText输入*/
    private void disableEditText(EditText et){
        et.setCursorVisible(false);
        et.setFocusable(false);
    }
	
	/*LView的自定义Adapter*/
    private class SettingAdapter extends BaseAdapter {
    	private LayoutInflater mInflater=null;
    	private ArrayList<HashMap<String,Object>> list=null;
    	public SettingAdapter(Context context, ArrayList<HashMap<String,Object>> setting){
    		this.mInflater=LayoutInflater.from(context);
    		list=setting;
    	}
    	public int getCount(){
    		return list.size();
    	}
    	public Object getItem(int position){
    		return null;
    	}
    	public long getItemId(int position){
    		return 0;
    	}
    	public View getView(final int position, View convertView, ViewGroup parent){
    		ViewSet viewset=new ViewSet();
    		
    		if(null==convertView){
    			convertView=mInflater.inflate(R.layout.setting_item,null);
    			viewset=new ViewSet();
    			viewset.title=(TextView)convertView.findViewById(R.id.ItemTitle);
    			viewset.text=(TextView)convertView.findViewById(R.id.ItemText);
    			viewset.click=(Button)convertView.findViewById(R.id.ItemButton);
    			convertView.setTag(viewset);
    		}else{
    			viewset=(ViewSet)convertView.getTag();
    		}
    		
    		viewset.title.setText(list.get(position).get("ItemTitle").toString());
    		viewset.text.setText(list.get(position).get("ItemText").toString());
    		viewset.click.setText("修 改");
    		viewset.click.setOnClickListener(new OnClickListener(){
    			public void onClick(View v){
    				boolean flag;
    				HashMap<String,Object> map;
    				switch(position){
    				case 0:
    				case 1:
    				case 5:
    				case 10:
    					new ModifySettingDialog(SettingActivity.this,position).show(); 
    					break;
    				case 2:
    					flag=!SettingAndStatus.settings.autologin;
    					SettingAndStatus.settings.autologin=flag;
            			SettingAndStatus.modify("autologin",flag?1:0);
    					map=list.get(position);
    					map.clear();
    					map.put("ItemTitle","启动时是否自动登录?");
    					map.put("ItemText",flag?"是":"否");
    					settingAdapter.notifyDataSetChanged();
    					break;
    				case 3:
    					flag=!SettingAndStatus.settings.logincheck;
    					SettingAndStatus.settings.logincheck=flag;
            			SettingAndStatus.modify("logincheck",flag?1:0);
    					map=list.get(position);
    					map.clear();
    					map.put("ItemTitle","登录前是否进行信息确认?");
    					map.put("ItemText",flag?"是":"否");
    					settingAdapter.notifyDataSetChanged();
    					break;
    				case 4:
    					flag=!SettingAndStatus.settings.logoutcheck;
    					SettingAndStatus.settings.logoutcheck=flag;
            			SettingAndStatus.modify("logoutcheck",flag?1:0);
    					map=list.get(position);
    					map.clear();
    					map.put("ItemTitle","断开链接前是否进行确认?");
    					map.put("ItemText",flag?"是":"否");
    					settingAdapter.notifyDataSetChanged();
    					break;
    				case 6:
    					flag=!SettingAndStatus.settings.avrecord;
    					SettingAndStatus.settings.avrecord=flag;
            			SettingAndStatus.modify("avrecord",flag?1:0);
    					map=list.get(position);
    					map.clear();
    					map.put("ItemTitle","录像时是否存成文件?");
    					map.put("ItemText",(flag?"是":"否")+"("+Utils.getSDCardInfo()+")");
    					settingAdapter.notifyDataSetChanged();
    					break;
    				case 7:
    					flag=!SettingAndStatus.settings.audioduplex0;
    					SettingAndStatus.settings.audioduplex0=flag;
            			SettingAndStatus.modify("audioduplex0",flag?1:0);
    					map=list.get(position);
    					map.clear();
    					map.put("ItemTitle","录像时是否双向语音?");
    					map.put("ItemText",flag?"是":"否");
    					settingAdapter.notifyDataSetChanged();
    					break;
    				case 8:
    					flag=!SettingAndStatus.settings.audioduplex1;
    					SettingAndStatus.settings.audioduplex1=flag;
            			SettingAndStatus.modify("audioduplex1",flag?1:0);
    					map=list.get(position);
    					map.clear();
    					map.put("ItemTitle","播放时是否双向语音?");
    					map.put("ItemText",flag?"是":"否");
    					settingAdapter.notifyDataSetChanged();
    					break;
    				case 9:
    					flag=!SettingAndStatus.settings.fullscreen; 
    					SettingAndStatus.settings.fullscreen=flag;
            			SettingAndStatus.modify("fullscreen",flag?1:0);
            			if(SettingAndStatus.vcaStatus.status>=SettingAndStatus.VcaStatus.OPENED){
	        				Vca.Arg arg=vca.new Arg(-1,-1);
	            			if(flag){
	            				arg.setWxH(SettingAndStatus.displayheight,
	            					SettingAndStatus.displaywidth);
	            			}
	            			Vca.control(Vca.OPID_CHGDESTSIZE,arg);
            			}
    					map=list.get(position);
    					map.clear();
    					map.put("ItemTitle","播放时视频是否全屏?");
    					map.put("ItemText",flag?"是":"否");
    					settingAdapter.notifyDataSetChanged();
    					break;
    				case 11:
    					flag=!SettingAndStatus.settings.picsave;
    					SettingAndStatus.settings.picsave=flag;
            			SettingAndStatus.modify("picsave",flag?1:0);
    					map=list.get(position);
    					map.clear();
    					map.put("ItemTitle","拍照时是否存成文件?");
    					map.put("ItemText",(flag?"是":"否")+"("+Utils.getSDCardInfo()+")");
    					settingAdapter.notifyDataSetChanged();
    					break;
    				default:
    					break;
    				}
    			}
    		});
    		
    		return convertView;
    	}
    }
    
    /*子View容器*/
    private final class ViewSet{
    	public TextView title;
    	public TextView text;
    	public Button	click;
    }
}
