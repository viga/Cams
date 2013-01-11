package com.viga.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

class HanderforTestCode implements Runnable
{
	private Socket		socket;
	File				store;
	FileOutputStream	fos;
	InputStream			socketIn	= null;
	byte[]				buffer		= new byte[512];
	int					length;
	File				d;
	int					i			= 0;

	public HanderforTestCode(Socket socket)
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
			// TODO Auto-generated catch block
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