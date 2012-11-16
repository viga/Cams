package com.viga.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.viga.activity.R;
import com.viga.utils.Utils;

public class NationListAdapter extends BaseAdapter{
	private static TextView tv,tv_infos,tv_size;
	private static ImageView iv;
	private Context context;
	private List<File> filelist = new ArrayList<File>();
	public NationListAdapter(List<File> filelist,Context context){
		this.filelist=filelist;
		this.context=context;
	}
	
	public int getCount() {
		return filelist.size();
	}

	public Object getItem(int position) {
		return filelist.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		String filename = filelist.get(position).getName();
		View view;
		if(convertView==null){
			view=View.inflate(context, R.layout.nation_listview_item,null);
		}else{
			view=convertView;
		}
		Bitmap bitmap = Utils.getThumbnail(filelist.get(position).getPath());
		iv=(ImageView) view.findViewById(R.id.iv_preview);
		if(bitmap!=null)
		iv.setImageBitmap(bitmap);
		tv=(TextView) view.findViewById(R.id.tv_lvitem_filename);
		tv.setText(filename);
		tv_infos=(TextView) view.findViewById(R.id.tv_lvitem_infos);
		//判断上传与否
		if("v".equalsIgnoreCase(filename.substring(0, 1))){
			tv_infos.setText("上传成功");
		}else{
			tv_infos.setText("未上传");
		}
		tv_size=(TextView) view.findViewById(R.id.tv_lvitem_filesize);
		tv_size.setText(Utils.FormetFileSize(filelist.get(position).length()));
		return view;
	}
}
