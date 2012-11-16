package com.vdsp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

import android.util.Log;

class Handnimei implements Runnable
{
	private Socket		socket;
	File				store;
	FileOutputStream	fos;
	InputStream			socketIn	= null;
	byte[]				buffer		= new byte[512];
	byte head[] = new byte[5];
	int					length;
	File				d;
	int					i			= 0;
	boolean  breakflag=true;
	int offset=0,size,payloadlen;
	public Handnimei(Socket socket)
	{
		this.socket = socket;
		d = new File("/mnt/sdcard/fucknimei");
		if (!d.exists())
		{
			d.mkdirs();
		}

		try
		{
			store = File.createTempFile("sdsd", ".264", d);
			fos = new FileOutputStream(store);
			socketIn = socket.getInputStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public  void addSps(){
			while ( offset < 36) {
				try {
					size = socketIn.read(buffer, offset, 36 - offset);
					if (size >= 0) {
						offset += size;
					}
				} catch (Exception e) {
				}
			}
			offset = 0;
			while ( offset < head.length) {
				try {
					size = socketIn.read(head, offset, head.length- offset);
					if (size >= 0) {
						offset += size;
					} 
				} catch (Exception e) {
				}
			}
			offset = H264Stream.getFrameHeadLength();
			boolean idr = H264Stream.isIDR(head[4]);
			if (idr) {
				offset += H264Stream.writeHead(buffer, offset,
						length - offset);
				offset += H264Stream.writeSPS(buffer, offset,
						length - offset,
						SettingAndStatus.settings.videosize);
				offset += H264Stream.writeHead(buffer, offset,
						length - offset);
				offset += H264Stream.writePPS(buffer, offset,
						length - offset);
			} 
			offset += H264Stream.writeHead(buffer, offset,
					length - offset);
			payloadlen += offset;
			buffer[offset++] = head[4];// NAL头
			while ( offset < payloadlen) {
				try {
					size = socketIn.read(buffer, offset, payloadlen
							- offset);
					if (size >= 0) {
						offset += size;
					} 
				} catch (Exception e) {
					
				}
			}
			int frmheadlen = H264Stream.getFrameHeadLength();
				try {
					fos.write(buffer, frmheadlen,payloadlen - frmheadlen);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	public void run()
	{
		try
		{
			System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
			while ((length = socketIn.read(buffer)) != -1)
			{
				fos.write(buffer, 0, length);
				fos.flush();
				System.out.println("正在写入中。。。。" + length);
			}

			fos.flush();
			store = null;
			socketIn.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				fos.close();
				fos = null;
				store = null;
				if (socket != null)
					socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("-----------------------------完毕");
	}
}